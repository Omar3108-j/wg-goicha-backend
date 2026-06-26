package com.wggoicha.backend.controller;

import com.wggoicha.backend.dto.CatalogoProductoDto;
import com.wggoicha.backend.entity.Categoria;
import com.wggoicha.backend.entity.Producto;
import com.wggoicha.backend.entity.ProductoVariante;
import com.wggoicha.backend.repository.ProductoRepository;
import com.wggoicha.backend.repository.ProductoVarianteRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductoControllerTest {

    @Test
    void devuelveCatalogoActivoConVariantesYPrecioMinimoEnDosConsultas() {
        ProductoRepository productoRepository = mock(ProductoRepository.class);
        ProductoVarianteRepository varianteRepository =
                mock(ProductoVarianteRepository.class);

        Categoria categoria = Categoria.builder()
                .id(3L)
                .nombre("Tuberías")
                .build();

        Producto producto = Producto.builder()
                .id(10L)
                .nombre("Tubo PVC")
                .precio(new BigDecimal("20.00"))
                .activo(true)
                .categoria(categoria)
                .build();

        ProductoVariante varianteMayor = ProductoVariante.builder()
                .id(101L)
                .nombre("1 pulgada")
                .precio(new BigDecimal("18.00"))
                .activo(true)
                .producto(producto)
                .build();

        ProductoVariante varianteMenor = ProductoVariante.builder()
                .id(102L)
                .nombre("1/2 pulgada")
                .precio(new BigDecimal("12.50"))
                .activo(true)
                .producto(producto)
                .build();

        when(productoRepository.findCatalogoActivos())
                .thenReturn(List.of(producto));
        when(varianteRepository.findActivasByProductoIds(List.of(10L)))
                .thenReturn(List.of(varianteMayor, varianteMenor));

        ProductoController controller = new ProductoController(
                productoRepository,
                varianteRepository
        );

        List<CatalogoProductoDto> catalogo = controller.listarCatalogo();

        assertEquals(1, catalogo.size());
        assertTrue(catalogo.get(0).tieneVariantesActivas());
        assertEquals(new BigDecimal("12.50"), catalogo.get(0).precioMinimo());
        assertEquals(2, catalogo.get(0).variantesActivas().size());
        assertEquals("Tuberías", catalogo.get(0).categoria().nombre());

        verify(productoRepository).findCatalogoActivos();
        verify(varianteRepository).findActivasByProductoIds(List.of(10L));
    }
}
