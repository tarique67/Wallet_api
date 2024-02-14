package com.swiggy.wallet.entities;

import com.swiggy.wallet.enums.Currency;
import com.swiggy.wallet.exceptions.InsufficientBalanceException;
import com.swiggy.wallet.exceptions.InvalidAmountException;
import jakarta.persistence.*;
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

    private Money money;

    public Wallet() {
        this.money = new Money(0.0, Currency.INR);
    }

    public void deposit(Money money) throws InvalidAmountException {
        this.money.add(money);
    }

    public void withdraw(Money money) throws InsufficientBalanceException, InvalidAmountException {
        this.money.subtract(money);
    }
}
