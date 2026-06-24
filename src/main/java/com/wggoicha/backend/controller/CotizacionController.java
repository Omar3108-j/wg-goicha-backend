package com.wggoicha.backend.controller;

import com.wggoicha.backend.entity.Cotizacion;
import com.wggoicha.backend.entity.CotizacionDetalle;
import com.wggoicha.backend.entity.ProductoInterno;
import com.wggoicha.backend.repository.CotizacionRepository;
import com.wggoicha.backend.repository.ProductoInternoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import com.wggoicha.backend.service.PdfCotizacionService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/cotizaciones")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class CotizacionController {

    private final CotizacionRepository cotizacionRepository;
    private final ProductoInternoRepository productoInternoRepository;
    private final PdfCotizacionService pdfCotizacionService;

    @GetMapping
    public List<Cotizacion> listar() {
        return cotizacionRepository.findAll()
                .stream()
                .sorted((a, b) -> b.getId().compareTo(a.getId()))
                .toList();
    }

    @PostMapping
    public Cotizacion crear(@RequestBody Cotizacion cotizacion) {
        if (cotizacion.getCliente() == null || cotizacion.getCliente().trim().isEmpty()) {
            cotizacion.setCliente("CLIENTE VARIOS");
        }
        BigDecimal subtotal = BigDecimal.ZERO;

        if (cotizacion.getDetalles() != null) {
            for (CotizacionDetalle detalle : cotizacion.getDetalles()) {
                detalle.setCotizacion(cotizacion);

                BigDecimal cantidad = detalle.getCantidad() != null ? detalle.getCantidad() : BigDecimal.ONE;
                BigDecimal precio = detalle.getPrecioUnitario() != null ? detalle.getPrecioUnitario() : BigDecimal.ZERO;

                BigDecimal totalLinea = cantidad.multiply(precio);
                detalle.setTotal(totalLinea);
                subtotal = subtotal.add(totalLinea);

                if (detalle.getDescripcion() != null && !detalle.getDescripcion().trim().isEmpty()) {
                    ProductoInterno productoInterno = productoInternoRepository
                            .findByNombreIgnoreCase(detalle.getDescripcion().trim())
                            .orElseGet(() -> productoInternoRepository.save(
                                    ProductoInterno.builder()
                                            .nombre(detalle.getDescripcion().trim())
                                            .precio(precio)
                                            .build()
                            ));

                    detalle.setProductoInterno(productoInterno);
                }
            }
        }

        BigDecimal total = subtotal;
        BigDecimal subtotalSinIgv = total.divide(new BigDecimal("1.18"), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal igv = total.subtract(subtotalSinIgv);

        cotizacion.setSubtotal(subtotalSinIgv);
        cotizacion.setIgv(igv);
        cotizacion.setTotal(total);
        cotizacion.setEstado("GENERADA");

        Cotizacion guardada = cotizacionRepository.save(cotizacion);
        guardada.setCodigo(String.format("COT-%05d", guardada.getId()));

        return cotizacionRepository.save(guardada);
    }
    @GetMapping("/{id}/pdf")
    public ResponseEntity<InputStreamResource> descargarPdf(@PathVariable Long id) {
        Cotizacion cotizacion = cotizacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cotización no encontrada"));

        InputStreamResource resource = new InputStreamResource(
                pdfCotizacionService.generarPdf(cotizacion)
        );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + cotizacion.getCodigo() + ".pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    @GetMapping("/{id}")
    public Cotizacion obtenerPorId(@PathVariable Long id) {
        return cotizacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cotización no encontrada"));
    }
    /* Persist quotation status V1 */
    @PatchMapping("/{id}/estado")
    public Cotizacion actualizarEstado(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String estado = body.get("estado");
        if (estado == null || estado.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El estado es obligatorio");
        }

        Cotizacion cotizacion = cotizacionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cotización no encontrada"
                ));

        cotizacion.setEstado(estado.trim());
        return cotizacionRepository.save(cotizacion);
    }

    @PutMapping("/{id}")
    public Cotizacion actualizar(@PathVariable Long id, @RequestBody Cotizacion cotizacionActualizada) {
        Cotizacion cotizacion = cotizacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cotización no encontrada"));

        cotizacion.setCliente(
                cotizacionActualizada.getCliente() == null || cotizacionActualizada.getCliente().trim().isEmpty()
                        ? "CLIENTE VARIOS"
                        : cotizacionActualizada.getCliente()
        );

        cotizacion.setRuc(cotizacionActualizada.getRuc());
        cotizacion.setTelefono(cotizacionActualizada.getTelefono());
        cotizacion.setCorreo(cotizacionActualizada.getCorreo());
        cotizacion.setDireccion(cotizacionActualizada.getDireccion());
        cotizacion.setObservaciones(cotizacionActualizada.getObservaciones());

        cotizacion.getDetalles().clear();

        BigDecimal total = BigDecimal.ZERO;

        if (cotizacionActualizada.getDetalles() != null) {
            for (CotizacionDetalle detalle : cotizacionActualizada.getDetalles()) {
                detalle.setCotizacion(cotizacion);

                BigDecimal cantidad = detalle.getCantidad() != null ? detalle.getCantidad() : BigDecimal.ONE;
                BigDecimal precio = detalle.getPrecioUnitario() != null ? detalle.getPrecioUnitario() : BigDecimal.ZERO;

                BigDecimal totalLinea = cantidad.multiply(precio);
                detalle.setTotal(totalLinea);

                total = total.add(totalLinea);

                if (detalle.getDescripcion() != null && !detalle.getDescripcion().trim().isEmpty()) {
                    ProductoInterno productoInterno = productoInternoRepository
                            .findByNombreIgnoreCase(detalle.getDescripcion().trim())
                            .orElseGet(() -> productoInternoRepository.save(
                                    ProductoInterno.builder()
                                            .nombre(detalle.getDescripcion().trim())
                                            .precio(precio)
                                            .build()
                            ));

                    detalle.setProductoInterno(productoInterno);
                }

                cotizacion.getDetalles().add(detalle);
            }
        }

        BigDecimal subtotalSinIgv = total.divide(new BigDecimal("1.18"), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal igv = total.subtract(subtotalSinIgv);

        cotizacion.setSubtotal(subtotalSinIgv);
        cotizacion.setIgv(igv);
        cotizacion.setTotal(total);

        return cotizacionRepository.save(cotizacion);
    }
}
