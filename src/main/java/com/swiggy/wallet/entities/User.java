package com.swiggy.wallet.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.swiggy.wallet.enums.Country;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer userId;

    @Column(unique = true)
    private String userName;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Enumerated(EnumType.STRING)
    private Country country;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "USERID")
    private List<Wallet> wallets = new ArrayList<>();

    public User(String userName, String password, Country country) {
        this.userName = userName;
        this.password = password;
        this.country = country;
        this.wallets.add(new Wallet(country));
    }
}
