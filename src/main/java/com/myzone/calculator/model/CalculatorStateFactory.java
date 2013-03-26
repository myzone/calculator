package com.myzone.calculator.model;

import com.myzone.calculator.view.CalculatorView;
import com.myzone.utils.Converter;
import com.myzone.utils.statemachine.State;
import org.jetbrains.annotations.NotNull;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

/**
 * @author: myzone
 * @date: 03.02.13 23:48
 */
public class CalculatorStateFactory implements State.Factory<Signal> {

    protected static final Converter<String, Double> DOUBLE_CONVERTER = new DoubleConverter(16, pow(10D, 16D), pow(10D, -16D));

    private static double parseDouble(String s) {
        try {
            return DOUBLE_CONVERTER.parse(s);
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    private static String renderDouble(double d) {
        if (Double.isNaN(d) || Double.isInfinite(d)) {
            throw new ArithmeticException("Double is NaN");
        }

        return DOUBLE_CONVERTER.render(d);
    }


    protected CalculatorModel model;
    protected CalculatorView view;
    protected State<Signal> initialState;
    protected State<Signal> afterDigitInLArg;
    protected State<Signal> afterDotInLArg;
    protected State<Signal> afterSingSelection;
    protected State<Signal> afterChangeInRArg;
    protected State<Signal> afterDigitInRArg;
    protected State<Signal> afterDotInRArg;
    protected State<Signal> afterEvaluation;
    protected State<Signal> errorInputState;

    public CalculatorStateFactory(@NotNull CalculatorModel model, @NotNull CalculatorView view) {
        this.model = model;
        this.view = view;

        initialState = createInitialState();
        afterDigitInLArg = createAfterDigitInLArg();
        afterDotInLArg = createAfterDotInLArg();
        afterSingSelection = createAfterSingSelection();
        afterChangeInRArg = createAfterChangeInRArg();
        afterDigitInRArg = createAfterDigitInRArg();
        afterDotInRArg = createAfterDotInRArg();
        afterEvaluation = createAfterEvaluation();
        errorInputState = createErrorInputState();
    }

    @Override
    @NotNull
    public State<Signal> getStartState() {
        return initialState;
    }

    @Override
    @NotNull
    public State<Signal> getEndState() {
        // this state machine have not any end state, so it's fake end state
        return new State<Signal>() {
            @NotNull
            @Override
            public State<Signal> react(@NotNull Signal signal) {
                return this;
            }
        };
    }

    private State<Signal> createInitialState() {
        return new LArgState("initialState") {
            @NotNull
            @Override
            public State<Signal> react(@NotNull Signal signal) {
                model.getLock().lock();
                try {
                    switch (signal) {
                        case DIGIT_0:
                            if ("0".equals(model.getDisplayText()))
                                return initialState;
                        case DIGIT_1:
                        case DIGIT_2:
                        case DIGIT_3:
                        case DIGIT_4:
                        case DIGIT_5:
                        case DIGIT_6:
                        case DIGIT_7:
                        case DIGIT_8:
                        case DIGIT_9:
                            model.setDisplayText(signal.getRepresentation());
                            model.setDisplayData(parseDouble(model.getDisplayText()));
                            view.invalidate();
                            return afterDigitInLArg;

                        case DOT:
                            model.setDisplayText(model.getDisplayText() + signal.getRepresentation());
                            model.setDisplayData(parseDouble(model.getDisplayText()));
                            view.invalidate();
                            return afterDotInLArg;

                        case PLUS:
                        case MINUS:
                        case MULTIPLY:
                        case DIVIDE:
                            model.setlArg(parseDouble(model.getDisplayText()));
                            model.setDisplayData(model.getlArg());
                            model.setDisplayText(renderDouble(model.getDisplayData()));
                            model.setOperation(CalculatorModel.Operation.bySignal(signal));
                            return afterSingSelection;

                        case EVALUATE:
                            if (model.getOperation() != null) {
                                try {
                                    model.setlArg(model.getOperation().evaluate(model.getDisplayData(), model.getrArg()));
                                    model.setDisplayData(model.getlArg());
                                    model.setDisplayText(renderDouble(model.getDisplayData()));
                                    view.invalidate();
                                    return afterEvaluation;
                                } catch (Exception e) {
                                    model.setDisplayText("ERR");
                                    view.invalidate();
                                    return errorInputState;
                                }
                            }

                            model.setDisplayText(renderDouble(model.getDisplayData()));
                            model.setDisplayData(parseDouble(model.getDisplayText()));
                            return initialState;

                        case BACK_SPACE:
                            model.setDisplayText(model.getDisplayText().substring(0, model.getDisplayText().length() - 1));
                            if (model.getDisplayText().isEmpty() || "-".equals(model.getDisplayText())) {
                                model.setDisplayText("0");
                            }
                            model.setDisplayData(parseDouble(model.getDisplayText()));
                            view.invalidate();
                            return initialState;
                    }

                    return super.react(signal);
                } finally {
                    model.getLock().unlock();
                }
            }
        };
    }

    private State<Signal> createAfterDigitInLArg() {
        return new LArgState("afterDigitInLArg") {
            @NotNull
            @Override
            public State<Signal> react(@NotNull Signal signal) {
                model.getLock().lock();
                try {
                    switch (signal) {
                        case DIGIT_0:
                        case DIGIT_1:
                        case DIGIT_2:
                        case DIGIT_3:
                        case DIGIT_4:
                        case DIGIT_5:
                        case DIGIT_6:
                        case DIGIT_7:
                        case DIGIT_8:
                        case DIGIT_9:
                            model.setDisplayText(model.getDisplayText() + signal.getRepresentation());
                            model.setDisplayData(parseDouble(model.getDisplayText()));
                            view.invalidate();
                            return afterDigitInLArg;

                        case DOT:
                            model.setDisplayText(model.getDisplayText() + signal.getRepresentation());
                            model.setDisplayData(parseDouble(model.getDisplayText()));
                            view.invalidate();
                            return afterDotInLArg;

                        case PLUS:
                        case MINUS:
                        case MULTIPLY:
                        case DIVIDE:
                            model.setlArg(model.getDisplayData());
                            model.setDisplayText(renderDouble(model.getDisplayData()));
                            model.setOperation(CalculatorModel.Operation.bySignal(signal));
                            return afterSingSelection;

                        case EVALUATE:
                            if (model.getOperation() != null) {
                                try {
                                    model.setlArg(model.getOperation().evaluate(model.getDisplayData(), model.getrArg()));
                                    model.setDisplayData(model.getlArg());
                                    model.setDisplayText(renderDouble(model.getDisplayData()));
                                    view.invalidate();
                                    return afterEvaluation;
                                } catch (Exception e) {
                                    model.setDisplayText("ERR");
                                    view.invalidate();
                                    return errorInputState;
                                }
                            }

                            model.setDisplayText(renderDouble(model.getDisplayData()));
                            model.setDisplayData(parseDouble(model.getDisplayText()));
                            view.invalidate();
                            return initialState;

                        case BACK_SPACE:
                            model.setDisplayText(model.getDisplayText().substring(0, model.getDisplayText().length() - 1));
                            if (model.getDisplayText().isEmpty() || "-".equals(model.getDisplayText()) || "-0".equals(model.getDisplayText())) {
                                model.setDisplayText("0");
                            }
                            model.setDisplayData(parseDouble(model.getDisplayText()));
                            view.invalidate();
                            return model.getDisplayText().length() < 3 ? initialState : afterDigitInLArg;
                    }

                    return super.react(signal);
                } finally {
                    model.getLock().unlock();
                }
            }
        };
    }

    private State<Signal> createAfterDotInLArg() {
        return new LArgState("afterDotInLArg") {
            @NotNull
            @Override
            public State<Signal> react(@NotNull Signal signal) {
                model.getLock().lock();
                try {
                    switch (signal) {
                        case DIGIT_0:
                        case DIGIT_1:
                        case DIGIT_2:
                        case DIGIT_3:
                        case DIGIT_4:
                        case DIGIT_5:
                        case DIGIT_6:
                        case DIGIT_7:
                        case DIGIT_8:
                        case DIGIT_9:
                            model.setDisplayText(model.getDisplayText() + signal.getRepresentation());
                            model.setDisplayData(parseDouble(model.getDisplayText()));
                            view.invalidate();
                            return afterDotInLArg;

                        case DOT:
                            return afterDotInLArg;

                        case PLUS:
                        case MINUS:
                        case MULTIPLY:
                        case DIVIDE:
                            model.setlArg(model.getDisplayData());
                            model.setDisplayText(renderDouble(model.getlArg()));
                            model.setOperation(CalculatorModel.Operation.bySignal(signal));
                            return afterSingSelection;

                        case EVALUATE:
                            if (model.getOperation() != null) {
                                try {
                                    model.setlArg(model.getOperation().evaluate(model.getDisplayData(), model.getrArg()));
                                    model.setDisplayText(renderDouble(model.getDisplayData()));
                                    view.invalidate();
                                    return afterEvaluation;
                                } catch (Exception e) {
                                    model.setDisplayText("ERR");
                                    view.invalidate();
                                    return errorInputState;
                                }
                            }

                            model.setDisplayText(renderDouble(model.getDisplayData()));
                            model.setDisplayData(parseDouble(model.getDisplayText()));
                            return initialState;

                        case BACK_SPACE:
                            if (model.getDisplayText().charAt(model.getDisplayText().length() - 1) == '.') {
                                model.setDisplayText(model.getDisplayText().substring(0, model.getDisplayText().length() - 1));
                                if (model.getDisplayText().isEmpty() || "-".equals(model.getDisplayText()) || "-0".equals(model.getDisplayText())) {
                                    model.setDisplayText("0");
                                }
                                model.setDisplayData(parseDouble(model.getDisplayText()));
                                view.invalidate();
                                return model.getDisplayData() != 0 ? afterDigitInLArg : initialState;
                            }
                            model.setDisplayText(model.getDisplayText().substring(0, model.getDisplayText().length() - 1));
                            if ("0".equals(model.getDisplayText()) || "-0".equals(model.getDisplayText())) {
                                model.setDisplayText("0");
                            }
                            model.setDisplayData(parseDouble(model.getDisplayText()));
                            view.invalidate();
                            return afterDotInLArg;

                        case REVERSE:
                            if (!"0".equals(model.getDisplayText())) {
                                if (model.getDisplayText().startsWith("-")) {
                                    model.setDisplayText(model.getDisplayText().substring(1));
                                } else {
                                    model.setDisplayText("-" + model.getDisplayText());
                                }
                            }
                            model.setDisplayData(parseDouble(model.getDisplayText()));
                            view.invalidate();
                            return afterDotInLArg;
                    }

                    return super.react(signal);
                } finally {
                    model.getLock().unlock();
                }
            }
        };
    }

    private State<Signal> createAfterSingSelection() {
        return new RArgState("afterSingSelection") {
            @NotNull
            @Override
            public State<Signal> react(@NotNull Signal signal) {
                model.getLock().lock();
                try {
                    switch (signal) {
                        case DIGIT_0:
                            model.setDisplayText(signal.getRepresentation());
                            model.setDisplayData(parseDouble(model.getDisplayText()));
                            view.invalidate();
                            return afterSingSelection;

                        case DIGIT_1:
                        case DIGIT_2:
                        case DIGIT_3:
                        case DIGIT_4:
                        case DIGIT_5:
                        case DIGIT_6:
                        case DIGIT_7:
                        case DIGIT_8:
                        case DIGIT_9:
                            model.setDisplayText(signal.getRepresentation());
                            model.setDisplayData(parseDouble(model.getDisplayText()));
                            view.invalidate();
                            return afterDigitInRArg;

                        case DOT:
                            model.setDisplayText("0" + signal.getRepresentation());
                            model.setDisplayData(parseDouble(model.getDisplayText()));
                            view.invalidate();
                            return afterDotInRArg;

                        case PLUS:
                        case MINUS:
                        case MULTIPLY:
                        case DIVIDE:
                            model.setOperation(CalculatorModel.Operation.bySignal(signal));
                            return afterSingSelection;

                        case EVALUATE:
                            try {
                                model.setrArg(model.getDisplayData());
                                model.setlArg(model.getOperation().evaluate(model.getlArg(), model.getrArg()));
                                model.setDisplayData(model.getlArg());
                                model.setDisplayText(renderDouble(model.getDisplayData()));
                                view.invalidate();
                                return afterEvaluation;
                            } catch (Exception e) {
                                model.setDisplayText("ERR");
                                view.invalidate();
                                return errorInputState;
                            }

                        case BACK_SPACE:
                            model.setDisplayText(model.getDisplayText().substring(0, model.getDisplayText().length() - 1));
                            if (model.getDisplayText().isEmpty() || "-".equals(model.getDisplayText()) || "-0".equals(model.getDisplayText())) {
                                model.setDisplayText("0");
                            }
                            model.setDisplayData(parseDouble(model.getDisplayText()));
                            view.invalidate();
                            return afterSingSelection;
                    }

                    return super.react(signal);
                } finally {
                    model.getLock().unlock();
                }
            }
        };
    }

    private State<Signal> createAfterChangeInRArg() {
        return new RArgState("afterChangeInRArg") {
            @NotNull
            @Override
            public State<Signal> react(@NotNull Signal signal) {
                model.getLock().lock();
                try {
                    switch (signal) {
                        case DIGIT_0:
                            model.setDisplayText(signal.getRepresentation());
                            model.setDisplayData(parseDouble(model.getDisplayText()));
                            view.invalidate();
                            return afterSingSelection;

                        case DIGIT_1:
                        case DIGIT_2:
                        case DIGIT_3:
                        case DIGIT_4:
                        case DIGIT_5:
                        case DIGIT_6:
                        case DIGIT_7:
                        case DIGIT_8:
                        case DIGIT_9:
                            model.setDisplayText(signal.getRepresentation());
                            model.setDisplayData(parseDouble(model.getDisplayText()));
                            view.invalidate();
                            return afterDigitInRArg;

                        case DOT:
                            model.setDisplayText("0" + signal.getRepresentation());
                            model.setDisplayData(parseDouble(model.getDisplayText()));
                            view.invalidate();
                            return afterDotInRArg;

                        case PLUS:
                        case MINUS:
                        case MULTIPLY:
                        case DIVIDE:
                            model.setlArg(model.getOperation().evaluate(model.getlArg(), model.getDisplayData()));
                            model.setDisplayData(model.getlArg());
                            model.setDisplayText(renderDouble(model.getDisplayData()));
                            model.setOperation(CalculatorModel.Operation.bySignal(signal));
                            view.invalidate();
                            return afterSingSelection;

                        case EVALUATE:
                            try {
                                model.setrArg(model.getDisplayData());
                                model.setlArg(model.getOperation().evaluate(model.getlArg(), model.getrArg()));
                                model.setDisplayData(model.getlArg());
                                model.setDisplayText(renderDouble(model.getDisplayData()));
                                view.invalidate();
                                return afterEvaluation;
                            } catch (Exception e) {
                                model.setDisplayText("ERR");
                                view.invalidate();
                                return errorInputState;
                            }

                        case BACK_SPACE:
                            model.setDisplayText(model.getDisplayText().substring(0, model.getDisplayText().length() - 1));
                            if (model.getDisplayText().isEmpty() || "-".equals(model.getDisplayText()) || "-0".equals(model.getDisplayText())) {
                                model.setDisplayText("0");
                            }
                            model.setDisplayData(parseDouble(model.getDisplayText()));
                            view.invalidate();
                            return afterSingSelection;
                    }

                    return super.react(signal);
                } finally {
                    model.getLock().unlock();
                }
            }
        };
    }

    private State<Signal> createAfterDigitInRArg() {
        return new RArgState("afterDigitInRArg") {
            @NotNull
            @Override
            public State<Signal> react(@NotNull Signal signal) {
                model.getLock().lock();
                try {
                    switch (signal) {
                        case DIGIT_0:
                        case DIGIT_1:
                        case DIGIT_2:
                        case DIGIT_3:
                        case DIGIT_4:
                        case DIGIT_5:
                        case DIGIT_6:
                        case DIGIT_7:
                        case DIGIT_8:
                        case DIGIT_9:
                            model.setDisplayText(model.getDisplayText() + signal.getRepresentation());
                            model.setDisplayData(parseDouble(model.getDisplayText()));
                            view.invalidate();
                            return afterDigitInRArg;

                        case DOT:
                            model.setDisplayText(model.getDisplayText() + signal.getRepresentation());
                            model.setDisplayData(parseDouble(model.getDisplayText()));
                            view.invalidate();
                            return afterDotInRArg;

                        case PLUS:
                        case MINUS:
                        case MULTIPLY:
                        case DIVIDE:
                            try {
                                model.setlArg(model.getOperation().evaluate(model.getlArg(), model.getDisplayData()));
                                model.setDisplayData(model.getlArg());
                                model.setDisplayText(renderDouble(model.getDisplayData()));
                                model.setOperation(CalculatorModel.Operation.bySignal(signal));
                                view.invalidate();
                                return afterSingSelection;
                            } catch (Exception e) {
                                model.setDisplayText("ERR");
                                view.invalidate();
                                return errorInputState;
                            }

                        case EVALUATE:
                            try {
                                model.setrArg(model.getDisplayData());
                                model.setlArg(model.getOperation().evaluate(model.getlArg(), model.getrArg()));
                            } catch (Exception e) {
                                model.setDisplayText("ERR");
                                view.invalidate();
                                return errorInputState;
                            }
                            model.setDisplayData(model.getlArg());
                            model.setDisplayText(renderDouble(model.getDisplayData()));
                            view.invalidate();
                            return afterEvaluation;

                        case BACK_SPACE:
                            model.setDisplayText(model.getDisplayText().substring(0, model.getDisplayText().length() - 1));
                            if (model.getDisplayText().isEmpty() || "-".equals(model.getDisplayText()) || "-0".equals(model.getDisplayText())) {
                                model.setDisplayText("0");
                            }
                            model.setDisplayData(parseDouble(model.getDisplayText()));
                            view.invalidate();
                            return model.getDisplayText().length() < 3 ? initialState : afterDigitInLArg;
                    }

                    return super.react(signal);
                } finally {
                    model.getLock().unlock();
                }
            }
        };
    }

    private State<Signal> createAfterDotInRArg() {
        return new RArgState("afterDotInRArg") {
            @NotNull
            @Override
            public State<Signal> react(@NotNull Signal signal) {
                model.getLock().lock();
                try {
                    switch (signal) {
                        case DIGIT_0:
                        case DIGIT_1:
                        case DIGIT_2:
                        case DIGIT_3:
                        case DIGIT_4:
                        case DIGIT_5:
                        case DIGIT_6:
                        case DIGIT_7:
                        case DIGIT_8:
                        case DIGIT_9:
                            model.setDisplayText(model.getDisplayText() + signal.getRepresentation());
                            model.setDisplayData(parseDouble(model.getDisplayText()));
                            view.invalidate();
                            return afterDotInRArg;

                        case DOT:
                            return afterDotInRArg;

                        case PLUS:
                        case MINUS:
                        case MULTIPLY:
                        case DIVIDE:
                            try {
                                model.setlArg(model.getOperation().evaluate(model.getlArg(), model.getDisplayData()));
                                model.setDisplayData(model.getlArg());
                                model.setDisplayText(renderDouble(model.getDisplayData()));
                                model.setOperation(CalculatorModel.Operation.bySignal(signal));
                                view.invalidate();
                                return afterSingSelection;
                            } catch (Exception e) {
                                model.setDisplayText("ERR");
                                view.invalidate();
                                return errorInputState;
                            }

                        case EVALUATE:
                            try {
                                model.setrArg(model.getDisplayData());
                                model.setlArg(model.getOperation().evaluate(model.getlArg(), model.getrArg()));
                                model.setDisplayData(model.getlArg());
                                model.setDisplayText(renderDouble(model.getDisplayData()));
                                view.invalidate();
                                return afterEvaluation;
                            } catch (Exception e) {
                                model.setDisplayText("ERR");
                                view.invalidate();
                                return errorInputState;
                            }

                        case BACK_SPACE:
                            if (model.getDisplayText().charAt(model.getDisplayText().length() - 1) == '.') {
                                model.setDisplayText(model.getDisplayText().substring(0, model.getDisplayText().length() - 1));
                                if (model.getDisplayText().isEmpty() || "-".equals(model.getDisplayText()) || "-0".equals(model.getDisplayText())) {
                                    model.setDisplayText("0");
                                }
                                model.setDisplayData(parseDouble(model.getDisplayText()));
                                view.invalidate();
                                return model.getDisplayData() != 0 ? afterDigitInRArg : afterChangeInRArg;
                            }
                            model.setDisplayText(model.getDisplayText().substring(0, model.getDisplayText().length() - 1));
                            if (model.getDisplayText().isEmpty() || "-".equals(model.getDisplayText()) || "-0".equals(model.getDisplayText())) {
                                model.setDisplayText("0");
                            }
                            model.setDisplayData(parseDouble(model.getDisplayText()));
                            view.invalidate();
                            return afterDotInRArg;

                        case REVERSE:
                            if (!"0".equals(model.getDisplayText())) {
                                if (model.getDisplayText().startsWith("-")) {
                                    model.setDisplayText(model.getDisplayText().substring(1));
                                } else {
                                    model.setDisplayText("-" + model.getDisplayText());
                                }
                            }
                            view.invalidate();
                            return afterDotInRArg;
                    }

                    return super.react(signal);
                } finally {
                    model.getLock().unlock();
                }
            }
        };
    }

    private State<Signal> createAfterEvaluation() {
        return new LArgState("afterEvaluation") {
            @NotNull
            @Override
            public State<Signal> react(@NotNull Signal signal) {
                model.getLock().lock();
                try {
                    switch (signal) {
                        case DIGIT_0:
                        case DIGIT_1:
                        case DIGIT_2:
                        case DIGIT_3:
                        case DIGIT_4:
                        case DIGIT_5:
                        case DIGIT_6:
                        case DIGIT_7:
                        case DIGIT_8:
                        case DIGIT_9:
                            model.setDisplayText(signal.getRepresentation());
                            model.setDisplayData(parseDouble(model.getDisplayText()));
                            view.invalidate();
                            return afterDigitInLArg;

                        case DOT:
                            model.setDisplayText("0" + signal.getRepresentation());
                            model.setDisplayData(parseDouble(model.getDisplayText()));
                            view.invalidate();
                            return afterDotInLArg;

                        case PLUS:
                        case MINUS:
                        case MULTIPLY:
                        case DIVIDE:
                            model.setlArg(parseDouble(model.getDisplayText()));
                            model.setOperation(CalculatorModel.Operation.bySignal(signal));
                            return afterSingSelection;

                        case EVALUATE:
                            try {
                                model.setDisplayData(model.getOperation().evaluate(model.getDisplayData(), model.getrArg()));
                                model.setDisplayText(renderDouble(model.getDisplayData()));
                                view.invalidate();
                                return afterEvaluation;
                            } catch (Exception e) {
                                model.setDisplayText("ERR");
                                view.invalidate();
                                return errorInputState;
                            }

                        case BACK_SPACE:
                            return afterEvaluation;
                    }

                    return super.react(signal);
                } finally {
                    model.getLock().unlock();
                }
            }
        };
    }

    private State<Signal> createErrorInputState() {
        return new AbstractState("errorInputState") {
            @NotNull
            @Override
            public State<Signal> react(@NotNull Signal signal) {
                model.getLock().lock();
                try {
                    switch (signal) {
                        case CLEAR:
                        case CLEAR_EVALUATION:
                            model.setlArg(0);
                            model.setrArg(0);
                            model.setDisplayText("0");
                            view.invalidate();
                            return initialState;

                        default:
                            return errorInputState;
                    }

                } finally {
                    model.getLock().unlock();
                }
            }
        };
    }

    protected abstract class AbstractState implements State<Signal> {

        protected final String name;

        public AbstractState(String name) {
            this.name = name;
        }

        @NotNull
        @Override
        public strictfp State<Signal> react(@NotNull Signal signal) {
            model.getLock().lock();
            try {
                switch (signal) {
                    case CLEAR:
                        model.setlArg(0);
                        model.setrArg(0);
                        model.setDisplayText("0");
                        model.setDisplayData(0);
                        view.invalidate();
                        return initialState;

                    case MEMORY_CLEAR:
                        model.setMemory(0);
                        view.invalidate();
                        return this;

                    case MEMORY_STORE:
                        model.setMemory(model.getDisplayData());
                        view.invalidate();
                        return this;

                    case MEMORY_PLUS:
                        model.setMemory(model.getMemory() + model.getDisplayData());
                        view.invalidate();
                        return this;

                    case MEMORY_MINUS:
                        model.setMemory(model.getMemory() - model.getDisplayData());
                        view.invalidate();
                        return this;
                }

                throw new IllegalStateException(signal + " was not processed");
            } finally {
                model.getLock().unlock();
            }
        }

        @Override
        public String toString() {
            return name;
        }
    }

    protected class LArgState extends AbstractState {

        public LArgState(String name) {
            super(name);
        }

        @NotNull
        @Override
        public State<Signal> react(@NotNull Signal signal) {
            model.getLock().lock();
            try {
                switch (signal) {
                    case PERCENT:
                        model.setDisplayData(model.getlArg() * model.getDisplayData() / 100);
                        model.setDisplayText(renderDouble(model.getDisplayData()));
                        view.invalidate();
                        return initialState;

                    case SQUARE_ROOT:
                        try {
                            if (model.getDisplayText().startsWith("-")) {
                                throw new ArithmeticException("Negative square root");
                            }

                            model.setDisplayData(sqrt(model.getDisplayData()));
                            model.setDisplayText(renderDouble(model.getDisplayData()));
                            view.invalidate();
                            return initialState;
                        } catch (Exception e) {
                            model.setDisplayText("ERR");
                            view.invalidate();
                            return errorInputState;
                        }

                    case REVERSE:
                        if (!"0".equals(model.getDisplayText())) {
                            if (model.getDisplayText().startsWith("-")) {
                                model.setDisplayText(model.getDisplayText().substring(1));
                            } else {
                                model.setDisplayText("-" + model.getDisplayText());
                            }
                        }
                        model.setDisplayData(parseDouble(model.getDisplayText()));
                        view.invalidate();
                        return initialState;

                    case INVERSE:
                        try {
                            model.setDisplayData(1D / model.getDisplayData());
                            model.setDisplayText(renderDouble(model.getDisplayData()));
                            view.invalidate();
                            return initialState;
                        } catch (Exception e) {
                            model.setDisplayText("ERR");
                            view.invalidate();
                            return errorInputState;
                        }

                    case CLEAR_EVALUATION:
                        model.setlArg(0);
                        model.setDisplayText("0");
                        model.setDisplayData(0);
                        view.invalidate();
                        return initialState;

                    case MEMORY_RESTORE:
                        try {
                            model.setDisplayData(model.getMemory());
                            model.setDisplayText(renderDouble(model.getDisplayData()));
                            view.invalidate();
                            return initialState;
                        } catch (Exception e) {
                            model.setDisplayText("ERR");
                            view.invalidate();
                            return errorInputState;
                        }
                }

                return super.react(signal);
            } finally {
                model.getLock().unlock();
            }
        }
    }

    protected class RArgState extends AbstractState {

        public RArgState(String name) {
            super(name);
        }

        @NotNull
        @Override
        public State<Signal> react(@NotNull Signal signal) {
            model.getLock().lock();
            try {
                switch (signal) {
                    case PERCENT:
                        model.setDisplayData(model.getlArg() * model.getDisplayData() / 100);
                        model.setDisplayText(renderDouble(model.getDisplayData()));
                        view.invalidate();
                        return afterChangeInRArg;

                    case SQUARE_ROOT:
                        try {
                            if (model.getDisplayText().startsWith("-")) {
                                throw new ArithmeticException("Negative square root");
                            }

                            model.setDisplayData(sqrt(model.getDisplayData()));
                            model.setDisplayText(renderDouble(model.getDisplayData()));
                            view.invalidate();
                            return afterChangeInRArg;
                        } catch (Exception e) {
                            model.setDisplayText("ERR");
                            view.invalidate();
                            return errorInputState;
                        }

                    case REVERSE:
                        if (!"0".equals(model.getDisplayText())) {
                            if (model.getDisplayText().startsWith("-")) {
                                model.setDisplayText(model.getDisplayText().substring(1));
                            } else {
                                model.setDisplayText("-" + model.getDisplayText());
                            }
                        }
                        model.setDisplayData(parseDouble(model.getDisplayText()));
                        view.invalidate();
                        return afterChangeInRArg;

                    case INVERSE:
                        try {
                            model.setDisplayData(1D / model.getDisplayData());
                            model.setDisplayText(renderDouble(model.getDisplayData()));
                            view.invalidate();
                            return afterChangeInRArg;
                        } catch (Exception e) {
                            model.setDisplayText("ERR");
                            view.invalidate();
                            return errorInputState;
                        }

                    case CLEAR_EVALUATION:
                        model.setrArg(0);
                        model.setDisplayText("0");
                        model.setDisplayData(0);
                        view.invalidate();
                        return afterChangeInRArg;

                    case MEMORY_RESTORE:
                        try {
                            model.setDisplayData(model.getMemory());
                            model.setDisplayText(renderDouble(model.getDisplayData()));
                            view.invalidate();
                            return afterChangeInRArg;
                        } catch (Exception e) {
                            model.setDisplayText("ERR");
                            view.invalidate();
                            return errorInputState;
                        }
                }

                return super.react(signal);
            } finally {
                model.getLock().unlock();
            }
        }
    }
}