package pe.com.isil.inversioneslazaro.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "producto_id")
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String codPro; // código único del producto

    @Column(nullable = false)
    private String nomPro;

    @Column(length = 1000, nullable = false)
    private String descripcionCorta;

    private String descripcionLarga;

    // precio con BigDecimal (mejor precisión para dinero)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false)
    private Integer stock;

    // Datos adicionales de la descripcion de productos
    private Double altoCm;
    private Double anchoCm;
    private Double fondoCm;

    @Column(length = 100)
    private String modelo;

    @Column(length = 100)
    private String material;

    private Integer potenciaBtu;

    private Integer garantiaMeses;

    @Column(length = 100)
    private String paisOrigen;

    @Column(length = 255)
    private String fichaTecnica;

    //Pequeña modificacion para las imagenes pero que sean varias o hasta 10
    //se esta mapeando con el campo producto en la clase imagenProducto
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImagenProducto> imagenes = new ArrayList<>();

    //claves foraneas
    @ManyToOne
    @JoinColumn(name = "idMarca", referencedColumnName = "idMarca", nullable = false)
    private Marca idMarca;

    @ManyToOne
    @JoinColumn(name = "idCate", referencedColumnName = "idCate", nullable = false)
    private Categoria IdCate;

    @ManyToOne
    @JoinColumn(name = "idTipo", referencedColumnName = "idTipo", nullable = false)
    private TipoProducto idTipo;

    //Fecha de creacion y actualizacion
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_act")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate(){
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate(){
        this.fechaActualizacion = LocalDateTime.now();
    }
}
