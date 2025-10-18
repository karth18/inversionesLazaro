/*
package pe.com.isil.inversioneslazaro.controller.admin;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.com.isil.inversioneslazaro.model.Cliente;
import pe.com.isil.inversioneslazaro.repository.ClienteRepository;
import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("/admin/clientes")
public class ClienteAdminController {

    private final ClienteRepository clienteRepository;

    public ClienteAdminController(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    // Listado
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("clientes", clienteRepository.findAll());
        return "cliente/list";
    }

    // Form nuevo
    @GetMapping("/nuevo")
    public String nuevoForm(Model model) {
        if (!model.containsAttribute("cliente")) {
            model.addAttribute("cliente", new Cliente());
        }
        return "cliente/form";
    }

    // Guardar (nuevo y edición)
    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute Cliente cliente,
                          BindingResult result,
                          RedirectAttributes ra,
                          Principal principal) {

        // Validación unicidad email (si es nuevo o si cambia)
        if (cliente.getId() == null) {
            if (clienteRepository.existsByEmail(cliente.getEmail())) {
                result.rejectValue("email", "EmailExists", "El correo ya está registrado");
            }
        } else {
            // editar: verificar que no exista en otro registro
            Optional<Cliente> porEmail = clienteRepository.findByEmail(cliente.getEmail());
            if (porEmail.isPresent() && !porEmail.get().getId().equals(cliente.getId())) {
                result.rejectValue("email", "EmailExists", "El correo ya está registrado por otro cliente");
            }
        }

        if (result.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.cliente", result);
            ra.addFlashAttribute("cliente", cliente);
            return "redirect:/admin/clientes/" + (cliente.getId() == null ? "nuevo" : "editar/" + cliente.getId());
        }

        String actor = principal != null ? principal.getName() : "system";
        if (cliente.getId() == null) {
            cliente.setCreatedBy(actor);
        } else {
            cliente.setModifiedBy(actor);
        }

        clienteRepository.save(cliente);
        ra.addFlashAttribute("mensajeExito", "Cliente guardado correctamente");
        return "redirect:/admin/clientes";
    }

    // Form editar
    @GetMapping("/editar/{id}")
    public String editarForm(@PathVariable Long id, RedirectAttributes ra, Model model) {
        Optional<Cliente> c = clienteRepository.findById(id);
        if (c.isEmpty()) {
            ra.addFlashAttribute("error", "Cliente no encontrado");
            return "redirect:/admin/clientes";
        }
        model.addAttribute("cliente", c.get());
        return "cliente/form";
    }

    // Desactivar (no eliminar físicamente)
    @PostMapping("/{id}/desactivar")
    public String desactivar(@PathVariable Long id, RedirectAttributes ra, Principal principal) {
        Optional<Cliente> cOpt = clienteRepository.findById(id);
        if (cOpt.isEmpty()) {
            ra.addFlashAttribute("error", "Cliente no encontrado");
            return "redirect:/admin/clientes";
        }
        Cliente c = cOpt.get();
        c.setActivo(false);
        c.setModifiedBy(principal != null ? principal.getName() : "system");
        clienteRepository.save(c);
        ra.addFlashAttribute("mensajeExito", "Cliente desactivado correctamente");
        return "redirect:/admin/clientes";
    }

}
*/
