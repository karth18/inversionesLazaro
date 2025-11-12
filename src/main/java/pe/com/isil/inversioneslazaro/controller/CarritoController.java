package pe.com.isil.inversioneslazaro.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.com.isil.inversioneslazaro.dto.CarritoItem;
import pe.com.isil.inversioneslazaro.dto.CarritoTotalesDTO;
import pe.com.isil.inversioneslazaro.service.CarritoService;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/carrito")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    // (agregarAlCarrito y verCarrito se quedan igual)
    @PostMapping("/agregar")
    public String agregarAlCarrito(@RequestParam("id") Long productoId,
                                   @RequestParam("cantidad") int cantidad,
                                   RedirectAttributes ra) {
        try {
            carritoService.agregarAlCarrito(productoId, cantidad);
            ra.addFlashAttribute("msgExito", "Producto añadido al carrito");
        } catch (Exception e) {
            ra.addFlashAttribute("msgError", "No se pudo añadir el producto");
        }
        return "redirect:/carrito";
    }
    @GetMapping("")
    public String verCarrito(Model model) {
        Collection<CarritoItem> items = carritoService.getItems();
        Map<String, BigDecimal> totalesMap = carritoService.calcularTotales();
        CarritoTotalesDTO totalesDTO = new CarritoTotalesDTO(totalesMap);
        model.addAttribute("carrito", items);
        model.addAttribute("totales", totalesDTO);
        return "carrito/ver";
    }

    // --- API PARA ACTUALIZAR (CORREGIDA) ---
    @PostMapping("/api/actualizar")
    @ResponseBody
    public ResponseEntity<Map<String, BigDecimal>> actualizarItem(
            @RequestParam("id") Long productoId,
            @RequestParam("cantidad") int cantidad,
            @RequestParam("seleccionado") boolean seleccionado) {

        // 1. Llama al método (que ahora actualiza Y devuelve los totales)
        Map<String, BigDecimal> totales = carritoService.actualizarItem(productoId, cantidad, seleccionado);

        // 2. Devuelve los totales (ya no llamamos a calcularTotales() aquí)
        return ResponseEntity.ok(totales);
    }

    // --- API PARA ELIMINAR (CORREGIDA) ---
    @PostMapping("/api/eliminar")
    @ResponseBody
    public ResponseEntity<Map<String, BigDecimal>> eliminarItem(
            @RequestParam("id") Long productoId) {

        // 1. Llama al método (que ahora elimina Y devuelve los totales)
        Map<String, BigDecimal> totales = carritoService.eliminarItem(productoId);

        // 2. Devuelve los totales (ya no llamamos a calcularTotales() aquí)
        return ResponseEntity.ok(totales);
    }

    // (agregarAlCarritoApi se queda igual)
    @PostMapping("/api/agregar")
    @ResponseBody
    public ResponseEntity<?> agregarAlCarritoApi(
            @RequestParam("id") Long productoId,
            @RequestParam(value = "cantidad", defaultValue = "1") int cantidad) {
        try {
            carritoService.agregarAlCarrito(productoId, cantidad);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Producto añadido al carrito");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error adding product to cart via API: id={}", productoId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "No se pudo añadir el producto. Intente de nuevo.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}