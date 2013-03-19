package com.myzone.calculator.model;

import com.myzone.calculator.view.CalculatorView;
import com.myzone.utils.Converter;
import com.myzone.utils.statemachine.State;
import org.jetbrains.annotations.NotNull;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

/**
 * @author: myzone
 * @date: 03.02.13 23:48
 */
public class CalculatorStateFactory implements State.Factory<Signal> {

    protected static final Converter<String, Double> DOUBLE_CONVERTER = new DoubleConverter(16, pow(10D, 16D), pow(10D, -16D));

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

    private State<Signal> createInitialState() {
        return new LoggableState<>("initialState", signal -> {
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
                    view.invalidate();
                    return afterDigitInLArg;

                case DOT:
                    model.setDisplayText(model.getDisplayText() + signal.getRepresentation());
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
                    if (model.getOperation() != null) {
                        try {
                            model.setlArg(model.getOperation().evaluate(
                                    parseDouble(model.getDisplayText()),
                                    model.getrArg()
                            ));
                        } catch (Exception e) {
                            model.setDisplayText("ERR");
                            view.invalidate();
                            return errorInputState;
                        }
                        model.setDisplayText(renderDouble(model.getlArg()));
                        view.invalidate();
                        return afterEvaluation;
                    }
                    return initialState;

                case CLEAR:
                    model.setlArg(0);
                    model.setrArg(0);
                    model.setDisplayText("0");
                    view.invalidate();
                    return initialState;

                case CLEAR_EVALUATION:
                    model.setlArg(0);
                    model.setDisplayText("0");
                    view.invalidate();
                    return initialState;

                case BACK_SPACE:
                    model.setDisplayText(model.getDisplayText().substring(0, model.getDisplayText().length() - 1));
                    if (model.getDisplayText().isEmpty() || "-".equals(model.getDisplayText())) {
                        model.setDisplayText("0");
                    }
                    view.invalidate();
                    return initialState;

                case PERCENT:
                    model.setDisplayText(renderDouble(model.getlArg() * parseDouble(model.getDisplayText()) / 100));
                    view.invalidate();
                    return initialState;

                case SQUARE_ROOT:
                    try {
                        if (model.getDisplayText().startsWith("-")) {
                            throw new ArithmeticException("Negative square root");
                        }

                        model.setDisplayText(renderDouble(sqrt(parseDouble(model.getDisplayText()))));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.invalidate();
                        return errorInputState;
                    }
                    view.invalidate();
                    return initialState;

                case REVERSE:
                    if (!"0".equals(model.getDisplayText())) {
                        if (model.getDisplayText().startsWith("-")) {
                            model.setDisplayText(model.getDisplayText().substring(1));
                        } else {
                            model.setDisplayText("-" + model.getDisplayText());
                        }
                    }
                    view.invalidate();
                    return initialState;

                case INVERSE:
                    try {
                        model.setDisplayText(renderDouble(1D / parseDouble(model.getDisplayText())));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.invalidate();
                        return errorInputState;
                    }
                    view.invalidate();
                    return initialState;

                case MEMORY_CLEAR:
                    model.setMemory(0);
                    view.invalidate();
                    return initialState;

                case MEMORY_STORE:
                    model.setMemory(parseDouble(model.getDisplayText()));
                    view.invalidate();
                    return initialState;

                case MEMORY_RESTORE:
                    try {
                        model.setDisplayText(renderDouble(model.getMemory()));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.invalidate();
                        return errorInputState;
                    }
                    view.invalidate();
                    return initialState;

                case MEMORY_PLUS:
                    model.setMemory(model.getMemory() + parseDouble(model.getDisplayText()));
                    view.invalidate();
                    return initialState;

                case MEMORY_MINUS:
                    model.setMemory(model.getMemory() - parseDouble(model.getDisplayText()));
                    view.invalidate();
                    return initialState;

            }

            throw new RuntimeException();
        });
    }

    private State<Signal> createAfterDigitInLArg() {
        return new LoggableState<>("afterDigitInLArg", (signal) -> {
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
                    view.invalidate();
                    return afterDigitInLArg;

                case DOT:
                    model.setDisplayText(model.getDisplayText() + signal.getRepresentation());
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
                    if (model.getOperation() != null) {
                        try {
                            model.setlArg(model.getOperation().evaluate(
                                    parseDouble(model.getDisplayText()),
                                    model.getrArg()
                            ));
                        } catch (Exception e) {
                            model.setDisplayText("ERR");
                            view.invalidate();
                            return errorInputState;
                        }
                        model.setDisplayText(renderDouble(model.getlArg()));
                        view.invalidate();
                        return afterEvaluation;
                    }
                    return initialState;

                case CLEAR:
                    model.setlArg(0);
                    model.setrArg(0);
                    model.setDisplayText("0");
                    view.invalidate();
                    return initialState;

                case CLEAR_EVALUATION:
                    model.setlArg(0);
                    model.setDisplayText("0");
                    view.invalidate();
                    return initialState;

                case BACK_SPACE:
                    model.setDisplayText(model.getDisplayText().substring(0, model.getDisplayText().length() - 1));
                    if (model.getDisplayText().isEmpty() || "-".equals(model.getDisplayText()) || "-0".equals(model.getDisplayText())) {
                        model.setDisplayText("0");
                    }
                    view.invalidate();
                    return model.getDisplayText().length() < 3 ? initialState : afterDigitInLArg;

                case PERCENT:
                    model.setDisplayText(renderDouble(model.getlArg() * parseDouble(model.getDisplayText()) / 100));
                    view.invalidate();
                    return afterDigitInLArg;

                case SQUARE_ROOT:
                    try {
                        if (model.getDisplayText().startsWith("-")) {
                            throw new ArithmeticException("Negative square root");
                        }

                        model.setDisplayText(renderDouble(sqrt(parseDouble(model.getDisplayText()))));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.invalidate();
                        return errorInputState;
                    }
                    view.invalidate();
                    return initialState;

                case REVERSE:
                    if (!"0".equals(model.getDisplayText())) {
                        if (model.getDisplayText().startsWith("-")) {
                            model.setDisplayText(model.getDisplayText().substring(1));
                        } else {
                            model.setDisplayText("-" + model.getDisplayText());
                        }
                    }
                    view.invalidate();
                    return afterDigitInLArg;

                case INVERSE:
                    try {
                        model.setDisplayText(renderDouble(1D / parseDouble(model.getDisplayText())));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.invalidate();
                        return errorInputState;
                    }
                    view.invalidate();
                    return afterDigitInLArg;

                case MEMORY_CLEAR:
                    model.setMemory(0);
                    view.invalidate();
                    return afterDigitInLArg;

                case MEMORY_STORE:
                    model.setMemory(parseDouble(model.getDisplayText()));
                    view.invalidate();
                    return afterDigitInLArg;

                case MEMORY_RESTORE:
                    try {
                        model.setDisplayText(renderDouble(model.getMemory()));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.invalidate();
                        return errorInputState;
                    }
                    view.invalidate();
                    return initialState;

                case MEMORY_PLUS:
                    model.setMemory(model.getMemory() + parseDouble(model.getDisplayText()));
                    view.invalidate();
                    return afterDigitInLArg;

                case MEMORY_MINUS:
                    model.setMemory(model.getMemory() - parseDouble(model.getDisplayText()));
                    view.invalidate();
                    return afterDigitInLArg;
            }

            throw new RuntimeException();
        });
    }

    private State<Signal> createAfterDotInLArg() {
        return new LoggableState<>("afterDotInLArg", (signal) -> {
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
                    view.invalidate();
                    return afterDotInLArg;

                case DOT:
                    return afterDotInLArg;

                case PLUS:
                case MINUS:
                case MULTIPLY:
                case DIVIDE:
                    model.setlArg(parseDouble(model.getDisplayText()));
                    model.setOperation(CalculatorModel.Operation.bySignal(signal));
                    return afterSingSelection;

                case EVALUATE:
                    if (model.getOperation() != null) {
                        try {
                            model.setlArg(model.getOperation().evaluate(
                                    parseDouble(model.getDisplayText()),
                                    model.getrArg()
                            ));
                        } catch (Exception e) {
                            model.setDisplayText("ERR");
                            view.invalidate();
                            return errorInputState;
                        }
                        model.setDisplayText(renderDouble(model.getlArg()));
                        view.invalidate();
                        return afterEvaluation;
                    }
                    return initialState;

                case CLEAR:
                    model.setlArg(0);
                    model.setrArg(0);
                    model.setDisplayText("0");
                    view.invalidate();
                    return initialState;

                case CLEAR_EVALUATION:
                    model.setlArg(0);
                    model.setDisplayText("0");
                    view.invalidate();
                    return initialState;

                case BACK_SPACE:
                    if (model.getDisplayText().charAt(model.getDisplayText().length() - 1) == '.') {
                        model.setDisplayText(model.getDisplayText().substring(0, model.getDisplayText().length() - 1));
                        if (model.getDisplayText().isEmpty() || "-".equals(model.getDisplayText()) || "-0".equals(model.getDisplayText())) {
                            model.setDisplayText("0");
                        }
                        view.invalidate();
                        return afterDigitInLArg;
                    }
                    model.setDisplayText(model.getDisplayText().substring(0, model.getDisplayText().length() - 1));
                    if ("0".equals(model.getDisplayText()) || "-0".equals(model.getDisplayText())) {
                        model.setDisplayText("0");
                    }
                    view.invalidate();
                    return afterDotInLArg;

                case PERCENT:
                    model.setDisplayText(renderDouble(model.getlArg() * parseDouble(model.getDisplayText()) / 100));
                    view.invalidate();
                    return afterDotInLArg;

                case SQUARE_ROOT:
                    try {
                        if (model.getDisplayText().startsWith("-")) {
                            throw new ArithmeticException("Negative square root");
                        }

                        model.setDisplayText(renderDouble(sqrt(parseDouble(model.getDisplayText()))));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.invalidate();
                        return errorInputState;
                    }
                    view.invalidate();
                    return initialState;

                case REVERSE:
                    if (!"0".equals(model.getDisplayText())) {
                        if (model.getDisplayText().startsWith("-")) {
                            model.setDisplayText(model.getDisplayText().substring(1));
                        } else {
                            model.setDisplayText("-" + model.getDisplayText());
                        }
                    }
                    view.invalidate();
                    return afterDotInLArg;

                case INVERSE:
                    try {
                        model.setDisplayText(renderDouble(1D / parseDouble(model.getDisplayText())));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.invalidate();
                        return errorInputState;
                    }
                    view.invalidate();
                    return afterDotInLArg;

                case MEMORY_CLEAR:
                    model.setMemory(0);
                    view.invalidate();
                    return afterDotInLArg;

                case MEMORY_STORE:
                    model.setMemory(parseDouble(model.getDisplayText()));
                    view.invalidate();
                    return afterDotInLArg;

                case MEMORY_RESTORE:
                    try {
                        model.setDisplayText(renderDouble(model.getMemory()));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.invalidate();
                        return errorInputState;
                    }
                    view.invalidate();
                    return initialState;

                case MEMORY_PLUS:
                    model.setMemory(model.getMemory() + parseDouble(model.getDisplayText()));
                    view.invalidate();
                    return afterDotInLArg;

                case MEMORY_MINUS:
                    model.setMemory(model.getMemory() - parseDouble(model.getDisplayText()));
                    view.invalidate();
                    return afterDotInLArg;
            }

            throw new RuntimeException();
        });
    }

    private State<Signal> createAfterSingSelection() {
        return new LoggableState<>("afterSingSelection", (signal) -> {
            switch (signal) {
                case DIGIT_0:
                    model.setDisplayText(signal.getRepresentation());
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
                    view.invalidate();
                    return afterDigitInRArg;

                case DOT:
                    model.setDisplayText("0" + signal.getRepresentation());
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
                        model.setrArg(parseDouble(model.getDisplayText()));
                        model.setlArg(model.getOperation().evaluate(model.getlArg(), model.getrArg()));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.invalidate();
                        return errorInputState;
                    }
                    model.setDisplayText(renderDouble(model.getlArg()));
                    view.invalidate();
                    return afterEvaluation;

                case CLEAR:
                    model.setlArg(0);
                    model.setrArg(0);
                    model.setDisplayText("0");
                    view.invalidate();
                    return initialState;

                case CLEAR_EVALUATION:
                    model.setrArg(0);
                    model.setDisplayText("0");
                    view.invalidate();
                    return afterSingSelection;

                case BACK_SPACE:
                    model.setDisplayText(model.getDisplayText().substring(0, model.getDisplayText().length() - 1));
                    if (model.getDisplayText().isEmpty() || "-".equals(model.getDisplayText()) || "-0".equals(model.getDisplayText())) {
                        model.setDisplayText("0");
                    }
                    view.invalidate();
                    return afterSingSelection;

                case PERCENT:
                    model.setDisplayText(renderDouble(model.getlArg() * parseDouble(model.getDisplayText()) / 100));
                    view.invalidate();
                    return afterChangeInRArg;

                case SQUARE_ROOT:
                    try {
                        if (model.getDisplayText().startsWith("-")) {
                            throw new ArithmeticException("Negative square root");
                        }

                        model.setDisplayText(renderDouble(sqrt(parseDouble(model.getDisplayText()))));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.invalidate();
                        return errorInputState;
                    }
                    view.invalidate();
                    return afterChangeInRArg;

                case REVERSE:
                    if (!"0".equals(model.getDisplayText())) {
                        if (model.getDisplayText().startsWith("-")) {
                            model.setDisplayText(model.getDisplayText().substring(1));
                        } else {
                            model.setDisplayText("-" + model.getDisplayText());
                        }
                    }
                    view.invalidate();
                    return afterSingSelection;

                case INVERSE:
                    try {
                        model.setDisplayText(renderDouble(1D / parseDouble(model.getDisplayText())));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.invalidate();
                        return errorInputState;
                    }
                    view.invalidate();
                    return afterSingSelection;

                case MEMORY_CLEAR:
                    model.setMemory(0);
                    view.invalidate();
                    return afterSingSelection;

                case MEMORY_STORE:
                    model.setMemory(parseDouble(model.getDisplayText()));
                    view.invalidate();
                    return afterSingSelection;

                case MEMORY_RESTORE:
                    try {
                        model.setDisplayText(renderDouble(model.getMemory()));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.invalidate();
                        return errorInputState;
                    }
                    view.invalidate();
                    return afterChangeInRArg;

                case MEMORY_PLUS:
                    model.setMemory(model.getMemory() + parseDouble(model.getDisplayText()));
                    view.invalidate();
                    return afterSingSelection;

                case MEMORY_MINUS:
                    model.setMemory(model.getMemory() - parseDouble(model.getDisplayText()));
                    view.invalidate();
                    return afterSingSelection;
            }

            throw new RuntimeException();
        });
    }

    private State<Signal> createAfterChangeInRArg() {
        return new LoggableState<>("afterChangeInRArg", (signal) -> {
            switch (signal) {
                case DIGIT_0:
                    model.setDisplayText(signal.getRepresentation());
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
                    view.invalidate();
                    return afterDigitInRArg;

                case DOT:
                    model.setDisplayText("0" + signal.getRepresentation());
                    view.invalidate();
                    return afterDotInRArg;

                case PLUS:
                case MINUS:
                case MULTIPLY:
                case DIVIDE:
                    model.setlArg(model.getOperation().evaluate(model.getlArg(), parseDouble(model.getDisplayText())));
                    model.setDisplayText(renderDouble(model.getlArg()));
                    model.setOperation(CalculatorModel.Operation.bySignal(signal));
                    view.invalidate();
                    return afterSingSelection;

                case EVALUATE:
                    try {
                        model.setrArg(parseDouble(model.getDisplayText()));
                        model.setlArg(model.getOperation().evaluate(model.getlArg(), model.getrArg()));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.invalidate();
                        return errorInputState;
                    }
                    model.setDisplayText(renderDouble(model.getlArg()));
                    view.invalidate();
                    return afterEvaluation;

                case CLEAR:
                    model.setlArg(0);
                    model.setrArg(0);
                    model.setDisplayText("0");
                    view.invalidate();
                    return initialState;

                case CLEAR_EVALUATION:
                    model.setrArg(0);
                    model.setDisplayText("0");
                    view.invalidate();
                    return afterSingSelection;

                case BACK_SPACE:
                    model.setDisplayText(model.getDisplayText().substring(0, model.getDisplayText().length() - 1));
                    if (model.getDisplayText().isEmpty() || "-".equals(model.getDisplayText()) || "-0".equals(model.getDisplayText())) {
                        model.setDisplayText("0");
                    }
                    view.invalidate();
                    return afterSingSelection;

                case PERCENT:
                    model.setDisplayText(renderDouble(model.getlArg() * parseDouble(model.getDisplayText()) / 100));
                    view.invalidate();
                    return afterSingSelection;

                case SQUARE_ROOT:
                    try {
                        if (model.getDisplayText().startsWith("-")) {
                            throw new ArithmeticException("Negative square root");
                        }

                        model.setDisplayText(renderDouble(sqrt(parseDouble(model.getDisplayText()))));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.invalidate();
                        return errorInputState;
                    }
                    view.invalidate();
                    return afterChangeInRArg;

                case REVERSE:
                    if (!"0".equals(model.getDisplayText())) {
                        if (model.getDisplayText().startsWith("-")) {
                            model.setDisplayText(model.getDisplayText().substring(1));
                        } else {
                            model.setDisplayText("-" + model.getDisplayText());
                        }
                    }
                    view.invalidate();
                    return afterSingSelection;

                case INVERSE:
                    try {
                        model.setDisplayText(renderDouble(1D / parseDouble(model.getDisplayText())));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.invalidate();
                        return errorInputState;
                    }
                    view.invalidate();
                    return afterSingSelection;

                case MEMORY_CLEAR:
                    model.setMemory(0);
                    view.invalidate();
                    return afterSingSelection;

                case MEMORY_STORE:
                    model.setMemory(parseDouble(model.getDisplayText()));
                    view.invalidate();
                    return afterSingSelection;

                case MEMORY_RESTORE:
                    try {
                        model.setDisplayText(renderDouble(model.getMemory()));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.invalidate();
                        return errorInputState;
                    }
                    view.invalidate();
                    return afterSingSelection;

                case MEMORY_PLUS:
                    model.setMemory(model.getMemory() + parseDouble(model.getDisplayText()));
                    view.invalidate();
                    return afterSingSelection;

                case MEMORY_MINUS:
                    model.setMemory(model.getMemory() - parseDouble(model.getDisplayText()));
                    view.invalidate();
                    return afterSingSelection;
            }

            throw new RuntimeException();
        });
    }

    private State<Signal> createAfterDigitInRArg() {
        return new LoggableState<>("afterDigitInRArg", (signal) -> {
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
                    view.invalidate();
                    return afterDigitInRArg;

                case DOT:
                    model.setDisplayText(model.getDisplayText() + signal.getRepresentation());
                    view.invalidate();
                    return afterDotInRArg;

                case PLUS:
                case MINUS:
                case MULTIPLY:
                case DIVIDE:
                    try {
                        model.setlArg(model.getOperation().evaluate(
                                model.getlArg(),
                                parseDouble(model.getDisplayText())
                        ));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.invalidate();
                        return errorInputState;
                    }
                    model.setDisplayText(renderDouble(model.getlArg()));
                    model.setOperation(CalculatorModel.Operation.bySignal(signal));
                    view.invalidate();
                    return afterSingSelection;

                case EVALUATE:
                    model.setrArg(parseDouble(model.getDisplayText()));
                    try {
                        model.setlArg(model.getOperation().evaluate(model.getlArg(), model.getrArg()));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.invalidate();
                        return errorInputState;
                    }
                    model.setDisplayText(renderDouble(model.getlArg()));
                    view.invalidate();
                    return afterEvaluation;

                case CLEAR:
                    model.setlArg(0);
                    model.setrArg(0);
                    model.setDisplayText("0");
                    view.invalidate();
                    return initialState;

                case CLEAR_EVALUATION:
                    model.setrArg(0);
                    model.setDisplayText("0");
                    view.invalidate();
                    return afterSingSelection;

                case BACK_SPACE:
                    model.setDisplayText(model.getDisplayText().substring(0, model.getDisplayText().length() - 1));
                    if (model.getDisplayText().isEmpty() || "-".equals(model.getDisplayText()) || "-0".equals(model.getDisplayText())) {
                        model.setDisplayText("0");
                    }
                    view.invalidate();
                    return model.getDisplayText().length() < 3 ? initialState : afterDigitInLArg;

                case PERCENT:
                    model.setDisplayText(renderDouble(model.getlArg() * parseDouble(model.getDisplayText()) / 100));
                    view.invalidate();
                    return afterDigitInRArg;

                case SQUARE_ROOT:
                    try {
                        if (model.getDisplayText().startsWith("-")) {
                            throw new ArithmeticException("Negative square root");
                        }
                        model.setDisplayText(renderDouble(sqrt(parseDouble(model.getDisplayText()))));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.invalidate();
                        return errorInputState;
                    }
                    view.invalidate();
                    return afterChangeInRArg;

                case REVERSE:
                    if (!"0".equals(model.getDisplayText())) {
                        if (model.getDisplayText().startsWith("-")) {
                            model.setDisplayText(model.getDisplayText().substring(1));
                        } else {
                            model.setDisplayText("-" + model.getDisplayText());
                        }
                    }
                    view.invalidate();
                    return afterDigitInRArg;

                case INVERSE:
                    try {
                        model.setDisplayText(renderDouble(1D / parseDouble(model.getDisplayText())));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.invalidate();
                        return errorInputState;
                    }
                    view.invalidate();
                    return afterDigitInRArg;

                case MEMORY_CLEAR:
                    model.setMemory(0);
                    view.invalidate();
                    return afterDigitInRArg;

                case MEMORY_STORE:
                    model.setMemory(parseDouble(model.getDisplayText()));
                    view.invalidate();
                    return afterDigitInRArg;

                case MEMORY_RESTORE:
                    try {
                        model.setDisplayText(renderDouble(model.getMemory()));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.invalidate();
                        return errorInputState;
                    }
                    view.invalidate();
                    return afterSingSelection;

                case MEMORY_PLUS:
                    model.setMemory(model.getMemory() + parseDouble(model.getDisplayText()));
                    view.invalidate();
                    return afterDigitInRArg;

                case MEMORY_MINUS:
                    model.setMemory(model.getMemory() - parseDouble(model.getDisplayText()));
                    view.invalidate();
                    return afterDigitInRArg;
            }

            throw new RuntimeException();
        });
    }

    private State<Signal> createAfterDotInRArg() {
        return new LoggableState<>("afterDotInRArg", (signal) -> {
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
                    view.invalidate();
                    return afterDotInRArg;

                case DOT:
                    return afterDotInRArg;

                case PLUS:
                case MINUS:
                case MULTIPLY:
                case DIVIDE:
                    try {
                        model.setlArg(model.getOperation().evaluate(
                                model.getlArg(),
                                parseDouble(model.getDisplayText())
                        ));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.invalidate();
                        return errorInputState;
                    }
                    model.setDisplayText(renderDouble(model.getlArg()));
                    model.setOperation(CalculatorModel.Operation.bySignal(signal));
                    view.invalidate();
                    return afterSingSelection;

                case EVALUATE:
                    model.setrArg(parseDouble(model.getDisplayText()));
                    try {
                        model.setlArg(model.getOperation().evaluate(model.getlArg(), model.getrArg()));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.invalidate();
                        return errorInputState;
                    }
                    model.setDisplayText(renderDouble(model.getlArg()));
                    view.invalidate();
                    return afterEvaluation;

                case CLEAR:
                    model.setlArg(0);
                    model.setrArg(0);
                    model.setDisplayText("0");
                    view.invalidate();
                    return initialState;

                case CLEAR_EVALUATION:
                    model.setrArg(0);
                    model.setDisplayText("0");
                    view.invalidate();
                    return afterSingSelection;

                case BACK_SPACE:
                    if (model.getDisplayText().charAt(model.getDisplayText().length() - 1) == '.') {
                        model.setDisplayText(model.getDisplayText().substring(0, model.getDisplayText().length() - 1));
                        if (model.getDisplayText().isEmpty() || "-".equals(model.getDisplayText()) || "-0".equals(model.getDisplayText())) {
                            model.setDisplayText("0");
                        }
                        view.invalidate();
                        return afterDigitInRArg;
                    }
                    model.setDisplayText(model.getDisplayText().substring(0, model.getDisplayText().length() - 1));
                    if (model.getDisplayText().isEmpty() || "-".equals(model.getDisplayText()) || "-0".equals(model.getDisplayText())) {
                        model.setDisplayText("0");
                    }
                    view.invalidate();
                    return afterDotInRArg;

                case PERCENT:
                    model.setDisplayText(renderDouble(model.getlArg() * parseDouble(model.getDisplayText()) / 100));
                    view.invalidate();
                    return afterDotInRArg;

                case SQUARE_ROOT:
                    try {
                        if (model.getDisplayText().startsWith("-")) {
                            throw new ArithmeticException("Negative square root");
                        }

                        model.setDisplayText(renderDouble(sqrt(parseDouble(model.getDisplayText()))));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.invalidate();
                        return errorInputState;
                    }
                    view.invalidate();
                    return afterChangeInRArg;

                case REVERSE:
                    if (!"0".equals(model.getDisplayText())) {
                        if (model.getDisplayText().startsWith("-")) {
                            model.setDisplayText(model.getDisplayText().substring(1));
                        } else {
                            model.setDisplayText("-" + model.getDisplayText());
                        }
                    }
                    view.invalidate();
                    return afterChangeInRArg;

                case INVERSE:
                    try {
                        model.setDisplayText(renderDouble(1D / parseDouble(model.getDisplayText())));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.invalidate();
                        return errorInputState;
                    }
                    view.invalidate();
                    return afterChangeInRArg;

                case MEMORY_CLEAR:
                    model.setMemory(0);
                    view.invalidate();
                    return afterDotInRArg;

                case MEMORY_STORE:
                    model.setMemory(parseDouble(model.getDisplayText()));
                    view.invalidate();
                    return afterDotInRArg;

                case MEMORY_RESTORE:
                    try {
                        model.setDisplayText(renderDouble(model.getMemory()));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.invalidate();
                        return errorInputState;
                    }
                    view.invalidate();
                    return afterSingSelection;

                case MEMORY_PLUS:
                    model.setMemory(model.getMemory() + parseDouble(model.getDisplayText()));
                    view.invalidate();
                    return initialState;

                case MEMORY_MINUS:
                    model.setMemory(model.getMemory() - parseDouble(model.getDisplayText()));
                    view.invalidate();
                    return afterDotInRArg;
            }

            throw new RuntimeException();
        });
    }

    private State<Signal> createAfterEvaluation() {
        return new LoggableState<>("afterEvaluation", (signal) -> {
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
                    view.invalidate();
                    return afterDigitInLArg;

                case DOT:
                    model.setDisplayText("0" + signal.getRepresentation());
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
                        model.setDisplayText(renderDouble(model.getOperation().evaluate(
                                parseDouble(model.getDisplayText()),
                                model.getrArg()
                        )));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.invalidate();
                        return errorInputState;
                    }
                    view.invalidate();
                    return afterEvaluation;

                case CLEAR:
                    model.setlArg(0);
                    model.setrArg(0);
                    model.setDisplayText("0");
                    view.invalidate();
                    return initialState;

                case CLEAR_EVALUATION:
                    model.setrArg(0);
                    model.setDisplayText("0");
                    view.invalidate();
                    return afterSingSelection;

                case BACK_SPACE:
                    return afterEvaluation;

                case PERCENT:
                    model.setDisplayText(renderDouble(model.getlArg() / 100 * parseDouble(model.getDisplayText())));
                    view.invalidate();
                    return afterEvaluation;

                case SQUARE_ROOT:
                    try {
                        if (model.getDisplayText().startsWith("-")) {
                            throw new ArithmeticException("Negative square root");
                        }

                        model.setDisplayText(renderDouble(sqrt(parseDouble(model.getDisplayText()))));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.invalidate();
                        return errorInputState;
                    }
                    view.invalidate();
                    return afterEvaluation;

                case REVERSE:
                    if (!"0".equals(model.getDisplayText())) {
                        if (model.getDisplayText().startsWith("-")) {
                            model.setDisplayText(model.getDisplayText().substring(1));
                        } else {
                            model.setDisplayText("-" + model.getDisplayText());
                        }
                    }
                    view.invalidate();
                    return afterEvaluation;

                case INVERSE:
                    try {
                        model.setDisplayText(renderDouble(1D / parseDouble(model.getDisplayText())));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.invalidate();
                        return errorInputState;
                    }
                    view.invalidate();
                    return afterEvaluation;

                case MEMORY_CLEAR:
                    model.setMemory(0);
                    view.invalidate();
                    return afterEvaluation;

                case MEMORY_STORE:
                    model.setMemory(parseDouble(model.getDisplayText()));
                    view.invalidate();
                    return afterEvaluation;

                case MEMORY_RESTORE:
                    try {
                        model.setDisplayText(renderDouble(model.getMemory()));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.invalidate();
                        return errorInputState;
                    }
                    view.invalidate();
                    return afterEvaluation;

                case MEMORY_PLUS:
                    model.setMemory(model.getMemory() + parseDouble(model.getDisplayText()));
                    view.invalidate();
                    return afterEvaluation;

                case MEMORY_MINUS:
                    model.setMemory(model.getMemory() - parseDouble(model.getDisplayText()));
                    view.invalidate();
                    return afterEvaluation;
            }

            throw new RuntimeException();
        });
    }

    private State<Signal> createErrorInputState() {
        return new LoggableState<>("errorInputState", (signal) -> {
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
        });
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

    protected static class LoggableState<S> implements State<S> {

        protected final String name;
        protected final State<S> decorated;

        public LoggableState(String name, @NotNull State<S> decorated) {
            this.name = name;
            this.decorated = decorated;
        }

        @Override
        @NotNull
        public State<S> react(@NotNull S stimulus) {
            return decorated.react(stimulus);
        }

        @Override
        public String toString() {
            return name;
        }
    }

}
