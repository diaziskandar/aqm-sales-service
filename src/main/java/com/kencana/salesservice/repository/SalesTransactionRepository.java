package com.kencana.salesservice.repository;

import com.kencana.salesservice.model.SalesTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface SalesTransactionRepository extends JpaRepository<SalesTransaction, Long> {

    // 1. Jika search, startDate, dan endDate semuanya kosong/null
    @Query("SELECT s FROM SalesTransaction s WHERE " +
           "(:search IS NULL OR :search = '' OR LOWER(s.customerName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(s.itemName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<SalesTransaction> searchOnly(
            @Param("search") String search,
            Pageable pageable
    );

    // 2. Jika search dan rentang tanggal (startDate & endDate) semuanya terisi
    @Query("SELECT s FROM SalesTransaction s WHERE " +
           "(:search IS NULL OR :search = '' OR LOWER(s.customerName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(s.itemName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND s.date >= :startDate AND s.date <= :endDate")
    Page<SalesTransaction> searchAndFilterByDateRange(
            @Param("search") String search,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    // 3. Jika hanya startDate yang terisi
    @Query("SELECT s FROM SalesTransaction s WHERE " +
           "(:search IS NULL OR :search = '' OR LOWER(s.customerName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(s.itemName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND s.date >= :startDate")
    Page<SalesTransaction> searchAndFilterByStartDate(
            @Param("search") String search,
            @Param("startDate") LocalDate startDate,
            Pageable pageable
    );

    // 4. Jika hanya endDate yang terisi
    @Query("SELECT s FROM SalesTransaction s WHERE " +
           "(:search IS NULL OR :search = '' OR LOWER(s.customerName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(s.itemName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND s.date <= :endDate")
    Page<SalesTransaction> searchAndFilterByEndDate(
            @Param("search") String search,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );
}