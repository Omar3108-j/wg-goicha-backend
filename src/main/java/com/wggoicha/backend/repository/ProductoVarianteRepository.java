
package com.wggoicha.backend.repository;

import com.wggoicha.backend.entity.ProductoVariante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoVarianteRepository extends JpaRepository<ProductoVariante, Long> {

    List<ProductoVariante> findByProductoIdOrderByIdAsc(Long productoId);

    List<ProductoVariante> findByProductoIdAndActivoTrueOrderByIdAsc(Long productoId);
}