package com.agloval.infrastructure.output.pdf;

import com.agloval.application.dto.QuotationLineResponse;
import com.agloval.application.dto.QuotationResponse;
import com.agloval.application.port.out.PdfGenerationPort;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class OpenPdfAdapter implements PdfGenerationPort {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");

    private static final Color AGLOVAL_BLUE = new Color(30, 58, 138);
    private static final Color TABLE_HEADER_BG = new Color(219, 234, 254);
    private static final Color LIGHT_GRAY = new Color(243, 244, 246);
    private static final Color BORDER_GRAY = new Color(209, 213, 219);

    private static final int CELL_PADDING = 6;
    private static final int CELL_PADDING_HEADER = 7;
    private static final int CELL_PADDING_CLIENT_DATA = 8;
    private static final int CELL_PADDING_GRAND_TOTAL = 9;

    @Override
    public byte[] generateQuotationPdf(QuotationResponse quotation) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4, 50, 50, 60, 60);
            PdfWriter.getInstance(doc, out);
            doc.open();

            addHeader(doc, quotation);
            addClientSection(doc, quotation);
            addLinesTable(doc, quotation.getLines());
            addTotalsSection(doc, quotation);
            addFooter(doc, quotation);

            doc.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new PdfGenerationException("Failed to generate PDF for quotation " + quotation.getId(), e);
        }
    }

    private void addHeader(Document doc, QuotationResponse q) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 26, AGLOVAL_BLUE);
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12, AGLOVAL_BLUE);
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);

        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{60, 40});
        headerTable.setSpacingAfter(20);

        PdfPCell brandCell = new PdfPCell();
        brandCell.setBorder(PdfPCell.NO_BORDER);
        brandCell.setPaddingBottom(10);
        Paragraph brand = new Paragraph("AGLOVAL", titleFont);
        brand.add(Chunk.NEWLINE);
        brand.add(new Chunk("Madera y Tableros", subtitleFont));
        brandCell.addElement(brand);
        headerTable.addCell(brandCell);

        PdfPCell metaCell = new PdfPCell();
        metaCell.setBorder(PdfPCell.NO_BORDER);
        metaCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        metaCell.setPaddingBottom(10);

        Paragraph meta = new Paragraph();
        meta.setAlignment(Element.ALIGN_RIGHT);
        meta.add(new Chunk("PRESUPUESTO\n",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, AGLOVAL_BLUE)));
        meta.add(new Chunk(q.getQuotationNumber() + "\n",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.BLACK)));
        meta.add(new Chunk("Fecha: ", labelFont));
        meta.add(new Chunk(q.getCreatedAt().format(DATE_FMT) + "\n", valueFont));
        meta.add(new Chunk("Válido hasta: ", labelFont));
        meta.add(new Chunk(q.getExpiryDate().format(DATE_FMT) + "\n", valueFont));
        meta.add(new Chunk("Estado: ", labelFont));
        meta.add(new Chunk(q.getStatus().name(), valueFont));
        metaCell.addElement(meta);
        headerTable.addCell(metaCell);

        doc.add(headerTable);

        PdfPTable separator = new PdfPTable(1);
        separator.setWidthPercentage(100);
        separator.setSpacingAfter(16);
        PdfPCell line = new PdfPCell(new Phrase(" "));
        line.setBorderWidthBottom(2f);
        line.setBorderColorBottom(AGLOVAL_BLUE);
        line.setBorderWidthTop(0);
        line.setBorderWidthLeft(0);
        line.setBorderWidthRight(0);
        separator.addCell(line);
        doc.add(separator);
    }

    private void addClientSection(Document doc, QuotationResponse q) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, AGLOVAL_BLUE);
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);

        PdfPTable clientTable = new PdfPTable(1);
        clientTable.setWidthPercentage(55);
        clientTable.setHorizontalAlignment(Element.ALIGN_LEFT);
        clientTable.setSpacingAfter(20);

        PdfPCell titleCell = new PdfPCell(new Phrase("DATOS DEL CLIENTE", sectionFont));
        titleCell.setBackgroundColor(TABLE_HEADER_BG);
        titleCell.setBorderColor(BORDER_GRAY);
        titleCell.setPadding(CELL_PADDING);
        clientTable.addCell(titleCell);

        Paragraph clientData = new Paragraph();
        clientData.add(new Chunk("Cliente: ", labelFont));
        clientData.add(new Chunk(q.getUserName() + "\n", valueFont));
        clientData.add(new Chunk("Email: ", labelFont));
        clientData.add(new Chunk(q.getUserEmail() + "\n", valueFont));

        PdfPCell dataCell = new PdfPCell();
        dataCell.setBorderColor(BORDER_GRAY);
        dataCell.setPadding(CELL_PADDING_CLIENT_DATA);
        dataCell.addElement(clientData);
        clientTable.addCell(dataCell);

        doc.add(clientTable);
    }

    private void addLinesTable(Document doc, List<QuotationLineResponse> lines) throws DocumentException {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, AGLOVAL_BLUE);
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);
        Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 7, Color.GRAY);

        Paragraph sectionTitle = new Paragraph("DETALLE DE LÍNEAS",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, AGLOVAL_BLUE));
        sectionTitle.setSpacingAfter(6);
        doc.add(sectionTitle);

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{35, 12, 15, 18, 20});
        table.setSpacingAfter(20);

        String[] headers = {"Producto", "Cantidad", "Precio Unit.", "Descuento", "Subtotal"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(TABLE_HEADER_BG);
            cell.setBorderColor(BORDER_GRAY);
            cell.setPadding(CELL_PADDING_HEADER);
            cell.setHorizontalAlignment(h.equals("Producto") ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT);
            table.addCell(cell);
        }

        boolean alternate = false;
        for (QuotationLineResponse line : lines) {
            Color rowBg = alternate ? LIGHT_GRAY : Color.WHITE;

            PdfPCell nameCell = new PdfPCell();
            nameCell.setBorderColor(BORDER_GRAY);
            nameCell.setBackgroundColor(rowBg);
            nameCell.setPadding(CELL_PADDING);
            Paragraph namePara = new Paragraph(line.getProductName(), cellFont);
            if (line.getDescription() != null && !line.getDescription().isBlank()) {
                namePara.add(Chunk.NEWLINE);
                namePara.add(new Chunk(line.getDescription(), smallFont));
            }
            nameCell.addElement(namePara);
            table.addCell(nameCell);

            table.addCell(rightCell(line.getQuantity().toPlainString(), cellFont, rowBg));
            table.addCell(rightCell(formatCurrency(line.getUnitPrice()), cellFont, rowBg));

            PdfPCell dtoCell = new PdfPCell();
            dtoCell.setBorderColor(BORDER_GRAY);
            dtoCell.setBackgroundColor(rowBg);
            dtoCell.setPadding(CELL_PADDING);
            dtoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            Paragraph dtoPara = new Paragraph(line.getDiscountPercent() + "%", cellFont);
            if (line.getDiscountBreakdown() != null && !line.getDiscountBreakdown().isBlank()) {
                dtoPara.add(Chunk.NEWLINE);
                dtoPara.add(new Chunk(line.getDiscountBreakdown(), smallFont));
            }
            dtoCell.addElement(dtoPara);
            table.addCell(dtoCell);

            table.addCell(rightCell(formatCurrency(line.getLineTotal()), cellFont, rowBg));
            alternate = !alternate;
        }

        doc.add(table);
    }

    private void addTotalsSection(Document doc, QuotationResponse q) throws DocumentException {
        BigDecimal vatAmount = q.getTotal().multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalWithVat = q.getTotal().add(vatAmount).setScale(2, RoundingMode.HALF_UP);

        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);
        Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.WHITE);

        PdfPTable totalsTable = new PdfPTable(2);
        totalsTable.setWidthPercentage(45);
        totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalsTable.setWidths(new float[]{55, 45});
        totalsTable.setSpacingAfter(24);

        addTotalRow(totalsTable, "Subtotal bruto:", formatCurrency(q.getSubtotal()),
                labelFont, valueFont, Color.WHITE);
        addTotalRow(totalsTable, "Descuento:", "- " + formatCurrency(q.getDiscountAmount()),
                labelFont, valueFont, LIGHT_GRAY);
        addTotalRow(totalsTable, "Total neto:", formatCurrency(q.getTotal()),
                labelFont, valueFont, Color.WHITE);
        addTotalRow(totalsTable, "IVA (21%):", formatCurrency(vatAmount),
                labelFont, valueFont, LIGHT_GRAY);

        PdfPCell totalLabel = new PdfPCell(new Phrase("TOTAL:", totalFont));
        totalLabel.setBackgroundColor(AGLOVAL_BLUE);
        totalLabel.setBorderColor(AGLOVAL_BLUE);
        totalLabel.setPadding(CELL_PADDING_GRAND_TOTAL);
        totalLabel.setHorizontalAlignment(Element.ALIGN_LEFT);
        totalsTable.addCell(totalLabel);

        PdfPCell totalValue = new PdfPCell(new Phrase(formatCurrency(totalWithVat), totalFont));
        totalValue.setBackgroundColor(AGLOVAL_BLUE);
        totalValue.setBorderColor(AGLOVAL_BLUE);
        totalValue.setPadding(CELL_PADDING_GRAND_TOTAL);
        totalValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalsTable.addCell(totalValue);

        doc.add(totalsTable);
    }

    private void addFooter(Document doc, QuotationResponse q) throws DocumentException {
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);
        Font footerBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.GRAY);

        PdfPTable footerTable = new PdfPTable(1);
        footerTable.setWidthPercentage(100);
        PdfPCell footerLine = new PdfPCell(new Phrase(" "));
        footerLine.setBorderWidthTop(1f);
        footerLine.setBorderColorTop(BORDER_GRAY);
        footerLine.setBorderWidthBottom(0);
        footerLine.setBorderWidthLeft(0);
        footerLine.setBorderWidthRight(0);
        footerTable.addCell(footerLine);
        doc.add(footerTable);

        Paragraph footer = new Paragraph();
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.add(new Chunk("Este presupuesto es válido por ", footerFont));
        footer.add(new Chunk(q.getValidityDays() + " días", footerBold));
        footer.add(new Chunk(" · Vence el " + q.getExpiryDate().format(DATE_FMT) + "\n", footerFont));
        footer.add(new Chunk("AGLOVAL S.L. · contacto@agloval.es · agloval.es", footerFont));
        doc.add(footer);
    }

    private void addTotalRow(PdfPTable table, String label, String value,
                             Font labelFont, Font valueFont, Color bg) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBackgroundColor(bg);
        labelCell.setBorderColor(BORDER_GRAY);
        labelCell.setPadding(CELL_PADDING);
        labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBackgroundColor(bg);
        valueCell.setBorderColor(BORDER_GRAY);
        valueCell.setPadding(CELL_PADDING);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }

    private PdfPCell rightCell(String text, Font font, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorderColor(BORDER_GRAY);
        cell.setBackgroundColor(bg);
        cell.setPadding(CELL_PADDING);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return cell;
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "€0,00";
        return String.format("€%,.2f", amount).replace(",", "X").replace(".", ",").replace("X", ".");
    }
}
