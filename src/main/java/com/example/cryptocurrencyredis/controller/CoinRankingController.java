package com.example.cryptocurrencyredis.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.cryptocurrencyredis.Utils.DateUtils;
import com.example.cryptocurrencyredis.Utils.RoundUtils;
import com.example.cryptocurrencyredis.model.CoinInfo;
import com.example.cryptocurrencyredis.model.HistoryData;
import com.example.cryptocurrencyredis.service.CoinsDataService;

import io.github.dengliming.redismodule.redistimeseries.Sample;
import lombok.extern.slf4j.Slf4j;

@CrossOrigin("http://localhost:3000")
@Controller
@RequestMapping("/api/v1/coins")
@Slf4j
public class CoinRankingController {

    private final CoinsDataService coinsDataService;

    @Autowired
    public CoinRankingController(CoinsDataService coinsDataService) {
        this.coinsDataService = coinsDataService;
    }

    @GetMapping
    public ResponseEntity<List<CoinInfo>> getCoins() {
        return ResponseEntity.ok().body(this.coinsDataService.getAllCoinsFromRedisJSON());
    }

    @GetMapping("/{coin}/{period}")
    public ResponseEntity<List<HistoryData>> getCoinHistoryPerPeriod(@PathVariable String coin,
            @PathVariable String period) {
        List<Sample.Value> coinHistoryPerPeriod = this.coinsDataService.getCoinHistoryPerPeriod(coin, period);
        List<HistoryData> coinHistoryData = coinHistoryPerPeriod.stream()
                .map(history -> new HistoryData(DateUtils.convertUnixTimeToDate(history.getTimestamp()),
                        RoundUtils.round(history.getValue(), 2)))
                .collect(Collectors.toList());
        return ResponseEntity.ok().body(coinHistoryData);
    }
}
