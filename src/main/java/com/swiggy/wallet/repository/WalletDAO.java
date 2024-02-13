package com.swiggy.wallet.repository;

import com.swiggy.wallet.entities.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletDAO extends JpaRepository<Wallet, Integer> {
}
