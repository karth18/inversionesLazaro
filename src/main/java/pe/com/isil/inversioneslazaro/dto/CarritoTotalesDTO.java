package pe.com.isil.inversioneslazaro.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
public class CarritoTotalesDTO {

    private BigDecimal subtotal;
    private BigDecimal ahorroPorProductos;
    private BigDecimal descuentoPorCupon;
    private BigDecimal total;

    public CarritoTotalesDTO(Map<String, BigDecimal> totalesMap) {
        this.subtotal = totalesMap.getOrDefault("subtotal", BigDecimal.ZERO);
        this.ahorroPorProductos = totalesMap.getOrDefault("ahorroPorProductos", BigDecimal.ZERO);
        this.descuentoPorCupon = totalesMap.getOrDefault("descuentoPorCupon", BigDecimal.ZERO);
        this.total = totalesMap.getOrDefault("total", BigDecimal.ZERO);
    }
}