package pe.com.isil.inversioneslazaro.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.com.isil.inversioneslazaro.model.ConfiguracionEmpresa;
import pe.com.isil.inversioneslazaro.repository.ConfiguracionEmpresaRepository;

import java.util.List;

@Controller
@RequestMapping("/admin/configuracion")
public class AdminConfigController {

    @Autowired
    private ConfiguracionEmpresaRepository configRepo;

    @GetMapping("")
    public String verConfiguracion(Model model) {
        // Buscamos cualquier configuración existente
        List<ConfiguracionEmpresa> configs = configRepo.findAll();

        // Si la lista está vacía, pasamos un objeto nuevo (vacío) al formulario
        // Si hay datos, pasamos el primero que encuentre (index 0)
        ConfiguracionEmpresa config = configs.isEmpty() ? new ConfiguracionEmpresa() : configs.get(0);

        model.addAttribute("config", config);
        return "admin/configuracion/index";
    }

    @PostMapping("/guardar")
    public String guardarConfiguracion(ConfiguracionEmpresa configForm, RedirectAttributes ra) {

        // 1. RECUPERAR LA ENTIDAD REAL DE LA BD
        List<ConfiguracionEmpresa> configs = configRepo.findAll();
        ConfiguracionEmpresa configDB;

        if (configs.isEmpty()) {
            // CASO A: BASE DE DATOS VACÍA -> Creamos una instancia nueva
            configDB = new ConfiguracionEmpresa();
            // No forzamos el ID, dejamos que la base de datos asigne el 1 automáticamente
        } else {
            // CASO B: YA EXISTE -> Tomamos la existente para editarla
            configDB = configs.get(0);
        }

        // 2. COPIAR DATOS DEL FORMULARIO A LA ENTIDAD REAL
        // Esto evita el error "Optimistic Locking" porque estamos llenando un objeto gestionado

        // General
        configDB.setUrlLogo(configForm.getUrlLogo());
        configDB.setTextoBoton(configForm.getTextoBoton());
        configDB.setFooterAgradecimiento(configForm.getFooterAgradecimiento());
        configDB.setRecomendacionesEntrega(configForm.getRecomendacionesEntrega());

        // Correos
        configDB.setAsuntoBienvenida(configForm.getAsuntoBienvenida());
        configDB.setMensajeBienvenida(configForm.getMensajeBienvenida());

        configDB.setAsuntoEnCamino(configForm.getAsuntoEnCamino());
        configDB.setMensajeEnCamino(configForm.getMensajeEnCamino());

        configDB.setAsuntoEntregado(configForm.getAsuntoEntregado());
        configDB.setMensajeEntregado(configForm.getMensajeEntregado());

        configDB.setAsuntoReagendado(configForm.getAsuntoReagendado());
        configDB.setMensajeReagendado(configForm.getMensajeReagendado());

        configDB.setAsuntoCancelado(configForm.getAsuntoCancelado());
        configDB.setMensajeCancelado(configForm.getMensajeCancelado());

        // 3. GUARDAR
        configRepo.save(configDB);

        ra.addFlashAttribute("msgExito", "Configuración guardada correctamente.");
        return "redirect:/admin/configuracion";
    }
}