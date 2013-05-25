package com.myzone.utils;

import com.myzone.utils.math.BigFraction;

/**
 * @author: myzone
 * @date: 11.05.13 4:
 */
public class BigFractionConverter implements Converter<String, BigFraction> {

    protected final int maxLength;
    protected final Converter<String, Double> doubleConverter;

    public BigFractionConverter(int maxLength) {
        this(maxLength, Double.POSITIVE_INFINITY, 0);
    }

    public BigFractionConverter(int maxLength, double maxThreshold, double minThreshold) {
        this.maxLength = maxLength;
        this.doubleConverter = new DoubleConverter(maxLength, maxThreshold, minThreshold);
    }

    @Override
    public BigFraction parse(String source) {
        if (source == null)
            return null;

        return BigFraction.valueOf(source);
    }

    @Override
    public String render(BigFraction source) {
        if (source == null)
            return null;

        return doubleConverter.render(source.toBigDecimal(maxLength + 1).doubleValue());
    }
}
