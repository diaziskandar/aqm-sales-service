package com.kencana.salesservice.controller;

import com.kencana.salesservice.model.SalesTransaction;
import com.kencana.salesservice.repository.SalesTransactionRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/sales")
public class SalesPdfController {

    private final SalesTransactionRepository repository;

    public SalesPdfController(SalesTransactionRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportToPdf(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<SalesTransaction> transactions = repository.findAll();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            Font boldFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.DARK_GRAY);
            Font normalFont = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.DARK_GRAY);
            Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);

            // --- KOP HEADER (AQM Hearing Center) ---
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{2f, 3f});

            PdfPCell leftCell = new PdfPCell();
            leftCell.setBorder(Rectangle.NO_BORDER);
            leftCell.addElement(new Paragraph("AQM Hearing Center", new Font(Font.HELVETICA, 14, Font.BOLD, new Color(192, 57, 43))));
            leftCell.addElement(new Paragraph("Laporan Transaksi Penjualan Sales", normalFont));
            headerTable.addCell(leftCell);

            PdfPCell rightCell = new PdfPCell();
            rightCell.setBorder(Rectangle.NO_BORDER);
            rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            String cetakTgl = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy", new Locale("id", "ID")));
            rightCell.addElement(new Paragraph("Tanggal Cetak: " + cetakTgl, normalFont));
            headerTable.addCell(rightCell);

            document.add(headerTable);

            Paragraph divider = new Paragraph("____________________________________________________________________________________");
            divider.getFont().setColor(new Color(189, 195, 199));
            divider.setSpacingAfter(15);
            document.add(divider);

            // --- RINGKASAN (SUMMARY) ---
            int totalTransaksi = transactions.size();
            double totalPendapatan = transactions.stream()
                    .mapToDouble(t -> t.getTotalPrice() != null ? t.getTotalPrice().doubleValue() : 0.0)
                    .sum();
            NumberFormat idrFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(40);
            summaryTable.setHorizontalAlignment(Element.ALIGN_LEFT);
            summaryTable.setSpacingAfter(15);

            addSummaryRow(summaryTable, "Total Transaksi:", totalTransaksi + " Pesanan", boldFont, normalFont);
            addSummaryRow(summaryTable, "Total Pendapatan:", idrFormat.format(totalPendapatan).replace("Rp", "Rp "), boldFont, normalFont);
            
            document.add(summaryTable);

            // --- TABEL DATA ---
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2f, 2.5f, 2.5f, 1.2f, 2.2f, 1.8f});

            String[] headers = {"Tanggal", "Customer", "Nama Item", "Jml", "Total Harga", "Type"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Paragraph(header, headerFont));
                cell.setBackgroundColor(new Color(41, 128, 185));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(6);
                table.addCell(cell);
            }

            for (SalesTransaction t : transactions) {
                table.addCell(new PdfPCell(new Paragraph(String.valueOf(t.getDate()), normalFont)));
                table.addCell(new PdfPCell(new Paragraph(t.getCustomerName(), normalFont)));
                table.addCell(new PdfPCell(new Paragraph(t.getItemName(), normalFont)));
                
                PdfPCell qtyCell = new PdfPCell(new Paragraph(String.valueOf(t.getQuantity()), normalFont));
                qtyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(qtyCell);

                double price = t.getTotalPrice() != null ? t.getTotalPrice().doubleValue() : 0.0;
                table.addCell(new PdfPCell(new Paragraph(idrFormat.format(price).replace("Rp", "Rp "), normalFont)));
                
                table.addCell(new PdfPCell(new Paragraph(t.getItemType() != null ? t.getItemType() : "-", normalFont)));
            }

            document.add(table);
            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Laporan_Sales_AQM.pdf");

        return ResponseEntity.ok().headers(headers).body(baos.toByteArray());
    }

    private void addSummaryRow(PdfPTable table, String label, String value, Font bold, Font normal) {
        PdfPCell c1 = new PdfPCell(new Paragraph(label, bold));
        c1.setBorder(Rectangle.NO_BORDER);
        c1.setPadding(3);
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Paragraph(value, normal));
        c2.setBorder(Rectangle.NO_BORDER);
        c2.setPadding(3);
        table.addCell(c2);
    }
}