package com.wggoicha.backend.controller;

import com.wggoicha.backend.entity.Cotizacion;
import com.wggoicha.backend.repository.CotizacionRepository;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CotizacionControllerTest {

    @Test
    void actualizaYPersisteSoloElEstado() {
        CotizacionRepository repository = mock(CotizacionRepository.class);
        Cotizacion cotizacion = new Cotizacion();
        cotizacion.setId(15L);
        cotizacion.setEstado("GENERADA");

        when(repository.findById(15L)).thenReturn(Optional.of(cotizacion));
        when(repository.save(cotizacion)).thenReturn(cotizacion);

        CotizacionController controller = new CotizacionController(repository, null);

        Cotizacion actualizada = controller.actualizarEstado(
                15L,
                Map.of("estado", "APROBADA")
        );

        assertEquals("APROBADA", actualizada.getEstado());
        verify(repository).save(cotizacion);
    }
}
