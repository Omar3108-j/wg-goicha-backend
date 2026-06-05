package com.wggoicha.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "productos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private String descripcion;

    private String imagen;

    private String marca;

    @Column(nullable = true)
    private String tipo;

    @Column(precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false)
    private Boolean destacado = false;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;
}
