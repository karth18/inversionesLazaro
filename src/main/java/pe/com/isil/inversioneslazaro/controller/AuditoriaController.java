package pe.com.isil.inversioneslazaro.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pe.com.isil.inversioneslazaro.model.Auditoria;
import pe.com.isil.inversioneslazaro.repository.AuditoriaRepository;

@Controller
@RequestMapping("/admin/auditoria")
public class AuditoriaController {
    @Autowired
    private AuditoriaRepository auditoriaRepository;

    @GetMapping("")
    public String verAuditoria(@RequestParam(value = "busqueda", required = false) String busqueda,
                               @RequestParam(defaultValue = "0") int page,
                               Model model) {

        Pageable pageable = PageRequest.of(page, 10, Sort.by("fechaAccion").descending());
        Page<Auditoria> audi;

        if (busqueda != null && !busqueda.trim().isEmpty()) {
            audi = auditoriaRepository.buscarAuditoria(busqueda.trim(), pageable);
        } else {
            audi = auditoriaRepository.findAll(pageable);
        }

        model.addAttribute("auditorias", audi.getContent());
        model.addAttribute("audi", audi);
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("totalRegistros", audi.getTotalElements());

        return "auditoria/index";
    }
}
