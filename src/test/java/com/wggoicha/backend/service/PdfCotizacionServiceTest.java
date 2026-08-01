package com.wggoicha.backend.service;

import com.lowagie.text.pdf.PdfArray;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import com.wggoicha.backend.entity.Cotizacion;
import com.wggoicha.backend.entity.CotizacionDetalle;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfCotizacionServiceTest {

    @Test
    void formateaCantidadesEnterasSinDecimales() {
        assertEquals("1", PdfCotizacionService.formatearCantidad(new BigDecimal("1.00")));
        assertEquals("10", PdfCotizacionService.formatearCantidad(new BigDecimal("10.00")));
        assertEquals("1000", PdfCotizacionService.formatearCantidad(new BigDecimal("1000.00")));
    }

    @Test
    void agregaEnlaceClicableAlSitioWeb() throws IOException {
        Cotizacion cotizacion = crearCotizacionPrueba();
        byte[] pdf = new PdfCotizacionService().generarPdf(cotizacion).readAllBytes();

        PdfReader reader = new PdfReader(pdf);
        PdfArray annotations = reader.getPageN(1).getAsArray(PdfName.ANNOTS);
        boolean enlaceEncontrado = false;

        if (annotations != null) {
            for (int i = 0; i < annotations.size(); i++) {
                PdfObject object = PdfReader.getPdfObject(annotations.getPdfObject(i));
                if (!(object instanceof PdfDictionary annotation)) continue;

                PdfDictionary action = annotation.getAsDict(PdfName.A);
                if (action != null
                        && action.getAsString(PdfName.URI) != null
                        && "https://www.wgcorporaciongoicha.com".equals(
                                action.getAsString(PdfName.URI).toUnicodeString()
                        )) {
                    enlaceEncontrado = true;
                    break;
                }
            }
        }

        reader.close();
        assertTrue(enlaceEncontrado);
    }

    @Test
    void permiteOcultarDetalleIgvEnPdf() throws IOException {
        Cotizacion cotizacion = crearCotizacionPrueba();
        cotizacion.setMostrarDetalleIgvPdf(false);

        PdfReader reader = new PdfReader(new PdfCotizacionService().generarPdf(cotizacion));
        String texto = new PdfTextExtractor(reader).getTextFromPage(1);
        reader.close();

        assertTrue(texto.contains("TOTAL FINAL"));
        assertTrue(texto.contains("S/ 120.00"));
        assertFalse(texto.contains("SUBTOTAL"));
        assertFalse(texto.contains("IGV 18%"));
    }

    @Test
    void usaSimboloDolaresCuandoLaCotizacionEsUsd() throws IOException {
        Cotizacion cotizacion = crearCotizacionPrueba();
        cotizacion.setMoneda("USD");

        PdfReader reader = new PdfReader(new PdfCotizacionService().generarPdf(cotizacion));
        String texto = new PdfTextExtractor(reader).getTextFromPage(1);
        reader.close();

        assertTrue(texto.contains("US$ 12.00"));
        assertTrue(texto.contains("US$ 120.00"));
    }

    private Cotizacion crearCotizacionPrueba() {
        Cotizacion cotizacion = new Cotizacion();
        cotizacion.setCodigo("COT-00001");
        cotizacion.setCliente("CLIENTE DE PRUEBA");
        cotizacion.setRuc("20123456789");
        cotizacion.setTelefono("999 999 999");
        cotizacion.setFechaCreacion(LocalDateTime.of(2026, 6, 25, 10, 0));
        cotizacion.setSubtotal(new BigDecimal("101.69"));
        cotizacion.setIgv(new BigDecimal("18.31"));
        cotizacion.setTotal(new BigDecimal("120.00"));

        CotizacionDetalle detalle = new CotizacionDetalle();
        detalle.setCantidad(new BigDecimal("10.00"));
        detalle.setDescripcion("PRODUCTO DE PRUEBA");
        detalle.setPrecioUnitario(new BigDecimal("12.00"));
        detalle.setTotal(new BigDecimal("120.00"));
        cotizacion.setDetalles(List.of(detalle));

        return cotizacion;
    }
}
