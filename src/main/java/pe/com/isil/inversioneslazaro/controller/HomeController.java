package pe.com.isil.inversioneslazaro.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pe.com.isil.inversioneslazaro.model.BannerHome; // Para el Hero
import pe.com.isil.inversioneslazaro.model.HomeComponente; // Para los bloques
import pe.com.isil.inversioneslazaro.model.Producto; // Para el carrusel
import pe.com.isil.inversioneslazaro.repository.BannerHomeRepository; // Para el Hero
import pe.com.isil.inversioneslazaro.repository.HomeComponenteRepository; // Para los bloques
import pe.com.isil.inversioneslazaro.repository.ProductoRepository; // Para el carrusel

import java.util.List;

@Controller
public class HomeController {

    // --- Inyecta solo los 3 repositorios que SÍ usas ---
    @Autowired private BannerHomeRepository bannerHomeRepository;
    @Autowired private HomeComponenteRepository homeComponenteRepository;
    @Autowired private ProductoRepository productoRepository;

    private static final int LIMITE_CARRUSEL_RANDOM = 18;

    @GetMapping("/")
    public String paginaDeInicio(Model model) {

        // --- 1. CARGAR EL BANNER HERO (Tu sistema original) ---
        // (Usa el método corregido que te pasé antes para evitar el error 'NonUniqueResult')
        Pageable limiteBanner = PageRequest.of(0, 1);
        List<BannerHome> banners = bannerHomeRepository.findByEstaActivoTrueOrderByOrdenAsc(limiteBanner);
        if (banners != null && !banners.isEmpty()) {
            model.addAttribute("bannerHero", banners.get(0));
        }

        // --- 2. CARGAR LOS COMPONENTES (Tu nuevo sistema) ---

        // Cargar las OFERTAS
        List<HomeComponente> ofertas = homeComponenteRepository
                .findBySeccionAndEstaActivoTrueOrderByOrdenAsc(HomeComponente.Seccion.OFERTA);
        model.addAttribute("seccionOfertas", ofertas);

        // Cargar el ANUNCIO TIRA
        homeComponenteRepository
                .findBySeccionAndEstaActivoTrueOrderByOrdenAsc(HomeComponente.Seccion.ANUNCIO_TIRA)
                .stream().findFirst().ifPresent(anuncio -> {
                    model.addAttribute("seccionAnuncio", anuncio);
                });

        // Cargar las CLASIFICACIONES
        List<HomeComponente> clasificaciones = homeComponenteRepository
                .findBySeccionAndEstaActivoTrueOrderByOrdenAsc(HomeComponente.Seccion.CLASIFICACION);
        model.addAttribute("seccionClasificaciones", clasificaciones);

        // --- 3. CARGAR EL CARRUSEL RANDOM ---
        List<Producto> carrusel = productoRepository.findRandomProductos(PageRequest.of(0, LIMITE_CARRUSEL_RANDOM));
        model.addAttribute("seccionCarrusel", carrusel);

        return "index"; // Tu vista 'index.html'
    }
}