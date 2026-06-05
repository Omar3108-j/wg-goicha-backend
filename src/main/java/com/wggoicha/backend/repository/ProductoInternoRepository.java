package com.wggoicha.backend.repository;

import com.wggoicha.backend.entity.ProductoInterno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoInternoRepository extends JpaRepository<ProductoInterno, Long> {

    List<ProductoInterno> findByNombreContainingIgnoreCase(String nombre);

    Optional<ProductoInterno> findByNombreIgnoreCase(String nombre);
}
