package com.myzone.calculator.model;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Block;

/**
 * @author: myzone
 * @date: 04.02.13 12:47
 */
public class CalculatorModel {

    private final ReentrantLock lock;

    private volatile double lArg;
    private volatile double rArg;

    private volatile double memory;

    private volatile String displayText;
    private volatile double displayData;

    private volatile Operation operation;

    public CalculatorModel() {
        lock = new ReentrantLock(true);

        lArg = 0;
        rArg = 0;

        memory = 0;

        displayText = "0";
        displayData = 0D;

        operation = null;
    }

    public ReentrantLock getLock() {
        return lock;
    }

    public double getlArg() {
        verifyLocked();

        return lArg;
    }

    public void setlArg(double lArg) {
        verifyLocked();

        this.lArg = lArg;
    }

    public double getrArg() {
        verifyLocked();

        return rArg;
    }

    public void setrArg(double rArg) {
        verifyLocked();

        this.rArg = rArg;
    }

    public double getMemory() {
        verifyLocked();

        if (!Double.isFinite(memory)) {
            throw new ArithmeticException("Memory is overflowed");
        }

        return memory;
    }

    public void setMemory(double memory) {
        verifyLocked();

        this.memory = memory;
    }

    public String getDisplayText() {
        verifyLocked();

        return displayText;
    }

    public void setDisplayText(String displayText) {
        verifyLocked();

        this.displayText = displayText.substring(0, Math.min(
                17
                        + (displayText.startsWith("-") ? 1 : 0)
                        + (displayText.contains("e") ? 5 : 0)
                        + (displayText.contains(".") ? 1 : 0),
                displayText.length()
        ));
    }

    public double getDisplayData() {
        verifyLocked();

        return displayData;
    }

    public void setDisplayData(double displayData) {
        verifyLocked();

        this.displayData = displayData;
    }

    public Operation getOperation() {
        verifyLocked();

        return operation;
    }

    public void setOperation(Operation operation) {
        verifyLocked();

        this.operation = operation;
    }

    protected void verifyLocked() {
        if(!lock.isLocked()) {
            throw new IllegalStateException(lock + " isn't locked");
        }
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
