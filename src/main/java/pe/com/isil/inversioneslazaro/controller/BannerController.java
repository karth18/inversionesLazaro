package pe.com.isil.inversioneslazaro.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.com.isil.inversioneslazaro.model.Auditoria;
import pe.com.isil.inversioneslazaro.model.BannerHome;
import pe.com.isil.inversioneslazaro.repository.BannerHomeRepository;
import pe.com.isil.inversioneslazaro.service.AuditoriaService;
import pe.com.isil.inversioneslazaro.service.StorageService;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/banners")
public class BannerController {

    @Autowired
    private BannerHomeRepository bannerHomeRepository;
    @Autowired
    private AuditoriaService auditoriaService;
    @Autowired
    private StorageService storageService;

    // (index, nuevo, editar, y guardar se quedan EXACTAMENTE IGUAL)
    @GetMapping("")
    public String index(Model model) {
        List<BannerHome> banners = bannerHomeRepository.findAll();
        model.addAttribute("banners", banners);
        return "admin/banner/index";
    }
    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("banner", new BannerHome());
        return "admin/banner/form";
    }
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Optional<BannerHome> banner = bannerHomeRepository.findById(id);
        if (banner.isPresent()) {
            model.addAttribute("banner", banner.get());
            return "admin/banner/form";
        } else {
            ra.addFlashAttribute("msgError", "El banner no fue encontrado.");
            return "redirect:/admin/banners";
        }
    }
    @PostMapping("/guardar")
    public String guardar(BannerHome banner,
                          @RequestParam("archivoImagen") MultipartFile archivoImagen,
                          RedirectAttributes ra) {

        Auditoria.AccionAuditoria accion = (banner.getId() == null)
                ? Auditoria.AccionAuditoria.CREAR
                : Auditoria.AccionAuditoria.ACTUALIZAR;

        if (!archivoImagen.isEmpty()) {
            if (accion == Auditoria.AccionAuditoria.ACTUALIZAR && banner.getImagenNombre() != null) {
                storageService.delete(banner.getImagenNombre());
            }
            String nombreArchivo = storageService.store(archivoImagen);
            banner.setImagenNombre(nombreArchivo);
        } else if (accion == Auditoria.AccionAuditoria.CREAR) {
            ra.addFlashAttribute("msgError", "Se requiere una imagen para crear un banner.");
            return "redirect:/admin/banners/nuevo";
        }

        BannerHome bannerGuardado = bannerHomeRepository.save(banner);
        registrarAuditoria("BannerHome", bannerGuardado.getId(), accion);
        ra.addFlashAttribute("msgExito", "Banner guardado con éxito.");
        return "redirect:/admin/banners";
    }


    /**
     * DELETE LÓGICO (Desactivar)
     */
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        Optional<BannerHome> bannerOpt = bannerHomeRepository.findById(id);
        if (bannerOpt.isPresent()) {
            try {
                BannerHome banner = bannerOpt.get();

                // --- 1. ESTA ES LA LÓGICA DE SOFT-DELETE ---
                banner.setEstaActivo(false); // <-- Solo lo desactiva
                bannerHomeRepository.save(banner); // <-- Guarda el cambio

                // 2. Audita la acción
                registrarAuditoria("BannerHome", id, Auditoria.AccionAuditoria.ELIMINAR);
                ra.addFlashAttribute("msgExito", "Banner desactivado con éxito.");
            } catch (Exception e) {
                ra.addFlashAttribute("msgError", "No se pudo desactivar el banner.");
            }
        } else {
            ra.addFlashAttribute("msgError", "Banner no encontrado.");
        }
        return "redirect:/admin/banners";
    }

    /**
     * (NUEVO) HABILITAR
     */
    @GetMapping("/habilitar/{id}")
    public String habilitar(@PathVariable Long id, RedirectAttributes ra) {
        Optional<BannerHome> bannerOpt = bannerHomeRepository.findById(id);
        if (bannerOpt.isPresent()) {
            try {
                BannerHome banner = bannerOpt.get();

                // 1. Reactiva el banner
                banner.setEstaActivo(true);
                bannerHomeRepository.save(banner);

                // 2. Audita (Asegúrate que tu Enum 'AccionAuditoria' tenga 'HABILITAR')
                registrarAuditoria("BannerHome", id, Auditoria.AccionAuditoria.HABILITAR);
                ra.addFlashAttribute("msgExito", "Banner activado con éxito.");
            } catch (Exception e) {
                ra.addFlashAttribute("msgError", "No se pudo activar el banner.");
            }
        } else {
            ra.addFlashAttribute("msgError", "Banner no encontrado.");
        }
        return "redirect:/admin/banners";
    }


    // --- MÉTODO DE AYUDA (Sin cambios) ---
    private void registrarAuditoria(String entidadNombre, Object entidadId, Auditoria.AccionAuditoria accion) {
        String emailLogueado = SecurityContextHolder.getContext().getAuthentication().getName();
        auditoriaService.registrarAccion(
                emailLogueado,
                entidadNombre,
                entidadId,
                accion
        );
    }
}