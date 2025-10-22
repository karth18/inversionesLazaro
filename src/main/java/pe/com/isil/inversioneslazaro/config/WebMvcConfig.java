package pe.com.isil.inversioneslazaro.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("login");

        //error 403
        registry.addViewController("/403").setViewName("403");
    }

    // Inyecta la ubicación del almacenamiento desde application.properties (ej: mediafiles/)
    @Value("${storage.location}")
    private String storageLocation;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // 1. Asegura que la ubicación termine con un separador de ruta
        // y le añade el prefijo "file:" para indicar que es una ruta de sistema de archivos.
        // Esto es necesario para que Spring interprete la ruta correctamente.
        String fullPath = storageLocation.endsWith("/") ? storageLocation : storageLocation + "/";

        // 2. Mapea la URL /uploads/** a la ubicación física del directorio.
        // Cuando el HTML pida th:src="@{'/uploads/imagen.jpg'}", Spring buscará en file:/path/to/mediafiles/imagen.jpg
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + fullPath);
    }
}
