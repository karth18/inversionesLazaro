package pe.com.isil.inversioneslazaro.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.com.isil.inversioneslazaro.model.Producto;
import pe.com.isil.inversioneslazaro.repository.ProductoRepository;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/catalogo")
public class CatalogoController {

    @Autowired
    private ProductoRepository productoRepository;

    @GetMapping("")
    public String catalogo(Model model) {
        // Obtener la lista completa de productos
        List<Producto> productos = productoRepository.findAll();

        // El atributo debe coincidir con th:each="p : ${productos}" en la vista
        model.addAttribute("productos", productos);

        return "producto/catalogo";
    }

    @GetMapping("/detalle/{id}")
    public String detalleProducto(@PathVariable Long id, Model model, RedirectAttributes ra) {
        // 1. Buscar el producto por ID
        Optional<Producto> productoOpt = productoRepository.findById(id);

        // 2. Manejar si el producto no existe
        if (productoOpt.isEmpty()) {
            ra.addFlashAttribute("msgError", "Producto no encontrado.");
            return "redirect:/catalogo";
        }

        // 3. Añadir el producto al modelo
        model.addAttribute("producto", productoOpt.get());

        // 4. Devolver la vista, que debe estar en /resources/templates/producto/detalle.html
        return "producto/detail";
    }




}
