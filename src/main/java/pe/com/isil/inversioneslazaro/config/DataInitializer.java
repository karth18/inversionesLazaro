package pe.com.isil.inversioneslazaro.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pe.com.isil.inversioneslazaro.model.HomeComponente;
import pe.com.isil.inversioneslazaro.repository.HomeComponenteRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private HomeComponenteRepository homeRepo;

    @Override
    public void run(String... args) throws Exception {
        // Asegura que los 4 slots de Clasificación SIEMPRE existan
        crearSlotSiNoExiste("Industrial", HomeComponente.Seccion.CLASIFICACION, 1);
        crearSlotSiNoExiste("Negocio", HomeComponente.Seccion.CLASIFICACION, 2);
        crearSlotSiNoExiste("Hogar", HomeComponente.Seccion.CLASIFICACION, 3);
        crearSlotSiNoExiste("Mobiliario", HomeComponente.Seccion.CLASIFICACION, 4);
    }

    private void crearSlotSiNoExiste(String titulo, HomeComponente.Seccion seccion, int orden) {
        // Busca si ya existe un slot con este título y sección
        if (homeRepo.findBySeccionOrderByOrdenAsc(seccion).stream().noneMatch(c -> c.getTitulo().equals(titulo))) {
            HomeComponente slot = new HomeComponente();
            slot.setTitulo(titulo);
            slot.setSeccion(seccion);
            slot.setOrden(orden);
            slot.setEstaActivo(true); // Activo por defecto
            slot.setEnlaceUrl("/catalogo"); // Enlace genérico
            homeRepo.save(slot);
            System.out.println("Creado slot de inicio: " + titulo);
        }
    }
}