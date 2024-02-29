package com.swiggy.wallet.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.swiggy.wallet.enums.IntraWalletTransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntraWalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int intraWalletTransactionId;

    private Money money;

    @Enumerated(EnumType.STRING)
    private IntraWalletTransactionType type;

    @ManyToOne(cascade = CascadeType.ALL)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Wallet wallet;

    private LocalDateTime timestamp;

    public IntraWalletTransaction(Money money, IntraWalletTransactionType type, Wallet wallet, LocalDateTime timestamp) {
        this.money = money;
        this.type = type;
        this.wallet = wallet;
        this.timestamp = timestamp;
    }
}