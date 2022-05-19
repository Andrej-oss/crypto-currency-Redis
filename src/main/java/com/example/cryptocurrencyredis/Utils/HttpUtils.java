package com.example.cryptocurrencyredis.Utils;

import java.util.Collections;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class HttpUtils {

    private final static String apiHost = "coinranking1.p.rapidapi.com";
    private final static String apiKey = "33fa0f0be7mshbfbf0b247f3dadep157b67jsn4df56dc89b90";

    public static HttpEntity<String> getHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("X-RapidAPI-Host", apiHost);
        headers.set("X-RapidAPI-Key", apiKey);
        return new HttpEntity<>(null, headers);
    }
}
