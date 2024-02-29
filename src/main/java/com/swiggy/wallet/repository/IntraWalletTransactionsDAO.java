package com.swiggy.wallet.repository;

import com.swiggy.wallet.entities.IntraWalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface IntraWalletTransactionsDAO extends JpaRepository<IntraWalletTransaction, Integer> {
    @Query("SELECT i FROM IntraWalletTransaction i WHERE i.wallet.walletId IN (?1)")
    List<IntraWalletTransaction> findByWallets(List<Integer> wallets);

    @Query("SELECT i FROM IntraWalletTransaction i WHERE i.wallet.walletId IN (?1) and (i.timestamp BETWEEN ?2 AND ?3)")
    List<IntraWalletTransaction> findByWalletsAndDate(List<Integer> wallets, LocalDateTime startDate, LocalDateTime endDate);
}
