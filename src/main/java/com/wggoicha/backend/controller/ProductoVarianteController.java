package com.wggoicha.backend.controller;

import com.wggoicha.backend.entity.Producto;
import com.wggoicha.backend.entity.ProductoVariante;
import com.wggoicha.backend.repository.ProductoRepository;
import com.wggoicha.backend.repository.ProductoVarianteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/variantes")
@RequiredArgsConstructor
public class ProductoVarianteController {

    private final ProductoVarianteRepository varianteRepository;
    private final ProductoRepository productoRepository;

    @GetMapping("/producto/{productoId}")
    public List<ProductoVariante> listarPorProducto(@PathVariable Long productoId) {
        return varianteRepository.findByProductoIdOrderByIdAsc(productoId);
    }

    @GetMapping("/producto/{productoId}/activas")
    public List<ProductoVariante> listarActivasPorProducto(@PathVariable Long productoId) {
        return varianteRepository.findByProductoIdAndActivoTrueOrderByIdAsc(productoId);
    }

    @PostMapping
    public ProductoVariante crear(@RequestBody ProductoVariante variante) {
        if (variante.getProducto() == null || variante.getProducto().getId() == null) {
            throw new RuntimeException("Debe enviar el producto asociado");
        }

        Producto producto = productoRepository.findById(variante.getProducto().getId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        variante.setProducto(producto);

        if (variante.getActivo() == null) {
            variante.setActivo(true);
        }

        return varianteRepository.save(variante);
    }

    @PutMapping("/{id}")
    public ProductoVariante actualizar(
            @PathVariable Long id,
            @RequestBody ProductoVariante varianteActualizada
    ) {
        ProductoVariante variante = varianteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Variante no encontrada"));

        variante.setNombre(varianteActualizada.getNombre());
        variante.setPrecio(varianteActualizada.getPrecio());

        if (varianteActualizada.getActivo() != null) {
            variante.setActivo(varianteActualizada.getActivo());
        }

        return varianteRepository.save(variante);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        varianteRepository.deleteById(id);
    }
}
