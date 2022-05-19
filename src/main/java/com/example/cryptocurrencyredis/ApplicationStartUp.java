package com.example.cryptocurrencyredis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.example.cryptocurrencyredis.service.CoinsDataService;

@Component
public class ApplicationStartUp implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private CoinsDataService coinsDataService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
//        this.coinsDataService.getCoins();
//        this.coinsDataService.getCoinHistory();
    }
}
