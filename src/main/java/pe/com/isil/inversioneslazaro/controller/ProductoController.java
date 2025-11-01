package pe.com.isil.inversioneslazaro.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.com.isil.inversioneslazaro.model.Auditoria;
import pe.com.isil.inversioneslazaro.model.ImagenProducto;
import pe.com.isil.inversioneslazaro.model.Producto;
import pe.com.isil.inversioneslazaro.model.TipoProducto;
import pe.com.isil.inversioneslazaro.repository.*;
import pe.com.isil.inversioneslazaro.service.AuditoriaService;
import pe.com.isil.inversioneslazaro.service.FileSystemStorageService;

import java.util.List;
import java.util.Optional;
@SuppressWarnings("unused")
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
    @Autowired
    private AuditoriaService auditoriaService;



    //Modificando a un metodo auxiliar
    private void cargarListas(Model model) {
        model.addAttribute("marcas", marcaRepository.findAll());
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("tiposProducto", tipoProductoRepository.findAll());
    }

    // listado de los productos
    @GetMapping("")
    String index(Model model,
                 @PageableDefault(size = 5, sort="nomPro")Pageable pageable,
                 @RequestParam(required=false)String nombre,
    @RequestParam(required = false)String estado) {

        Page<Producto> productos;
        // --- 2. LÓGICA DE FILTRADO MEJORADA ---
        String nombreBusqueda = (nombre != null && !nombre.trim().isEmpty()) ? nombre.trim() : null;
        Boolean estadoBusqueda = null; // null = Todos

        if ("true".equals(estado)) {
            estadoBusqueda = true;
        } else if ("false".equals(estado)) {
            estadoBusqueda = false;
        }

        // Lógica de búsqueda combinada
        if (nombreBusqueda != null && estadoBusqueda != null) {
            // Busca por nombre Y estado
            productos = productoRepository.findByNomProContainingAndEstado(nombreBusqueda, estadoBusqueda, pageable);
        } else if (nombreBusqueda != null) {
            // Busca solo por nombre (todos los estados)
            productos = productoRepository.findByNomProContaining(nombreBusqueda, pageable);
        } else if (estadoBusqueda != null) {
            // Busca solo por estado
            productos = productoRepository.findByEstado(estadoBusqueda, pageable);
        } else {
            // Sin filtros, muestra todos
            productos = productoRepository.findAll(pageable);
        }

        model.addAttribute("producto", productos);
        model.addAttribute("paramNombre", nombre);
        model.addAttribute("paramEstado", estado);
        return "producto/list";

    }

    @GetMapping("/nuevo")
    String nuevo(Model model){
        Producto nuevoProducto = new Producto();
        nuevoProducto.setEstado(true); // Asignar 'activo' por defecto
        model.addAttribute("producto", nuevoProducto);
        cargarListas(model);
        return "producto/form";
    }

    @GetMapping("/editar/{id}")
    String editar(Model model, @PathVariable Long id, RedirectAttributes ra) {
        Optional<Producto> productoOpt = productoRepository.findById(id);

        if (productoOpt.isEmpty()) {
            ra.addFlashAttribute("msgError", "Producto no encontrado para editar.");
            return "redirect:/admin/productos";
        }


        model.addAttribute("producto", productoOpt.get());
        cargarListas(model);
        return "producto/form";
    }


    @PostMapping("/guardar")
    @Transactional
    String guardar(Model model,
                   @Valid Producto producto,
                   BindingResult bindingResult,
                   RedirectAttributes ra,
                   @RequestParam(value = "archivosImagen", required = false) MultipartFile[] archivosImagen) {

        boolean esNuevo = producto.getId() == null;
        // **Validación de Imagen (Solo si es nuevo y no hay archivos válidos)**
        boolean hayArchivosValidos = false;

        String emailLogueado = SecurityContextHolder.getContext().getAuthentication().getName();

        if (esNuevo && producto.getCodPro() != null && !producto.getCodPro().trim().isEmpty()) {
            boolean codigoExiste = productoRepository.findByCodPro(producto.getCodPro().trim()).isPresent();
            if (codigoExiste) {
                // Añade un error específico para el campo 'codPro'
                bindingResult.rejectValue("codPro", "CodProAlreadyExists", "Este código de producto ya está registrado.");
            }
        }

        if (archivosImagen != null) {
            for (MultipartFile file : archivosImagen) {
                if (!file.isEmpty()) {
                    hayArchivosValidos = true;
                    break;
                }
            }
        }

        // 1. Manejo de Errores de Imagen
        if (esNuevo && !hayArchivosValidos) {
            bindingResult.rejectValue("imagenes", "MultipartNotEmpty.producto.imagen", "Debe seleccionar al menos una imagen para el nuevo producto.");
        }

        // 2. Manejo de Errores de Validación de Campos (@Valid)
        if (bindingResult.hasErrors()) {
            cargarListas(model);
            model.addAttribute("producto", producto);
            return "producto/form";
        }

        // 3. Cargar y Mapear el producto
        Producto productoExistente = esNuevo ? producto : productoRepository.findById(producto.getId()).orElse(producto);


        // Si no es nuevo (es edición), mapeamos todos los campos del formulario al objeto existente
        if (!esNuevo) {
            // Sección 1: Datos Principales
            productoExistente.setCodPro(producto.getCodPro());
            productoExistente.setNomPro(producto.getNomPro());
            productoExistente.setPrecio(producto.getPrecio());
            productoExistente.setPrecioOferta(producto.getPrecioOferta());
            productoExistente.setStock(producto.getStock());
            productoExistente.setDescripcionCorta(producto.getDescripcionCorta());
            productoExistente.setDescripcionLarga(producto.getDescripcionLarga());
            productoExistente.setEstado(producto.isEstado());

            // Sección 2: Relaciones y Dimensiones
            productoExistente.setIdMarca(producto.getIdMarca());
            productoExistente.setIdCate(producto.getIdCate());
            productoExistente.setIdTipo(producto.getIdTipo());
            productoExistente.setAltoCm(producto.getAltoCm());
            productoExistente.setAnchoCm(producto.getAnchoCm());
            productoExistente.setFondoCm(producto.getFondoCm());

            // Sección 3: Especificaciones Adicionales
            productoExistente.setModelo(producto.getModelo());
            productoExistente.setMaterial(producto.getMaterial());
            productoExistente.setPotenciaBtu(producto.getPotenciaBtu());
            productoExistente.setGarantiaMeses(producto.getGarantiaMeses());
            productoExistente.setPaisOrigen(producto.getPaisOrigen());
            productoExistente.setFichaTecnica(producto.getFichaTecnica());

            auditoriaService.registrarAccion(
                    emailLogueado,
                    "producto",
                    producto.getCodPro(),
                    Auditoria.AccionAuditoria.ACTUALIZAR);
        }

        // 4. Guardar el Producto principal
        Producto productoGuardado = productoRepository.save(productoExistente);

        // 5. Lógica de Manejo de Múltiples Imágenes (Sin cambios)
        if (archivosImagen != null && archivosImagen.length > 0) {
            int totalImagenesExistentes = productoGuardado.getImagenes().size();
            int contadorNuevas = 0;

            for (MultipartFile file : archivosImagen) {

                if (!file.isEmpty()) {
                    if (totalImagenesExistentes + contadorNuevas >= 10) {
                        ra.addFlashAttribute("msgAdvertencia", "Se ha alcanzado el límite de 10 imágenes. Solo se subieron las primeras 10.");
                        break;
                    }

                    String nombreArchivo = productoGuardado.getId() + "-" + (totalImagenesExistentes + contadorNuevas) + "-" + file.getOriginalFilename();
                    String rutaUnica = fileSystemStorageService.store(file, nombreArchivo);

                    ImagenProducto imagen = new ImagenProducto();
                    imagen.setRuta(rutaUnica);
                    imagen.setProducto(productoGuardado);

                    if (totalImagenesExistentes == 0 && contadorNuevas == 0) {
                        imagen.setEsPrincipal(true);
                    } else {
                        imagen.setEsPrincipal(false);
                    }

                    productoGuardado.getImagenes().add(imagen);
                    contadorNuevas++;
                }
            }

            auditoriaService.registrarAccion(
                    emailLogueado,
                    "Producto",
                    producto.getCodPro(),
                    Auditoria.AccionAuditoria.CREAR);
            productoRepository.save(productoGuardado);
        }

        String mensaje = esNuevo ? "Producto registrado con éxito" : "Producto actualizado con éxito";
        ra.addFlashAttribute("msgExito", mensaje);

        return "redirect:/admin/productos";
    }


    @PostMapping("/eliminar/{id}")
    @Transactional
    String eliminar(@PathVariable Long id, RedirectAttributes ra) {

        Optional<Producto> productoOpt = productoRepository.findById(id);
        String emailLogueado = SecurityContextHolder.getContext().getAuthentication().getName();
        if (productoOpt.isPresent()) {
            Producto producto = productoOpt.get();
            producto.setEstado(false); // Eliminación lógica

            auditoriaService.registrarAccion(
                    emailLogueado,
                    "Producto",
                    producto.getCodPro(),
                    Auditoria.AccionAuditoria.ELIMINAR);

            productoRepository.save(producto);

            ra.addFlashAttribute("msgExito", "Producto desactivado con éxito. 🗑️");

        } else {
            ra.addFlashAttribute("msgError", "Producto no encontrado.");
        }

        return "redirect:/admin/productos";
    }

    @PostMapping("/activar/{id}")
    @Transactional
    String activar(@PathVariable Long id, RedirectAttributes ra) {
        Optional<Producto> productoOpt = productoRepository.findById(id);
        String emailLogueado = SecurityContextHolder.getContext().getAuthentication().getName();
        if (productoOpt.isPresent()) {
            Producto producto = productoOpt.get();
            producto.setEstado(true);
            auditoriaService.registrarAccion(
                    emailLogueado,
                    "Producto",
                    producto.getCodPro(),
                    Auditoria.AccionAuditoria.HABILITAR);
            productoRepository.save(producto);
            ra.addFlashAttribute("msgExito", "Producto activado con éxito. ✅");
        } else {
            ra.addFlashAttribute("msgError", "Producto no encontrado.");
        }

        // Redirige de vuelta a la lista (manteniendo los filtros si es posible)
        return "redirect:/admin/productos";
    }

    @GetMapping("/api/tipos-por-categoria/{idCategoria}")
    @ResponseBody
    public List<TipoProducto> obtenerTiposPorCategoria(@PathVariable Long idCategoria) {
        return tipoProductoRepository.findTiposByCategoriaId(idCategoria);
    }
}
