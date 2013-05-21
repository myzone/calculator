package com.myzone.calculator.model;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.myzone.utils.BigFraction;
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

    private static final BigFraction MAX_THRESHOLD = BigFraction.TEN.pow(250);
    private static final BigFraction MIN_THRESHOLD = BigFraction.TEN.pow(-250);

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

    private volatile BigFraction lArg;
    private volatile BigFraction rArg;
    private volatile BigFraction memory;
    private volatile String displayText;
    private volatile BigFraction displayData;
    private volatile Operation operation;

    public CalculatorModel() {
        lock = new ReentrantLock(true);
        activeSessions = new ThreadLocal<>();

        lArg = BigFraction.ZERO;
        rArg = BigFraction.ZERO;

        memory = BigFraction.ZERO;

        displayText = "0";
        displayData = BigFraction.ZERO;

        operation = null;
    }

    @NotNull
    public Session createSession() {
        if (activeSessions.get() != null) {
            return proxySession;
        }

        return new BlockingSession();
    }

    protected BigFraction getlArg() {
        return lArg;
    }

    protected void setlArg(BigFraction lArg) {
        this.lArg = lArg;
    }

    protected BigFraction getrArg() {
        return rArg;
    }

    protected void setrArg(BigFraction rArg) {
        this.rArg = rArg;
    }

    protected BigFraction getMemory() {
        return memory;
    }

    protected void setMemory(BigFraction memory) {
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

    protected BigFraction getDisplayData() {
        return displayData;
    }

    protected void setDisplayData(BigFraction displayData) {
        this.displayData = displayData;
    }

    protected Operation getOperation() {
        return operation;
    }

    protected void setOperation(Operation operation) {
        this.operation = operation;
    }

    public static interface Session extends AutoCloseable {

        BigFraction getlArg();

        void setlArg(BigFraction lArg);

        BigFraction getrArg();

        void setrArg(BigFraction rArg);

        public BigFraction getMemory();

        public void setMemory(BigFraction memory);

        public String getDisplayText();

        void setDisplayText(String displayText);

        BigFraction getDisplayData();

        void setDisplayData(BigFraction displayData);

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
        public BigFraction getlArg() {
            return CalculatorModel.this.getlArg();
        }

        @Override
        public void setlArg(BigFraction lArg) {
            CalculatorModel.this.setlArg(lArg);
        }

        @Override
        public BigFraction getrArg() {
            return CalculatorModel.this.getrArg();
        }

        @Override
        public void setrArg(BigFraction rArg) {
            CalculatorModel.this.setrArg(rArg);
        }

        @Override
        public BigFraction getMemory() {
            return CalculatorModel.this.getMemory();
        }

        @Override
        public void setMemory(BigFraction memory) {
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
        public BigFraction getDisplayData() {
            return CalculatorModel.this.getDisplayData();
        }

        @Override
        public void setDisplayData(BigFraction displayData) {
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

            return id == that.id;
        }

        @Override
        public int hashCode() {
            return (int) (id ^ (id >>> 32));
        }

    }

    public static enum Operation {
        ADD {
            @Override
            public strictfp BigFraction evaluate(BigFraction lArg, BigFraction rArg) {
                BigFraction result = lArg.add(rArg);

                if (!isValid(result))
                    throw new ArithmeticException();

                return result;
            }
        },
        SUBTRACT {
            @Override
            public strictfp BigFraction evaluate(BigFraction lArg, BigFraction rArg) {
                BigFraction result = lArg.subtract(rArg);

                if (!isValid(result))
                    throw new ArithmeticException();

                return result;
            }
        },
        MULTIPLY {
            @Override
            public strictfp BigFraction evaluate(BigFraction lArg, BigFraction rArg) {
                BigFraction result = lArg.multiply(rArg);

                if (!isValid(result))
                    throw new ArithmeticException();

                return result;
            }
        },
        DIVIDE {
            @Override
            public strictfp BigFraction evaluate(BigFraction lArg, BigFraction rArg) {
                BigFraction result = lArg.divide(rArg);

                if (!isValid(result))
                    throw new ArithmeticException();

                return result;
            }
        };

        private static final Map<Signal, Operation> signalOperationMap = ImmutableMap.
                <Signal, Operation>builder()
                .put(Signal.PLUS, ADD)
                .put(Signal.MINUS, SUBTRACT)
                .put(Signal.MULTIPLY, MULTIPLY)
                .put(Signal.DIVIDE, DIVIDE)
                .build();

        public static Operation bySignal(Signal signal) {
            return signalOperationMap.get(signal);
        }

        public abstract BigFraction evaluate(BigFraction lArg, BigFraction rArg);

        protected boolean isValid(BigFraction bigFraction) {
            if (bigFraction.abs().getNumerator().compareTo(MAX_THRESHOLD.getNumerator()) >= 0)
                return false;

            if (bigFraction.abs().getDenominator().compareTo(MIN_THRESHOLD.getDenominator()) >= 0)
                return false;

            return true;
        }
    }

}
