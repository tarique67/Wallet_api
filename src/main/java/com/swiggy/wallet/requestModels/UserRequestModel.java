package com.swiggy.wallet.requestModels;

import com.swiggy.wallet.enums.Country;
import lombok.*;

@Data
@AllArgsConstructor
public class UserRequestModel {

    private String userName;
    private String password;
    private Country country;

}
