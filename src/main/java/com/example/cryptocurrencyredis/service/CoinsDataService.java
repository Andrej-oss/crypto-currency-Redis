package com.example.cryptocurrencyredis.service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.cryptocurrencyredis.Utils.HttpUtils;
import com.example.cryptocurrencyredis.model.CoinData;
import com.example.cryptocurrencyredis.model.CoinInfo;
import com.example.cryptocurrencyredis.model.CoinPriceHistory;
import com.example.cryptocurrencyredis.model.CoinPriceHistoryExchangeRate;
import com.example.cryptocurrencyredis.model.Coins;

import io.github.dengliming.redismodule.redisjson.RedisJSON;
import io.github.dengliming.redismodule.redisjson.args.GetArgs;
import io.github.dengliming.redismodule.redisjson.args.SetArgs;
import io.github.dengliming.redismodule.redisjson.utils.GsonUtils;
import io.github.dengliming.redismodule.redistimeseries.DuplicatePolicy;
import io.github.dengliming.redismodule.redistimeseries.RedisTimeSeries;
import io.github.dengliming.redismodule.redistimeseries.Sample;
import io.github.dengliming.redismodule.redistimeseries.TimeSeriesOptions;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CoinsDataService {

    public static final String GET_COINS_URL = "https://coinranking1.p.rapidapi.com/coins?referenceCurrencyUuid=yhjMzLPhuIDl&timePeriod=24h&tiers=1&orderBy=marketCap&orderDirection=desc&limit=50&offset=0";
    private static final String COINS_KEY = "coins";
    public static final String GET_COIN_HISTORY_API = "https://coinranking1.p.rapidapi.com/coin/";
    public static final String COIN_HISTORY_TIME_PERIOD_PARAM = "/history?timePeriod=";
    public static final List<String> timePeriods = List.of("24h", "7d", "30d", "3m", "1y", "3y", "5y");

    private final RestTemplate restTemplate;
    private final RedisJSON redisJSON;
    private final RedisTimeSeries redisTimeSeries;

    @Autowired
    public CoinsDataService(RestTemplate restTemplate, RedisJSON redisJSON, RedisTimeSeries redisTimeSeries) {
        this.restTemplate = restTemplate;
        this.redisJSON = redisJSON;
        this.redisTimeSeries = redisTimeSeries;
    }

    public void getCoins() {
        log.info("Getting coins from " + GET_COINS_URL);
        ResponseEntity<Coins> coins = restTemplate.exchange(GET_COINS_URL, HttpMethod.GET, HttpUtils.getHttpEntity(),
                Coins.class);
        this.saveCoinsInRedisJSON(coins.getBody());
    }

    public void getCoinHistory() {
        log.info("Getting coin history from ");
        List<CoinInfo> coins = this.getAllCoinsFromRedisJSON();
        coins.forEach(coinInfo -> {
            timePeriods.forEach(period -> {
                try {
                    this.getCoinHistoryForTimePeriod(coinInfo, period);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    private void getCoinHistoryForTimePeriod(CoinInfo coinInfo, String period) throws InterruptedException {
        log.info("Getting coin history for " + coinInfo + " and time period " + period);
        String coinUri = GET_COIN_HISTORY_API + coinInfo.getUuid() + COIN_HISTORY_TIME_PERIOD_PARAM + period;
        ResponseEntity<CoinPriceHistory> coinPriceHistoryResponseEntity = this.restTemplate.exchange(coinUri, HttpMethod.GET,
                HttpUtils.getHttpEntity(), CoinPriceHistory.class);
        Thread.sleep(2000);
        this.saveCoinHistoryInRedisSeriesTimeStamp(Objects.requireNonNull(coinPriceHistoryResponseEntity.getBody()), coinInfo.getSymbol(), period);
    }

    private void saveCoinHistoryInRedisSeriesTimeStamp(CoinPriceHistory coinPriceHistory, String symbol, String period) {
        log.info("Storing Coin History of {} for Time Period {} into Redis TS", symbol, period);
        List<CoinPriceHistoryExchangeRate> history = coinPriceHistory.getData().getHistory();
        history.stream()
                .filter(coinHistory -> coinHistory.getPrice() != null && coinHistory.getTimestamp() != null)
                .forEach(coinHistoryPrice -> {
                    redisTimeSeries.add(new Sample(symbol + ":" + period,
                            Sample.Value.of(Long.parseLong(coinHistoryPrice.getTimestamp()),
                                    Double.parseDouble(coinHistoryPrice.getPrice()))),
                            new TimeSeriesOptions().unCompressed().duplicatePolicy(DuplicatePolicy.LAST));
                });
        log.info("Complete: Stored Coin History of {} for Time Period {} into Redis TS", symbol, period);
    }

    public List<CoinInfo> getAllCoinsFromRedisJSON() {
        CoinData coinData = redisJSON.get(COINS_KEY, CoinData.class,
                new GetArgs().path(".data").indent("\t").newLine("\n").space(" "));
        return coinData.getCoins();
    }

    private void saveCoinsInRedisJSON(Coins coins) {
        this.redisJSON.set(COINS_KEY, SetArgs.Builder.create(".", GsonUtils.toJson(coins)));
    }

    public List<Sample.Value> getCoinHistoryPerPeriod(String coin, String period) {
        log.info("Getting Coin history from RedisTimeSeries for {} coin per {} period", coin, period);
        Map<String, Object> tsInfo = this.getTSFromRedisTSForSymbol(coin, period);
        long firstTimeStamp = Long.parseLong(tsInfo.get("firstTimestamp").toString());
        long lastTimeStamp = Long.parseLong(tsInfo.get("lastTimestamp").toString());
        return this.getCoinHistoryFromTSForPeriod(coin, period, firstTimeStamp,
                lastTimeStamp);
    }

    private List<Sample.Value> getCoinHistoryFromTSForPeriod(String coin, String period, long firstTimeStamp, long lostTimeStamp) {
        String key = coin + ":" + period;
        return this.redisTimeSeries.range(key, firstTimeStamp, lostTimeStamp);
    }

    private Map<String, Object> getTSFromRedisTSForSymbol(String coin, String period) {
        return redisTimeSeries.info(coin + ":" + period);
    }
}
