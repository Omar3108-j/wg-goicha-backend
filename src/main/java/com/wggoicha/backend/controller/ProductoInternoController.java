package com.wggoicha.backend.controller;

import com.wggoicha.backend.entity.ProductoInterno;
import com.wggoicha.backend.repository.ProductoInternoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos-internos")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ProductoInternoController {

    private final ProductoInternoRepository productoInternoRepository;

    @GetMapping
    public List<ProductoInterno> listar(@RequestParam(required = false) String buscar) {
        if (buscar != null && !buscar.trim().isEmpty()) {
            return productoInternoRepository.findByNombreContainingIgnoreCase(buscar);
        }

        return productoInternoRepository.findAll();
    }
}
