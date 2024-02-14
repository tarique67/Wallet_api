package com.swiggy.wallet.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WalletRequestModel {

    private Money money;

    public WalletRequestModel(Money money) {
        this.money = money;
    }
}
