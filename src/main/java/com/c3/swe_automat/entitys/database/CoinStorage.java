package com.c3.swe_automat.entitys.database;

import com.c3.swe_automat.enums.Coin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class CoinStorage extends AbstractBaseEntity {

    @Enumerated(EnumType.STRING)
    Coin coin;

    int amount;

    int maxAmount;

    Integer pufferAmount;
}
