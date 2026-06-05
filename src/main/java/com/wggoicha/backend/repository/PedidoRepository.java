package com.wggoicha.backend.repository;

import com.wggoicha.backend.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // Obtiene pedidos ordenados del más reciente al más antiguo
    List<Pedido> findAllByOrderByIdDesc();
}
