package com.example.cryptocurrencyredis.Utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateUtils {

    public static String convertUnixTimeToDate(Long unixTime) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", new Locale("us"));
        return Instant.ofEpochSecond(unixTime)
                .atZone(ZoneId.of("GMT"))
                .format(timeFormatter);
    }

}
