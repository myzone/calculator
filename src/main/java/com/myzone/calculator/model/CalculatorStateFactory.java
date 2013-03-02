package com.myzone.calculator.model;

import com.myzone.calculator.view.CalculatorView;
import com.myzone.utils.statemachine.State;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

/**
 * @author: myzone
 * @date: 03.02.13 23:48
 */
public class CalculatorStateFactory implements State.Factory<Signal> {

    private static final State<Signal> ERR = new State<Signal>() {
        @Override
        public State<Signal> react(Signal stimulus) {
            return this;
        }

        @Override
        public String toString() {
            return "ERR";
        }

        @Override
        public boolean equals(Object obj) {
            return false;
        }
    };

    private static final double MAX_THRESHOLD = Math.pow(10, 10);

    private static final DecimalFormat NORMAL_DECIMAL_FORMAT;
    private static final DecimalFormat SCIENTIFIC_DECIMAL_FORMAT;
    private static final DecimalFormat BIG_SCIENTIFIC_DECIMAL_FORMAT;

    private static final String NORMAL_DECIMAL_FORMAT_PATTERN = "#0.#################";
    private static final String SCIENTIFIC_DECIMAL_FORMAT_PATTERN = "0.000000E00";

    static {
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setDecimalSeparator('.');
        decimalFormatSymbols.setExponentSeparator("e");

        NORMAL_DECIMAL_FORMAT = new DecimalFormat();
        NORMAL_DECIMAL_FORMAT.applyPattern(NORMAL_DECIMAL_FORMAT_PATTERN);
        NORMAL_DECIMAL_FORMAT.setDecimalSeparatorAlwaysShown(false);
        NORMAL_DECIMAL_FORMAT.setDecimalFormatSymbols(decimalFormatSymbols);

        SCIENTIFIC_DECIMAL_FORMAT = new DecimalFormat();
        SCIENTIFIC_DECIMAL_FORMAT.applyPattern(SCIENTIFIC_DECIMAL_FORMAT_PATTERN);
        SCIENTIFIC_DECIMAL_FORMAT.setDecimalSeparatorAlwaysShown(false);
        SCIENTIFIC_DECIMAL_FORMAT.setDecimalFormatSymbols(decimalFormatSymbols);

        DecimalFormatSymbols bigDecimalFormatSymbols = new DecimalFormatSymbols();
        bigDecimalFormatSymbols.setDecimalSeparator('.');
        bigDecimalFormatSymbols.setExponentSeparator("e+");

        BIG_SCIENTIFIC_DECIMAL_FORMAT = new DecimalFormat();
        BIG_SCIENTIFIC_DECIMAL_FORMAT.applyPattern(SCIENTIFIC_DECIMAL_FORMAT_PATTERN);
        BIG_SCIENTIFIC_DECIMAL_FORMAT.setDecimalSeparatorAlwaysShown(false);
        BIG_SCIENTIFIC_DECIMAL_FORMAT.setDecimalFormatSymbols(bigDecimalFormatSymbols);
    }

    protected CalculatorModel model;
    protected CalculatorView view;

    protected State<Signal> initialState;
    protected State<Signal> afterDigitInLArg;
    protected State<Signal> afterDotInLArg;
    protected State<Signal> afterSingSelection;
    protected State<Signal> afterDigitInRArg;
    protected State<Signal> afterDotInRArg;
    protected State<Signal> afterEvaluation;
    protected State<Signal> errorInputState;

    public CalculatorStateFactory(CalculatorModel model, CalculatorView view) {
        this.model = model;
        this.view = view;

        initialState = createInitialState();
        afterDigitInLArg = createAfterDigitInLArg();
        afterDotInLArg = createAfterDotInLArg();
        afterSingSelection = createAfterSingSelection();
        afterDigitInRArg = createAfterDigitInRArg();
        afterDotInRArg = createAfterDotInRArg();
        afterEvaluation = createAfterEvaluation();
        errorInputState = createErrorInputState();
    }

    private State<Signal> createInitialState() {
        return new LoggableState<>("initialState", (signal) -> {
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
                    view.refresh();
                    return afterDigitInLArg;

                case DOT:
                    model.setDisplayText(model.getDisplayText() + signal.getRepresentation());
                    view.refresh();
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
                            view.refresh();
                            return errorInputState;
                        }
                        model.setDisplayText(renderDouble(model.getlArg()));
                        view.refresh();
                        return afterEvaluation;
                    }
                    return initialState;

                case CLEAR:
                    model.setlArg(0);
                    model.setrArg(0);
                    model.setMemory(0);
                    model.setDisplayText("0");
                    view.refresh();
                    return initialState;

                case CLEAR_EVALUATION:
                    model.setlArg(0);
                    model.setDisplayText("0");
                    view.refresh();
                    return initialState;

                case BACK_SPACE:
                    model.setDisplayText(model.getDisplayText().substring(0, model.getDisplayText().length() - 1));
                    if (model.getDisplayText().isEmpty() || "-".equals(model.getDisplayText())) {
                        model.setDisplayText("0");
                    }
                    view.refresh();
                    return initialState;

                case PERCENT:
                    model.setDisplayText(renderDouble(model.getlArg() * parseDouble(model.getDisplayText()) / 100));
                    view.refresh();
                    return initialState;

                case SQUARE_ROOT:
                    try {
                        if (model.getDisplayText().startsWith("-")) {
                            throw new ArithmeticException("Negative square root");
                        }

                        model.setDisplayText(renderDouble(sqrt(parseDouble(model.getDisplayText()))));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.refresh();
                        return errorInputState;
                    }
                    view.refresh();
                    return initialState;

                case REVERSE:
                    return initialState;

                case MEMORY_CLEAR:
                    model.setMemory(0);
                    return initialState;

                case MEMORY_STORE:
                    model.setMemory(parseDouble(model.getDisplayText()));
                    return initialState;

                case MEMORY_RESTORE:
                    try {
                        model.setDisplayText(renderDouble(model.getMemory()));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.refresh();
                        return errorInputState;
                    }
                    view.refresh();
                    return initialState;

                case MEMORY_PLUS:
                    model.setMemory(model.getMemory() + parseDouble(model.getDisplayText()));
                    return initialState;

                case MEMORY_MINUS:
                    model.setMemory(model.getMemory() - parseDouble(model.getDisplayText()));
                    return initialState;

                default:
                    return ERR;
            }
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
                    view.refresh();
                    return afterDigitInLArg;

                case DOT:
                    model.setDisplayText(model.getDisplayText() + signal.getRepresentation());
                    view.refresh();
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
                            view.refresh();
                            return errorInputState;
                        }
                        model.setDisplayText(renderDouble(model.getlArg()));
                        view.refresh();
                        return afterEvaluation;
                    }
                    return initialState;

                case CLEAR:
                    model.setlArg(0);
                    model.setrArg(0);
                    model.setMemory(0);
                    model.setDisplayText("0");
                    view.refresh();
                    return initialState;

                case CLEAR_EVALUATION:
                    model.setlArg(0);
                    model.setDisplayText("0");
                    view.refresh();
                    return initialState;

                case BACK_SPACE:
                    model.setDisplayText(model.getDisplayText().substring(0, model.getDisplayText().length() - 1));
                    if (model.getDisplayText().isEmpty() || "-".equals(model.getDisplayText())) {
                        model.setDisplayText("0");
                    }
                    view.refresh();
                    if (model.getDisplayText().startsWith("-")) {
                        return model.getDisplayText().length() < 3 ? initialState : afterDigitInLArg;
                    }
                    return model.getDisplayText().length() < 3 ? initialState : afterDigitInLArg;

                case PERCENT:
                    model.setDisplayText(renderDouble(model.getlArg() * parseDouble(model.getDisplayText()) / 100));
                    view.refresh();
                    return afterDigitInLArg;

                case SQUARE_ROOT:
                    try {
                        if (model.getDisplayText().startsWith("-")) {
                            throw new ArithmeticException("Negative square root");
                        }

                        model.setDisplayText(renderDouble(sqrt(parseDouble(model.getDisplayText()))));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.refresh();
                        return errorInputState;
                    }
                    view.refresh();
                    return initialState;

                case REVERSE:
                    if(model.getDisplayText().startsWith("-")) {
                        model.setDisplayText(model.getDisplayText().substring(1));
                    } else {
                        model.setDisplayText("-" + model.getDisplayText());
                    }
                    view.refresh();
                    return afterDigitInLArg;

                case MEMORY_CLEAR:
                    model.setMemory(0);
                    return afterDigitInLArg;

                case MEMORY_STORE:
                    model.setMemory(parseDouble(model.getDisplayText()));
                    return afterDigitInLArg;

                case MEMORY_RESTORE:
                    try {
                        model.setDisplayText(renderDouble(model.getMemory()));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.refresh();
                        return errorInputState;
                    }
                    view.refresh();
                    return initialState;

                case MEMORY_PLUS:
                    model.setMemory(model.getMemory() + parseDouble(model.getDisplayText()));
                    return afterDigitInLArg;

                case MEMORY_MINUS:
                    model.setMemory(model.getMemory() - parseDouble(model.getDisplayText()));
                    return afterDigitInLArg;

                default:
                    return ERR;
            }
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
                    view.refresh();
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
                            view.refresh();
                            return errorInputState;
                        }
                        model.setDisplayText(renderDouble(model.getlArg()));
                        view.refresh();
                        return afterEvaluation;
                    }
                    return initialState;

                case CLEAR:
                    model.setlArg(0);
                    model.setrArg(0);
                    model.setMemory(0);
                    model.setDisplayText("0");
                    view.refresh();
                    return initialState;

                case CLEAR_EVALUATION:
                    model.setlArg(0);
                    model.setDisplayText("0");
                    view.refresh();
                    return initialState;

                case BACK_SPACE:
                    if (model.getDisplayText().charAt(model.getDisplayText().length() - 1) == '.') {
                        model.setDisplayText(model.getDisplayText().substring(0, model.getDisplayText().length() - 1));
                        view.refresh();
                        return afterDigitInLArg;
                    }
                    model.setDisplayText(model.getDisplayText().substring(0, model.getDisplayText().length() - 1));
                    view.refresh();
                    return afterDotInLArg;

                case PERCENT:
                    model.setDisplayText(renderDouble(model.getlArg() * parseDouble(model.getDisplayText()) / 100));
                    view.refresh();
                    return afterDotInLArg;

                case SQUARE_ROOT:
                    try {
                        if (model.getDisplayText().startsWith("-")) {
                            throw new ArithmeticException("Negative square root");
                        }

                        model.setDisplayText(renderDouble(sqrt(parseDouble(model.getDisplayText()))));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.refresh();
                        return errorInputState;
                    }
                    view.refresh();
                    return afterDotInLArg;

                case REVERSE:
                    if(model.getDisplayText().startsWith("-")) {
                        model.setDisplayText(model.getDisplayText().substring(1));
                    } else {
                        model.setDisplayText("-" + model.getDisplayText());
                    }
                    view.refresh();
                    return afterDotInLArg;

                case MEMORY_CLEAR:
                    model.setMemory(0);
                    return afterDotInLArg;

                case MEMORY_STORE:
                    model.setMemory(parseDouble(model.getDisplayText()));
                    return afterDotInLArg;

                case MEMORY_RESTORE:
                    try {
                        model.setDisplayText(renderDouble(model.getMemory()));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.refresh();
                        return errorInputState;
                    }
                    view.refresh();
                    return initialState;

                case MEMORY_PLUS:
                    model.setMemory(model.getMemory() + parseDouble(model.getDisplayText()));
                    return afterDotInLArg;

                case MEMORY_MINUS:
                    model.setMemory(model.getMemory() - parseDouble(model.getDisplayText()));
                    return afterDotInLArg;

                default:
                    return ERR;
            }
        });
    }

    private State<Signal> createAfterSingSelection() {
        return new LoggableState<>("afterSingSelection", (signal) -> {
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
                    view.refresh();
                    return afterDigitInRArg;

                case DOT:
                    model.setDisplayText("0" + signal.getRepresentation());
                    view.refresh();
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
                        view.refresh();
                        return errorInputState;
                    }
                    model.setDisplayText(renderDouble(model.getlArg()));
                    view.refresh();
                    return afterEvaluation;

                case CLEAR:
                    model.setlArg(0);
                    model.setrArg(0);
                    model.setMemory(0);
                    model.setDisplayText("0");
                    view.refresh();
                    return initialState;

                case CLEAR_EVALUATION:
                    model.setrArg(0);
                    model.setDisplayText("0");
                    view.refresh();
                    return afterSingSelection;

                case BACK_SPACE:
                    model.setDisplayText(model.getDisplayText().substring(0, model.getDisplayText().length() - 1));
                    if (model.getDisplayText().isEmpty() || "-".equals(model.getDisplayText())) {
                        model.setDisplayText("0");
                    }
                    view.refresh();
                    return afterSingSelection;

                case PERCENT:
                    model.setDisplayText(renderDouble(model.getlArg() * parseDouble(model.getDisplayText()) / 100));
                    view.refresh();
                    return afterSingSelection;

                case SQUARE_ROOT:
                    try {
                        if (model.getDisplayText().startsWith("-")) {
                            throw new ArithmeticException("Negative square root");
                        }

                        model.setDisplayText(renderDouble(sqrt(parseDouble(model.getDisplayText()))));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.refresh();
                        return errorInputState;
                    }
                    view.refresh();
                    return afterSingSelection;

                case REVERSE:
                    if(model.getDisplayText().startsWith("-")) {
                        model.setDisplayText(model.getDisplayText().substring(1));
                    } else {
                        model.setDisplayText("-" + model.getDisplayText());
                    }
                    view.refresh();
                    return afterSingSelection;

                case MEMORY_CLEAR:
                    model.setMemory(0);
                    return afterSingSelection;

                case MEMORY_STORE:
                    model.setMemory(parseDouble(model.getDisplayText()));
                    return afterSingSelection;

                case MEMORY_RESTORE:
                    try {
                        model.setDisplayText(renderDouble(model.getMemory()));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.refresh();
                        return errorInputState;
                    }
                    view.refresh();
                    return afterSingSelection;

                case MEMORY_PLUS:
                    model.setMemory(model.getMemory() + parseDouble(model.getDisplayText()));
                    return afterSingSelection;

                case MEMORY_MINUS:
                    model.setMemory(model.getMemory() - parseDouble(model.getDisplayText()));
                    return afterSingSelection;

                default:
                    return ERR;
            }
        });
    }

    private State<Signal> createAfterDigitInRArg() {
        return new LoggableState<>("afterDigitInRArg", (signal) -> {
            switch (signal) {
                case DIGIT_0:
                    if ("0".equals(model.getDisplayText()))
                        return afterDigitInRArg;
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
                    view.refresh();
                    return afterDigitInRArg;

                case DOT:
                    model.setDisplayText(model.getDisplayText() + signal.getRepresentation());
                    view.refresh();
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
                        view.refresh();
                        return errorInputState;
                    }
                    model.setDisplayText(renderDouble(model.getlArg()));
                    model.setOperation(CalculatorModel.Operation.bySignal(signal));
                    view.refresh();
                    return afterSingSelection;

                case EVALUATE:
                    model.setrArg(parseDouble(model.getDisplayText()));
                    try {
                        model.setlArg(model.getOperation().evaluate(model.getlArg(), model.getrArg()));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.refresh();
                        return errorInputState;
                    }
                    model.setDisplayText(renderDouble(model.getlArg()));
                    view.refresh();
                    return afterEvaluation;

                case CLEAR:
                    model.setlArg(0);
                    model.setrArg(0);
                    model.setMemory(0);
                    model.setDisplayText("0");
                    view.refresh();
                    return initialState;

                case CLEAR_EVALUATION:
                    model.setrArg(0);
                    model.setDisplayText("0");
                    view.refresh();
                    return afterSingSelection;

                case BACK_SPACE:
                    model.setDisplayText(model.getDisplayText().substring(0, model.getDisplayText().length() - 1));
                    if (model.getDisplayText().isEmpty() || "-".equals(model.getDisplayText())) {
                        model.setDisplayText("0");
                    }
                    view.refresh();
                    if (model.getDisplayText().startsWith("-")) {
                        return model.getDisplayText().length() < 3 ? initialState : afterDigitInLArg;
                    }
                    return model.getDisplayText().length() < 3 ? initialState : afterDigitInLArg;

                case PERCENT:
                    try {
                        model.setDisplayText(renderDouble(model.getlArg() * parseDouble(model.getDisplayText()) / 100));
                        view.refresh();
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.refresh();
                        return errorInputState;
                    }
                    return afterDigitInRArg;

                case SQUARE_ROOT:
                    try {
                        if (model.getDisplayText().startsWith("-")) {
                            throw new ArithmeticException("Negative square root");
                        }

                        model.setDisplayText(renderDouble(sqrt(parseDouble(model.getDisplayText()))));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.refresh();
                        return errorInputState;
                    }
                    view.refresh();

                case REVERSE:
                    if(model.getDisplayText().startsWith("-")) {
                        model.setDisplayText(model.getDisplayText().substring(1));
                    } else {
                        model.setDisplayText("-" + model.getDisplayText());
                    }
                    view.refresh();
                    return afterDigitInRArg;

                case MEMORY_CLEAR:
                    model.setMemory(0);
                    return afterDigitInRArg;

                case MEMORY_STORE:
                    model.setMemory(parseDouble(model.getDisplayText()));
                    return afterDigitInRArg;

                case MEMORY_RESTORE:
                    try {
                        model.setDisplayText(renderDouble(model.getMemory()));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.refresh();
                        return errorInputState;
                    }
                    view.refresh();
                    return afterSingSelection;

                case MEMORY_PLUS:
                    model.setMemory(model.getMemory() + parseDouble(model.getDisplayText()));
                    return afterDigitInRArg;

                case MEMORY_MINUS:
                    model.setMemory(model.getMemory() - parseDouble(model.getDisplayText()));
                    return afterDigitInRArg;

                default:
                    return ERR;
            }
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
                    view.refresh();
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
                        view.refresh();
                        return errorInputState;
                    }
                    model.setDisplayText(renderDouble(model.getlArg()));
                    model.setOperation(CalculatorModel.Operation.bySignal(signal));
                    view.refresh();
                    return afterSingSelection;

                case EVALUATE:
                    model.setrArg(parseDouble(model.getDisplayText()));
                    try {
                        model.setlArg(model.getOperation().evaluate(model.getlArg(), model.getrArg()));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.refresh();
                        return errorInputState;
                    }
                    model.setDisplayText(renderDouble(model.getlArg()));
                    view.refresh();
                    return afterEvaluation;

                case CLEAR:
                    model.setlArg(0);
                    model.setrArg(0);
                    model.setMemory(0);
                    model.setDisplayText("0");
                    view.refresh();
                    return initialState;

                case CLEAR_EVALUATION:
                    model.setrArg(0);
                    model.setDisplayText("0");
                    view.refresh();
                    return afterSingSelection;

                case BACK_SPACE:
                    if (model.getDisplayText().charAt(model.getDisplayText().length() - 1) == '.') {
                        model.setDisplayText(model.getDisplayText().substring(0, model.getDisplayText().length() - 1));
                        view.refresh();
                        return afterDigitInRArg;
                    }
                    model.setDisplayText(model.getDisplayText().substring(0, model.getDisplayText().length() - 1));
                    view.refresh();
                    return afterDotInRArg;

                case PERCENT:
                    model.setDisplayText(renderDouble(model.getlArg() * parseDouble(model.getDisplayText()) / 100));
                    view.refresh();
                    return afterDotInRArg;

                case SQUARE_ROOT:
                    try {
                        if (model.getDisplayText().startsWith("-")) {
                            throw new ArithmeticException("Negative square root");
                        }

                        model.setDisplayText(renderDouble(sqrt(parseDouble(model.getDisplayText()))));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.refresh();
                        return errorInputState;
                    }
                    view.refresh();
                    return afterDotInRArg;

                case REVERSE:
                    if(model.getDisplayText().startsWith("-")) {
                        model.setDisplayText(model.getDisplayText().substring(1));
                    } else {
                        model.setDisplayText("-" + model.getDisplayText());
                    }
                    view.refresh();
                    return afterDotInRArg;

                case MEMORY_CLEAR:
                    model.setMemory(0);
                    return afterDotInRArg;

                case MEMORY_STORE:
                    model.setMemory(parseDouble(model.getDisplayText()));
                    return afterDotInRArg;

                case MEMORY_RESTORE:
                    try {
                        model.setDisplayText(renderDouble(model.getMemory()));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.refresh();
                        return errorInputState;
                    }
                    view.refresh();
                    return afterSingSelection;

                case MEMORY_PLUS:
                    model.setMemory(model.getMemory() + parseDouble(model.getDisplayText()));
                    return initialState;

                case MEMORY_MINUS:
                    model.setMemory(model.getMemory() - parseDouble(model.getDisplayText()));
                    return afterDotInRArg;

                default:
                    return ERR;
            }
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
                    view.refresh();
                    return afterDigitInLArg;

                case DOT:
                    model.setDisplayText("0" + signal.getRepresentation());
                    view.refresh();
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
                        view.refresh();
                        return errorInputState;
                    }
                    view.refresh();
                    return afterEvaluation;

                case CLEAR:
                    model.setlArg(0);
                    model.setrArg(0);
                    model.setMemory(0);
                    model.setDisplayText("0");
                    view.refresh();
                    return initialState;

                case CLEAR_EVALUATION:
                    model.setrArg(0);
                    model.setDisplayText("0");
                    view.refresh();
                    return afterSingSelection;

                case BACK_SPACE:
                    return afterEvaluation;

                case PERCENT:
                    model.setDisplayText(renderDouble(model.getlArg() * parseDouble(model.getDisplayText()) / 100));
                    view.refresh();
                    return afterEvaluation;

                case SQUARE_ROOT:
                    try {
                        if (model.getDisplayText().startsWith("-")) {
                            throw new ArithmeticException("Negative square root");
                        }

                        model.setDisplayText(renderDouble(sqrt(parseDouble(model.getDisplayText()))));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.refresh();
                        return errorInputState;
                    }
                    view.refresh();
                    return afterEvaluation;

                case REVERSE:
                    if (!"0".equals(model.getDisplayText())) {
                        if(model.getDisplayText().startsWith("-")) {
                            model.setDisplayText(model.getDisplayText().substring(1));
                        } else {
                            model.setDisplayText("-" + model.getDisplayText());
                        }
                        view.refresh();
                    }
                    return afterEvaluation;

                case MEMORY_CLEAR:
                    model.setMemory(0);
                    return afterEvaluation;

                case MEMORY_STORE:
                    model.setMemory(parseDouble(model.getDisplayText()));
                    return afterEvaluation;

                case MEMORY_RESTORE:
                    try {
                        model.setDisplayText(renderDouble(model.getMemory()));
                    } catch (Exception e) {
                        model.setDisplayText("ERR");
                        view.refresh();
                        return errorInputState;
                    }
                    view.refresh();
                    return afterEvaluation;

                case MEMORY_PLUS:
                    model.setMemory(model.getMemory() + parseDouble(model.getDisplayText()));
                    return afterEvaluation;

                case MEMORY_MINUS:
                    model.setMemory(model.getMemory() - parseDouble(model.getDisplayText()));
                    return afterEvaluation;

                default:
                    return ERR;
            }
        });
    }

    private State<Signal> createErrorInputState() {
        return new LoggableState<>("errorInputState", (signal) -> {
            switch (signal) {
                case CLEAR:
                    model.setMemory(0);
                case CLEAR_EVALUATION:
                    model.setlArg(0);
                    model.setrArg(0);
                    model.setDisplayText("0");
                    view.refresh();
                    return initialState;

                default:
                    return errorInputState;
            }
        });
    }

    @Override
    public State<Signal> getStartState() {
        return initialState;
    }

    @Override
    public State<Signal> getEndState() {
        // this state machine have not any end state, so it's fake end state
        return new State<Signal>() {
            @Override
            public State<Signal> react(Signal stimulus) {
                return this;
            }
        };
    }

    protected static class LoggableState<S> implements State<S> {

        protected final String name;
        protected final State<S> decorated;

        public LoggableState(String name, State<S> decorated) {
            this.name = name;
            this.decorated = decorated;
        }

        @Override
        public State<S> react(S stimulus) {
            return decorated.react(stimulus);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static double parseDouble(String s) {
        try {
            return (s.contains("e+")
                    ? BIG_SCIENTIFIC_DECIMAL_FORMAT
                    : (s.contains("e")
                    ? SCIENTIFIC_DECIMAL_FORMAT
                    : NORMAL_DECIMAL_FORMAT)).parse(s).doubleValue();
        } catch (ParseException e) {
            return Double.NaN;
        }
    }

    private static String renderDouble(double d) {
        if (Double.isNaN(d) || Double.isInfinite(d)) {
            throw new ArithmeticException("Double is NaN");
        }

        return (abs(d) < MAX_THRESHOLD
                ? NORMAL_DECIMAL_FORMAT
                : (abs(d) > 1
                ? BIG_SCIENTIFIC_DECIMAL_FORMAT
                : SCIENTIFIC_DECIMAL_FORMAT)).format(d);
    }

}
