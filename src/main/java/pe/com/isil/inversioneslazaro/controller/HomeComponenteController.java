package pe.com.isil.inversioneslazaro.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils; // <-- IMPORTAR ESTO
import org.springframework.validation.BindingResult;
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
        Integer maxOrden = homeRepo.findMaxOrdenBySeccion(seccion);

        // Si no hay nadie (null), es el 1. Si hay, es el siguiente.
        int siguienteOrden = (maxOrden == null) ? 1 : maxOrden + 1;
        componente.setOrden(siguienteOrden);

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
    public String guardarComponente(@ModelAttribute("componente") HomeComponente componente,BindingResult bindingResult, RedirectAttributes ra ) {

        Auditoria.AccionAuditoria accion = (componente.getId() == null)
                ? Auditoria.AccionAuditoria.CREAR
                : Auditoria.AccionAuditoria.ACTUALIZAR;

        // 1. VALIDACIÓN DE ORDEN ÚNICO
        if (componente.getSeccion() != HomeComponente.Seccion.CLASIFICACION && componente.getOrden() != null) {
            // Buscamos si ya existe alguien con esa sección y ese orden
            Optional<HomeComponente> existente = homeRepo.findBySeccionAndOrden(componente.getSeccion(), componente.getOrden());

            if (existente.isPresent()) {
                // Si existe, verificamos que NO sea el mismo que estamos editando (por ID)
                // Si es nuevo (id null) o el id es diferente, entonces es un duplicado ilegal.
                if (componente.getId() == null || !existente.get().getId().equals(componente.getId())) {
                    bindingResult.rejectValue("orden", "error.orden", "El número de orden " + componente.getOrden() + " ya está ocupado en esta sección.");
                }
            }
        }

        // 2. Si hay errores (incluyendo el que acabamos de crear), volvemos al formulario
        if (bindingResult.hasErrors()) {
            // Asegúrate de volver a cargar cosas necesarias si tu HTML las usa (listas, etc.)
            return "admin/home-editor/form"; // Pon aquí la ruta a tu archivo HTML
        }

        // Lógica de subida de archivo
        MultipartFile archivo = componente.getArchivoImagen();
        if (archivo != null && !archivo.isEmpty()) {

            if (StringUtils.hasText(componente.getImagenNombre())) {
                storageService.delete(componente.getImagenNombre());
            }


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