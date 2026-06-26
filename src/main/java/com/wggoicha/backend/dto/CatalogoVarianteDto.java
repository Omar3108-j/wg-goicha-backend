package com.wggoicha.backend.dto;

import java.math.BigDecimal;

public record CatalogoVarianteDto(
        Long id,
        String nombre,
        BigDecimal precio,
        Boolean activo
) {
}
