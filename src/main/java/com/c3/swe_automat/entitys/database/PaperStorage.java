package com.c3.swe_automat.entitys.database;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class PaperStorage extends AbstractBaseEntity {
    private int maxAmount;

    private int amount;

    private Integer pufferAmount;
}
