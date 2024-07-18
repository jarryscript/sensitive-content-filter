package com.github.jarryzhou.sensitivecontentfilter.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
@Entity
public class SensitiveContentHitHistory {
    @Id
    @GeneratedValue
    public long id;
}
