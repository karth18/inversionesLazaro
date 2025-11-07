package pe.com.isil.inversioneslazaro.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import pe.com.isil.inversioneslazaro.exception.StorageException;
import pe.com.isil.inversioneslazaro.exception.FileNotFoundException;


import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileSystemStorageService implements StorageService {
    @Value("${storage.location}")
    private String storageLocation;

    @PostConstruct
    @Override
    public void init() {
        try {
            Files.createDirectories(Paths.get(storageLocation));
        } catch (IOException e) {
            throw new StorageException("No se pudo inicializar el storage location", e);
        }
    }

    // Método auxiliar para sanitizar el nombre de archivo

    private String sanitizeFilename(String filename) {
        if (filename == null) return "";
        // Reemplaza caracteres no alfanuméricos, espacios y barras con guiones bajos
        // y normaliza la cadena (quita acentos, etc. si aplica)
        String cleanName = filename.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
        // Limita la longitud para seguridad si es necesario
        return cleanName.length() > 50 ? cleanName.substring(0, 50) + cleanName.substring(cleanName.lastIndexOf(".")) : cleanName;
    }

    @Override
    public String store(MultipartFile file) {
        // Generamos un nombre de archivo único
        String originalFilename = file.getOriginalFilename();

        String sanitizedFilename = sanitizeFilename(originalFilename);
        // Creamos un nombre único: UUID + guion bajo + nombre original
        String uniqueFilename = UUID.randomUUID().toString() + "_" + sanitizedFilename;

        // Llamamos a tu método de dos parámetros para hacer el guardado real
        return this.store(file, uniqueFilename);
    }

    public String store(MultipartFile file, String uniqueFilename) {
        String filename = uniqueFilename;

        if (file.isEmpty())
        {
            throw new StorageException("Error en guardar el archivo está vacío :" + filename);
        }

        try {
            InputStream inputStream = file.getInputStream();
            Files.copy(inputStream, Paths.get(storageLocation).resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        }catch (IOException e)
        {
            throw new StorageException("Error en guardar el archivo: " + filename, e);
        }

        return filename;
    }

    @Override
    public Path load(String filename) {
        return Paths.get(storageLocation).resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) {
        try
        {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable())
            {
                return resource;
            }else {
                throw new FileNotFoundException("No se puede cargar o leer el archivo: " + filename);
            }
        }catch (MalformedURLException e)
        {
            throw new FileNotFoundException("No se puede cargar o leer el archivo: " + filename, e);
        }
    }

    @Override
    public void delete(String filename) {
        try {
            // 1. Si el nombre es nulo o vacío, no hagas nada.
            if (filename == null || filename.isBlank()) {
                return;
            }

            Path file = load(filename);

            // 2. Asegúrate de que es un archivo (no una carpeta) antes de borrar
            if (Files.exists(file) && Files.isRegularFile(file)) {
                Files.delete(file);
            }
        } catch (IOException e) {
            // Lanza la excepción solo si realmente falló la eliminación
            throw new FileNotFoundException("No se pudo eliminar el archivo: " + filename, e);
        }
    }
}
