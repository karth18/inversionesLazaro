package pe.com.isil.inversioneslazaro.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.com.isil.inversioneslazaro.model.Categoria;
import pe.com.isil.inversioneslazaro.model.TipoProducto;
import pe.com.isil.inversioneslazaro.repository.CategoriaRepository;
import pe.com.isil.inversioneslazaro.repository.TipoProductoRepository;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/tproducto")
public class TipoProductoController {

    @Autowired
    private TipoProductoRepository tipoProductoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;


    @GetMapping
    public String index(Model model) {

        List<TipoProducto> tiposProducto = tipoProductoRepository.findByEstadoTrue();
        model.addAttribute("tiposProducto", tiposProducto);
        return "tipoProducto/index";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {

        model.addAttribute("tipoProducto", new TipoProducto());
        model.addAttribute("categorias", categoriaRepository.findByEstadoTrue());

        return "tipoProducto/form";
    }

    @PostMapping("/nuevo")
    public String registrar(@Valid TipoProducto tipoProducto, Model model, BindingResult bindingResult, RedirectAttributes ra) {

        if (bindingResult.hasErrors()) {

            model.addAttribute("categorias", categoriaRepository.findByEstadoTrue());
            return "tipoProducto/form";
        }


        Optional<TipoProducto> existente = tipoProductoRepository.findByNombre(tipoProducto.getNombre());
        if (existente.isPresent() && (tipoProducto.getId() == null || !existente.get().getId().equals(tipoProducto.getId()))) {
            bindingResult.rejectValue("nombre", "error.tipoProducto", "El nombre de Tipo de Producto ya existe");
            model.addAttribute("categorias", categoriaRepository.findByEstadoTrue());
            return "tipoProducto/form";
        }

        tipoProductoRepository.save(tipoProducto);
        ra.addFlashAttribute("msgExito", "Tipo de Producto guardado/actualizado con éxito");
        return "redirect:/admin/tproducto";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Optional<TipoProducto> tipoProducto = tipoProductoRepository.findById(id);

        if (tipoProducto.isEmpty()) {
            ra.addFlashAttribute("msgError", "Tipo de Producto no encontrado");
            return "redirect:/admin/tproducto";
        }

        model.addAttribute("tipoProducto", tipoProducto.get());
        // ⭐ CLAVE: Pasamos las categorías activas para el select de edición ⭐
        model.addAttribute("categorias", categoriaRepository.findByEstadoTrue());

        return "tipoProducto/form";
    }


    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        Optional<TipoProducto> tipoProductoOptional = tipoProductoRepository.findById(id);

        if (tipoProductoOptional.isEmpty()) {
            ra.addFlashAttribute("msgError", "El Tipo de Producto que intentas eliminar no existe");
            return "redirect:/admin/tproducto";
        }
        TipoProducto tipoProducto = tipoProductoOptional.get();
        try {
            tipoProducto.setEstado(false); // Desactivar
            tipoProductoRepository.save(tipoProducto);
            ra.addFlashAttribute("msgExito", "Tipo de Producto desactivado con éxito");
        } catch (Exception e) {
            ra.addFlashAttribute("msgError", "El Tipo de Producto no fue desactivado con éxito");
        }
        return "redirect:/admin/tproducto";
    }


    @GetMapping("/habilitar")
    public String listahabilitar(Model model){
        List<TipoProducto> tiposProducto = tipoProductoRepository.findByEstadoFalse();
        model.addAttribute("tiposProducto", tiposProducto);
        return"tipoProducto/enable";
    }

    @GetMapping("/habilitar/{id}")
    public String habilitar(@PathVariable Long id, RedirectAttributes ra){
        Optional<TipoProducto> tipoProductoOptional = tipoProductoRepository.findById(id);

        if(tipoProductoOptional.isEmpty()){
            ra.addFlashAttribute("msgError", "El Tipo de Producto que intentas Habilitar no existe");
            return"redirect:/admin/tproducto/habilitar";
        }
        TipoProducto tipoProducto = tipoProductoOptional.get();
        try{
            tipoProducto.setEstado(true);
            tipoProductoRepository.save(tipoProducto);
            ra.addFlashAttribute("msgExito", "Tipo de Producto Activado con exito");
        }catch (Exception e){
            ra.addFlashAttribute("msgError", "El Tipo de Producto no fue Activado con exito");
        }
        return "redirect:/admin/tproducto/habilitar";
    }
}