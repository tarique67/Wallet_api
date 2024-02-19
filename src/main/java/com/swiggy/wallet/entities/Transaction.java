package com.swiggy.wallet.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int transactionId;

    private LocalDateTime timestamp;

    private Money money;

    @ManyToOne(cascade = CascadeType.ALL)
    private User sender;

    @ManyToOne(cascade = CascadeType.ALL)
    private User receiver;

    public Transaction(LocalDateTime timestamp, Money money, User sender, User receiver) {
        this.timestamp = timestamp;
        this.money = money;
        this.sender = sender;
        this.receiver = receiver;
    }
}
