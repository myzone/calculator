package com.myzone.calculator.model;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * @author: myzone
 * @date: 04.02.13 12:47
 */
public class CalculatorModel {

    private volatile double lArg;
    private volatile double rArg;

    private volatile double memory;

    private volatile String displayText;
    private volatile double displayData;

    private volatile Operation operation;

    public CalculatorModel() {
        lArg = 0;
        rArg = 0;

        memory = 0;

        displayText = "0";
        displayData = 0D;

        operation = null;
    }

    public double getlArg() {
        return lArg;
    }

    public void setlArg(double lArg) {
        this.lArg = lArg;
    }

    public double getrArg() {
        return rArg;
    }

    public void setrArg(double rArg) {
        this.rArg = rArg;
    }

    public double getMemory() {
        if (!Double.isFinite(memory)) {
            throw new ArithmeticException("Memory is overflowed");
        }

        return memory;
    }

    public void setMemory(double memory) {
        this.memory = memory;
    }

    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText.substring(0, Math.min(
                17
                        + (displayText.startsWith("-") ? 1 : 0)
                        + (displayText.contains("e") ? 5 : 0)
                        + (displayText.contains(".") ? 1 : 0),
                displayText.length()
        ));
    }

    public double getDisplayData() {
        return displayData;
    }

    public void setDisplayData(double displayData) {
        this.displayData = displayData;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public static enum Operation {
        PLUS {
            @Override
            public strictfp double evaluate(double lArg, double rArg) {
                return lArg + rArg;
            }
        },
        MINUS {
            @Override
            public strictfp double evaluate(double lArg, double rArg) {
                return lArg - rArg;
            }
        },
        MULTIPLY {
            @Override
            public strictfp double evaluate(double lArg, double rArg) {
                return lArg * rArg;
            }
        },
        DIVIDE {
            @Override
            public strictfp double evaluate(double lArg, double rArg) {
                if (rArg == 0)
                    throw new ArithmeticException("Zero division");

                return lArg / rArg;
            }
        };
        private static final Map<Signal, Operation> signalOperationMap = ImmutableMap.<Signal, Operation>builder()
                .put(Signal.PLUS, PLUS)
                .put(Signal.MINUS, MINUS)
                .put(Signal.MULTIPLY, MULTIPLY)
                .put(Signal.DIVIDE, DIVIDE)
                .build();

        public static Operation bySignal(Signal signal) {
            return signalOperationMap.get(signal);
        }

        public abstract double evaluate(double lArg, double rArg);

    }

}
