package com.kencana.salesservice.service;

import com.kencana.salesservice.model.SalesTransaction;
import com.kencana.salesservice.model.SalesTransactionItem;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Service
public class SalesPdfService {

    public ByteArrayInputStream generateInvoicePdf(SalesTransaction transaction) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 30, 30, 30, 30);

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();

            // Fonts
            Font companyTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13f, Color.BLACK);
            Font invoiceTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15f, Color.BLACK);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, Color.BLACK);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, Color.BLACK);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, Color.WHITE);

            // --- HEADER ATAS: PERUSAHAAN & FAKTUR PENJUALAN ---
            PdfPTable topTable = new PdfPTable(2);
            topTable.setWidthPercentage(100);
            topTable.setWidths(new float[]{50f, 50f});

            PdfPCell topLeft = new PdfPCell(new Paragraph("", normalFont));
            topLeft.setBorder(Rectangle.NO_BORDER);
            topTable.addCell(topLeft);

            PdfPCell topRight = new PdfPCell();
            topRight.setBorder(Rectangle.NO_BORDER);
            topRight.addElement(new Paragraph("PT. Raynor Ascarya Jagaddhita", companyTitleFont));
            topRight.addElement(new Paragraph("Ruko Golden Madrid 1 Blok E 17 BSD, Jl. Letnan Sutopo No.15, Rw. Mekar Jaya, Kec. Serpong, Kota Tangerang Selatan, Banten 15318", normalFont));
            topRight.addElement(new Paragraph("Kota Tangerang Selatan Banten", normalFont));
            topRight.addElement(new Paragraph("Indonesia", normalFont));
            topRight.addElement(new Paragraph("\n"));
            
            Paragraph invTitle = new Paragraph("Faktur Penjualan", invoiceTitleFont);
            invTitle.setAlignment(Element.ALIGN_RIGHT);
            topRight.addElement(invTitle);
            topTable.addCell(topRight);

            document.add(topTable);
            document.add(new Paragraph("\n"));

            // --- BAGIAN KEPADA & TABEL METADATA (KOTAK) ---
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{45f, 55f});

            PdfPCell cLeft = new PdfPCell();
            cLeft.setPadding(6);
            cLeft.addElement(new Paragraph("Kepada :", boldFont));
            cLeft.addElement(new Paragraph(transaction.getCustomerName(), boldFont));
            cLeft.addElement(new Paragraph("JI. Kliningan No.21 B, Turangga, Kec. Lengkong\nBandung Jawa Barat 40264\nIndonesia", normalFont));
            infoTable.addCell(cLeft);

            PdfPTable metaGrid = new PdfPTable(2);
            metaGrid.setWidthPercentage(100);
            metaGrid.setWidths(new float[]{50f, 50f});

            metaGrid.addCell(createMetaCell("Tanggal\n" + (transaction.getDate() != null ? transaction.getDate().toString() : ""), boldFont, normalFont));
            metaGrid.addCell(createMetaCell("Nomor\nSI." + transaction.getId(), boldFont, boldFont));
            
            metaGrid.addCell(createMetaCell("Metode Pembayaran\nTunai", boldFont, normalFont));
            metaGrid.addCell(createMetaCell("FOB\n ", boldFont, normalFont));

            metaGrid.addCell(createMetaCell("Ekspedisi\n ", boldFont, normalFont));
            metaGrid.addCell(createMetaCell("Tanggal Pengiriman\n" + (transaction.getDate() != null ? transaction.getDate().toString() : ""), boldFont, normalFont));

            metaGrid.addCell(createMetaCell("PO No\n ", boldFont, normalFont));
            metaGrid.addCell(createMetaCell("Mata Uang\nIndonesian Rupiah", boldFont, normalFont));

            PdfPCell cRight = new PdfPCell(metaGrid);
            cRight.setPadding(0);
            infoTable.addCell(cRight);

            document.add(infoTable);
            document.add(new Paragraph("\n"));

            // --- TABEL RINCIAN ITEM BARANG ---
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{15f, 40f, 10f, 15f, 10f, 15f});

            String[] headers = {"Kode Barang", "Nama Barang", "Kts.", "@Harga", "Diskon", "Total Harga"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Paragraph(h, headerFont));
                cell.setBackgroundColor(new Color(40, 40, 40));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(6);
                table.addCell(cell);
            }

            double grandTotal = 0;
            if (transaction.getItems() != null) {
                for (SalesTransactionItem item : transaction.getItems()) {
                    table.addCell(createCell(item.getItemId() != null ? "B0" + item.getItemId() : "-", normalFont, Element.ALIGN_LEFT));
                    table.addCell(createCell(item.getItemName(), normalFont, Element.ALIGN_LEFT));
                    table.addCell(createCell(String.valueOf(item.getQuantity()), normalFont, Element.ALIGN_CENTER));
                    
                    double totalPrice = item.getTotalPrice() != null ? item.getTotalPrice().doubleValue() : 0.0;
                    int qty = item.getQuantity() > 0 ? item.getQuantity() : 1;
                    double price = totalPrice / qty;

                    table.addCell(createCell(String.format("%,.0f", price), normalFont, Element.ALIGN_RIGHT));
                    table.addCell(createCell("0", normalFont, Element.ALIGN_RIGHT));
                    table.addCell(createCell(String.format("%,.0f", totalPrice), normalFont, Element.ALIGN_RIGHT));
                    
                    grandTotal += totalPrice;
                }
            }
            document.add(table);

            // --- TERBILANG & RINGKASAN BIAYA ---
            document.add(new Paragraph("\n"));
            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(100);
            summaryTable.setWidths(new float[]{60f, 40f});

            PdfPCell leftSummary = new PdfPCell();
            leftSummary.setBorder(Rectangle.NO_BORDER);

            String terbilangText = terbilang((long) grandTotal) + " rupiah";
            if (!terbilangText.isEmpty()) {
                terbilangText = terbilangText.substring(0, 1).toUpperCase() + terbilangText.substring(1);
            }

            Paragraph pTerbilang = new Paragraph();
            pTerbilang.add(new Chunk("Terbilang :  ", boldFont));
            pTerbilang.add(new Chunk(terbilangText, normalFont));
            
            PdfPCell tBoxCell = new PdfPCell(pTerbilang);
            tBoxCell.setPadding(6);
            tBoxCell.setBorderWidth(0.5f);
            
            PdfPTable tTable = new PdfPTable(1);
            tTable.setWidthPercentage(100);
            tTable.addCell(tBoxCell);
            leftSummary.addElement(tTable);

            leftSummary.addElement(new Paragraph("\n"));

            PdfPCell kCell = new PdfPCell();
            kCell.setPadding(6);
            kCell.addElement(new Paragraph("Keterangan :", boldFont));
            kCell.setFixedHeight(65f);
            
            PdfPTable kTable = new PdfPTable(1);
            kTable.setWidthPercentage(100);
            kTable.addCell(kCell);
            leftSummary.addElement(kTable);

            summaryTable.addCell(leftSummary);

            // Tabel Kalkulasi Kanan
            PdfPTable calcTable = new PdfPTable(2);
            calcTable.setWidthPercentage(100);
            calcTable.addCell(createCalcCell("Sub Total", boldFont, Element.ALIGN_LEFT));
            calcTable.addCell(createCalcCell(String.format("Rp %,.0f", grandTotal), boldFont, Element.ALIGN_RIGHT));

            calcTable.addCell(createCalcCell("Diskon", normalFont, Element.ALIGN_LEFT));
            calcTable.addCell(createCalcCell("Rp 0", normalFont, Element.ALIGN_RIGHT));

            calcTable.addCell(createCalcCell("PPN (0%)", normalFont, Element.ALIGN_LEFT));
            calcTable.addCell(createCalcCell("Rp 0", normalFont, Element.ALIGN_RIGHT));

            calcTable.addCell(createCalcCell("Biaya Lain-lain", normalFont, Element.ALIGN_LEFT));
            calcTable.addCell(createCalcCell("Rp 0", normalFont, Element.ALIGN_RIGHT));

            calcTable.addCell(createCalcCell("Total", boldFont, Element.ALIGN_LEFT));
            calcTable.addCell(createCalcCell(String.format("Rp %,.0f", grandTotal), boldFont, Element.ALIGN_RIGHT));

            PdfPCell rightSummary = new PdfPCell(calcTable);
            rightSummary.setBorder(Rectangle.NO_BORDER);
            rightSummary.setPaddingLeft(10);
            summaryTable.addCell(rightSummary);

            document.add(summaryTable);

            // --- TANDA TANGAN 3 KOLOM (Disiapkan Oleh, Disetujui Oleh, Diterima Oleh) & STEMPEL LUNAS ---
            document.add(new Paragraph("\n"));
            PdfPTable signTable = new PdfPTable(3);
            signTable.setWidthPercentage(100);
            signTable.setWidths(new float[]{33.33f, 33.33f, 33.33f});
            signTable.addCell(createSignBox("Disiapkan Oleh", "Tgl"));
            signTable.addCell(createSignBox("Disetujui Oleh", "Tgl."));
            signTable.addCell(createSignBox("Diterima Oleh", "Tgl."));
            document.add(signTable);

            document.add(new Paragraph("\n"));
            Paragraph lunasStamp = new Paragraph("LUNAS", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22f, new Color(46, 125, 50)));
            lunasStamp.setAlignment(Element.ALIGN_RIGHT);
            document.add(lunasStamp);

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private String terbilang(long angka) {
        String[] huruf = {"", "satu", "dua", "tiga", "empat", "lima", "enam", "tujuh", "delapan", "sembilan", "sepuluh", "sebelas"};
        if (angka < 12) {
            return huruf[(int) angka];
        } else if (angka < 20) {
            return terbilang(angka - 10) + " belas";
        } else if (angka < 100) {
            return terbilang(angka / 10) + " puluh " + terbilang(angka % 10);
        } else if (angka < 200) {
            return "seratus " + terbilang(angka - 100);
        } else if (angka < 1000) {
            return terbilang(angka / 100) + " ratus " + terbilang(angka % 100);
        } else if (angka < 2000) {
            return "seribu " + terbilang(angka - 1000);
        } else if (angka < 1000000) {
            return terbilang(angka / 1000) + " ribu " + terbilang(angka % 1000);
        } else if (angka < 1000000000) {
            return terbilang(angka / 1000000) + " juta " + terbilang(angka % 1000000);
        }
        return "";
    }

    private PdfPCell createCell(String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(5);
        return cell;
    }

    private PdfPCell createMetaCell(String text, Font titleFont, Font valFont) {
        Paragraph p = new Paragraph();
        String[] lines = text.split("\n");
        if(lines.length > 0) p.add(new Chunk(lines[0] + "\n", titleFont));
        if(lines.length > 1) p.add(new Chunk(lines[1], valFont));
        
        PdfPCell cell = new PdfPCell(p);
        cell.setPadding(4);
        cell.setBorderWidth(0.5f);
        return cell;
    }

    private PdfPCell createCalcCell(String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(4);
        cell.setBorderWidth(0.5f);
        return cell;
    }

    private PdfPCell createSignBox(String title, String dateLabel) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(8);
        cell.setFixedHeight(85f);
        cell.addElement(new Paragraph(title, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9f)));
        cell.addElement(new Paragraph("\n\n________________________", FontFactory.getFont(FontFactory.HELVETICA, 9f)));
        cell.addElement(new Paragraph(dateLabel, FontFactory.getFont(FontFactory.HELVETICA, 8f)));
        return cell;
    }
}