package com.myzone.calculator.model;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Executable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.LongSupplier;

import static com.google.common.collect.Collections2.filter;
import static java.util.Arrays.asList;

/**
 * @author: myzone
 * @date: 04.02.13 12:47
 */
public class CalculatorModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(CalculatorModel.class);
    private static final Set<Executable> IGNORED_METHODS = ImmutableSet
            .<Executable>builder()
            .addAll(asList(Session.class.getConstructors()))
            .addAll(filter(asList(Session.class.getMethods()), (method) -> "close".equals(method.getName())))
            .build();

    private static final AtomicLong SESSION_COUNTER = new AtomicLong(0);

    private final Lock lock;
    private final ThreadLocal<BlockingSession> activeSessions;
    private final Session proxySession = (Session) Proxy.newProxyInstance(
            BlockingSession.class.getClassLoader(),
            new Class[]{Session.class, LongSupplier.class},
            new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    if (!IGNORED_METHODS.contains(method)) {
                        method.setAccessible(true);

                        return method.invoke(activeSessions.get(), args);
                    }

                    return null;
                }
            }
    );

    private volatile double lArg;
    private volatile double rArg;
    private volatile double memory;
    private volatile String displayText;
    private volatile double displayData;
    private volatile Operation operation;

    public CalculatorModel() {
        lock = new ReentrantLock(true);
        activeSessions = new ThreadLocal<>();

        lArg = 0;
        rArg = 0;

        memory = 0;

        displayText = "0";
        displayData = 0D;

        operation = null;
    }

    @NotNull
    public Session createSession() {
        if (activeSessions.get() != null) {
            return proxySession;
        }

        return new BlockingSession();
    }

    protected double getlArg() {
        return lArg;
    }

    protected void setlArg(double lArg) {
        this.lArg = lArg;
    }

    protected double getrArg() {
        return rArg;
    }

    protected void setrArg(double rArg) {
        this.rArg = rArg;
    }

    protected double getMemory() {
        if (!Double.isFinite(memory)) {
            throw new ArithmeticException("Memory is overflowed");
        }

        return memory;
    }

    protected void setMemory(double memory) {
        this.memory = memory;
    }

    protected String getDisplayText() {
        return displayText;
    }

    protected void setDisplayText(String displayText) {
        this.displayText = displayText.substring(0, Math.min(
                15
                        + (StringUtils.countMatches(displayText, "-"))
                        + (displayText.contains("e") ? 5 : 0)
                        + (displayText.contains(".") ? 1 : 0),
                displayText.length()
        ));
    }

    protected double getDisplayData() {
        return displayData;
    }

    protected void setDisplayData(double displayData) {
        this.displayData = displayData;
    }

    protected Operation getOperation() {
        return operation;
    }

    protected void setOperation(Operation operation) {
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
                if (rArg == 0) {
                    throw new ArithmeticException("Zero division");
                }

                return lArg / rArg;
            }
        };
        private static final Map<Signal, Operation> signalOperationMap = ImmutableMap.
                <Signal, Operation>builder()
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

    public static interface Session extends AutoCloseable {

        double getlArg();

        void setlArg(double lArg);

        double getrArg();

        void setrArg(double rArg);

        public double getMemory();

        public void setMemory(double memory);

        public String getDisplayText();

        void setDisplayText(String displayText);

        double getDisplayData();

        void setDisplayData(double displayData);

        Operation getOperation();

        void setOperation(Operation operation);

        @Override
        void close();

    }

    protected class BlockingSession implements Session {

        private final long id;

        public BlockingSession() {
            CalculatorModel.this.lock.lock();
            CalculatorModel.this.activeSessions.set(this);

            id = SESSION_COUNTER.incrementAndGet();

            LOGGER.info(
                    "Calculator model session {} has been opened with "
                            + "lArg: {}, "
                            + "rArg: {}, "
                            + "memory: {}, "
                            + "displayText: '{}', "
                            + "displayData: {}, "
                            + "operation: {}",
                    id,
                    lArg,
                    rArg,
                    memory,
                    displayText,
                    displayData,
                    operation
            );
        }

        @Override
        public double getlArg() {
            return CalculatorModel.this.getlArg();
        }

        @Override
        public void setlArg(double lArg) {
            CalculatorModel.this.setlArg(lArg);
        }

        @Override
        public double getrArg() {
            return CalculatorModel.this.getrArg();
        }

        @Override
        public void setrArg(double rArg) {
            CalculatorModel.this.setrArg(rArg);
        }

        @Override
        public double getMemory() {
            return CalculatorModel.this.getMemory();
        }

        @Override
        public void setMemory(double memory) {
            CalculatorModel.this.setMemory(memory);
        }

        @Override
        public String getDisplayText() {
            return CalculatorModel.this.getDisplayText();
        }

        @Override
        public void setDisplayText(String displayText) {
            CalculatorModel.this.setDisplayText(displayText);
        }

        @Override
        public double getDisplayData() {
            return CalculatorModel.this.getDisplayData();
        }

        @Override
        public void setDisplayData(double displayData) {
            CalculatorModel.this.setDisplayData(displayData);
        }

        @Override
        public Operation getOperation() {
            return CalculatorModel.this.getOperation();
        }

        @Override
        public void setOperation(Operation operation) {
            CalculatorModel.this.setOperation(operation);
        }

        @Override
        public void close() {
            LOGGER.info(
                    "Calculator model session {} has been closed with "
                            + "lArg: {}, "
                            + "rArg: {}, "
                            + "memory: {}, "
                            + "displayText: '{}', "
                            + "displayData: {}, "
                            + "operation: {}",
                    id,
                    lArg,
                    rArg,
                    memory,
                    displayText,
                    displayData,
                    operation
            );

            CalculatorModel.this.activeSessions.remove();
            CalculatorModel.this.lock.unlock();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            BlockingSession that = (BlockingSession) o;

            if (id != that.id) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return (int) (id ^ (id >>> 32));
        }

    }

}
