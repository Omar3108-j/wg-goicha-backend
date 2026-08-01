package com.wggoicha.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cotizaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cotizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codigo;

    private String cliente;

    private String ruc;

    private String telefono;

    private String correo;

    @Column(columnDefinition = "TEXT")
    private String direccion;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    private BigDecimal subtotal;

    private BigDecimal igv;

    private BigDecimal total;

    @Builder.Default
    private String estado = "GENERADA";

    /* Quotation PDF display options V1 */
    @Builder.Default
    @Column(length = 3)
    private String moneda = "PEN";

    @Builder.Default
    @Column(name = "mostrar_detalle_igv_pdf")
    private Boolean mostrarDetalleIgvPdf = true;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @OneToMany(mappedBy = "cotizacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CotizacionDetalle> detalles = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) fechaCreacion = LocalDateTime.now();
        aplicarDefaults();
    }

    @PreUpdate
    public void preUpdate() {
        aplicarDefaults();
    }

    private void aplicarDefaults() {
        if (estado == null) estado = "GENERADA";
        if (subtotal == null) subtotal = BigDecimal.ZERO;
        if (igv == null) igv = BigDecimal.ZERO;
        if (total == null) total = BigDecimal.ZERO;
        if (moneda == null || moneda.trim().isEmpty()) {
            moneda = "PEN";
        } else {
            moneda = moneda.trim().toUpperCase();
        }
        if (!"USD".equals(moneda)) moneda = "PEN";
        if (mostrarDetalleIgvPdf == null) mostrarDetalleIgvPdf = true;
    }
}
