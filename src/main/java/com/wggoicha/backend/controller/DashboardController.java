package com.wggoicha.backend.controller;

import com.wggoicha.backend.entity.Pedido;
import com.wggoicha.backend.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class DashboardController {

    private final PedidoRepository pedidoRepository;

    @GetMapping("/ventas-mensuales")
    public List<Map<String, Object>> ventasMensuales() {
        List<Pedido> pedidos = pedidoRepository.findAll();

        Map<Integer, BigDecimal> ventasPorMes = new TreeMap<>();

        for (Pedido pedido : pedidos) {
            if (pedido.getFechaCreacion() == null || pedido.getTotal() == null) {
                continue;
            }

            int mes = pedido.getFechaCreacion().getMonthValue();

            ventasPorMes.put(
                    mes,
                    ventasPorMes.getOrDefault(mes, BigDecimal.ZERO).add(pedido.getTotal())
            );
        }

        return ventasPorMes.entrySet()
                .stream()
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();

                    String mesNombre = Month.of(entry.getKey())
                            .getDisplayName(TextStyle.SHORT, new Locale("es", "ES"))
                            .replace(".", "");

                    item.put("mes", mesNombre);
                    item.put("ventas", entry.getValue());

                    return item;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/estado-pedidos")
    public List<Map<String, Object>> estadoPedidos() {
        List<Pedido> pedidos = pedidoRepository.findAll();

        Map<String, Long> estados = pedidos.stream()
                .collect(Collectors.groupingBy(
                        pedido -> pedido.getEstado() != null ? pedido.getEstado() : "SIN_ESTADO",
                        Collectors.counting()
                ));

        return estados.entrySet()
                .stream()
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", entry.getKey());
                    item.put("value", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());
    }
}
