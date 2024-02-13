package com.swiggy.wallet.entities;

import com.swiggy.wallet.exceptions.InsufficientBalanceException;
import com.swiggy.wallet.exceptions.InvalidAmountException;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
@Entity
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer walletId;

    private int balance;

    public Wallet() {
        this.balance = 0;
    }

    public void deposit(int money) throws InvalidAmountException {
        if(money < 0)
            throw new InvalidAmountException("Amount cannot be negative.");
        this.balance += money;
    }

    public void withdraw(int money) throws InsufficientBalanceException {
        if(money > this.balance)
            throw new InsufficientBalanceException("Insufficient balance.");
        this.balance -= money;
    }
}
