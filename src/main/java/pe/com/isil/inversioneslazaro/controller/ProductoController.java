package pe.com.isil.inversioneslazaro.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.com.isil.inversioneslazaro.model.ImagenProducto;
import pe.com.isil.inversioneslazaro.model.Producto;
import pe.com.isil.inversioneslazaro.repository.*;
import pe.com.isil.inversioneslazaro.service.FileSystemStorageService;

import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/admin/productos")
public class ProductoController {

    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private ImagenProductoRepository imagenProductoRepository;
    @Autowired
    private FileSystemStorageService fileSystemStorageService;
    @Autowired
    private MarcaRepository marcaRepository;
    @Autowired
    private TipoProductoRepository tipoProductoRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;


    //Modificando a un metodo auxiliar a ver si funciona
    private void cargarListas(Model model) {
        model.addAttribute("marcas", marcaRepository.findAll());
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("tiposProducto", tipoProductoRepository.findAll());
    }

// listado de los productos
    @GetMapping("")
    String index(Model model,
                 @PageableDefault(size = 5, sort="nomPro")Pageable pageable,
                 @RequestParam(required=false)String nombre) {

        Page<Producto> productos;
        if(nombre != null && !nombre.trim().isEmpty()){
            productos = productoRepository.findByNomProContaining(nombre,pageable);
        }
        else {
            productos= productoRepository.findAll(pageable);
        }

        model.addAttribute("producto", productos);
        return "producto/list";

    }

    @GetMapping("/nuevo")
    String nuevo(Model model){

        model.addAttribute("producto", new Producto());
        //aqui agrego el metodo auxiliar
        cargarListas(model);

        return "producto/form";
    }

    @GetMapping("/editar/{id}")
    String editar(Model model, @PathVariable Long id, RedirectAttributes ra) {
        Optional<Producto> productoOpt = productoRepository.findById(id);

        if (!productoOpt.isPresent()) {
            ra.addFlashAttribute("msgError", "Producto no encontrado para editar.");
            return "redirect:/admin/productos";
        }

        model.addAttribute("producto", productoOpt.get());
        cargarListas(model); // <--- Necesario para el formulario
        return "producto/form";
    }

    // --- CREATE/UPDATE: Registrar o Actualizar producto (MÉTODO GUARDAR MODIFICADO) ---
    @PostMapping("/guardar")
    @Transactional
    String guardar(Model model,
                   @Valid Producto producto,
                   BindingResult bindingResult,
                   RedirectAttributes ra,
                   // Recibimos los archivos de imagen. El nombre 'archivosImagen' debe coincidir con el campo 'name' del input file en la vista.
                   @RequestParam("archivosImagen") MultipartFile[] archivosImagen) {

        boolean esNuevo = producto.getId() == null;

        // **Validación de Imagen (Solo si es nuevo y no hay archivos válidos)**
        boolean hayArchivosValidos = false;
        if (archivosImagen != null) {
            for (MultipartFile file : archivosImagen) {
                if (!file.isEmpty()) {
                    hayArchivosValidos = true;
                    break;
                }
            }
        }

        // Si es nuevo y no subió ninguna imagen, marcamos un error
        if (esNuevo && !hayArchivosValidos) {
            // No podemos usar bindingResult para @RequestParam. Usaremos FlashAttribute para el error general.
            ra.addFlashAttribute("msgError", "Debe seleccionar al menos una imagen para el nuevo producto.");
            // También podríamos devolver la vista, pero la validación aquí es clave:
            // model.addAttribute("producto", producto);
            // cargarListas(model);
            // return "producto/form";
        }

        // ... (Tu lógica de validación de codPro se mantiene aquí) ...

        if (bindingResult.hasErrors()) {
            cargarListas(model);
            model.addAttribute("producto", producto);
            return "producto/form";
        }

        // 1. Cargar el producto existente si es una edición
        Producto productoExistente = esNuevo ? producto : productoRepository.findById(producto.getId()).orElse(producto);

        // 2. Mapear campos actualizables (Esto es vital en la edición, ya que el request NO contiene toda la lista de ImagenProducto)
        if (!esNuevo) {
            productoExistente.setCodPro(producto.getCodPro());
            productoExistente.setNomPro(producto.getNomPro());
            productoExistente.setDescripcionCorta(producto.getDescripcionCorta());
            productoExistente.setDescripcionLarga(producto.getDescripcionLarga());
            productoExistente.setPrecio(producto.getPrecio());
            productoExistente.setStock(producto.getStock());
            productoExistente.setAltoCm(producto.getAltoCm());
            productoExistente.setAnchoCm(producto.getAnchoCm());
            productoExistente.setFondoCm(producto.getFondoCm());
            // Actualiza las FK
            productoExistente.setIdMarca(producto.getIdMarca());
            productoExistente.setIdCate(producto.getIdCate());
            productoExistente.setIdTipo(producto.getIdTipo());
            // ... (otros campos)
        }

        // 3. Guardar el Producto principal (para obtener/confirmar el ID)
        Producto productoGuardado = productoRepository.save(productoExistente);

        // 4. Lógica de Manejo de Múltiples Imágenes (Añadir las nuevas imágenes)
        if (archivosImagen != null && archivosImagen.length > 0) {
            int totalImagenesExistentes = productoGuardado.getImagenes().size();
            int contadorNuevas = 0;

            for (MultipartFile file : archivosImagen) {

                if (!file.isEmpty()) {
                    // Validar límite (10 imágenes)
                    if (totalImagenesExistentes + contadorNuevas >= 10) {
                        ra.addFlashAttribute("msgAdvertencia", "Se ha alcanzado el límite de 10 imágenes. Solo se subieron las primeras 10.");
                        break;
                    }

                    // A. Guardar el archivo en el sistema de archivos
                    // Usar un nombre único para evitar colisiones (ej: ID_PRODUCTO-NUMERO.jpg)
                    // NOTA: Usa un UUID + nombre original para mayor seguridad.
                    String nombreArchivo = productoGuardado.getId() + "-" + (totalImagenesExistentes + contadorNuevas) + "-" + file.getOriginalFilename();
                    String rutaUnica = fileSystemStorageService.store(file, nombreArchivo);

                    // B. Crear y guardar la entidad ImagenProducto
                    ImagenProducto imagen = new ImagenProducto();
                    imagen.setRuta(rutaUnica);
                    imagen.setProducto(productoGuardado);

                    // Si el producto no tenía imágenes, la primera subida es la principal
                    if (totalImagenesExistentes == 0 && contadorNuevas == 0) {
                        imagen.setEsPrincipal(true);
                    } else {
                        imagen.setEsPrincipal(false);
                    }

                    productoGuardado.getImagenes().add(imagen);
                    contadorNuevas++;
                }
            }
            // Guarda para persistir la lista de imágenes
            productoRepository.save(productoGuardado);
        }

        String mensaje = esNuevo ? "Producto registrado con éxito" : "Producto actualizado con éxito";
        ra.addFlashAttribute("msgExito", mensaje);

        return "redirect:/admin/productos";
    }


// Archivo: pe.com.isil.inversioneslazaro.controller.ProductoController

// ... (dentro de la clase ProductoController)

    // --- DELETE: Eliminar producto (Mejorado) ---
    @PostMapping("/eliminar/{id}")
    @Transactional
    String eliminar(@PathVariable Long id, RedirectAttributes ra) {

        // 1. Buscar el producto por su ID
        Optional<Producto> productoOpt = productoRepository.findById(id);

        if (productoOpt.isPresent()) {
            Producto producto = productoOpt.get();

            // 2. ELIMINAR LOS ARCHIVOS FÍSICOS ASOCIADOS
            // Es crucial eliminar los archivos físicos ANTES de eliminar el registro en la DB.

            // Iteramos sobre la colección de objetos ImagenProducto
            for (ImagenProducto imagen : producto.getImagenes()) {
                try {
                    // Llamamos al método de eliminación con la ruta (String) de cada imagen.
                    fileSystemStorageService.delete(imagen.getRuta());
                } catch (Exception e) {
                    // Capturar la excepción es importante para que la eliminación de otros
                    // archivos o del producto principal no se detenga por un error en un archivo.
                    log.error("No se pudo eliminar el archivo físico: {}", imagen.getRuta(), e);
                    // Opcional: Podrías registrar un mensaje de advertencia para el usuario.
                }
            }

            // 3. ELIMINAR EL REGISTRO DEL PRODUCTO EN LA BASE DE DATOS
            // Gracias a CascadeType.ALL y orphanRemoval=true en la entidad Producto,
            // los registros de ImagenProducto se eliminarán automáticamente.
            productoRepository.deleteById(id);

            ra.addFlashAttribute("msgExito", "Producto y sus imágenes asociadas eliminados con éxito.🗑️");

        } else {
            ra.addFlashAttribute("msgError", "Producto no encontrado.");
        }

        return "redirect:/admin/productos";
    }




    /*private final ProductoRepository productoRepository;

    @Value("${app.uploads.path:uploads}")
    private String uploadsPath;

    public ProductoController(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    *//* ---------------- Publico: catálogo y detalle ----------------- *//*

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

    *//* ---------------- Admin: CRUD / subida de imagen ----------------- *//*
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
    }*/
}
