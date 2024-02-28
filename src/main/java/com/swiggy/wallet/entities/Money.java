package com.swiggy.wallet.entities;

import com.swiggy.wallet.currencyConverterGrpcClient.CurrencyConverter;
import com.swiggy.wallet.enums.Currency;
import com.swiggy.wallet.exceptions.InsufficientBalanceException;
import com.swiggy.wallet.exceptions.InvalidAmountException;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import proto.ConvertResponse;

import static com.swiggy.wallet.responseModels.ResponseMessage.*;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Money {

    private double amount;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    public void add(Money money) throws InvalidAmountException {
        ConvertResponse res = CurrencyConverter.convertMoney(money, this.currency ,this.currency);
        if(res.getMoney().getAmount() <= 0)
            throw new InvalidAmountException(DEPOSIT_SHOULD_BE_GREATER_THAN_0);

        this.amount += res.getMoney().getAmount();
    }

    public void subtract(Money money) throws InvalidAmountException, InsufficientBalanceException {
        ConvertResponse res = CurrencyConverter.convertMoney(money, this.currency ,this.currency);

        if(res.getMoney().getAmount() > this.amount)
            throw new InsufficientBalanceException(INSUFFICIENT_BALANCE_EXCEPTION);

        if(res.getMoney().getAmount() <= 0)
            throw new InvalidAmountException(WITHDRAWAL_SHOULD_BE_GREATER_THAN_0);

        this.amount -= res.getMoney().getAmount();
    }
}
