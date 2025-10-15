package pe.com.isil.inversioneslazaro.controller.admin;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.com.isil.inversioneslazaro.model.DetalleVenta;
import pe.com.isil.inversioneslazaro.model.Producto;
import pe.com.isil.inversioneslazaro.model.Venta;
import pe.com.isil.inversioneslazaro.repository.ClienteRepository;
import pe.com.isil.inversioneslazaro.repository.ProductoRepository;
import pe.com.isil.inversioneslazaro.repository.VentaRepository;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/ventas")
public class VentaAdminController {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;

    public VentaAdminController(VentaRepository ventaRepository,
                                ProductoRepository productoRepository,
                                ClienteRepository clienteRepository) {
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
        this.clienteRepository = clienteRepository;
    }

    /**
     * Listado con filtros: página, tamaño, clienteId (optional), desde/hasta (optional, ISO date yyyy-MM-dd)
     */
    @GetMapping
    public String listarVentas(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "20") int size,
                               @RequestParam(required = false) Long clienteId,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                               Model model) {

        Page<Venta> ventas;

        // normalizamos fecha a LocalDateTime con inicio/fin de día
        LocalDateTime desdeDt = null, hastaDt = null;
        if (desde != null) desdeDt = desde.atStartOfDay();
        if (hasta != null) hastaDt = hasta.atTime(LocalTime.MAX);

        if (clienteId != null && desdeDt != null && hastaDt != null) {
            ventas = ventaRepository.findByClienteIdAndFechaBetween(clienteId, desdeDt, hastaDt, PageRequest.of(page, size));
        } else if (clienteId != null) {
            ventas = ventaRepository.findByClienteId(clienteId, PageRequest.of(page, size));
        } else if (desdeDt != null && hastaDt != null) {
            ventas = ventaRepository.findByFechaBetween(desdeDt, hastaDt, PageRequest.of(page, size));
        } else {
            ventas = ventaRepository.findAll(PageRequest.of(page, size));
        }

        model.addAttribute("ventas", ventas);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("clienteId", clienteId);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        model.addAttribute("clientes", clienteRepository.findAll()); // para el select cliente en la vista
        return "venta/list";
    }

    @GetMapping("/{id}")
    public String detalleVenta(@PathVariable Long id, Model model) {
        Optional<Venta> vOpt = ventaRepository.findById(id);
        if (vOpt.isEmpty()) {
            model.addAttribute("error", "Venta no encontrada");
            return "venta/detail";
        }
        Venta v = vOpt.get();

        // si existe clienteId, obtener nombre actualizado
        if (v.getClienteId() != null) {
            clienteRepository.findById(v.getClienteId()).ifPresent(cli -> v.setClienteNombre(cli.getNombreCompleto()));
        }

        model.addAttribute("venta", v);
        return "venta/detail";
    }

    /**
     * Dev-only: crea una venta sencilla con el primer producto disponible.
     * Mantén este endpoint para pruebas y luego elimínalo o protéjalo.
     */
    @PostMapping("/seed")
    public String seedVenta(Authentication authentication) {
        // obtener primer producto (si no hay, no hacer nada)
        Optional<Producto> pOpt = Optional.empty();
        try {
            pOpt = productoRepository.findAll().stream().findFirst();
        } catch (Exception ex) {
            // Si productoRepository no soporta findAll() streaming por alguna razón, intentamos findAll directo
            try {
                pOpt = Optional.ofNullable(productoRepository.findAll().isEmpty() ? null : productoRepository.findAll().get(0));
            } catch (Exception ignore) {
                pOpt = Optional.empty();
            }
        }

        if (pOpt.isEmpty()) {
            // no hay producto para seed; simplemente redirigimos al listado
            return "redirect:/admin/ventas";
        }

        Producto p = pOpt.get();

        Venta venta = new Venta();
        venta.setMetodoPago("Efectivo");

        // convierte BigDecimal a Double si es necesario, con chequeo null
        Double precio = null;
        try {
            BigDecimal bd = p.getPrecio();
            if (bd != null) precio = bd.doubleValue();
        } catch (Exception ex) {
            // si precio no es BigDecimal (si cambiaste tipo), intentamos obtenerlo por reflexion / toString
            try {
                Object possible = p.getPrecio();
                if (possible instanceof Number) {
                    precio = ((Number) possible).doubleValue();
                }
            } catch (Exception ignored) {}
        }
        if (precio == null) precio = 0.0;

        venta.setTotal(precio);
        venta.setVendedor(null);
        venta.setClienteId(null);
        venta.setClienteNombre("Cliente anónimo");

        DetalleVenta d = new DetalleVenta();
        d.setProducto(p);
        d.setCantidad(1);
        d.setPrecioUnitario(precio);
        d.setSubtotal(precio);

        venta.addDetalle(d);

        String actor = authentication != null ? authentication.getName() : "system";
        venta.setCreatedBy(actor);
        venta.setCreatedAt(LocalDateTime.now());

        ventaRepository.save(venta);
        return "redirect:/admin/ventas";
    }

    /* ---------------- EXPORT: Excel -------------------- */
    @GetMapping("/export/excel")
    public void exportExcel(
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            HttpServletResponse response) throws IOException {

        // preparar lista según filtros (sin paginar para export completo)
        List<Venta> ventas = fetchVentasForExport(clienteId, desde, hasta);

        // crear workbook
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Ventas");
            int rownum = 0;

            // header
            Row header = sheet.createRow(rownum++);
            String[] cols = new String[]{"ID", "Fecha", "Cliente", "Total", "MetodoPago", "CreadoPor"};
            for (int i = 0; i < cols.length; i++) {
                Cell c = header.createCell(i);
                c.setCellValue(cols[i]);
            }

            // rows
            for (Venta v : ventas) {
                Row r = sheet.createRow(rownum++);
                r.createCell(0).setCellValue(v.getId());
                r.createCell(1).setCellValue(v.getFecha() != null ? v.getFecha().toString() : "");
                r.createCell(2).setCellValue((v.getClienteNombre() != null ? v.getClienteNombre() : "Cliente anónimo"));
                r.createCell(3).setCellValue(v.getTotal() != null ? v.getTotal() : 0.0);
                r.createCell(4).setCellValue(v.getMetodoPago() != null ? v.getMetodoPago() : "");
                r.createCell(5).setCellValue(v.getCreatedBy() != null ? v.getCreatedBy() : "");
            }

            // auto-size
            for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);

            // write response
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String filename = "ventas.xlsx";
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            try (OutputStream os = response.getOutputStream()) {
                wb.write(os);
                os.flush();
            }
        }
    }

    /* ---------------- EXPORT: PDF -------------------- */
    @GetMapping("/export/pdf")
    public void exportPdf(
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            HttpServletResponse response) throws IOException {

        List<Venta> ventas = fetchVentasForExport(clienteId, desde, hasta);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"ventas.pdf\"");

        try {
            Document document = new Document(PageSize.A4.rotate(), 36, 36, 54, 36);
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Paragraph title = new Paragraph("Historial de Ventas", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(12f);
            document.add(title);

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.2f, 2.5f, 3f, 1.5f, 2.5f, 2f});
            // header
            table.addCell("ID");
            table.addCell("Fecha");
            table.addCell("Cliente");
            table.addCell("Total");
            table.addCell("Método pago");
            table.addCell("Creado por");

            for (Venta v : ventas) {
                table.addCell(String.valueOf(v.getId()));
                table.addCell(v.getFecha() != null ? v.getFecha().toString() : "");
                table.addCell(v.getClienteNombre() != null ? v.getClienteNombre() : "Cliente anónimo");
                table.addCell(String.format("%.2f", v.getTotal() != null ? v.getTotal() : 0.0));
                table.addCell(v.getMetodoPago() != null ? v.getMetodoPago() : "");
                table.addCell(v.getCreatedBy() != null ? v.getCreatedBy() : "");
            }

            document.add(table);
            document.close();
        } catch (DocumentException de) {
            throw new IOException(de);
        }
    }

    // helper para obtener lista según filtros (no paginada)
    private List<Venta> fetchVentasForExport(Long clienteId, LocalDate desde, LocalDate hasta) {
        LocalDateTime desdeDt = null, hastaDt = null;
        if (desde != null) desdeDt = desde.atStartOfDay();
        if (hasta != null) hastaDt = hasta.atTime(LocalTime.MAX);

        if (clienteId != null && desdeDt != null && hastaDt != null) {
            return ventaRepository.findByClienteIdAndFechaBetween(clienteId, desdeDt, hastaDt, PageRequest.of(0, Integer.MAX_VALUE)).getContent();
        } else if (clienteId != null) {
            return ventaRepository.findByClienteId(clienteId, PageRequest.of(0, Integer.MAX_VALUE)).getContent();
        } else if (desdeDt != null && hastaDt != null) {
            return ventaRepository.findByFechaBetween(desdeDt, hastaDt, PageRequest.of(0, Integer.MAX_VALUE)).getContent();
        } else {
            return ventaRepository.findAll();
        }
    }
}
