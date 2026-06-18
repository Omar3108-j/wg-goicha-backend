package com.wggoicha.backend.controller;

import com.wggoicha.backend.entity.Producto;
import com.wggoicha.backend.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoRepository productoRepository;

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
}