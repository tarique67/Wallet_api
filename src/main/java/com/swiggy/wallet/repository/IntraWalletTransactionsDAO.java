package com.swiggy.wallet.repository;

import com.swiggy.wallet.entities.IntraWalletTransactions;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntraWalletTransactionsDAO extends JpaRepository<IntraWalletTransactions, Integer> {
}
