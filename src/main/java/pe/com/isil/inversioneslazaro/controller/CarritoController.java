package pe.com.isil.inversioneslazaro.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.com.isil.inversioneslazaro.service.CarritoService;

import java.math.BigDecimal;
import java.util.Map;

@Controller
@RequestMapping("/carrito")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    // 1. Endpoint que recibe el FORMULARIO de tu detail.html
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
        // Redirige a la vista del carrito
        return "redirect:/carrito";
    }

    // 2. Endpoint para MOSTRAR la página del carrito (la que describiste)
    @GetMapping("")
    public String verCarrito(Model model) {
        model.addAttribute("carrito", carritoService.getCarrito().values());
        model.addAttribute("totales", carritoService.calcularTotales());
        return "carrito/ver"; // La nueva vista que crearemos en el Paso 5
    }

    // 3. API para ACTUALIZAR (Checkbox y Cantidad) - Se llama con JavaScript
    @PostMapping("/api/actualizar")
    @ResponseBody // Retorna JSON
    public ResponseEntity<Map<String, BigDecimal>> actualizarItem(
            @RequestParam("id") Long productoId,
            @RequestParam("cantidad") int cantidad,
            @RequestParam("seleccionado") boolean seleccionado) {

        carritoService.actualizarItem(productoId, cantidad, seleccionado);
        Map<String, BigDecimal> totales = carritoService.calcularTotales();
        return ResponseEntity.ok(totales); // Devuelve los nuevos totales
    }

    // 4. API para ELIMINAR (Menú de 3 puntos) - Se llama con JavaScript
    @PostMapping("/api/eliminar")
    @ResponseBody // Retorna JSON
    public ResponseEntity<Map<String, BigDecimal>> eliminarItem(
            @RequestParam("id") Long productoId) {

        carritoService.eliminarItem(productoId);
        Map<String, BigDecimal> totales = carritoService.calcularTotales();
        return ResponseEntity.ok(totales); // Devuelve los nuevos totales
    }
}