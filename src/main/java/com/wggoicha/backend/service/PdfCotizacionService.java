package com.wggoicha.backend.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Chunk;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.wggoicha.backend.entity.Cotizacion;
import com.wggoicha.backend.entity.CotizacionDetalle;
import org.springframework.stereotype.Service;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfGState;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.ColumnText;

import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import javax.imageio.ImageIO;
import org.springframework.core.io.ClassPathResource;


@Service
public class PdfCotizacionService {

    public ByteArrayInputStream generarPdf(Cotizacion cotizacion) {

        Document document = new Document(PageSize.A4, 30, 30, 40, 30);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {

            PdfWriter writer = PdfWriter.getInstance(document, out);

            writer.setPageEvent(new PdfPageEventHelper() {
                @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    try {
                        PdfContentByte canvas = writer.getDirectContent();

                        // LOGO TRANSLÚCIDO DE FONDO
                        Image watermark = cargarLogo();
                        watermark.scaleToFit(300, 220);

                        float x = (PageSize.A4.getWidth() - watermark.getScaledWidth()) / 2;
                        float y = (PageSize.A4.getHeight() - watermark.getScaledHeight()) / 2;

                        watermark.setAbsolutePosition(x, y);

                        PdfGState gs = new PdfGState();
                        gs.setFillOpacity(0.035f);

                        canvas.saveState();
                        canvas.setGState(gs);
                        canvas.addImage(watermark);
                        canvas.restoreState();

                        // LÍNEA INFERIOR ELEGANTE
                        PdfContentByte footerCanvas = writer.getDirectContent();
                        footerCanvas.setColorStroke(new Color(226, 232, 240));
                        footerCanvas.moveTo(document.left(), 24);
                        footerCanvas.lineTo(document.right(), 24);
                        footerCanvas.stroke();

                        // NUMERACIÓN DE PÁGINA
                        Font pageFont = new Font(
                                Font.HELVETICA,
                                8,
                                Font.BOLD,
                                new Color(100, 116, 139)
                        );

                        Phrase pageNumber = new Phrase(
                                "Página " + writer.getPageNumber(),
                                pageFont
                        );

                        ColumnText.showTextAligned(
                                footerCanvas,
                                Element.ALIGN_RIGHT,
                                pageNumber,
                                document.right(),
                                12,
                                0
                        );

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            document.open();

            Font titleFont = new Font(Font.HELVETICA, 26, Font.BOLD, new Color(15, 23, 42));
            Font subtitleFont = new Font(Font.HELVETICA, 11, Font.BOLD, new Color(239, 68, 68));
            Font textFont = new Font(Font.HELVETICA, 11, Font.NORMAL, Color.DARK_GRAY);
            Font boldFont = new Font(Font.HELVETICA, 11, Font.BOLD, new Color(15, 23, 42));

            // HEADER PREMIUM W&G - VERSIÓN LIMPIA
            PdfPTable header = new PdfPTable(3);
            header.setWidthPercentage(100);
            header.setWidths(new int[]{25, 50, 25});

// LOGO
            PdfPCell logoCell = new PdfPCell();
            logoCell.setBorder(Rectangle.NO_BORDER);
            logoCell.setPadding(8);
            logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

            try {
                Image logo = cargarLogo();

                logo.scaleToFit(115, 70);
                logo.setAlignment(Element.ALIGN_LEFT);
                logoCell.addElement(logo);
            } catch (Exception e) {
                Paragraph logoText = new Paragraph(
                        "W&G",
                        new Font(Font.HELVETICA, 30, Font.BOLD, new Color(239, 68, 68))
                );
                logoCell.addElement(logoText);
            }

            header.addCell(logoCell);

// INFO EMPRESA
            PdfPCell infoCell = new PdfPCell();
            infoCell.setBorder(Rectangle.NO_BORDER);
            infoCell.setPadding(8);
            infoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

            Paragraph distribuidor = new Paragraph(
                    "DISTRIBUIDOR AUTORIZADO",
                    new Font(Font.HELVETICA, 9, Font.BOLD, new Color(239, 68, 68))
            );
            distribuidor.setSpacingAfter(5);

            Paragraph rubro = new Paragraph(
                    "Tuberías PVC y CPVC",
                    new Font(Font.HELVETICA, 16, Font.BOLD, new Color(15, 23, 42))
            );
            rubro.setSpacingAfter(5);

            Paragraph marcas = new Paragraph(
                    "PAVCO · KINPLAST · NICOLL · CIM · VALVE · SANI",
                    new Font(Font.HELVETICA, 8, Font.BOLD, new Color(71, 85, 105))
            );
            marcas.setSpacingAfter(8);

            Paragraph contacto = new Paragraph(
                    "Av. Guillermo Dansey N° 481 Int. 159 - Lima\n" +
                            "431-4470  |  994 079 602  |  994 079 698\n" +
                            "wgcorporaciongoicha@gmail.com",
                    new Font(Font.HELVETICA, 8, Font.NORMAL, new Color(71, 85, 105))
            );
            contacto.setLeading(12f);

            infoCell.addElement(distribuidor);
            infoCell.addElement(rubro);
            infoCell.addElement(marcas);
            infoCell.addElement(contacto);

            header.addCell(infoCell);

// TARJETA COTIZACIÓN
            PdfPCell quoteCell = new PdfPCell();
            quoteCell.setBorder(Rectangle.NO_BORDER);
            quoteCell.setPadding(8);
            quoteCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

            PdfPTable quoteBox = new PdfPTable(1);
            quoteBox.setWidthPercentage(100);

            PdfPCell codigoCell = new PdfPCell(new Phrase(
                    cotizacion.getCodigo(),
                    new Font(Font.HELVETICA, 16, Font.BOLD, Color.WHITE)
            ));
            codigoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            codigoCell.setPadding(10);
            codigoCell.setBackgroundColor(new Color(15, 23, 42));
            codigoCell.setBorder(Rectangle.NO_BORDER);

            PdfPCell tituloCell = new PdfPCell(new Phrase(
                    "COTIZACIÓN",
                    new Font(Font.HELVETICA, 11, Font.BOLD, Color.WHITE)
            ));
            tituloCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            tituloCell.setPadding(8);
            tituloCell.setBackgroundColor(new Color(239, 68, 68));
            tituloCell.setBorder(Rectangle.NO_BORDER);

            PdfPCell fechaCell = new PdfPCell(new Phrase(
                    cotizacion.getFechaCreacion().toLocalDate().toString(),
                    new Font(Font.HELVETICA, 10, Font.BOLD, new Color(15, 23, 42))
            ));
            fechaCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            fechaCell.setPadding(8);
            fechaCell.setBackgroundColor(new Color(248, 250, 252));
            fechaCell.setBorderColor(new Color(226, 232, 240));

            quoteBox.addCell(codigoCell);
            quoteBox.addCell(tituloCell);
            quoteBox.addCell(fechaCell);

            quoteCell.addElement(quoteBox);
            header.addCell(quoteCell);

            document.add(header);

// LÍNEA ROJA ELEGANTE
            PdfPTable redLine = new PdfPTable(1);
            redLine.setWidthPercentage(100);

            PdfPCell redCell = new PdfPCell();
            redCell.setFixedHeight(3);
            redCell.setBorder(Rectangle.NO_BORDER);
            redCell.setBackgroundColor(new Color(239, 68, 68));

            redLine.addCell(redCell);
            document.add(redLine);

            document.add(new Paragraph("\n"));

            PdfPTable clienteCards = new PdfPTable(3);
            clienteCards.setWidthPercentage(100);
            clienteCards.setSpacingBefore(10);
            clienteCards.setSpacingAfter(18);
            clienteCards.setWidths(new int[]{33, 33, 34});

            Color cardBorder = new Color(226, 232, 240);
            Color titleColor = new Color(100, 116, 139);
            Color valueColor = new Color(15, 23, 42);

            Font clientTitleFont = new Font(
                    Font.HELVETICA,
                    7,
                    Font.BOLD,
                    titleColor
            );

            Font clientValueFont = new Font(
                    Font.HELVETICA,
                    10,
                    Font.NORMAL,
                    valueColor
            );

// ================= CLIENTE =================
            PdfPCell clienteCard = new PdfPCell();
            clienteCard.setPadding(8);
            clienteCard.setBorderColor(cardBorder);
            clienteCard.setBackgroundColor(new Color(248, 250, 252));

            Paragraph clienteTitle = new Paragraph(
                    "CLIENTE",
                    clientTitleFont
            );

            Paragraph clienteValue = new Paragraph(
                    "\n" + (
                            cotizacion.getCliente() != null &&
                                    !cotizacion.getCliente().trim().isEmpty()
                                    ? cotizacion.getCliente()
                                    : "CLIENTE VARIOS"
                    ),
                    clientValueFont
            );

            clienteValue.setLeading(14f);

            clienteCard.addElement(clienteTitle);
            clienteCard.addElement(clienteValue);

// ================= RUC =================
            PdfPCell rucCard = new PdfPCell();
            rucCard.setPadding(8);
            rucCard.setBorderColor(cardBorder);
            rucCard.setBackgroundColor(new Color(248, 250, 252));

            Paragraph rucTitle = new Paragraph(
                    "RUC / DNI",
                    clientTitleFont
            );

            Paragraph rucValue = new Paragraph(
                    "\n" + (
                            cotizacion.getRuc() != null &&
                                    !cotizacion.getRuc().trim().isEmpty()
                                    ? cotizacion.getRuc()
                                    : "-"
                    ),
                    clientValueFont
            );

            rucValue.setLeading(14f);

            rucCard.addElement(rucTitle);
            rucCard.addElement(rucValue);

// ================= TELÉFONO =================
            PdfPCell telefonoCard = new PdfPCell();
            telefonoCard.setPadding(8);
            telefonoCard.setBorderColor(cardBorder);
            telefonoCard.setBackgroundColor(new Color(248, 250, 252));

            Paragraph telefonoTitle = new Paragraph(
                    "CONTACTO",
                    clientTitleFont
            );

            Paragraph telefonoValue = new Paragraph(
                    "\n" + (
                            cotizacion.getTelefono() != null &&
                                    !cotizacion.getTelefono().trim().isEmpty()
                                    ? cotizacion.getTelefono()
                                    : "-"
                    ),
                    clientValueFont
            );

            telefonoValue.setLeading(14f);

            telefonoCard.addElement(telefonoTitle);
            telefonoCard.addElement(telefonoValue);

// ================= ADD =================
            clienteCards.addCell(clienteCard);
            clienteCards.addCell(rucCard);
            clienteCards.addCell(telefonoCard);

            document.add(clienteCards);

            // TABLA PRODUCTOS PREMIUM
            PdfPTable tabla = new PdfPTable(4);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new int[]{10, 56, 16, 18});
            tabla.setSpacingBefore(6);
            tabla.setSpacingAfter(14);

            tabla.addCell(crearHeaderPremium("Cant."));
            tabla.addCell(crearHeaderPremium("Descripción"));
            tabla.addCell(crearHeaderPremium("P. Unit"));
            tabla.addCell(crearHeaderPremium("Total"));

            int fila = 0;

            for (CotizacionDetalle item : cotizacion.getDetalles()) {
                Color rowColor = fila % 2 == 0
                        ? Color.WHITE
                        : new Color(248, 250, 252);

                tabla.addCell(crearBodyPremium(formatearCantidad(item.getCantidad()), rowColor, Element.ALIGN_CENTER));
                tabla.addCell(crearBodyPremium(item.getDescripcion(), rowColor, Element.ALIGN_LEFT));
                tabla.addCell(crearBodyPremium("S/ " + item.getPrecioUnitario(), rowColor, Element.ALIGN_RIGHT));
                tabla.addCell(crearBodyPremium("S/ " + item.getTotal(), rowColor, Element.ALIGN_RIGHT));

                fila++;
            }

            document.add(tabla);

            document.add(new Paragraph("\n"));

            // TOTALES ULTRA PREMIUM
            PdfPTable resumenTotales = new PdfPTable(1);
            resumenTotales.setWidthPercentage(38);
            resumenTotales.setHorizontalAlignment(Element.ALIGN_RIGHT);
            resumenTotales.setSpacingBefore(6);

// SUBTOTAL / IGV
            PdfPTable subtotales = new PdfPTable(2);
            subtotales.setWidthPercentage(100);
            subtotales.setWidths(new int[]{50, 50});

            subtotales.addCell(crearResumenLabel("SUBTOTAL"));
            subtotales.addCell(crearResumenValue("S/ " + cotizacion.getSubtotal()));

            subtotales.addCell(crearResumenLabel("IGV 18%"));
            subtotales.addCell(crearResumenValue("S/ " + cotizacion.getIgv()));

            PdfPCell subtotalesCell = new PdfPCell(subtotales);
            subtotalesCell.setBorder(Rectangle.NO_BORDER);
            subtotalesCell.setPadding(0);
            resumenTotales.addCell(subtotalesCell);

// ESPACIO
            PdfPCell spaceCell = new PdfPCell(new Phrase(" "));
            spaceCell.setBorder(Rectangle.NO_BORDER);
            spaceCell.setFixedHeight(8);
            resumenTotales.addCell(spaceCell);

// TOTAL DESTACADO
            PdfPCell totalBox = new PdfPCell();
            totalBox.setBorder(Rectangle.NO_BORDER);
            totalBox.setPadding(14);
            totalBox.setBackgroundColor(new Color(15, 23, 42));

            Paragraph totalLabel = new Paragraph(
                    "TOTAL FINAL",
                    new Font(Font.HELVETICA, 10, Font.BOLD, new Color(203, 213, 225))
            );
            totalLabel.setAlignment(Element.ALIGN_CENTER);

            Paragraph totalValue = new Paragraph(
                    "S/ " + cotizacion.getTotal(),
                    new Font(Font.HELVETICA, 18, Font.BOLD, Color.WHITE)
            );
            totalValue.setAlignment(Element.ALIGN_CENTER);
            totalValue.setSpacingBefore(6);

            totalBox.addElement(totalLabel);
            totalBox.addElement(totalValue);

            resumenTotales.addCell(totalBox);

            document.add(resumenTotales);

            document.add(new Paragraph("\n\n"));

            document.add(new Paragraph("\n"));

            // FOOTER PREMIUM
            PdfPTable footer = new PdfPTable(2);
            footer.setWidthPercentage(100);
            footer.setSpacingBefore(10);
            footer.setWidths(new int[]{50, 50});

// ================= BANCO =================
            PdfPCell bancoCard = new PdfPCell();
            bancoCard.setPadding(16);
            bancoCard.setBorderColor(new Color(226, 232, 240));
            bancoCard.setBackgroundColor(new Color(248, 250, 252));

            Paragraph bancoTitle = new Paragraph(
                    "DATOS BANCARIOS",
                    new Font(Font.HELVETICA, 11, Font.BOLD, new Color(239, 68, 68))
            );

            bancoTitle.setSpacingAfter(10);

            Paragraph bancoInfo = new Paragraph(
                    "BCP SOLES: 1912530035008\n" +
                            "CCI SOLES: 00219100253003500856\n\n" +
                            "BCP DÓLARES: 1912524516170\n" +
                            "CCI DÓLARES: 00219100252451617055",
                    new Font(Font.HELVETICA, 8, Font.NORMAL, new Color(15, 23, 42))
            );

            bancoInfo.setLeading(12f);

            bancoCard.addElement(bancoTitle);
            bancoCard.addElement(bancoInfo);

// ================= NOTA =================
            PdfPCell notaCard = new PdfPCell();
            notaCard.setPadding(16);
            notaCard.setBorderColor(new Color(226, 232, 240));
            notaCard.setBackgroundColor(Color.WHITE);

            Paragraph notaTitle = new Paragraph(
                    "INFORMACIÓN COMERCIAL",
                    new Font(Font.HELVETICA, 11, Font.BOLD, new Color(15, 23, 42))
            );

            notaTitle.setSpacingAfter(10);

            Paragraph notaInfo = new Paragraph(
                    "• Atención personalizada para cada proyecto.\n" +
                            "• Productos de calidad garantizada.\n" +
                            "• Trabajamos con marcas reconocidas y productos certificados.\n",
                    new Font(Font.HELVETICA, 8, Font.NORMAL, new Color(71, 85, 105))
            );

            notaInfo.add(new Chunk(crearIconoCatalogo(), 0, -1, true));
            notaInfo.add(new Chunk(
                    " Visita nuestra web\n",
                    new Font(Font.HELVETICA, 8, Font.BOLD, new Color(71, 85, 105))
            ));

            Chunk catalogoLink = new Chunk(
                    "www.wgcorporaciongoicha.com",
                    new Font(
                            Font.HELVETICA,
                            8,
                            Font.BOLD | Font.UNDERLINE,
                            new Color(21, 101, 192)
                    )
            );
            catalogoLink.setAnchor("https://www.wgcorporaciongoicha.com");
            notaInfo.add(catalogoLink);

            notaInfo.setLeading(12f);

            notaCard.addElement(notaTitle);
            notaCard.addElement(notaInfo);

// ================= ADD =================
            footer.addCell(bancoCard);
            footer.addCell(notaCard);

            document.add(footer);

            document.add(new Paragraph("\n"));

            Paragraph cierre = new Paragraph(
                    "W&G CORPORACIÓN GOICHA E.I.R.L. · Tuberías · Conexiones · Accesorios",
                    new Font(Font.HELVETICA, 8, Font.BOLD, new Color(100, 116, 139))
            );
            cierre.setAlignment(Element.ALIGN_CENTER);
            document.add(cierre);

            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    static String formatearCantidad(BigDecimal cantidad) {
        return cantidad.stripTrailingZeros().toPlainString();
    }

    private Image crearIconoCatalogo() throws Exception {
        int size = 64;
        BufferedImage bufferedImage = new BufferedImage(
                size,
                size,
                BufferedImage.TYPE_INT_ARGB
        );
        Graphics2D graphics = bufferedImage.createGraphics();

        graphics.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );
        graphics.setColor(new Color(71, 85, 105));
        graphics.setStroke(new BasicStroke(5f));
        graphics.drawOval(6, 6, 52, 52);
        graphics.drawOval(20, 6, 24, 52);
        graphics.drawLine(7, 32, 57, 32);
        graphics.dispose();

        ByteArrayOutputStream iconBytes = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", iconBytes);

        Image icon = Image.getInstance(iconBytes.toByteArray());
        icon.scaleAbsolute(8, 8);
        return icon;
    }

    private PdfPCell crearCelda(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(10);
        return cell;
    }

    private PdfPCell crearHeader(String text) {
        PdfPCell cell = new PdfPCell(
                new Phrase(
                        text,
                        new Font(Font.HELVETICA, 11, Font.BOLD, Color.WHITE)
                )
        );

        cell.setBackgroundColor(new Color(15, 23, 42));
        cell.setPadding(10);

        return cell;
    }

    private PdfPCell crearBody(String text) {
        PdfPCell cell = new PdfPCell(
                new Phrase(
                        text,
                        new Font(Font.HELVETICA, 10)
                )
        );

        cell.setPadding(10);

        return cell;
    }

    private PdfPCell crearTotalLabel(String text) {
        PdfPCell cell = new PdfPCell(
                new Phrase(
                        text,
                        new Font(Font.HELVETICA, 11, Font.BOLD)
                )
        );

        cell.setPadding(10);

        return cell;
    }

    private PdfPCell crearTotalValue(String text) {
        PdfPCell cell = new PdfPCell(
                new Phrase(
                        text,
                        new Font(Font.HELVETICA, 11)
                )
        );

        cell.setPadding(10);

        return cell;
    }

    private PdfPCell crearTotalLabelPremium(String text) {
        PdfPCell cell = new PdfPCell(
                new Phrase(
                        text,
                        new Font(Font.HELVETICA, 13, Font.BOLD, Color.WHITE)
                )
        );

        cell.setBackgroundColor(new Color(239, 68, 68));
        cell.setPadding(12);

        return cell;
    }

    private PdfPCell crearTotalValuePremium(String text) {
        PdfPCell cell = new PdfPCell(
                new Phrase(
                        text,
                        new Font(Font.HELVETICA, 13, Font.BOLD, Color.WHITE)
                )
        );

        cell.setBackgroundColor(new Color(239, 68, 68));
        cell.setPadding(12);

        return cell;
    }
    private PdfPCell crearHeaderPremium(String text) {
        PdfPCell cell = new PdfPCell(
                new Phrase(
                        text,
                        new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE)
                )
        );

        cell.setBackgroundColor(new Color(15, 23, 42));
        cell.setPadding(10);
        cell.setBorderColor(new Color(15, 23, 42));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        return cell;
    }

    private PdfPCell crearBodyPremium(String text, Color backgroundColor, int align) {
        PdfPCell cell = new PdfPCell(
                new Phrase(
                        text != null ? text : "",
                        new Font(Font.HELVETICA, 9, Font.NORMAL, new Color(15, 23, 42))
                )
        );

        cell.setPadding(9);
        cell.setBackgroundColor(backgroundColor);
        cell.setBorderColor(new Color(226, 232, 240));
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        return cell;
    }

    private PdfPCell crearTotalLabelSoft(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(
                text,
                new Font(Font.HELVETICA, 10, Font.BOLD, new Color(15, 23, 42))
        ));
        cell.setPadding(9);
        cell.setBorderColor(new Color(226, 232, 240));
        cell.setBackgroundColor(new Color(248, 250, 252));
        return cell;
    }

    private PdfPCell crearTotalValueSoft(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(
                text,
                new Font(Font.HELVETICA, 10, Font.BOLD, new Color(15, 23, 42))
        ));
        cell.setPadding(9);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setBorderColor(new Color(226, 232, 240));
        return cell;
    }

    private PdfPCell crearResumenLabel(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(
                text,
                new Font(Font.HELVETICA, 9, Font.BOLD, new Color(100, 116, 139))
        ));

        cell.setPadding(8);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(new Color(226, 232, 240));

        return cell;
    }

    private PdfPCell crearResumenValue(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(
                text,
                new Font(Font.HELVETICA, 9, Font.BOLD, new Color(15, 23, 42))
        ));

        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(new Color(226, 232, 240));

        return cell;
    }
    private Image cargarLogo() throws Exception {
        ClassPathResource resource =
                new ClassPathResource("static/logo-wg.png");

        return Image.getInstance(
                resource.getInputStream().readAllBytes()
        );
    }

}
