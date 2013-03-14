package com.myzone.calculator.model;

import com.myzone.utils.Converter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import static java.lang.Math.*;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.repeat;

/**
 * @author: myzone
 * @date: 05.03.13 19:20
 */
public class DoubleConverter implements Converter<String, Double> {

    private final int maxLength;
    private final double maxThreshold;
    private final double minThreshold;

    private final DecimalFormat normalDecimalFormat;
    private final DecimalFormat scientificDecimalFormat;
    private final DecimalFormat bigScientificDecimalFormat;

    public DoubleConverter(int maxLength) {
        this(maxLength, Double.POSITIVE_INFINITY, 0);
    }

    public DoubleConverter(int maxLength, double maxThreshold, double minThreshold) {
        this.maxLength = maxLength;
        this.maxThreshold = maxThreshold;
        this.minThreshold = minThreshold;

        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setDecimalSeparator('.');
        decimalFormatSymbols.setExponentSeparator("e");

        normalDecimalFormat = new DecimalFormat();
        normalDecimalFormat.setDecimalSeparatorAlwaysShown(false);
        normalDecimalFormat.setDecimalFormatSymbols(decimalFormatSymbols);

        scientificDecimalFormat = new DecimalFormat();
        scientificDecimalFormat.setDecimalSeparatorAlwaysShown(true);
        scientificDecimalFormat.setDecimalFormatSymbols(decimalFormatSymbols);

        DecimalFormatSymbols bigDecimalFormatSymbols = new DecimalFormatSymbols();
        bigDecimalFormatSymbols.setDecimalSeparator('.');
        bigDecimalFormatSymbols.setExponentSeparator("e+");

        bigScientificDecimalFormat = new DecimalFormat();
        bigScientificDecimalFormat.setDecimalSeparatorAlwaysShown(true);
        bigScientificDecimalFormat.setDecimalFormatSymbols(bigDecimalFormatSymbols);
    }

    @Override
    public Double parse(String source) {
        return new BigDecimal(source).doubleValue();
    }

    @Override
    public String render(Double source) {
        DecimalFormat formatter;

        if (source == 0D || source == -0D) {
            return "0";
        } else if (abs(source) > maxThreshold) {
            formatter = bigScientificDecimalFormat;
            formatter.applyPattern(format(
                    "0.%sE00",
                    repeat("#", maxLength - 1)
            ));
            formatter.setDecimalSeparatorAlwaysShown(true);
            formatter.setRoundingMode(RoundingMode.UP);
        } else if (abs(source) < minThreshold) {
            formatter = scientificDecimalFormat;
            formatter.applyPattern(format(
                    "0.%sE00",
                    repeat("#", maxLength - 1)
            ));
            formatter.setDecimalSeparatorAlwaysShown(true);
            formatter.setRoundingMode(RoundingMode.HALF_UP);
        } else {
            formatter = normalDecimalFormat;
            String pattern = format(
                    "%s0.#%s",
                    (floor(source) == 0D ? "#" : ""),
                    repeat("#", max(
                            maxLength
                                    - (floor(source) != source ? 1 : 0) // for dot
                                    - (source < 0D ? 1 : 0) // for minus
                                    + (source.intValue() == 0 ? 1 : 0) // for additional position if digit part equals 0
                                    - Long.toString(source.longValue()).length(),
                            0
                    ))
            );
            formatter.applyPattern(pattern);
            formatter.setRoundingMode(RoundingMode.HALF_DOWN);
        }

        return formatter.format(source);
    }
}
