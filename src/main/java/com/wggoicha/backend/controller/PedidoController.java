package com.wggoicha.backend.controller;

import com.wggoicha.backend.entity.Pedido;
import com.wggoicha.backend.entity.PedidoDetalle;
import com.wggoicha.backend.repository.PedidoRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.wggoicha.backend.service.PdfPedidoService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "http://localhost:5173")
public class PedidoController {

    private final PedidoRepository pedidoRepository;
    // Servicio encargado de generar el PDF del pedido
    private final PdfPedidoService pdfPedidoService;

    public PedidoController(
            PedidoRepository pedidoRepository,
            PdfPedidoService pdfPedidoService
    ) {
        this.pedidoRepository = pedidoRepository;
        this.pdfPedidoService = pdfPedidoService;
    }

    @GetMapping
    public List<Pedido> listar() {
        return pedidoRepository.findAllByOrderByIdDesc();
    }

    @PostMapping
    public Pedido crear(@RequestBody Pedido pedido) {
        BigDecimal total = BigDecimal.ZERO;

        if (pedido.getDetalles() != null) {
            for (PedidoDetalle detalle : pedido.getDetalles()) {
                detalle.setPedido(pedido);

                BigDecimal precio = detalle.getPrecio() != null ? detalle.getPrecio() : BigDecimal.ZERO;
                int cantidad = detalle.getCantidad() != null ? detalle.getCantidad() : 1;

                BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(cantidad));
                detalle.setSubtotal(subtotal);

                total = total.add(subtotal);
            }
        }

        pedido.setTotal(total);
        pedido.setEstado("PENDIENTE");

        Pedido pedidoGuardado = pedidoRepository.save(pedido);
        pedidoGuardado.setCodigo(String.format("PED-%05d", pedidoGuardado.getId()));

        return pedidoRepository.save(pedidoGuardado);
    }
    // ========================================
// DESCARGAR PDF DEL PEDIDO
// ========================================
    @GetMapping("/{id}/pdf")
    public ResponseEntity<InputStreamResource> descargarPdf(
            @PathVariable Long id
    ) {

        // Busca el pedido en base de datos por ID
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Pedido no encontrado")
                );

        // Genera el PDF usando el servicio
        InputStreamResource resource = new InputStreamResource(
                pdfPedidoService.generarPdf(pedido)
        );

        // Retorna el archivo PDF para descarga
        return ResponseEntity.ok()

                // Fuerza descarga del archivo
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=pedido-" + pedido.getId() + ".pdf"
                )

                // Tipo de contenido PDF
                .contentType(MediaType.APPLICATION_PDF)

                // Contenido del archivo
                .body(resource);
    }

    @PutMapping("/{id}/estado")
    public Pedido actualizarEstado(@PathVariable Long id, @RequestParam String estado) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        pedido.setEstado(estado);
        return pedidoRepository.save(pedido);
    }
}
