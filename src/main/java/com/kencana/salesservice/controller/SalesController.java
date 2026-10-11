package com.kencana.salesservice.controller;

import com.kencana.salesservice.model.SalesTransaction;
import com.kencana.salesservice.service.SalesService;
import com.kencana.salesservice.service.SalesPdfService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;

@RestController
@RequestMapping("/api/sales")
public class SalesController {

    @Autowired
    private SalesService salesService;

    @Autowired
    private SalesPdfService salesPdfService;

    // Endpoint untuk mendapatkan data sales (dengan pagination, sorting, search)
    @GetMapping
    public ResponseEntity<Page<SalesTransaction>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        Page<SalesTransaction> transactions = salesService.getTransactions(page, size, sortBy, direction, search, startDate, endDate);
        return ResponseEntity.ok(transactions);
    }

    // Endpoint: Menyimpan transaksi dengan dukungan Multiple File Upload (Opsional)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createTransaction(
            @RequestPart("transaction") String transactionJson,
            @RequestPart(value = "attachments", required = false) MultipartFile[] attachments) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            SalesTransaction transaction = objectMapper.readValue(transactionJson, SalesTransaction.class);

            SalesTransaction savedTransaction = salesService.saveTransactionWithMultipleAttachments(transaction, attachments);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedTransaction);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Gagal menyimpan transaksi: " + e.getMessage());
        }
    }

    // Endpoint baru: Mengunduh Faktur Penjualan dalam format PDF yang identik
    @GetMapping("/{id}/invoice-pdf")
    public ResponseEntity<InputStreamResource> downloadInvoicePdf(@PathVariable Long id) {
        // Mengambil transaksi berdasarkan ID (pastikan method ini tersedia di SalesService / Repository Anda)
        SalesTransaction transaction = salesService.getTransactionById(id);
        
        if (transaction == null) {
            return ResponseEntity.notFound().build();
        }

        ByteArrayInputStream bis = salesPdfService.generateInvoicePdf(transaction);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=Faktur_Penjualan_SI." + id + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }

    // Endpoint riwayat sales berdasarkan customer name
    @GetMapping("/customer/{customerName}")
    public ResponseEntity<List<SalesTransaction>> getSalesByCustomer(@PathVariable String customerName) {
        List<SalesTransaction> list = salesService.getSalesByCustomerName(customerName);
        return ResponseEntity.ok(list);
    }
}