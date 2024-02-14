package com.swiggy.wallet.entities;

import com.swiggy.wallet.enums.Currency;
import com.swiggy.wallet.exceptions.InsufficientBalanceException;
import com.swiggy.wallet.exceptions.InvalidAmountException;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Enumerated;
import lombok.*;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Money {

    private double amount;

    @Enumerated
    private Currency currency;

    public void add(Money money) throws InvalidAmountException {
        if((money.amount * money.currency.getConversionFactor()) <= 0)
            throw new InvalidAmountException("Amount to deposit should be greater than 0.");

        this.amount += money.amount * money.currency.getConversionFactor();
    }

    public void subtract(Money money) throws InvalidAmountException, InsufficientBalanceException {
        if((money.amount * money.currency.getConversionFactor()) > this.amount)
            throw new InsufficientBalanceException("Insufficient balance.");

        if((money.amount * money.currency.getConversionFactor()) <= 0)
            throw new InvalidAmountException("Withdrawal amount should be greater than 0.");

        this.amount -= money.amount * money.currency.getConversionFactor();
    }
}
