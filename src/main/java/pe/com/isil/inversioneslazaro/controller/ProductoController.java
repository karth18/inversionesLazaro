package pe.com.isil.inversioneslazaro.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.com.isil.inversioneslazaro.model.Producto;
import pe.com.isil.inversioneslazaro.repository.ProductoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;

@Controller
public class ProductoController {

    private final ProductoRepository productoRepository;

    @Value("${app.uploads.path:uploads}")
    private String uploadsPath;

    public ProductoController(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    /* ---------------- Publico: catálogo y detalle ----------------- */

    @GetMapping({"/", "/index", "/home"})
    public String index(Model model) {
        List<Producto> productos = productoRepository.findAll();
        model.addAttribute("productos", productos);
        return "index";
    }

    @GetMapping("/productos/{id}")
    public String detalleProducto(@PathVariable Long id, Model model) {
        Optional<Producto> p = productoRepository.findById(id);
        if (p.isEmpty()) {
            model.addAttribute("error", "Producto no encontrado");
            return "producto/detail";
        }
        model.addAttribute("producto", p.get());
        return "producto/detail";
    }

    /* ---------------- Admin: CRUD / subida de imagen ----------------- */
    // Listado admin
    @GetMapping("/admin/productos")
    public String listarAdmin(Model model) {
        List<Producto> productos = productoRepository.findAll();
        model.addAttribute("productos", productos);
        return "producto/list";
    }

    // Form nuevo
    @GetMapping("/admin/productos/nuevo")
    public String nuevoForm(Model model) {
        if (!model.containsAttribute("producto")) {
            model.addAttribute("producto", new Producto());
        }
        return "producto/form";
    }

    // Form editar
    @GetMapping("/admin/productos/editar/{id}")
    public String editarForm(@PathVariable Long id, Model model) {
        Optional<Producto> p = productoRepository.findById(id);
        if (p.isEmpty()) {
            model.addAttribute("error", "Producto no encontrado");
            return "redirect:/admin/productos";
        }
        model.addAttribute("producto", p.get());
        return "producto/form";
    }

    // Guardar (nuevo/editar) con foto
    @PostMapping("/admin/productos/guardar")
    public String guardar(@ModelAttribute Producto producto,
                          @RequestParam(value = "foto", required = false) MultipartFile foto,
                          HttpServletRequest request,
                          Model model) {

        // validaciones básicas
        if (producto.getCodigo() == null || producto.getCodigo().trim().isEmpty()) {
            model.addAttribute("error", "El código es obligatorio");
            model.addAttribute("producto", producto);
            return "producto/form";
        }

        // manejo de archivo (si subieron)
        if (foto != null && !foto.isEmpty()) {
            String original = StringUtils.cleanPath(foto.getOriginalFilename());
            String ext = "";
            int dot = original.lastIndexOf('.');
            if (dot >= 0) ext = original.substring(dot);
            String filename = producto.getCodigo() + ext; // guardamos con nombre basado en código
            try {
                Path uploadDir = Paths.get(uploadsPath);
                if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir);
                Path target = uploadDir.resolve(filename);
                // guarda el archivo reemplazando si existe
                Files.copy(foto.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
                producto.setFotoPath(filename);
            } catch (IOException e) {
                model.addAttribute("error", "No se pudo guardar la foto: " + e.getMessage());
                model.addAttribute("producto", producto);
                return "producto/form";
            }
        }

        // si el precio fue recibido como null, evitar NPE
        if (producto.getPrecio() == null) {
            producto.setPrecio(BigDecimal.ZERO);
        }

        productoRepository.save(producto);
        return "redirect:/admin/productos";
    }

    // Eliminar (fisicamente)
    @PostMapping("/admin/productos/{id}/eliminar")
    public String eliminar(@PathVariable Long id) {
        productoRepository.findById(id).ifPresent(p -> {
            // si hubiera archivo, podríamos eliminarlo opcionalmente
            if (p.getFotoPath() != null) {
                try {
                    Path f = Paths.get(uploadsPath).resolve(p.getFotoPath());
                    Files.deleteIfExists(f);
                } catch (Exception ignored) {}
            }
            productoRepository.deleteById(id);
        });
        return "redirect:/admin/productos";
    }

    // Seed dev (crea productos de ejemplo si no hay)
    @PostMapping("/admin/productos/seed")
    public String seedProductos() {
        if (productoRepository.count() == 0) {
            Producto p1 = new Producto();
            p1.setCodigo("M-001");
            p1.setNombre("Carrito Sanguchero Básico");
            p1.setDescripcion("Carrito sanguchero en acero, 2 repisas, ruedas giratorias, ideal para ferias.");
            p1.setPrecio(new BigDecimal("1250.00"));
            p1.setStock(10);
            p1.setFotoPath("m_001.jpg");
            productoRepository.save(p1);

            Producto p2 = new Producto();
            p2.setCodigo("M-002");
            p2.setNombre("Carrito Sanguchero Premium");
            p2.setDescripcion("Acero inoxidable AISI304, plancha integrada y cajón térmico.");
            p2.setPrecio(new BigDecimal("4250.00"));
            p2.setStock(5);
            p2.setFotoPath("m_002.jpg");
            productoRepository.save(p2);

            Producto p3 = new Producto();
            p3.setCodigo("M-003");
            p3.setNombre("Vitrina de Exhibición 100cm");
            p3.setDescripcion("Vitrina refrigerada para repostería o sándwiches, con iluminación LED.");
            p3.setPrecio(new BigDecimal("2980.00"));
            p3.setStock(3);
            p3.setFotoPath("m_003.jpg");
            productoRepository.save(p3);
        }
        return "redirect:/admin/productos";
    }

    @GetMapping("/catalogo")
    public String catalogo(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String q
    ) {
        // si quieres búsqueda simple por nombre/código, añade método en repo.
        // Por ahora listamos todos paginados:
        Pageable pageable = PageRequest.of(page, size);

        // Si no tienes método paginado personalizado, usa findAll(Pageable)
        Page<Producto> productosPage = productoRepository.findAll(pageable);

        model.addAttribute("productosPage", productosPage);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("q", q);
        return "catalogo/index"; // nueva plantilla
    }
}
