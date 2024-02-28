package com.swiggy.wallet.entities;

import com.swiggy.wallet.enums.IntraWalletTransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntraWalletTransactions {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private Money money;

    @Enumerated(EnumType.STRING)
    private IntraWalletTransactionType type;

    @ManyToOne(cascade = CascadeType.ALL)
    private Wallet wallet;

    public IntraWalletTransactions(Money money, IntraWalletTransactionType type, Wallet wallet) {
        this.money = money;
        this.type = type;
        this.wallet = wallet;
    }
}