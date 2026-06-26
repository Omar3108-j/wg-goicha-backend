
package com.wggoicha.backend.repository;

import com.wggoicha.backend.entity.ProductoVariante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductoVarianteRepository extends JpaRepository<ProductoVariante, Long> {

    List<ProductoVariante> findByProductoIdOrderByIdAsc(Long productoId);

    List<ProductoVariante> findByProductoIdAndActivoTrueOrderByIdAsc(Long productoId);

    @Query("""
            select v
            from ProductoVariante v
            where v.producto.id in :productoIds
              and v.activo = true
            order by v.producto.id asc, v.id asc
            """)
    List<ProductoVariante> findActivasByProductoIds(
            @Param("productoIds") List<Long> productoIds
    );
}
