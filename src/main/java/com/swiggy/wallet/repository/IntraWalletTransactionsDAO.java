package com.swiggy.wallet.repository;

import com.swiggy.wallet.entities.IntraWalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntraWalletTransactionsDAO extends JpaRepository<IntraWalletTransaction, Integer> {
}
