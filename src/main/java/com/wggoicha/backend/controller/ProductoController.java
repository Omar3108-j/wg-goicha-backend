package com.wggoicha.backend.controller;

import com.wggoicha.backend.entity.Producto;
import com.wggoicha.backend.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ProductoController {

    private final ProductoRepository productoRepository;

    @GetMapping("/destacados")
    public List<Producto> listarDestacados() {
        return productoRepository.findByDestacadoTrue();
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
        producto.setDestacado(productoActualizado.getDestacado());

        return productoRepository.save(producto);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        productoRepository.deleteById(id);
    }
}
