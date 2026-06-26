package com.wggoicha.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public record CatalogoProductoDto(
        Long id,
        String nombre,
        String descripcion,
        String imagen,
        String marca,
        String tipo,
        BigDecimal precio,
        Boolean destacado,
        Boolean activo,
        CatalogoCategoriaDto categoria,
        List<CatalogoVarianteDto> variantesActivas,
        BigDecimal precioMinimo,
        boolean tieneVariantesActivas
) {
}
