package pe.com.isil.inversioneslazaro.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.com.isil.inversioneslazaro.model.Producto;
import pe.com.isil.inversioneslazaro.repository.CategoriaRepository;
import pe.com.isil.inversioneslazaro.repository.ProductoRepository;
import pe.com.isil.inversioneslazaro.repository.TipoProductoRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
@SuppressWarnings("unused")
@Controller
@RequestMapping("/catalogo")
public class CatalogoController {

    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private TipoProductoRepository tipoProductoRepository;

    //listado del catálogo o productos
    @GetMapping("")
    public String catalogo(
            Model model,
            @RequestParam(name = "q", required = false) String busqueda,
            @RequestParam(name = "categoria", required = false) Long idCategoria,
            @RequestParam(name = "tipo", required = false) Long idTipo,
            @RequestParam(name = "min", required = false) BigDecimal min,
            @RequestParam(name = "max", required = false) BigDecimal max
    ) {
        // 1. Ejecutar la búsqueda inteligente
        List<Producto> productos = productoRepository.filtrarProductos(busqueda, idCategoria, idTipo, min, max);

        // 2. Pasar productos a la vista
        model.addAttribute("productos", productos);

        // 3. Pasar listas para los filtros laterales
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("tipos", tipoProductoRepository.findAll());

        // 4. Mantener los filtros seleccionados en la vista (Para que no se borren al recargar)
        model.addAttribute("busquedaActual", busqueda);
        model.addAttribute("catActual", idCategoria);
        model.addAttribute("tipoActual", idTipo);
        model.addAttribute("minActual", min);
        model.addAttribute("maxActual", max);

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
