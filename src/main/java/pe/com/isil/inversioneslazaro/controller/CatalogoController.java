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
@SuppressWarnings("unused")
@Controller
@RequestMapping("/catalogo")
public class CatalogoController {

    @Autowired
    private ProductoRepository productoRepository;

    //listado del catálogo o productos
    @GetMapping("")
    public String catalogo(Model model) {
        List<Producto> productos = productoRepository.findByEstadoIsTrue();
        model.addAttribute("productos", productos);

        return "producto/catalogo";
    }

    // detalle del producto
    @GetMapping("/detalle/{id}")
    public String detalleProducto(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Optional<Producto> productoOpt = productoRepository.findById(id);

        if (productoOpt.isEmpty()) {
            ra.addFlashAttribute("msgError", "Producto no encontrado.");
            return "redirect:/catalogo";
        }
        model.addAttribute("producto", productoOpt.get());
        return "producto/detail";
    }


}
