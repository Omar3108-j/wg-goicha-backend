package com.wggoicha.backend.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.wggoicha.backend.entity.Pedido;
import com.wggoicha.backend.entity.PedidoDetalle;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Service
public class PdfPedidoService {

    public ByteArrayInputStream generarPdf(Pedido pedido) {
        Document document = new Document(PageSize.A4, 30, 30, 40, 30);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // HEADER
            PdfPTable header = new PdfPTable(2);
            header.setWidthPercentage(100);
            header.setWidths(new int[]{65, 35});

            PdfPCell left = new PdfPCell();
            left.setBorder(Rectangle.NO_BORDER);

            try {
                Image logo = Image.getInstance("src/main/resources/static/logo-wg.png");
                logo.scaleToFit(120, 70);
                left.addElement(logo);
            } catch (Exception e) {
                left.addElement(new Paragraph("W&G", new Font(Font.HELVETICA, 30, Font.BOLD, new Color(239, 68, 68))));
            }

            left.addElement(new Paragraph(
                    "W&G CORPORACIÓN GOICHA",
                    new Font(Font.HELVETICA, 14, Font.BOLD, new Color(15, 23, 42))
            ));

            left.addElement(new Paragraph(
                    "Tuberías · Conexiones · Accesorios",
                    new Font(Font.HELVETICA, 9, Font.NORMAL, new Color(100, 116, 139))
            ));

            PdfPCell right = new PdfPCell();
            right.setBorder(Rectangle.NO_BORDER);

            PdfPTable box = new PdfPTable(1);
            box.setWidthPercentage(100);

            box.addCell(headerBox(pedido.getCodigo() != null ? pedido.getCodigo() : "PED-" + pedido.getId(), new Color(15, 23, 42), Color.WHITE));
            box.addCell(headerBox("PEDIDO WEB", new Color(239, 68, 68), Color.WHITE));
            box.addCell(headerBox(
                    pedido.getFechaCreacion() != null ? pedido.getFechaCreacion().toLocalDate().toString() : "-",
                    new Color(248, 250, 252),
                    new Color(15, 23, 42)
            ));

            right.addElement(box);

            header.addCell(left);
            header.addCell(right);
            document.add(header);

            addLine(document);

            // CLIENTE
            PdfPTable cliente = new PdfPTable(3);
            cliente.setWidthPercentage(100);
            cliente.setSpacingBefore(16);
            cliente.setSpacingAfter(18);
            cliente.setWidths(new int[]{34, 33, 33});

            cliente.addCell(infoCard("CLIENTE", pedido.getCliente()));
            cliente.addCell(infoCard("TELÉFONO", pedido.getTelefono()));
            cliente.addCell(infoCard("ESTADO", pedido.getEstado()));

            document.add(cliente);

            // DIRECCIÓN
            if (pedido.getDireccion() != null && !pedido.getDireccion().trim().isEmpty()) {
                PdfPTable direccion = new PdfPTable(1);
                direccion.setWidthPercentage(100);
                direccion.setSpacingAfter(18);
                direccion.addCell(infoCard("DIRECCIÓN", pedido.getDireccion()));
                document.add(direccion);
            }

            // PRODUCTOS
            PdfPTable tabla = new PdfPTable(4);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new int[]{12, 52, 16, 20});

            tabla.addCell(tableHeader("Cant."));
            tabla.addCell(tableHeader("Producto"));
            tabla.addCell(tableHeader("P. Unit"));
            tabla.addCell(tableHeader("Total"));

            int fila = 0;
            for (PedidoDetalle item : pedido.getDetalles()) {
                Color bg = fila % 2 == 0 ? Color.WHITE : new Color(248, 250, 252);

                tabla.addCell(tableBody(String.valueOf(item.getCantidad()), bg, Element.ALIGN_CENTER));
                tabla.addCell(tableBody(item.getProductoNombre(), bg, Element.ALIGN_LEFT));
                tabla.addCell(tableBody("S/ " + item.getPrecio(), bg, Element.ALIGN_RIGHT));
                tabla.addCell(tableBody("S/ " + item.getSubtotal(), bg, Element.ALIGN_RIGHT));

                fila++;
            }

            document.add(tabla);

            document.add(new Paragraph("\n"));

            // TOTAL
            PdfPTable totalBox = new PdfPTable(1);
            totalBox.setWidthPercentage(38);
            totalBox.setHorizontalAlignment(Element.ALIGN_RIGHT);

            PdfPCell totalCell = new PdfPCell();
            totalCell.setPadding(14);
            totalCell.setBackgroundColor(new Color(15, 23, 42));
            totalCell.setBorder(Rectangle.NO_BORDER);

            Paragraph label = new Paragraph("TOTAL DEL PEDIDO", new Font(Font.HELVETICA, 10, Font.BOLD, new Color(203, 213, 225)));
            label.setAlignment(Element.ALIGN_CENTER);

            Paragraph value = new Paragraph("S/ " + pedido.getTotal(), new Font(Font.HELVETICA, 18, Font.BOLD, Color.WHITE));
            value.setAlignment(Element.ALIGN_CENTER);
            value.setSpacingBefore(6);

            totalCell.addElement(label);
            totalCell.addElement(value);
            totalBox.addCell(totalCell);

            document.add(totalBox);

            document.add(new Paragraph("\n\n"));

            Paragraph footer = new Paragraph(
                    "W&G CORPORACIÓN GOICHA E.I.R.L. · Pedido generado desde la plataforma web",
                    new Font(Font.HELVETICA, 8, Font.BOLD, new Color(100, 116, 139))
            );
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private PdfPCell headerBox(String text, Color bg, Color color) {
        PdfPCell cell = new PdfPCell(new Phrase(text, new Font(Font.HELVETICA, 12, Font.BOLD, color)));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(9);
        cell.setBackgroundColor(bg);
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    private PdfPCell infoCard(String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(10);
        cell.setBorderColor(new Color(226, 232, 240));
        cell.setBackgroundColor(new Color(248, 250, 252));

        cell.addElement(new Paragraph(label, new Font(Font.HELVETICA, 7, Font.BOLD, new Color(100, 116, 139))));
        cell.addElement(new Paragraph(
                "\n" + (value != null && !value.trim().isEmpty() ? value : "-"),
                new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(15, 23, 42))
        ));

        return cell;
    }

    private PdfPCell tableHeader(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE)));
        cell.setBackgroundColor(new Color(15, 23, 42));
        cell.setPadding(10);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }

    private PdfPCell tableBody(String text, Color bg, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", new Font(Font.HELVETICA, 9, Font.NORMAL, new Color(15, 23, 42))));
        cell.setPadding(9);
        cell.setBackgroundColor(bg);
        cell.setBorderColor(new Color(226, 232, 240));
        cell.setHorizontalAlignment(align);
        return cell;
    }

    private void addLine(Document document) throws DocumentException {
        PdfPTable line = new PdfPTable(1);
        line.setWidthPercentage(100);

        PdfPCell cell = new PdfPCell();
        cell.setFixedHeight(3);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setBackgroundColor(new Color(239, 68, 68));

        line.addCell(cell);
        document.add(line);
    }
}