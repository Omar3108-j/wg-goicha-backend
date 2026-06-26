package com.wggoicha.backend.repository;

import com.wggoicha.backend.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByDestacadoTrue();

    List<Producto> findByActivoTrue();

    List<Producto> findByDestacadoTrueAndActivoTrue();

    @Query("""
            select p
            from Producto p
            left join fetch p.categoria
            where p.activo = true
            order by p.id asc
            """)
    List<Producto> findCatalogoActivos();
}
