package com.example.cryptocurrencyredis.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class RoundUtils {

    public static double round(double value, int places ) {
        BigDecimal bigDecimal = BigDecimal.valueOf(value);
        bigDecimal = bigDecimal.setScale(places, RoundingMode.HALF_UP);
        return bigDecimal.doubleValue();
    }
}
