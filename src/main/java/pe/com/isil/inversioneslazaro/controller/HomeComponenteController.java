package pe.com.isil.inversioneslazaro.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils; // <-- IMPORTAR ESTO
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.com.isil.inversioneslazaro.model.HomeComponente;
import pe.com.isil.inversioneslazaro.repository.HomeComponenteRepository;
import pe.com.isil.inversioneslazaro.service.StorageService;
import pe.com.isil.inversioneslazaro.service.AuditoriaService;
import pe.com.isil.inversioneslazaro.model.Auditoria;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Controller
@RequestMapping("/admin/home-editor")
public class HomeComponenteController {

    @Autowired private HomeComponenteRepository homeRepo;
    @Autowired private StorageService storageService;
    @Autowired private AuditoriaService auditoriaService;

    // (El método editorHome se queda igual)
    @GetMapping("")
    public String editorHome(Model model) {
        model.addAttribute("seccionOfertas",
                homeRepo.findBySeccionOrderByOrdenAsc(HomeComponente.Seccion.OFERTA));
        model.addAttribute("seccionAnuncio",
                homeRepo.findBySeccionOrderByOrdenAsc(HomeComponente.Seccion.ANUNCIO_TIRA));
        model.addAttribute("seccionClasificaciones",
                homeRepo.findBySeccionOrderByOrdenAsc(HomeComponente.Seccion.CLASIFICACION));
        return "admin/home-editor/index";
    }

    // (El método nuevoComponente se queda igual)
    @GetMapping("/nuevo")
    public String nuevoComponente(@RequestParam("seccion") HomeComponente.Seccion seccion, Model model) {
        HomeComponente componente = new HomeComponente();
        componente.setSeccion(seccion);
        if (seccion == HomeComponente.Seccion.CLASIFICACION) {
            model.addAttribute("info", "Estás creando una nueva Clasificación (ej: Industrial, Hogar...)");
        }
        model.addAttribute("componente", componente);
        return "admin/home-editor/form";
    }

    // (El método editarComponente se queda igual)
    @GetMapping("/editar/{id}")
    public String editarComponente(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Optional<HomeComponente> componente = homeRepo.findById(id);
        if (componente.isPresent()) {
            model.addAttribute("componente", componente.get());
            return "admin/home-editor/form";
        }
        ra.addFlashAttribute("msgError", "Componente no encontrado.");
        return "redirect:/admin/home-editor";
    }

    /**
     * Guarda (Crea o Actualiza) un componente
     */
    @PostMapping("/guardar")
    public String guardarComponente(HomeComponente componente, RedirectAttributes ra) {

        Auditoria.AccionAuditoria accion = (componente.getId() == null)
                ? Auditoria.AccionAuditoria.CREAR
                : Auditoria.AccionAuditoria.ACTUALIZAR;

        // Lógica de subida de archivo
        MultipartFile archivo = componente.getArchivoImagen();
        if (archivo != null && !archivo.isEmpty()) {

            // --- INICIO DE LA CORRECCIÓN ---
            // Reemplaza '!= null' por 'StringUtils.hasText()'
            // Esto comprueba que no sea null Y TAMPOCO esté vacío.
            if (StringUtils.hasText(componente.getImagenNombre())) {
                storageService.delete(componente.getImagenNombre());
            }
            // --- FIN DE LA CORRECCIÓN ---

            String nombreArchivo = storageService.store(archivo);
            componente.setImagenNombre(nombreArchivo);
        }

        HomeComponente guardado = homeRepo.save(componente);

        // Auditoría
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        auditoriaService.registrarAccion(email, "HomeComponente", guardado.getId(), accion);

        ra.addFlashAttribute("msgExito", "Componente guardado.");
        return "redirect:/admin/home-editor";
    }
}