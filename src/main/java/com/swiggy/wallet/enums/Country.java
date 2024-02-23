package com.swiggy.wallet.enums;

import static com.swiggy.wallet.enums.Currency.*;

public enum Country {

    INDIA(INR),
    USA(USD),
    EUROPE(EUR);

    private final Currency currency;

    Country(Currency currency) {
        this.currency = currency;
    }

    public Currency getCurrency() {
        return this.currency;
    }
}
