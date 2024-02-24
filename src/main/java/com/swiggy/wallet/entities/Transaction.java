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

    private int senderWalletId;

    @ManyToOne(cascade = CascadeType.ALL)
    private User receiver;

    private int receiverWalletId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "service_charge")),
            @AttributeOverride(name = "currency", column = @Column(name = "service_charge_currency"))
    })
    private Money serviceCharge;

    public Transaction(LocalDateTime timestamp, Money money, User sender, int senderWalletId, User receiver, int receiverWalletId, Money serviceCharge) {
        this.timestamp = timestamp;
        this.money = money;
        this.sender = sender;
        this.senderWalletId = senderWalletId;
        this.receiver = receiver;
        this.receiverWalletId = receiverWalletId;
        this.serviceCharge = serviceCharge;
    }
}
