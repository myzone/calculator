package com.myzone.calculator;

import com.myzone.calculator.model.DoubleConverter;
import org.junit.Before;
import org.junit.Test;

import static java.lang.Math.pow;
import static org.junit.Assert.assertEquals;

/**
 * @author : myzone
 * @date: 06.03.13 2:49
 */
public class DoubleConverterTest {

    private DoubleConverter doubleConverter;

    @Before
    public void setUp() throws Exception {
        doubleConverter = new DoubleConverter(16, pow(10D, 16D), pow(10D, -16D));
    }

    @Test
    public void testRenderNormal() {
        assertEquals("132", doubleConverter.render(132D));
        assertEquals("132.2", doubleConverter.render(132.2D));
        assertEquals("-132.2", doubleConverter.render(-132.2D));
        assertEquals("10000000000", doubleConverter.render(pow(10, 10)));
        assertEquals("1000000000000000", doubleConverter.render(pow(10, 15)));
        assertEquals("1.272792206135786", doubleConverter.render(1.2727922061357856D));
    }

    @Test
    public void testRenderBigScientific() {
        assertEquals("3.e+17", doubleConverter.render(3 * pow(10, 17)));
        assertEquals("3.2e+17", doubleConverter.render(3.2 * pow(10, 17)));
        assertEquals("3.2e+19", doubleConverter.render(320 * pow(10, 17)));
    }

    @Test
    public void testRounding() {
        assertEquals("1", doubleConverter.render(Math.sqrt(0.99999999999999D)));
        assertEquals("0.00000000000001", doubleConverter.render(0.00000000000002D / 2));
    }

    @Test
    public void testRenderSmallScientific() {
        assertEquals("3.e-17", doubleConverter.render(3 * 1 / pow(10, 17)));
    }

}
