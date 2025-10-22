package pe.com.isil.inversioneslazaro.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotBlank(message = "El codigo es obligatorio")
    @Column(nullable = false, length = 50, unique = true)
    private String codPro;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(nullable = false)
    private String nomPro;

    @NotBlank(message = "La descripcion corta es obligatoria")
    @Column(length = 1000, nullable = false)
    private String descripcionCorta;

    private String descripcionLarga;

    // precio con BigDecimal (mejor precisión para dinero)
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "el precio debe ser mayor a cero")
    private BigDecimal precio;

    @NotNull(message = "El estock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
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

    private boolean estado;


    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImagenProducto> imagenes = new ArrayList<>();


    @ManyToOne
    @JoinColumn(name = "idMarca", referencedColumnName = "idMarca", nullable = false)
    @NotNull
    private Marca idMarca;

    @ManyToOne
    @JoinColumn(name = "idCate", referencedColumnName = "idCate", nullable = false)
    @NotNull
    private Categoria IdCate;

    @ManyToOne
    @JoinColumn(name = "idTipo", referencedColumnName = "idTipo", nullable = false)
    @NotNull
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
