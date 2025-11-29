package pe.com.isil.inversioneslazaro.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pe.com.isil.inversioneslazaro.model.CotizadorProducto;
import pe.com.isil.inversioneslazaro.repository.CotizadorProductoRepository;

import java.util.List;

@Controller
@RequestMapping("/cotizador") // La URL será: localhost:8080/cotizador
public class PublicCotizadorController {

    @Autowired
    private CotizadorProductoRepository productoRepository;

    @GetMapping("")
    public String mostrarCotizador(Model model) {
        // 1. Buscamos TODOS los productos en la base de datos
        List<CotizadorProducto> listaProductos = productoRepository.findAll();

        // 2. Los enviamos a la vista con el nombre "productos"
        // (Esto es lo que tu HTML está buscando en th:each="prod : ${productos}")
        model.addAttribute("productos", listaProductos);

        return "personaliza/index"; // Asegúrate de que tu HTML se llame 'cotizador.html' en templates
    }
}