package com.wggoicha.backend.controller;

import com.wggoicha.backend.dto.CatalogoCategoriaDto;
import com.wggoicha.backend.dto.CatalogoProductoDto;
import com.wggoicha.backend.dto.CatalogoVarianteDto;
import com.wggoicha.backend.entity.Producto;
import com.wggoicha.backend.entity.ProductoVariante;
import com.wggoicha.backend.repository.ProductoRepository;
import com.wggoicha.backend.repository.ProductoVarianteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoRepository productoRepository;
    private final ProductoVarianteRepository productoVarianteRepository;

    @GetMapping("/catalogo")
    public List<CatalogoProductoDto> listarCatalogo() {
        List<Producto> productos = productoRepository.findCatalogoActivos();
        if (productos.isEmpty()) {
            return List.of();
        }

        List<Long> productoIds = productos.stream()
                .map(Producto::getId)
                .toList();

        Map<Long, List<ProductoVariante>> variantesPorProducto =
                productoVarianteRepository.findActivasByProductoIds(productoIds)
                        .stream()
                        .collect(Collectors.groupingBy(
                                variante -> variante.getProducto().getId()
                        ));

        return productos.stream()
                .map(producto -> crearCatalogoProducto(
                        producto,
                        variantesPorProducto.getOrDefault(
                                producto.getId(),
                                Collections.emptyList()
                        )
                ))
                .toList();
    }

    @GetMapping("/destacados")
    public List<Producto> listarDestacados() {
        return productoRepository.findByDestacadoTrueAndActivoTrue();
    }

    @GetMapping("/activos")
    public List<Producto> listarActivos() {
        return productoRepository.findByActivoTrue();
    }

    @GetMapping
    public List<Producto> listar() {
        return productoRepository.findAll();
    }

    @GetMapping("/{id}")
    public Producto obtenerPorId(@PathVariable Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
    }

    @PostMapping
    public Producto crear(@RequestBody Producto producto) {

        if (producto.getDestacado() == null) {
            producto.setDestacado(false);
        }

        if (producto.getActivo() == null) {
            producto.setActivo(true);
        }

        return productoRepository.save(producto);
    }

    @PutMapping("/{id}")
    public Producto actualizar(@PathVariable Long id, @RequestBody Producto productoActualizado) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        producto.setNombre(productoActualizado.getNombre());
        producto.setDescripcion(productoActualizado.getDescripcion());
        producto.setImagen(productoActualizado.getImagen());
        producto.setTipo(productoActualizado.getTipo());
        producto.setCategoria(productoActualizado.getCategoria());
        producto.setMarca(productoActualizado.getMarca());
        producto.setPrecio(productoActualizado.getPrecio());

        if (productoActualizado.getDestacado() != null) {
            producto.setDestacado(productoActualizado.getDestacado());
        }

        if (productoActualizado.getActivo() != null) {
            producto.setActivo(productoActualizado.getActivo());
        }

        return productoRepository.save(producto);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        productoRepository.deleteById(id);
    }

    private CatalogoProductoDto crearCatalogoProducto(
            Producto producto,
            List<ProductoVariante> variantes
    ) {
        List<CatalogoVarianteDto> variantesDto = variantes.stream()
                .map(variante -> new CatalogoVarianteDto(
                        variante.getId(),
                        variante.getNombre(),
                        variante.getPrecio(),
                        variante.getActivo()
                ))
                .toList();

        BigDecimal precioMinimo = variantes.stream()
                .map(ProductoVariante::getPrecio)
                .min(BigDecimal::compareTo)
                .orElse(producto.getPrecio());

        CatalogoCategoriaDto categoria = producto.getCategoria() == null
                ? null
                : new CatalogoCategoriaDto(
                        producto.getCategoria().getId(),
                        producto.getCategoria().getNombre()
                );

        return new CatalogoProductoDto(
                producto.getId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getImagen(),
                producto.getMarca(),
                producto.getTipo(),
                producto.getPrecio(),
                producto.getDestacado(),
                producto.getActivo(),
                categoria,
                variantesDto,
                precioMinimo,
                !variantesDto.isEmpty()
        );
    }
}
