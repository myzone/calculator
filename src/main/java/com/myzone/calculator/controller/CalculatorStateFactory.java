package com.myzone.calculator.controller;

import com.myzone.calculator.model.CalculatorModel;
import com.myzone.calculator.model.Signal;
import com.myzone.calculator.view.CalculatorView;
import com.myzone.utils.BigFraction;
import com.myzone.utils.BigFractionConverter;
import com.myzone.utils.Converter;
import com.myzone.utils.statemachine.State;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

import static java.lang.Math.abs;
import static java.lang.Math.pow;

/**
 * @author: myzone
 * @date: 03.02.13 23:48
 */
public class CalculatorStateFactory implements State.Factory<Signal> {

    protected static final Converter<String, BigFraction> BIG_FRACTION_CONVERTER = new BigFractionConverter(15, pow(10D, 15D), pow(10D, -15D));

    private static BigFraction parseDouble(@NotNull String s) {
        return BIG_FRACTION_CONVERTER.parse(s);
    }

    @NotNull
    private static String renderDouble(BigFraction d) {
        // rounding hook
        if (abs(1 - d.doubleValue()) < pow(10, -10)) {
            d = BigFraction.ONE;
        }

        return BIG_FRACTION_CONVERTER.render(d).replace("(e-?)\\d\\d\\d+", "$199");
    }

    private static final Pattern firstPattern = Pattern.compile("^([0-9]+)((\\.)([0-9]*?)0*(e(\\+|\\-)[0-9]{2})?)?$");
    private static final Pattern secondPattern = Pattern.compile("(.*)\\.$");

    @NotNull
    private static String normalize(@NotNull String s) {
        String result = s;

        result = firstPattern.matcher(result).replaceAll("$1$3$4$5");
        result = secondPattern.matcher(result).replaceAll("$1");

        return result;
    }

    protected final CalculatorModel model;
    protected final CalculatorView view;

    protected final State<Signal> initialState;
    protected final State<Signal> afterDigitInLArg;
    protected final State<Signal> afterDotInLArg;
    protected final State<Signal> afterSingSelection;
    protected final State<Signal> afterChangeInRArg;
    protected final State<Signal> afterDigitInRArg;
    protected final State<Signal> afterDotInRArg;
    protected final State<Signal> afterEvaluation;
    protected final State<Signal> errorState;

    public CalculatorStateFactory(@NotNull CalculatorModel model, @NotNull CalculatorView view) {
        this.model = model;
        this.view = view;

        initialState = new InitialState();
        afterDigitInLArg = new AfterDigitInLArgState();
        afterDotInLArg = new AfterDotInLArgState();
        afterSingSelection = new AfterSingSelectionState();
        afterChangeInRArg = new AfterChangeInRArgState();
        afterDigitInRArg = new AfterDigitInRArgState();
        afterDotInRArg = new AfterDotInRArgState();
        afterEvaluation = new AfterEvaluationState();
        errorState = new ErrorState();
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

    protected class InitialState extends LArgState {

        public InitialState() {
            super("initialState");
        }

        @NotNull
        @Override
        public State<Signal> react(@NotNull Signal signal) {
            try (CalculatorModel.Session session = model.createSession()) {
                switch (signal) {
                    case DIGIT_0:
                        if ("0".equals(session.getDisplayText()))
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
                        session.setDisplayText(signal.getRepresentation());
                        session.setDisplayData(parseDouble(session.getDisplayText()));
                        view.invalidate();
                        return afterDigitInLArg;

                    case DOT:
                        session.setDisplayText("0" + signal.getRepresentation());
                        session.setDisplayData(parseDouble(session.getDisplayText()));
                        view.invalidate();
                        return afterDotInLArg;

                    case PLUS:
                    case MINUS:
                    case MULTIPLY:
                    case DIVIDE:
                        session.setlArg(parseDouble(session.getDisplayText()));
                        session.setDisplayData(session.getlArg());
                        session.setDisplayText(normalize(session.getDisplayText()));
                        session.setOperation(CalculatorModel.Operation.bySignal(signal));
                        return afterSingSelection;

                    case EVALUATE:
                        if (session.getOperation() != null) {
                            try {
                                session.setlArg(session.getOperation().evaluate(session.getDisplayData(), session.getrArg()));
                                session.setDisplayData(session.getlArg());
                                session.setDisplayText(renderDouble(session.getDisplayData()));
                                view.invalidate();
                                return afterEvaluation;
                            } catch (Exception e) {
                                session.setDisplayText("ERR");
                                view.invalidate();
                                return errorState;
                            }
                        }

                        session.setDisplayText(renderDouble(session.getDisplayData()));
                        session.setDisplayData(parseDouble(session.getDisplayText()));
                        view.invalidate();
                        return initialState;

                    case BACK_SPACE:
                        return initialState;
                }

                return super.react(signal);
            }
        }
    }

    protected class AfterDigitInLArgState extends LArgState {

        public AfterDigitInLArgState() {
            super("afterDigitInLArg");
        }

        @NotNull
        @Override
        public State<Signal> react(@NotNull Signal signal) {
            try (CalculatorModel.Session session = model.createSession()) {
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
                        session.setDisplayText(session.getDisplayText() + signal.getRepresentation());
                        session.setDisplayData(parseDouble(session.getDisplayText()));
                        view.invalidate();
                        return afterDigitInLArg;

                    case DOT:
                        session.setDisplayText(session.getDisplayText() + signal.getRepresentation());
                        session.setDisplayData(parseDouble(session.getDisplayText()));
                        view.invalidate();
                        return afterDotInLArg;

                    case PLUS:
                    case MINUS:
                    case MULTIPLY:
                    case DIVIDE:
                        session.setlArg(session.getDisplayData());
                        session.setDisplayText(normalize(session.getDisplayText()));
                        session.setOperation(CalculatorModel.Operation.bySignal(signal));
                        return afterSingSelection;

                    case EVALUATE:
                        if (session.getOperation() != null) {
                            try {
                                session.setlArg(session.getOperation().evaluate(session.getDisplayData(), session.getrArg()));
                                session.setDisplayData(session.getlArg());
                                session.setDisplayText(renderDouble(session.getDisplayData()));
                                view.invalidate();
                                return afterEvaluation;
                            } catch (Exception e) {
                                session.setDisplayText("ERR");
                                view.invalidate();
                                return errorState;
                            }
                        }

                        session.setDisplayText(renderDouble(session.getDisplayData()));
                        session.setDisplayData(parseDouble(session.getDisplayText()));
                        view.invalidate();
                        return initialState;

                    case BACK_SPACE:
                        session.setDisplayText(session.getDisplayText().substring(0, session.getDisplayText().length() - 1));
                        if (session.getDisplayText().isEmpty() || "-".equals(session.getDisplayText()) || "-0".equals(session.getDisplayText())) {
                            session.setDisplayText("0");
                        }
                        session.setDisplayData(parseDouble(session.getDisplayText()));
                        view.invalidate();
                        return "0".equals(session.getDisplayText()) ? initialState : afterDigitInLArg;
                }

                return super.react(signal);
            }
        }
    }

    protected class AfterDotInLArgState extends LArgState {

        public AfterDotInLArgState() {
            super("afterDotInLArg");
        }

        @NotNull
        @Override
        public State<Signal> react(@NotNull Signal signal) {
            try (CalculatorModel.Session session = model.createSession()) {
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
                        session.setDisplayText(session.getDisplayText() + signal.getRepresentation());
                        session.setDisplayData(parseDouble(session.getDisplayText()));
                        view.invalidate();
                        return afterDotInLArg;

                    case DOT:
                        return afterDotInLArg;

                    case PLUS:
                    case MINUS:
                    case MULTIPLY:
                    case DIVIDE:
                        session.setlArg(session.getDisplayData());
                        session.setDisplayText(normalize(session.getDisplayText()));
                        session.setOperation(CalculatorModel.Operation.bySignal(signal));
                        view.invalidate();
                        return afterSingSelection;

                    case EVALUATE:
                        if (session.getOperation() != null) {
                            try {
                                session.setlArg(session.getOperation().evaluate(session.getDisplayData(), session.getrArg()));
                                session.setDisplayText(renderDouble(session.getDisplayData()));
                                view.invalidate();
                                return afterEvaluation;
                            } catch (Exception e) {
                                session.setDisplayText("ERR");
                                view.invalidate();
                                return errorState;
                            }
                        }

                        session.setDisplayText(renderDouble(session.getDisplayData()));
                        session.setDisplayData(parseDouble(session.getDisplayText()));
                        view.invalidate();
                        return initialState;

                    case BACK_SPACE:
                        if (session.getDisplayText().charAt(session.getDisplayText().length() - 1) == '.') {
                            session.setDisplayText(session.getDisplayText().substring(0, session.getDisplayText().length() - 1));
                            if (session.getDisplayText().isEmpty() || "-".equals(session.getDisplayText()) || "-0".equals(session.getDisplayText())) {
                                session.setDisplayText("0");
                            }
                            session.setDisplayData(parseDouble(session.getDisplayText()));
                            view.invalidate();
                            return !session.getDisplayData().equals(BigFraction.ZERO) ? afterDigitInLArg : initialState;
                        }
                        session.setDisplayText(session.getDisplayText().substring(0, session.getDisplayText().length() - 1));
                        if ("0".equals(session.getDisplayText()) || "-0".equals(session.getDisplayText())) {
                            session.setDisplayText("0");
                        }
                        session.setDisplayData(parseDouble(session.getDisplayText()));
                        view.invalidate();
                        return afterDotInLArg;
                }

                return super.react(signal);
            }
        }
    }

    protected class AfterSingSelectionState extends RArgState {

        public AfterSingSelectionState() {
            super("afterSingSelection");
        }

        @NotNull
        @Override
        public State<Signal> react(@NotNull Signal signal) {
            try (CalculatorModel.Session session = model.createSession()) {
                switch (signal) {
                    case DIGIT_0:
                        session.setDisplayText(signal.getRepresentation());
                        session.setDisplayData(parseDouble(session.getDisplayText()));
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
                        session.setDisplayText(signal.getRepresentation());
                        session.setDisplayData(parseDouble(session.getDisplayText()));
                        view.invalidate();
                        return afterDigitInRArg;

                    case DOT:
                        session.setDisplayText("0" + signal.getRepresentation());
                        session.setDisplayData(parseDouble(session.getDisplayText()));
                        view.invalidate();
                        return afterDotInRArg;

                    case PLUS:
                    case MINUS:
                    case MULTIPLY:
                    case DIVIDE:
                        session.setOperation(CalculatorModel.Operation.bySignal(signal));
                        return afterSingSelection;

                    case EVALUATE:
                        try {
                            session.setrArg(session.getDisplayData());
                            session.setlArg(session.getOperation().evaluate(session.getlArg(), session.getrArg()));
                            session.setDisplayData(session.getlArg());
                            session.setDisplayText(renderDouble(session.getDisplayData()));
                            view.invalidate();
                            return afterEvaluation;
                        } catch (Exception e) {
                            session.setDisplayText("ERR");
                            view.invalidate();
                            return errorState;
                        }

                    case BACK_SPACE:
                        return afterSingSelection;
                }

                return super.react(signal);
            }
        }
    }

    protected class AfterChangeInRArgState extends RArgState {

        public AfterChangeInRArgState() {
            super("afterChangeInRArg");
        }

        @NotNull
        @Override
        public State<Signal> react(@NotNull Signal signal) {
            try (CalculatorModel.Session session = model.createSession()) {
                switch (signal) {
                    case DIGIT_0:
                        session.setDisplayText(signal.getRepresentation());
                        session.setDisplayData(parseDouble(session.getDisplayText()));
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
                        session.setDisplayText(signal.getRepresentation());
                        session.setDisplayData(parseDouble(session.getDisplayText()));
                        view.invalidate();
                        return afterDigitInRArg;

                    case DOT:
                        session.setDisplayText("0" + signal.getRepresentation());
                        session.setDisplayData(parseDouble(session.getDisplayText()));
                        view.invalidate();
                        return afterDotInRArg;

                    case PLUS:
                    case MINUS:
                    case MULTIPLY:
                    case DIVIDE:
                        session.setlArg(session.getOperation().evaluate(session.getlArg(), session.getDisplayData()));
                        session.setDisplayData(session.getlArg());
                        session.setDisplayText(renderDouble(session.getDisplayData()));
                        session.setOperation(CalculatorModel.Operation.bySignal(signal));
                        view.invalidate();
                        return afterSingSelection;

                    case EVALUATE:
                        try {
                            session.setrArg(session.getDisplayData());
                            session.setlArg(session.getOperation().evaluate(session.getlArg(), session.getrArg()));
                            session.setDisplayData(session.getlArg());
                            session.setDisplayText(renderDouble(session.getDisplayData()));
                            view.invalidate();
                            return afterEvaluation;
                        } catch (Exception e) {
                            session.setDisplayText("ERR");
                            view.invalidate();
                            return errorState;
                        }

                    case BACK_SPACE:
                        return afterChangeInRArg;
                }

                return super.react(signal);
            }
        }
    }

    protected class AfterDigitInRArgState extends RArgState {

        public AfterDigitInRArgState() {
            super("afterDigitInRArg");
        }

        @NotNull
        @Override
        public State<Signal> react(@NotNull Signal signal) {
            try (CalculatorModel.Session session = model.createSession()) {
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
                        session.setDisplayText(session.getDisplayText() + signal.getRepresentation());
                        session.setDisplayData(parseDouble(session.getDisplayText()));
                        view.invalidate();
                        return afterDigitInRArg;

                    case DOT:
                        session.setDisplayText(session.getDisplayText() + signal.getRepresentation());
                        session.setDisplayData(parseDouble(session.getDisplayText()));
                        view.invalidate();
                        return afterDotInRArg;

                    case PLUS:
                    case MINUS:
                    case MULTIPLY:
                    case DIVIDE:
                        try {
                            session.setlArg(session.getOperation().evaluate(session.getlArg(), session.getDisplayData()));
                            session.setDisplayData(session.getlArg());
                            session.setDisplayText(renderDouble(session.getDisplayData()));
                            session.setOperation(CalculatorModel.Operation.bySignal(signal));
                            view.invalidate();
                            return afterSingSelection;
                        } catch (Exception e) {
                            session.setDisplayText("ERR");
                            view.invalidate();
                            return errorState;
                        }

                    case EVALUATE:
                        try {
                            session.setrArg(session.getDisplayData());
                            session.setlArg(session.getOperation().evaluate(session.getlArg(), session.getrArg()));
                        } catch (Exception e) {
                            session.setDisplayText("ERR");
                            view.invalidate();
                            return errorState;
                        }
                        session.setDisplayData(session.getlArg());
                        session.setDisplayText(renderDouble(session.getDisplayData()));
                        view.invalidate();
                        return afterEvaluation;

                    case BACK_SPACE:
                        session.setDisplayText(session.getDisplayText().substring(0, session.getDisplayText().length() - 1));
                        if (session.getDisplayText().isEmpty() || "-".equals(session.getDisplayText()) || "-0".equals(session.getDisplayText())) {
                            session.setDisplayText("0");
                        }
                        session.setDisplayData(parseDouble(session.getDisplayText()));
                        view.invalidate();
                        return session.getDisplayText().length() < 3 ? initialState : afterDigitInLArg;
                }

                return super.react(signal);
            }
        }
    }


    protected class AfterDotInRArgState extends RArgState {
        public AfterDotInRArgState() {
            super("afterDotInRArg");
        }

        @NotNull
        @Override
        public State<Signal> react(@NotNull Signal signal) {
            try (CalculatorModel.Session session = model.createSession()) {
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
                        session.setDisplayText(session.getDisplayText() + signal.getRepresentation());
                        session.setDisplayData(parseDouble(session.getDisplayText()));
                        view.invalidate();
                        return afterDotInRArg;

                    case DOT:
                        return afterDotInRArg;

                    case PLUS:
                    case MINUS:
                    case MULTIPLY:
                    case DIVIDE:
                        try {
                            session.setlArg(session.getOperation().evaluate(session.getlArg(), session.getDisplayData()));
                            session.setDisplayData(session.getlArg());
                            session.setDisplayText(renderDouble(session.getDisplayData()));
                            session.setOperation(CalculatorModel.Operation.bySignal(signal));
                            view.invalidate();
                            return afterSingSelection;
                        } catch (Exception e) {
                            session.setDisplayText("ERR");
                            view.invalidate();
                            return errorState;
                        }

                    case EVALUATE:
                        try {
                            session.setrArg(session.getDisplayData());
                            session.setlArg(session.getOperation().evaluate(session.getlArg(), session.getrArg()));
                            session.setDisplayData(session.getlArg());
                            session.setDisplayText(renderDouble(session.getDisplayData()));
                            view.invalidate();
                            return afterEvaluation;
                        } catch (Exception e) {
                            session.setDisplayText("ERR");
                            view.invalidate();
                            return errorState;
                        }

                    case BACK_SPACE:
                        if (session.getDisplayText().charAt(session.getDisplayText().length() - 1) == '.') {
                            session.setDisplayText(session.getDisplayText().substring(0, session.getDisplayText().length() - 1));
                            if (session.getDisplayText().isEmpty() || "-".equals(session.getDisplayText()) || "-0".equals(session.getDisplayText())) {
                                session.setDisplayText("0");
                            }
                            session.setDisplayData(parseDouble(session.getDisplayText()));
                            view.invalidate();
                            return !session.getDisplayData().equals(BigFraction.ZERO) ? afterDigitInRArg : afterChangeInRArg;
                        }
                        session.setDisplayText(session.getDisplayText().substring(0, session.getDisplayText().length() - 1));
                        if (session.getDisplayText().isEmpty() || "-".equals(session.getDisplayText()) || "-0".equals(session.getDisplayText())) {
                            session.setDisplayText("0");
                        }
                        session.setDisplayData(parseDouble(session.getDisplayText()));
                        view.invalidate();
                        return afterDotInRArg;
                }

                return super.react(signal);
            }
        }
    }


    protected class AfterEvaluationState extends LArgState {
        public AfterEvaluationState() {
            super("afterEvaluation");
        }

        public AfterEvaluationState(String name) {
            super(name);
        }

        @NotNull
        @Override
        public State<Signal> react(@NotNull Signal signal) {
            try (CalculatorModel.Session session = model.createSession()) {
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
                        session.setDisplayText(signal.getRepresentation());
                        session.setDisplayData(parseDouble(session.getDisplayText()));
                        view.invalidate();
                        return afterDigitInLArg;

                    case DOT:
                        session.setDisplayText("0" + signal.getRepresentation());
                        session.setDisplayData(parseDouble(session.getDisplayText()));
                        view.invalidate();
                        return afterDotInLArg;

                    case PLUS:
                    case MINUS:
                    case MULTIPLY:
                    case DIVIDE:
                        session.setlArg(parseDouble(session.getDisplayText()));
                        session.setOperation(CalculatorModel.Operation.bySignal(signal));
                        return afterSingSelection;

                    case EVALUATE:
                        try {
                            session.setDisplayData(session.getOperation().evaluate(session.getDisplayData(), session.getrArg()));
                            session.setDisplayText(renderDouble(session.getDisplayData()));
                            view.invalidate();
                            return afterEvaluation;
                        } catch (Exception e) {
                            session.setDisplayText("ERR");
                            view.invalidate();
                            return errorState;
                        }

                    case BACK_SPACE:
                        return afterEvaluation;
                }

                return super.react(signal);
            }
        }
    }


    protected class ErrorState extends AbstractState {

        public ErrorState() {
            super("errorState");
        }

        @NotNull
        @Override
        public State<Signal> react(@NotNull Signal signal) {
            try (CalculatorModel.Session session = model.createSession()) {
                switch (signal) {
                    case CLEAR:
                    case CLEAR_EVALUATION:
                        session.setlArg(BigFraction.ZERO);
                        session.setrArg(BigFraction.ZERO);
                        session.setDisplayText("0");
                        session.setDisplayData(BigFraction.ZERO);
                        session.setOperation(null);
                        view.invalidate();
                        return initialState;

                    default:
                        return errorState;
                }
            }
        }
    }

    protected class LArgState extends AbstractState {

        public LArgState(String name) {
            super(name);
        }

        @NotNull
        @Override
        public State<Signal> react(@NotNull Signal signal) {
            try (CalculatorModel.Session session = model.createSession()) {
                switch (signal) {
                    case PERCENT:
                        session.setDisplayData(session.getlArg().multiply(session.getDisplayData()).divide(BigFraction.valueOf(100)));
                        session.setDisplayText(renderDouble(session.getDisplayData()));
                        view.invalidate();
                        return initialState;

                    case SQUARE_ROOT:
                        try {
                            if (session.getDisplayText().startsWith("-")) {
                                throw new ArithmeticException("Negative square root");
                            }

                            session.setDisplayData(sqrt(session.getDisplayData()));
                            session.setDisplayText(renderDouble(session.getDisplayData()));
                            view.invalidate();
                            return initialState;
                        } catch (Exception e) {
                            session.setDisplayText("ERR");
                            view.invalidate();
                            return errorState;
                        }

                    case INVERSE:
                        try {
                            session.setDisplayData(session.getDisplayData().pow(-1));
                            session.setDisplayText(renderDouble(session.getDisplayData()));
                            view.invalidate();
                            return initialState;
                        } catch (Exception e) {
                            session.setDisplayText("ERR");
                            view.invalidate();
                            return errorState;
                        }

                    case CLEAR_EVALUATION:
                        session.setlArg(BigFraction.ZERO);
                        session.setDisplayText("0");
                        session.setDisplayData(BigFraction.ZERO);
                        view.invalidate();
                        return initialState;

                    case MEMORY_RESTORE:
                        try {
                            session.setDisplayData(session.getMemory());
                            session.setDisplayText(renderDouble(session.getDisplayData()));
                            view.invalidate();
                            return initialState;
                        } catch (Exception e) {
                            session.setDisplayText("ERR");
                            view.invalidate();
                            return errorState;
                        }

                    case MEMORY_STORE:
                        session.setDisplayText(normalize(session.getDisplayText()));
                        session.setMemory(session.getDisplayData());
                        view.invalidate();
                        return initialState;

                    case MEMORY_CLEAR:
                        session.setDisplayText(normalize(session.getDisplayText()));
                        session.setMemory(BigFraction.ZERO);
                        view.invalidate();
                        return initialState;

                    case MEMORY_PLUS:
                        session.setDisplayText(normalize(session.getDisplayText()));
                        session.setMemory(session.getMemory().add(session.getDisplayData()));
                        view.invalidate();
                        return initialState;

                    case MEMORY_MINUS:
                        session.setDisplayText(normalize(session.getDisplayText()));
                        session.setMemory(session.getMemory().subtract(session.getDisplayData()));
                        view.invalidate();
                        return initialState;

                }

                return super.react(signal);
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
            try (CalculatorModel.Session session = model.createSession()) {
                switch (signal) {
                    case PERCENT:
                        session.setDisplayData(session.getlArg().multiply(session.getDisplayData()).divide(100));
                        session.setDisplayText(renderDouble(session.getDisplayData()));
                        view.invalidate();
                        return afterChangeInRArg;

                    case SQUARE_ROOT:
                        try {
                            if (session.getDisplayText().startsWith("-")) {
                                throw new ArithmeticException("Negative square root");
                            }

                            session.setDisplayData(sqrt(session.getDisplayData()));
                            session.setDisplayText(renderDouble(session.getDisplayData()));
                            view.invalidate();
                            return afterChangeInRArg;
                        } catch (Exception e) {
                            session.setDisplayText("ERR");
                            view.invalidate();
                            return errorState;
                        }

                    case INVERSE:
                        try {
                            session.setDisplayData(session.getDisplayData().pow(-1));
                            session.setDisplayText(renderDouble(session.getDisplayData()));
                            view.invalidate();
                            return afterChangeInRArg;
                        } catch (Exception e) {
                            session.setDisplayText("ERR");
                            view.invalidate();
                            return errorState;
                        }

                    case CLEAR_EVALUATION:
                        session.setrArg(BigFraction.ZERO);
                        session.setDisplayText("0");
                        session.setDisplayData(BigFraction.ZERO);
                        view.invalidate();
                        return afterChangeInRArg;

                    case MEMORY_RESTORE:
                        try {
                            session.setDisplayData(session.getMemory());
                            session.setDisplayText(renderDouble(session.getDisplayData()));
                            view.invalidate();
                            return afterChangeInRArg;
                        } catch (Exception e) {
                            session.setDisplayText("ERR");
                            view.invalidate();
                            return errorState;
                        }

                    case MEMORY_STORE:
                        session.setDisplayText(normalize(session.getDisplayText()));
                        session.setMemory(session.getDisplayData());
                        view.invalidate();
                        return afterChangeInRArg;

                    case MEMORY_CLEAR:
                        session.setDisplayText(normalize(session.getDisplayText()));
                        session.setMemory(BigFraction.ZERO);
                        view.invalidate();
                        return afterChangeInRArg;

                    case MEMORY_PLUS:
                        session.setDisplayText(normalize(session.getDisplayText()));
                        session.setMemory(session.getMemory().add(session.getDisplayData()));
                        view.invalidate();
                        return afterChangeInRArg;

                    case MEMORY_MINUS:
                        session.setDisplayText(normalize(session.getDisplayText()));
                        session.setMemory(session.getMemory().subtract(session.getDisplayData()));
                        view.invalidate();
                        return afterChangeInRArg;
                }

                return super.react(signal);
            }
        }
    }

    protected abstract class AbstractState implements State<Signal> {

        protected final String name;

        public AbstractState(String name) {
            this.name = name;
        }

        @NotNull
        @Override
        public strictfp State<Signal> react(@NotNull Signal signal) {
            try (CalculatorModel.Session session = model.createSession()) {
                switch (signal) {
                    case CLEAR:
                        session.setlArg(BigFraction.ZERO);
                        session.setrArg(BigFraction.ZERO);
                        session.setDisplayText("0");
                        session.setDisplayData(BigFraction.ZERO);
                        session.setOperation(null);
                        view.invalidate();
                        return initialState;

                    case REVERSE:
                        if (!"0".equals(session.getDisplayText())) {
                            if (session.getDisplayText().startsWith("-")) {
                                session.setDisplayText(session.getDisplayText().substring(1));
                            } else {
                                session.setDisplayText("-" + session.getDisplayText());
                            }
                        }
                        session.setDisplayData(session.getDisplayData().negate());
                        view.invalidate();
                        return this;
                }

                throw new IllegalStateException(signal + " was not processed");
            }
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static BigFraction sqrt(BigFraction bigFraction) {
        return BigFraction.valueOf(
                Math.sqrt(bigFraction.getNumerator().doubleValue()),
                Math.sqrt(bigFraction.getDenominator().doubleValue())
        );
    }

}