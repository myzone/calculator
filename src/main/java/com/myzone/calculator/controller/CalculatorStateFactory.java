package com.myzone.calculator.controller;

import com.myzone.calculator.model.CalculatorModel;
import com.myzone.calculator.model.Signal;
import com.myzone.calculator.view.CalculatorView;
import com.myzone.calculator.view.DoubleConverter;
import com.myzone.utils.Converter;
import com.myzone.utils.statemachine.State;
import org.jetbrains.annotations.NotNull;

import static java.lang.Math.*;

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

        // rounding hook
        if (abs(1 - d) < pow(10, -10)) {
            d = 1;
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
    protected State<Signal> errorState;

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
                        session.setDisplayText(session.getDisplayText() + signal.getRepresentation());
                        session.setDisplayData(parseDouble(session.getDisplayText()));
                        view.invalidate();
                        return afterDotInLArg;

                    case PLUS:
                    case MINUS:
                    case MULTIPLY:
                    case DIVIDE:
                        session.setlArg(parseDouble(session.getDisplayText()));
                        session.setDisplayData(session.getlArg());
                        session.setDisplayText(renderDouble(session.getDisplayData()));
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
                        return initialState;

                    case BACK_SPACE:
                        session.setDisplayText(session.getDisplayText().substring(0, session.getDisplayText().length() - 1));
                        if (session.getDisplayText().isEmpty() || "-".equals(session.getDisplayText())) {
                            session.setDisplayText("0");
                        }
                        session.setDisplayData(parseDouble(session.getDisplayText()));
                        view.invalidate();
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
                        session.setDisplayText(renderDouble(session.getDisplayData()));
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
                        return session.getDisplayText().length() < 3 ? initialState : afterDigitInLArg;
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
                        session.setDisplayText(renderDouble(session.getlArg()));
                        session.setOperation(CalculatorModel.Operation.bySignal(signal));
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
                        return initialState;

                    case BACK_SPACE:
                        if (session.getDisplayText().charAt(session.getDisplayText().length() - 1) == '.') {
                            session.setDisplayText(session.getDisplayText().substring(0, session.getDisplayText().length() - 1));
                            if (session.getDisplayText().isEmpty() || "-".equals(session.getDisplayText()) || "-0".equals(session.getDisplayText())) {
                                session.setDisplayText("0");
                            }
                            session.setDisplayData(parseDouble(session.getDisplayText()));
                            view.invalidate();
                            return session.getDisplayData() != 0 ? afterDigitInLArg : initialState;
                        }
                        session.setDisplayText(session.getDisplayText().substring(0, session.getDisplayText().length() - 1));
                        if ("0".equals(session.getDisplayText()) || "-0".equals(session.getDisplayText())) {
                            session.setDisplayText("0");
                        }
                        session.setDisplayData(parseDouble(session.getDisplayText()));
                        view.invalidate();
                        return afterDotInLArg;

                    case REVERSE:
                        if (!"0".equals(session.getDisplayText())) {
                            if (session.getDisplayText().startsWith("-")) {
                                session.setDisplayText(session.getDisplayText().substring(1));
                            } else {
                                session.setDisplayText("-" + session.getDisplayText());
                            }
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
                        session.setDisplayText(session.getDisplayText().substring(0, session.getDisplayText().length() - 1));
                        if (session.getDisplayText().isEmpty() || "-".equals(session.getDisplayText()) || "-0".equals(session.getDisplayText())) {
                            session.setDisplayText("0");
                        }
                        session.setDisplayData(parseDouble(session.getDisplayText()));
                        view.invalidate();
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
                        session.setDisplayText(session.getDisplayText().substring(0, session.getDisplayText().length() - 1));
                        if (session.getDisplayText().isEmpty() || "-".equals(session.getDisplayText()) || "-0".equals(session.getDisplayText())) {
                            session.setDisplayText("0");
                        }
                        session.setDisplayData(parseDouble(session.getDisplayText()));
                        view.invalidate();
                        return afterSingSelection;
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
                            return session.getDisplayData() != 0 ? afterDigitInRArg : afterChangeInRArg;
                        }
                        session.setDisplayText(session.getDisplayText().substring(0, session.getDisplayText().length() - 1));
                        if (session.getDisplayText().isEmpty() || "-".equals(session.getDisplayText()) || "-0".equals(session.getDisplayText())) {
                            session.setDisplayText("0");
                        }
                        session.setDisplayData(parseDouble(session.getDisplayText()));
                        view.invalidate();
                        return afterDotInRArg;

                    case REVERSE:
                        if (!"0".equals(session.getDisplayText())) {
                            if (session.getDisplayText().startsWith("-")) {
                                session.setDisplayText(session.getDisplayText().substring(1));
                            } else {
                                session.setDisplayText("-" + session.getDisplayText());
                            }
                        }
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
                        session.setlArg(0);
                        session.setrArg(0);
                        session.setDisplayText("0");
                        session.setDisplayData(0);
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
                        session.setDisplayData(session.getlArg() * session.getDisplayData() / 100);
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

                    case REVERSE:
                        if (!"0".equals(session.getDisplayText())) {
                            if (session.getDisplayText().startsWith("-")) {
                                session.setDisplayText(session.getDisplayText().substring(1));
                            } else {
                                session.setDisplayText("-" + session.getDisplayText());
                            }
                        }
                        session.setDisplayData(parseDouble(session.getDisplayText()));
                        view.invalidate();
                        return initialState;

                    case INVERSE:
                        try {
                            session.setDisplayData(1D / session.getDisplayData());
                            session.setDisplayText(renderDouble(session.getDisplayData()));
                            view.invalidate();
                            return initialState;
                        } catch (Exception e) {
                            session.setDisplayText("ERR");
                            view.invalidate();
                            return errorState;
                        }

                    case CLEAR_EVALUATION:
                        session.setlArg(0);
                        session.setDisplayText("0");
                        session.setDisplayData(0);
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
                        session.setMemory(session.getDisplayData());
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
                        session.setDisplayData(session.getlArg() * session.getDisplayData() / 100);
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

                    case REVERSE:
                        if (!"0".equals(session.getDisplayText())) {
                            if (session.getDisplayText().startsWith("-")) {
                                session.setDisplayText(session.getDisplayText().substring(1));
                            } else {
                                session.setDisplayText("-" + session.getDisplayText());
                            }
                        }
                        session.setDisplayData(parseDouble(session.getDisplayText()));
                        view.invalidate();
                        return afterChangeInRArg;

                    case INVERSE:
                        try {
                            session.setDisplayData(1D / session.getDisplayData());
                            session.setDisplayText(renderDouble(session.getDisplayData()));
                            view.invalidate();
                            return afterChangeInRArg;
                        } catch (Exception e) {
                            session.setDisplayText("ERR");
                            view.invalidate();
                            return errorState;
                        }

                    case CLEAR_EVALUATION:
                        session.setrArg(0);
                        session.setDisplayText("0");
                        session.setDisplayData(0);
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
                        session.setMemory(session.getDisplayData());
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
                        session.setlArg(0);
                        session.setrArg(0);
                        session.setDisplayText("0");
                        session.setDisplayData(0);
                        view.invalidate();
                        return initialState;

                    case MEMORY_CLEAR:
                        session.setMemory(0);
                        view.invalidate();
                        return this;

                    case MEMORY_PLUS:
                        session.setMemory(session.getMemory() + session.getDisplayData());
                        view.invalidate();
                        return this;

                    case MEMORY_MINUS:
                        session.setMemory(session.getMemory() - session.getDisplayData());
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

}