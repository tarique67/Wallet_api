package com.swiggy.wallet.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InterWalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int interWalletTransactionId;

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

    @OneToOne(cascade = CascadeType.ALL)
    private IntraWalletTransaction deposit;

    @OneToOne(cascade = CascadeType.ALL)
    private IntraWalletTransaction withdrawal;

    public InterWalletTransaction(User sender, int senderWalletId, User receiver, int receiverWalletId, Money serviceCharge, IntraWalletTransaction deposit, IntraWalletTransaction withdrawal) {
        this.sender = sender;
        this.senderWalletId = senderWalletId;
        this.receiver = receiver;
        this.receiverWalletId = receiverWalletId;
        this.serviceCharge = serviceCharge;
        this.deposit = deposit;
        this.withdrawal = withdrawal;
    }
}
