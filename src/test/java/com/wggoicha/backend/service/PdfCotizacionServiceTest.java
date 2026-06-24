package com.wggoicha.backend.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PdfCotizacionServiceTest {

    @Test
    void formateaCantidadesEnterasSinDecimales() {
        assertEquals("1", PdfCotizacionService.formatearCantidad(new BigDecimal("1.00")));
        assertEquals("10", PdfCotizacionService.formatearCantidad(new BigDecimal("10.00")));
        assertEquals("1000", PdfCotizacionService.formatearCantidad(new BigDecimal("1000.00")));
    }
}
