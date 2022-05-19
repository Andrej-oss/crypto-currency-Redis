package com.example.cryptocurrencyredis.model;

import lombok.Data;

@Data
public class Coins {
    private String status;
    private CoinData data;
}