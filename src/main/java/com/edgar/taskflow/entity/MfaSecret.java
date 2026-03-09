package com.edgar.taskflow.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity
public class MfaSecret {

    @Id
    @GeneratedValue
    private Long id;

    private String secret;

    private boolean verified;

    @OneToOne
    private User user;
}