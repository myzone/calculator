package com.myzone.calculator.view;

import com.google.common.collect.ImmutableMap;
import com.myzone.calculator.controller.CalculatorStateFactory;
import com.myzone.calculator.model.CalculatorModel;
import com.myzone.calculator.model.Signal;
import com.myzone.utils.BigFraction;
import com.myzone.utils.statemachine.EventStateMachine;
import com.myzone.utils.statemachine.StateMachine;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFieldBuilder;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.myzone.calculator.model.Signal.*;

/**
 * @author: myzone
 * @date: 03.02.13 22:38
 */
public class CalculatorView extends Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(CalculatorView.class);

    private static final double SPACING_SIZE = 5;
    private static final double PREF_COLUMN_HEIGHT = 28;
    private static final double PREF_COLUMN_WIDTH = 45;

    private static final String FONT = "LCD Display Grid";
    private static final double FONT_SIZE = 19.5;

    private CalculatorModel model;
    private StateMachine<Signal> stateMachine;

    private final ExecutorService stateMachineThread;
    private final Map<String, SignalEmitter<KeyEvent>> signalEmittersMap;

    private final TextField memoryDisplayTextField;
    private final TextField mainDisplayTextField;

    public CalculatorView() {
        model = new CalculatorModel();
        stateMachine = new EventStateMachine<>(new CalculatorStateFactory(model, this));

        stateMachineThread = Executors.newSingleThreadExecutor();
        signalEmittersMap = ImmutableMap
                .<String, SignalEmitter<KeyEvent>>builder()
                .put("0", new SignalEmitter<>(DIGIT_0))
                .put("1", new SignalEmitter<>(DIGIT_1))
                .put("2", new SignalEmitter<>(DIGIT_2))
                .put("3", new SignalEmitter<>(DIGIT_3))
                .put("4", new SignalEmitter<>(DIGIT_4))
                .put("5", new SignalEmitter<>(DIGIT_5))
                .put("6", new SignalEmitter<>(DIGIT_6))
                .put("7", new SignalEmitter<>(DIGIT_7))
                .put("8", new SignalEmitter<>(DIGIT_8))
                .put("9", new SignalEmitter<>(DIGIT_9))
                .put("+", new SignalEmitter<>(PLUS))
                .put("-", new SignalEmitter<>(MINUS))
                .put("*", new SignalEmitter<>(MULTIPLY))
                .put("/", new SignalEmitter<>(DIVIDE))
                .put("=", new SignalEmitter<>(EVALUATE))
                .put("<", new SignalEmitter<>(BACK_SPACE))
                .put(".", new SignalEmitter<>(DOT))
                .build();

        memoryDisplayTextField = TextFieldBuilder
                .create()
                .editable(false)
                .cache(false)
                .focusTraversable(false)
                .font(Font.font(FONT, FONT_SIZE))
                .prefHeight(PREF_COLUMN_HEIGHT + SPACING_SIZE * 2)
                .prefWidth(PREF_COLUMN_WIDTH)
                .alignment(Pos.CENTER)
                .text("")
                .build();

        mainDisplayTextField = TextFieldBuilder
                .create()
                .editable(false)
                .cache(false)
                .font(Font.font(FONT, FONT_SIZE))
                .focusTraversable(false)
                .prefHeight(PREF_COLUMN_HEIGHT + SPACING_SIZE * 2)
                .prefWidth((PREF_COLUMN_WIDTH + SPACING_SIZE) * 5)
                .alignment(Pos.CENTER_RIGHT)
                .text("0")
                .build();
    }

    @Override
    public void start(Stage stage) throws Exception {
        stateMachineThread.submit(stateMachine);

        VBox mainContainer = createColumn(
                createRow(
                        memoryDisplayTextField,
                        mainDisplayTextField
                ),
                createRow(
                        createColumn(
                                createButton("←", BACK_SPACE),
                                createButton("7", DIGIT_7),
                                createButton("4", DIGIT_4),
                                createButton("1", DIGIT_1)
                        ),
                        createColumn(
                                createButton("C", CLEAR),
                                createButton("8", DIGIT_8),
                                createButton("5", DIGIT_5),
                                createButton("2", DIGIT_2)
                        ),
                        createColumn(
                                createButton("CE", CLEAR_EVALUATION),
                                createButton("9", DIGIT_9),
                                createButton("6", DIGIT_6),
                                createButton("3", DIGIT_3)

                        ),
                        createColumn(
                                createButton("/", DIVIDE),
                                createButton("*", MULTIPLY),
                                createButton("-", MINUS),
                                createButton("+", PLUS)
                        ),
                        createColumn(
                                createButton("±", REVERSE),
                                createButton("xˉ¹", INVERSE),
                                createButton("%", PERCENT),
                                createButton("√", SQUARE_ROOT)
                        ),
                        createColumn(
                                createButton("MR", MEMORY_RESTORE),
                                createButton("MC", MEMORY_CLEAR),
                                createButton("MS", MEMORY_STORE),
                                createButton("M+", MEMORY_PLUS)
                        )
                ),
                createRow(
                        createBigButton("0", DIGIT_0),
                        createButton(".", DOT),
                        createBigButton("=", EVALUATE),
                        createButton("M-", MEMORY_MINUS)
                )
        );

        mainContainer.setPadding(new Insets(30, 0, 15, 15)); // chosen empirically
        mainContainer.setOnKeyReleased((event) -> {
            switch (event.getCode()) {
                case ENTER:
                    signalEmittersMap.get("=").handle(event);
                    break;
                case BACK_SPACE:
                    signalEmittersMap.get("<").handle(event);
                    break;
                default:
                    SignalEmitter<KeyEvent> signalEmitter = signalEmittersMap.get(event.getText());

                    if (signalEmitter != null) {
                        signalEmitter.handle(event);
                    }
            }
        });

        stage.setOnCloseRequest((event) -> stateMachineThread.shutdownNow());
        stage.setScene(new Scene(mainContainer));
        stage.setResizable(false);
        stage.show();
    }

    public void invalidate() {
        try (CalculatorModel.Session session = model.createSession()) {
            BigFraction memory = session.getMemory();
            String displayText = session.getDisplayText();

            Platform.runLater(() -> {
                memoryDisplayTextField.setText(!memory.equals(BigFraction.ZERO) ? "M" : "");
                mainDisplayTextField.setText(displayText);
            });
        }
    }

    protected Button createButton(String text, Signal signal) {
        return ButtonBuilder
                .create()
                .prefHeight(PREF_COLUMN_HEIGHT)
                .minWidth(PREF_COLUMN_WIDTH)
                .text(text)
                .onMouseClicked(new SignalEmitter<>(signal))
                .focusTraversable(false)
                .build();
    }

    protected Button createBigButton(String text, Signal signal) {
        return ButtonBuilder
                .create()
                .prefHeight(PREF_COLUMN_HEIGHT)
                .minWidth(PREF_COLUMN_WIDTH * 2 + SPACING_SIZE)
                .text(text)
                .onMouseClicked(new SignalEmitter<>(signal))
                .focusTraversable(false)
                .build();
    }

    protected VBox createColumn(Node... nodes) {
        return VBoxBuilder
                .create()
                .spacing(SPACING_SIZE)
                .children(nodes)
                .focusTraversable(false)
                .build();
    }

    protected HBox createRow(Node... nodes) {
        return HBoxBuilder
                .create()
                .spacing(SPACING_SIZE)
                .children(nodes)
                .focusTraversable(false)
                .build();
    }

    protected class SignalEmitter<E extends Event> extends StimulusEmitter<Signal, E> {

        public SignalEmitter(Signal signal) {
            super(CalculatorView.this.stateMachine, signal);
        }

        @Override
        public void handle(E event) {
            LOGGER.info("Signal {stimulus} has been emitted by {event}", stimulus, event);

            super.handle(event);
        }
    }

}