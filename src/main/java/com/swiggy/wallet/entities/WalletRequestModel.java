package com.swiggy.wallet.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WalletRequestModel {

    private int walletId;
    private Money money;

    public WalletRequestModel(Money money) {
        this.money = money;
    }
}
