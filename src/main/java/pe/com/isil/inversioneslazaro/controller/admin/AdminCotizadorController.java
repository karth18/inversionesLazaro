package pe.com.isil.inversioneslazaro.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.com.isil.inversioneslazaro.model.*;
import pe.com.isil.inversioneslazaro.repository.*;
import pe.com.isil.inversioneslazaro.service.StorageService; // Tu servicio de guardar fotos


@Controller
@RequestMapping("/admin/cotizador")
public class AdminCotizadorController {

    @Autowired private CotizadorProductoRepository productoRepo;
    @Autowired private CotizadorRangoRepository rangoRepo;
    @Autowired private CotizadorComponenteRepository componenteRepo;
    @Autowired private StorageService storageService;

    // 1. VISTA TARJETAS (Index)
    @GetMapping("")
    public String index(Model model) {
        model.addAttribute("productos", productoRepo.findAll());
        return "personaliza/adminCotizador/index";
    }

    // Guardar Producto Nuevo
    @PostMapping("/producto/guardar")
    public String guardarProducto(@RequestParam("nombre") String nombre,
                                  @RequestParam("alto") Double alto,
                                  @RequestParam("fondo") Double fondo,
                                  @RequestParam("imagen") MultipartFile imagen) {
        CotizadorProducto p = new CotizadorProducto();
        p.setNombre(nombre);
        p.setAltoEstandar(alto);
        p.setFondoEstandar(fondo);
        p.setActivo(true);
        if (!imagen.isEmpty()) {
            p.setImagenUrl(storageService.store(imagen)); // Tu lógica de guardar imagen
        }
        productoRepo.save(p);
        return "redirect:/admin/cotizador";
    }

    // Toggle Activo/Inactivo (Eliminación Lógica)
    @GetMapping("/producto/toggle/{id}")
    public String toggleEstado(@PathVariable Long id) {
        CotizadorProducto p = productoRepo.findById(id).orElseThrow();
        p.setActivo(!p.isActivo());
        productoRepo.save(p);
        return "redirect:/admin/cotizador";
    }

    // 2. VISTA CONFIGURADOR (El Cerebro)
    @GetMapping("/configurar/{id}")
    public String configurar(@PathVariable Long id, Model model) {
        CotizadorProducto p = productoRepo.findById(id).orElseThrow();
        model.addAttribute("producto", p);
        model.addAttribute("nuevoRango", new CotizadorRango());
        model.addAttribute("nuevoComponente", new CotizadorComponente());
        return "personaliza/adminCotizador/configurar";
    }

    // Agregar Rango
    @PostMapping("/rango/guardar")
    public String guardarRango(CotizadorRango rango, @RequestParam("productoId") Long productoId) {
        CotizadorProducto p = productoRepo.findById(productoId).orElseThrow();
        rango.setProducto(p);
        rangoRepo.save(rango);
        return "redirect:/admin/cotizador/configurar/" + productoId;
    }

    // Agregar Componente
    @PostMapping("/componente/guardar")
    public String guardarComponente(CotizadorComponente comp, @RequestParam("productoId") Long productoId) {
        CotizadorProducto p = productoRepo.findById(productoId).orElseThrow();
        comp.setProducto(p);
        componenteRepo.save(comp);
        return "redirect:/admin/cotizador/configurar/" + productoId;
    }

    @PostMapping("/producto/actualizar-dimensiones")
    public String actualizarDimensiones(@RequestParam("id") Long id,
                                        @RequestParam("alto") Double alto,
                                        @RequestParam("fondo") Double fondo) {
        CotizadorProducto p = productoRepo.findById(id).orElseThrow();
        p.setAltoEstandar(alto);
        p.setFondoEstandar(fondo);
        productoRepo.save(p);
        return "redirect:/admin/cotizador/configurar/" + id;
    }

    // Aquí agregarías los métodos para eliminar rango y componente por ID...
}