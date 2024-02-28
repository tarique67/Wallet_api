package com.swiggy.wallet.entities;

import com.swiggy.wallet.currencyConverterGrpcClient.CurrencyConverter;
import com.swiggy.wallet.enums.Country;
import com.swiggy.wallet.enums.Currency;
import com.swiggy.wallet.enums.IntraWalletTransactionType;
import com.swiggy.wallet.exceptions.InsufficientBalanceException;
import com.swiggy.wallet.exceptions.InvalidAmountException;
import com.swiggy.wallet.requestModels.TransactionRequestModel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import proto.ConvertResponse;

import java.time.LocalDateTime;

import static com.swiggy.wallet.responseModels.ResponseMessage.AMOUNT_LESS_THAN_SERVICE_CHARGE;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer walletId;

    private Money money;

    public Wallet(Country country) {
        this.money = new Money(0.0, country.getCurrency());
    }

    public void deposit(Money money) throws InvalidAmountException {
        this.money.add(money);
    }

    public void withdraw(Money money) throws InsufficientBalanceException, InvalidAmountException {
        this.money.subtract(money);
    }

    public Transaction transact(TransactionRequestModel requestModel, User sender, Wallet receiverWallet, User receiver) throws InsufficientBalanceException, InvalidAmountException {
        ConvertResponse res = CurrencyConverter.convertMoney(requestModel.getMoney(), this.getMoney().getCurrency(), receiverWallet.getMoney().getCurrency());

        double serviceCharge = res.getServiceCharge().getAmount();

        if(serviceCharge >= res.getMoney().getAmount())
            throw new InvalidAmountException(AMOUNT_LESS_THAN_SERVICE_CHARGE);

        this.withdraw(requestModel.getMoney());
        IntraWalletTransactions withdrawTransaction = new IntraWalletTransactions(new Money(requestModel.getMoney().getAmount(), requestModel.getMoney().getCurrency()), IntraWalletTransactionType.WITHDRAW, this);

        if(serviceCharge > 0.0)
            requestModel.getMoney().subtract(new Money(serviceCharge, receiverWallet.getMoney().getCurrency()));

        receiverWallet.deposit(requestModel.getMoney());
        IntraWalletTransactions depositTransaction = new IntraWalletTransactions(requestModel.getMoney(), IntraWalletTransactionType.DEPOSIT,receiverWallet);

        return new Transaction(LocalDateTime.now(),requestModel.getMoney(), sender, this.getWalletId(), receiver, receiverWallet.getWalletId(), new Money(res.getServiceCharge().getAmount(), Currency.valueOf(res.getServiceCharge().getCurrency())), depositTransaction, withdrawTransaction);
    }
}
