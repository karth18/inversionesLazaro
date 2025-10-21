package pe.com.isil.inversioneslazaro.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.com.isil.inversioneslazaro.model.Categoria;
import pe.com.isil.inversioneslazaro.repository.CategoriaRepository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/categoria")
public class CategoriaController {

    @Autowired
    private CategoriaRepository categoriaRepository;


    @GetMapping("")
    String Index(Model model){
        List<Categoria> categoria = categoriaRepository.findByEstadoTrue();
        model.addAttribute("categoria", categoria);
        return"categoria/index";
    }

    @GetMapping("/habilitar")
    String listahabilitar(Model model){
        List<Categoria> categoria = categoriaRepository.findByEstadoFalse();
        model.addAttribute("categoria", categoria);
        return"categoria/enable";
    }

    @GetMapping("/habilitar/{id}")
    String habilitar(@PathVariable Long id, RedirectAttributes ra){
        Optional<Categoria> categorias = categoriaRepository.findById(id);

        if(categorias.isEmpty()){
            ra.addFlashAttribute("msgError", "La categoria que intentas Habilitar no existe");
            return"redirect:/admin/categoria/habilitar";
        }
        Categoria categoria = categorias.get();
        try{
            categoria.setEstado(true);
            categoriaRepository.save(categoria);
            ra.addFlashAttribute("msgExito", "categoría Activada con exito");
        }catch (Exception e){
            ra.addFlashAttribute("msgError", "categoría no fue Activada con exito");
        }
        return "redirect:/admin/categoria/habilitar";
    }


    @GetMapping("/nuevo")
    String nuevo(Model model){

        model.addAttribute("categoria", new Categoria());
        return"categoria/form";

    }

    @PostMapping("/nuevo")
    String registrar(@Valid Categoria categoria, Model model, BindingResult bindingResult, RedirectAttributes ra){

        if(bindingResult.hasErrors()){
            return "categoria/form";
        }

        Optional<Categoria> existente = categoriaRepository.findByNombre(categoria.getNombre());

        if(existente.isPresent() && (categoria.getId() == null || !existente.get().getId().equals(categoria.getId()))){
            bindingResult.rejectValue("nombre", "error.categoria","El nombre de la categoría ya existe");
            return"categoria/form";
        }
        categoriaRepository.save(categoria);
        ra.addFlashAttribute("msgExito", "Categoria guardada/actualizada con éxito");
        return "redirect:/admin/categoria";
    }

    @GetMapping("/editar/{id}")
    String editar(@PathVariable Long id, Model model, RedirectAttributes ra){
        Optional<Categoria> categorias = categoriaRepository.findById(id);

        if(categorias.isEmpty()){
            ra.addFlashAttribute("msgError", "Categoria no encontrada");
            return "redirect:/admin/categoria";
        }
        model.addAttribute("categoria",categorias.get());
        return "categoria/form";
    }

    @GetMapping("/eliminar/{id}")
    String eliminar(@PathVariable Long id, RedirectAttributes ra){
        Optional<Categoria> categorias = categoriaRepository.findById(id);

        if(categorias.isEmpty()){
            ra.addFlashAttribute("msgError", "La categoria que intentas eliminar no existe");
            return"redirect:/admin/categoria";
        }
        Categoria categoria = categorias.get();
        try{
            categoria.setEstado(false);
            categoriaRepository.save(categoria);
            ra.addFlashAttribute("msgExito", "categoría desactivada con exito");
        }catch (Exception e){
            ra.addFlashAttribute("msgError", "categoría no fue desactivado con exito");
        }
        return "redirect:/admin/categoria";
    }






}
