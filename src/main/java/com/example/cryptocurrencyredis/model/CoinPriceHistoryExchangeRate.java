package com.example.cryptocurrencyredis.model;

import lombok.Data;

@Data
public class CoinPriceHistoryExchangeRate {
    private String price;
    private String timestamp;
}
