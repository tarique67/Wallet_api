package com.swiggy.wallet.repository;

import com.swiggy.wallet.entities.InterWalletTransaction;
import com.swiggy.wallet.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InterWalletTransactionDAO extends JpaRepository<InterWalletTransaction, Integer> {

    @Query("SELECT t FROM InterWalletTransaction t where t.sender = ?1 or t.receiver = ?1")
    public List<InterWalletTransaction> findTransactionsOfUser(User user);

    @Query("SELECT t FROM InterWalletTransaction t where (t.sender = ?1 or t.receiver = ?1) and (t.timestamp BETWEEN ?2 AND ?3)")
    public List<InterWalletTransaction> findTransactionsOfUserDateBased(User user, LocalDateTime startDate, LocalDateTime endDate);
}
