package pe.com.isil.inversioneslazaro.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "configuracion_empresa")
public class ConfiguracionEmpresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String urlLogo;

    // --- 1. SITUACIÓN: BIENVENIDA (PENDIENTE) ---
    private String asuntoBienvenida;
    @Column(length = 2000) // Texto largo
    private String mensajeBienvenida;

    // --- 2. SITUACIÓN: EN CAMINO ---
    private String asuntoEnCamino;
    @Column(length = 2000)
    private String mensajeEnCamino;

    // --- 3. SITUACIÓN: ENTREGADO ---
    private String asuntoEntregado;
    @Column(length = 2000)
    private String mensajeEntregado;

    // --- 4. SITUACIÓN: REAGENDADO ---
    private String asuntoReagendado;
    @Column(length = 2000) // Aquí el admin usará {motivo} y {fecha}
    private String mensajeReagendado;

    // --- 5. SITUACIÓN: CANCELADO ---
    private String asuntoCancelado;
    @Column(length = 2000)
    private String mensajeCancelado;

    // Textos generales
    private String textoBoton;
    private String footerAgradecimiento;
    @Column(length = 1000)
    private String recomendacionesEntrega;
}