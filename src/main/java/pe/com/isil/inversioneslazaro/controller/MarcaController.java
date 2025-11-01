package pe.com.isil.inversioneslazaro.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.com.isil.inversioneslazaro.model.Auditoria;
import pe.com.isil.inversioneslazaro.model.Marca;
import pe.com.isil.inversioneslazaro.repository.MarcaRepository;
import pe.com.isil.inversioneslazaro.service.AuditoriaService;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
@SuppressWarnings("unused")
@Controller
@RequestMapping("/admin/marca")
public class MarcaController {

    @Autowired
    private MarcaRepository marcaRepository;

    @Autowired
    private AuditoriaService auditoriaService;


    @GetMapping("")
    String Index(Model model){
        List<Marca> marca = marcaRepository.findByEstadoTrue();
        model.addAttribute("marca", marca);
        return"marca/index";
    }

    @GetMapping("/habilitar")
    String listahabilitar(Model model){
        List<Marca> marca = marcaRepository.findByEstadoFalse();
        model.addAttribute("marca", marca);
        return"marca/enable";
    }

    @GetMapping("/habilitar/{id}")
    String habilitar(@PathVariable Long id, RedirectAttributes ra){
        Optional<Marca> marcas = marcaRepository.findById(id);


        if(marcas.isEmpty()){
            ra.addFlashAttribute("msgError", "La marca que intentas Habilitar no existe");
            return"redirect:/admin/categoria/habilitar";
        }
        Marca marca = marcas.get();
        try{
            marca.setEstado(true);
            Marca guardarMarca = marcaRepository.save(marca);
            registrarAuditoria("Marca",guardarMarca.getId(), Auditoria.AccionAuditoria.HABILITAR);

            ra.addFlashAttribute("msgExito", "marca Activada con exito");
        }catch (Exception e){
            ra.addFlashAttribute("msgError", "marca no fue Activada con exito");
        }
        return "redirect:/admin/marca/habilitar";
    }


    @GetMapping("/nuevo")
    String nuevo(Model model){

        model.addAttribute("marca", new Marca());
        return"marca/form";

    }

    @PostMapping("/nuevo")
    String registrar(@Valid Marca marca, Model model, BindingResult bindingResult, RedirectAttributes ra){

        if(bindingResult.hasErrors()){
            return "marca/form";
        }

        Optional<Marca> existente = marcaRepository.findByNombre(marca.getNombre());

        if(existente.isPresent() && (marca.getId() == null || !existente.get().getId().equals(marca.getId()))){
            bindingResult.rejectValue("nombre", "error.marca","El nombre de la marca ya existe");
            return"marca/form";
        }
        Auditoria.AccionAuditoria accion = (marca.getId() == null)? Auditoria.AccionAuditoria.CREAR: Auditoria.AccionAuditoria.ACTUALIZAR;

        Marca guardarMarca = marcaRepository.save(marca);
        registrarAuditoria("Marca",guardarMarca.getId(), accion);
        ra.addFlashAttribute("msgExito", "Marca guardada/actualizada con éxito");
        return "redirect:/admin/marca";
    }

    @GetMapping("/editar/{id}")
    String editar(@PathVariable Long id, Model model, RedirectAttributes ra){
        Optional<Marca> marca = marcaRepository.findById(id);

        if(marca.isEmpty()){
            ra.addFlashAttribute("msgError", "Marca no encontrada");
            return "redirect:/admin/marca";
        }
        model.addAttribute("marca",marca.get());
        return "marca/form";
    }

    @GetMapping("/eliminar/{id}")
    String eliminar(@PathVariable Long id, RedirectAttributes ra){
        Optional<Marca> marca = marcaRepository.findById(id);

        if(marca.isEmpty()){
            ra.addFlashAttribute("msgError", "La marca que intentas eliminar no existe");
            return"redirect:/admin/categoria";
        }
        Marca marcas = marca.get();
        try{
            marcas.setEstado(false);
            Marca guardarMarca = marcaRepository.save(marcas);
            registrarAuditoria("Marca",guardarMarca.getId(), Auditoria.AccionAuditoria.ELIMINAR);
            ra.addFlashAttribute("msgExito", "marca desactivada con exito");
        }catch (Exception e){
            ra.addFlashAttribute("msgError", "marca no fue desactivado con exito");
        }
        return "redirect:/admin/marca";
    }

    private void registrarAuditoria(String entidadNombre, Object entidadId, Auditoria.AccionAuditoria accion){
        String emailLogueado = SecurityContextHolder.getContext().getAuthentication().getName();

        auditoriaService.registrarAccion(
                emailLogueado,
                entidadNombre,
                entidadId,
                accion
        );
    }

}
