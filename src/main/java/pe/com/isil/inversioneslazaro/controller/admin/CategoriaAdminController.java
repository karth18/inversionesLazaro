package pe.com.isil.inversioneslazaro.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.com.isil.inversioneslazaro.dto.CategoriaDTO;
import pe.com.isil.inversioneslazaro.model.CategoriaProducto;
import pe.com.isil.inversioneslazaro.service.CategoriaService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/categorias")
public class CategoriaAdminController {

    private final CategoriaService service;

    public CategoriaAdminController(CategoriaService service) {
        this.service = service;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("categorias", service.listAll());
        return "admin/categorias/index";
    }

    @GetMapping("/nuevo")
    public String nuevoForm(Model model) {
        model.addAttribute("categoria", new CategoriaDTO());
        return "admin/categorias/form";
    }

    @PostMapping("/nuevo")
    public String crear(@Valid @ModelAttribute("categoria") CategoriaDTO dto, BindingResult br, Model model, RedirectAttributes ra) {
        if (br.hasErrors()) {
            return "admin/categorias/form";
        }
        try {
            service.create(dto);
            ra.addFlashAttribute("success", "Categoría creada correctamente");
            return "redirect:/admin/categorias";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            return "admin/categorias/form";
        }
    }

    @GetMapping("/editar/{id}")
    public String editarForm(@PathVariable Long id, Model model) {
        CategoriaProducto c = service.getById(id);
        CategoriaDTO dto = new CategoriaDTO();
        dto.id = c.getId();
        dto.nombre = c.getNombre();
        dto.slug = c.getSlug();
        dto.descripcion = c.getDescripcion();
        dto.activo = c.getActivo();
        model.addAttribute("categoria", dto);
        return "admin/categorias/form";
    }

    @PostMapping("/editar/{id}")
    public String editar(@PathVariable Long id, @Valid @ModelAttribute("categoria") CategoriaDTO dto, BindingResult br, Model model, RedirectAttributes ra) {
        if (br.hasErrors()) return "admin/categorias/form";
        try {
            service.update(id, dto);
            ra.addFlashAttribute("success", "Categoría actualizada");
            return "redirect:/admin/categorias";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            return "admin/categorias/form";
        }
    }

    @PostMapping("/toggle/{id}")
    public String toggle(@PathVariable Long id, RedirectAttributes ra) {
        try {
            service.toggleActivo(id);
            ra.addFlashAttribute("success", "Estado actualizado");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/categorias";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            service.delete(id);
            ra.addFlashAttribute("success", "Categoría eliminada");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/categorias";
    }

}
