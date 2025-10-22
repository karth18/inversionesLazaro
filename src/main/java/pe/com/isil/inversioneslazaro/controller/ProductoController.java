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
import pe.com.isil.inversioneslazaro.model.TipoProducto;
import pe.com.isil.inversioneslazaro.repository.*;
import pe.com.isil.inversioneslazaro.service.FileSystemStorageService;

import java.util.List;
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
        if (archivosImagen != null) {
            for (MultipartFile file : archivosImagen) {
                if (!file.isEmpty()) {
                    hayArchivosValidos = true;
                    break;
                }
            }
        }

        // 1. Manejo de Errores de Imagen
        // Si es nuevo y no subió ninguna imagen, marcamos un error
        if (esNuevo && !hayArchivosValidos) {
            // Usamos model para mantener el error en la vista actual
            model.addAttribute("msgError", "Debe seleccionar al menos una imagen para el nuevo producto.");
            model.addAttribute("producto", producto);
            cargarListas(model);
            return "producto/form";
        }

        // 2. Manejo de Errores de Validación de Campos (@Valid)
        if (bindingResult.hasErrors()) {
            cargarListas(model);
            model.addAttribute("producto", producto);
            return "producto/form";
        }

        // 3. Cargar y Mapear el producto
        Producto productoExistente = esNuevo ? producto : productoRepository.findById(producto.getId()).orElse(producto);

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
            // Actualiza las FK (Objetos)
            productoExistente.setIdMarca(producto.getIdMarca());
            productoExistente.setIdCate(producto.getIdCate());
            productoExistente.setIdTipo(producto.getIdTipo());
        }

        // 4. Guardar el Producto principal
        Producto productoGuardado = productoRepository.save(productoExistente);

        // 5. Lógica de Manejo de Múltiples Imágenes (Añadir las nuevas imágenes)
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


    @PostMapping("/eliminar/{id}")
    @Transactional
    String eliminar(@PathVariable Long id, RedirectAttributes ra) {

        Optional<Producto> productoOpt = productoRepository.findById(id);

        if (productoOpt.isPresent()) {
            Producto producto = productoOpt.get();


            for (ImagenProducto imagen : producto.getImagenes()) {
                try {

                    fileSystemStorageService.delete(imagen.getRuta());
                } catch (Exception e) {

                    log.error("No se pudo eliminar el archivo físico: {}", imagen.getRuta(), e);

                }
            }

            productoRepository.deleteById(id);

            ra.addFlashAttribute("msgExito", "Producto y sus imágenes asociadas eliminados con éxito.🗑️");

        } else {
            ra.addFlashAttribute("msgError", "Producto no encontrado.");
        }

        return "redirect:/admin/productos";
    }


    @GetMapping("/api/tipos-por-categoria/{idCategoria}")
    @ResponseBody // Indica a Spring que debe devolver los datos directamente (JSON) y no una vista
    public List<TipoProducto> obtenerTiposPorCategoria(@PathVariable Long idCategoria) {
        return tipoProductoRepository.findTiposByCategoriaId(idCategoria);
    }
}
