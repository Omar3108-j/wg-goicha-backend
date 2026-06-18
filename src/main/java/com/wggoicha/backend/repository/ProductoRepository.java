package com.wggoicha.backend.repository;

import com.wggoicha.backend.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByDestacadoTrue();

    List<Producto> findByActivoTrue();

    List<Producto> findByDestacadoTrueAndActivoTrue();
}