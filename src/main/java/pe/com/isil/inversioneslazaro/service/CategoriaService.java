package pe.com.isil.inversioneslazaro.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.isil.inversioneslazaro.dto.CategoriaDTO;
import pe.com.isil.inversioneslazaro.model.CategoriaProducto;
import pe.com.isil.inversioneslazaro.repository.CategoriaProductoRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoriaService {


    private final CategoriaProductoRepository repo;

    public CategoriaService(CategoriaProductoRepository repo) {
        this.repo = repo;
    }

    public List<CategoriaProducto> listAll() {
        return repo.findAll().stream()
                .sorted((a,b) -> a.getNombre().compareToIgnoreCase(b.getNombre()))
                .collect(Collectors.toList());
    }

    public CategoriaProducto getById(Long id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
    }

    @Transactional
    public CategoriaProducto create(CategoriaDTO dto) {
        if (repo.existsByNombreIgnoreCase(dto.nombre)) {
            throw new RuntimeException("Ya existe una categoría con ese nombre");
        }
        if (repo.existsBySlug(dto.slug)) {
            throw new RuntimeException("El slug ya está en uso");
        }
        CategoriaProducto c = new CategoriaProducto();
        c.setNombre(dto.nombre);
        c.setSlug(dto.slug);
        c.setDescripcion(dto.descripcion);
        c.setActivo(dto.activo != null ? dto.activo : true);
        return repo.save(c);
    }

    @Transactional
    public CategoriaProducto update(Long id, CategoriaDTO dto) {
        CategoriaProducto c = getById(id);
        if (!c.getNombre().equalsIgnoreCase(dto.nombre) && repo.existsByNombreIgnoreCase(dto.nombre)) {
            throw new RuntimeException("Ya existe una categoría con ese nombre");
        }
        if (!c.getSlug().equals(dto.slug) && repo.existsBySlug(dto.slug)) {
            throw new RuntimeException("El slug ya está en uso");
        }
        c.setNombre(dto.nombre);
        c.setSlug(dto.slug);
        c.setDescripcion(dto.descripcion);
        c.setActivo(dto.activo != null ? dto.activo : c.getActivo());
        return repo.save(c);
    }

    @Transactional
    public void toggleActivo(Long id) {
        CategoriaProducto c = getById(id);
        c.setActivo(!c.getActivo());
        repo.save(c);
    }

    @Transactional
    public void delete(Long id) {
        // En lugar de eliminar físicamente, preferimos soft-delete; pero si quieres eliminar físicamente:
        // repo.deleteById(id);
        // Por ahora lanzamos excepción para indicarlo manualmente si hay productos asociados (control manual)
        repo.deleteById(id);
    }

}
