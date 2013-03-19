package com.myzone.calculator.view;

import com.google.common.collect.ImmutableMap;
import com.myzone.calculator.model.CalculatorModel;
import com.myzone.calculator.model.CalculatorStateFactory;
import com.myzone.calculator.model.Signal;
import com.myzone.utils.statemachine.EventStateMachine;
import com.myzone.utils.statemachine.StateMachine;
import com.sun.javafx.scene.CssFlags;
import javafx.application.Application;
import javafx.css.CssMetaData;
import javafx.css.StyleOrigin;
import javafx.css.Styleable;
import javafx.css.StyleableProperty;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFieldBuilder;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Map;

import static com.myzone.calculator.model.Signal.*;

/**
 * @author: myzone
 * @date: 03.02.13 22:38
 */
public class CalculatorView extends Application {

    private static final double SPACING_SIZE = 5;
    private static final double PREF_COLUMN_HEIGHT = 28;
    private static final double PREF_COLUMN_WIDTH = 45;

    private static final Logger logger = LoggerFactory.getLogger(CalculatorView.class);

    private CalculatorModel model;
    private StateMachine<Signal> controller;

    private final Thread stateMachineThread;
    private final Map<String, SignalEmitter<KeyEvent>> signalEmittersMap;

    private final TextField memoryDisplayTextField;
    private final TextField mainDisplayTextField;

    public CalculatorView() {
        model = new CalculatorModel();
        controller = new EventStateMachine<>(new CalculatorStateFactory(model, this));

        stateMachineThread = new Thread(controller, "StateMachine Thread");
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
                .prefHeight(PREF_COLUMN_HEIGHT + SPACING_SIZE)
                .prefColumnCount(2)
                .alignment(Pos.CENTER)
                .text("")
                .build();

        mainDisplayTextField = TextFieldBuilder
                .create()
                .editable(false)
                .prefHeight(PREF_COLUMN_HEIGHT + SPACING_SIZE)
                .prefColumnCount(22)
                .cache(false)
                .alignment(Pos.CENTER_RIGHT)
                .text("0")
                .onKeyReleased((event) -> {
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
                })
                .build();

    }

    @Override
    public void start(Stage stage) throws Exception {
        stateMachineThread.start();

        VBox mainContainer = VBoxBuilder
                .create()
                .padding(new Insets(10.0))
                .spacing(SPACING_SIZE)
                .children(
                        HBoxBuilder
                                .create()
                                .children(
                                        memoryDisplayTextField,
                                        mainDisplayTextField
                                )
                                .build(),
                        HBoxBuilder
                                .create()
                                .spacing(SPACING_SIZE)
                                .children(
                                        VBoxBuilder
                                                .create()
                                                .spacing(SPACING_SIZE)
                                                .children(
                                                        ButtonBuilder
                                                                .create()
                                                                .prefHeight(PREF_COLUMN_HEIGHT)
                                                                .minWidth(PREF_COLUMN_WIDTH)
                                                                .text("←")
                                                                .onMouseClicked(new SignalEmitter<>(BACK_SPACE))
                                                                .focusTraversable(false)
                                                                .build(),
                                                        ButtonBuilder
                                                                .create()
                                                                .prefHeight(PREF_COLUMN_HEIGHT)
                                                                .minWidth(PREF_COLUMN_WIDTH)
                                                                .text("7")
                                                                .onMouseClicked(new SignalEmitter<>(DIGIT_7))
                                                                .focusTraversable(false)
                                                                .build(),
                                                        ButtonBuilder
                                                                .create()
                                                                .prefHeight(PREF_COLUMN_HEIGHT)
                                                                .minWidth(PREF_COLUMN_WIDTH)
                                                                .text("4")
                                                                .onMouseClicked(new SignalEmitter<>(DIGIT_4))
                                                                .focusTraversable(false)
                                                                .build(),
                                                        ButtonBuilder
                                                                .create()
                                                                .prefHeight(PREF_COLUMN_HEIGHT)
                                                                .minWidth(PREF_COLUMN_WIDTH)
                                                                .text("1")
                                                                .onMouseClicked(new SignalEmitter<>(DIGIT_1))
                                                                .focusTraversable(false)
                                                                .build()
                                                )
                                                .focusTraversable(false)
                                                .build(),
                                        VBoxBuilder
                                                .create()
                                                .spacing(SPACING_SIZE)
                                                .children(
                                                        ButtonBuilder
                                                                .create()
                                                                .prefHeight(PREF_COLUMN_HEIGHT)
                                                                .minWidth(PREF_COLUMN_WIDTH)
                                                                .text("C")
                                                                .onMouseClicked(new SignalEmitter<>(CLEAR))
                                                                .focusTraversable(false)
                                                                .build(),
                                                        ButtonBuilder
                                                                .create()
                                                                .prefHeight(PREF_COLUMN_HEIGHT)
                                                                .minWidth(PREF_COLUMN_WIDTH)
                                                                .text("8")
                                                                .onMouseClicked(new SignalEmitter<>(DIGIT_8))
                                                                .focusTraversable(false)
                                                                .build(),
                                                        ButtonBuilder
                                                                .create()
                                                                .prefHeight(PREF_COLUMN_HEIGHT)
                                                                .minWidth(PREF_COLUMN_WIDTH)
                                                                .text("5")
                                                                .onMouseClicked(new SignalEmitter<>(DIGIT_5))
                                                                .focusTraversable(false)
                                                                .build(),
                                                        ButtonBuilder
                                                                .create()
                                                                .prefHeight(PREF_COLUMN_HEIGHT)
                                                                .minWidth(PREF_COLUMN_WIDTH)
                                                                .text("2")
                                                                .onMouseClicked(new SignalEmitter<>(DIGIT_2))
                                                                .focusTraversable(false)
                                                                .build()
                                                )
                                                .focusTraversable(false)
                                                .build(),
                                        VBoxBuilder
                                                .create()
                                                .spacing(SPACING_SIZE)
                                                .children(
                                                        ButtonBuilder
                                                                .create()
                                                                .prefHeight(PREF_COLUMN_HEIGHT)
                                                                .minWidth(PREF_COLUMN_WIDTH)
                                                                .text("CE")
                                                                .onMouseClicked(new SignalEmitter<>(CLEAR_EVALUATION))
                                                                .focusTraversable(false)
                                                                .build(),
                                                        ButtonBuilder
                                                                .create()
                                                                .prefHeight(PREF_COLUMN_HEIGHT)
                                                                .minWidth(PREF_COLUMN_WIDTH)
                                                                .text("9")
                                                                .onMouseClicked(new SignalEmitter<>(DIGIT_9))
                                                                .focusTraversable(false)
                                                                .build(),
                                                        ButtonBuilder
                                                                .create()
                                                                .prefHeight(PREF_COLUMN_HEIGHT)
                                                                .minWidth(PREF_COLUMN_WIDTH)
                                                                .text("6")
                                                                .onMouseClicked(new SignalEmitter<>(DIGIT_6))
                                                                .focusTraversable(false)
                                                                .build(),
                                                        ButtonBuilder
                                                                .create()
                                                                .prefHeight(PREF_COLUMN_HEIGHT)
                                                                .minWidth(PREF_COLUMN_WIDTH)
                                                                .text("3")
                                                                .onMouseClicked(new SignalEmitter<>(DIGIT_3))
                                                                .focusTraversable(false)
                                                                .build()
                                                )
                                                .focusTraversable(false).build(),
                                        VBoxBuilder
                                                .create()
                                                .spacing(SPACING_SIZE)
                                                .children(
                                                        ButtonBuilder
                                                                .create()
                                                                .prefHeight(PREF_COLUMN_HEIGHT)
                                                                .minWidth(PREF_COLUMN_WIDTH)
                                                                .text("/")
                                                                .onMouseClicked(new SignalEmitter<>(DIVIDE))
                                                                .focusTraversable(false)
                                                                .build(),
                                                        ButtonBuilder
                                                                .create()
                                                                .prefHeight(PREF_COLUMN_HEIGHT)
                                                                .minWidth(PREF_COLUMN_WIDTH)
                                                                .text("*")
                                                                .onMouseClicked(new SignalEmitter<>(MULTIPLY))
                                                                .focusTraversable(false)
                                                                .build(),
                                                        ButtonBuilder
                                                                .create()
                                                                .prefHeight(PREF_COLUMN_HEIGHT)
                                                                .minWidth(PREF_COLUMN_WIDTH)
                                                                .text("-")
                                                                .onMouseClicked(new SignalEmitter<>(MINUS))
                                                                .focusTraversable(false)
                                                                .build(),
                                                        ButtonBuilder
                                                                .create()
                                                                .prefHeight(PREF_COLUMN_HEIGHT)
                                                                .minWidth(PREF_COLUMN_WIDTH)
                                                                .text("+")
                                                                .onMouseClicked(new SignalEmitter<>(PLUS))
                                                                .focusTraversable(false)
                                                                .build()
                                                )
                                                .focusTraversable(false).build(),
                                        VBoxBuilder
                                                .create()
                                                .spacing(SPACING_SIZE)
                                                .children(
                                                        ButtonBuilder
                                                                .create()
                                                                .prefHeight(PREF_COLUMN_HEIGHT)
                                                                .minWidth(PREF_COLUMN_WIDTH)
                                                                .text("±")
                                                                .onMouseClicked(new SignalEmitter<>(REVERSE))
                                                                .focusTraversable(false)
                                                                .build(),
                                                        ButtonBuilder
                                                                .create()
                                                                .prefHeight(PREF_COLUMN_HEIGHT)
                                                                .minWidth(PREF_COLUMN_WIDTH)
                                                                .text("xˉ¹")
                                                                .onMouseClicked(new SignalEmitter<>(INVERSE))
                                                                .focusTraversable(false)
                                                                .build(),
                                                        ButtonBuilder
                                                                .create()
                                                                .prefHeight(PREF_COLUMN_HEIGHT)
                                                                .minWidth(PREF_COLUMN_WIDTH)
                                                                .text("%")
                                                                .onMouseClicked(new SignalEmitter<>(PERCENT))
                                                                .focusTraversable(false)
                                                                .build(),
                                                        ButtonBuilder
                                                                .create()
                                                                .prefHeight(PREF_COLUMN_HEIGHT)
                                                                .minWidth(PREF_COLUMN_WIDTH)
                                                                .text("√")
                                                                .onMouseClicked(new SignalEmitter<>(SQUARE_ROOT))
                                                                .focusTraversable(false)
                                                                .build()
                                                )
                                                .focusTraversable(false)
                                                .build(),
                                        VBoxBuilder
                                                .create()
                                                .spacing(SPACING_SIZE)
                                                .children(
                                                        ButtonBuilder
                                                                .create()
                                                                .prefHeight(PREF_COLUMN_HEIGHT)
                                                                .minWidth(PREF_COLUMN_WIDTH)
                                                                .text("MR")
                                                                .onMouseClicked(new SignalEmitter<>(MEMORY_RESTORE))
                                                                .focusTraversable(false)
                                                                .build(),
                                                        ButtonBuilder
                                                                .create()
                                                                .prefHeight(PREF_COLUMN_HEIGHT)
                                                                .minWidth(PREF_COLUMN_WIDTH)
                                                                .text("MC")
                                                                .onMouseClicked(new SignalEmitter<>(MEMORY_CLEAR))
                                                                .focusTraversable(false)
                                                                .build(),
                                                        ButtonBuilder
                                                                .create()
                                                                .prefHeight(PREF_COLUMN_HEIGHT)
                                                                .minWidth(PREF_COLUMN_WIDTH)
                                                                .text("MS")
                                                                .onMouseClicked(new SignalEmitter<>(MEMORY_STORE))
                                                                .focusTraversable(false)
                                                                .build(),
                                                        ButtonBuilder
                                                                .create()
                                                                .prefHeight(PREF_COLUMN_HEIGHT)
                                                                .minWidth(PREF_COLUMN_WIDTH)
                                                                .text("M+")
                                                                .onMouseClicked(new SignalEmitter<>(MEMORY_PLUS))
                                                                .focusTraversable(false)
                                                                .build()
                                                )
                                                .focusTraversable(false)
                                                .build()
                                )
                                .focusTraversable(false)
                                .build(),
                        HBoxBuilder
                                .create()
                                .spacing(SPACING_SIZE)
                                .children(
                                        ButtonBuilder
                                                .create()
                                                .prefHeight(PREF_COLUMN_HEIGHT)
                                                .minWidth(PREF_COLUMN_WIDTH * 2 + SPACING_SIZE)
                                                .text("0")
                                                .onMouseClicked(new SignalEmitter<>(DIGIT_0))
                                                .focusTraversable(false)
                                                .build(),
                                        ButtonBuilder
                                                .create()
                                                .prefHeight(PREF_COLUMN_HEIGHT)
                                                .minWidth(PREF_COLUMN_WIDTH)
                                                .text(".")
                                                .onMouseClicked(new SignalEmitter<>(DOT))
                                                .focusTraversable(false)
                                                .build(),
                                        ButtonBuilder
                                                .create()
                                                .prefHeight(PREF_COLUMN_HEIGHT)
                                                .minWidth(PREF_COLUMN_WIDTH * 2 + SPACING_SIZE)
                                                .text("=")
                                                .onMouseClicked(new SignalEmitter<>(EVALUATE))
                                                .focusTraversable(false)
                                                .build(),
                                        ButtonBuilder
                                                .create()
                                                .prefHeight(PREF_COLUMN_HEIGHT)
                                                .minWidth(PREF_COLUMN_WIDTH)
                                                .text("M-")
                                                .onMouseClicked(new SignalEmitter<>(MEMORY_MINUS))
                                                .focusTraversable(false)
                                                .build()
                                )
                                .focusTraversable(false)
                                .build()
                )
                .focusTraversable(false)
                .build();

        stage.setOnCloseRequest((event) -> stateMachineThread.interrupt());
        stage.setScene(new Scene(mainContainer));
        stage.setResizable(false);
        stage.show();
    }

    public synchronized void invalidate() {
        memoryDisplayTextField.setText(model.getMemory() != 0 ? "M" : "");
        mainDisplayTextField.setText(model.getDisplayText());
    }

    protected class SignalEmitter<E extends Event> extends StimulusEmitter<Signal, E> {

        public SignalEmitter(Signal signal) {
            super(controller, signal);
        }

        @Override
        public void handle(E event) {
            logger.info("Signal {} has been emitted by {}", stimulus, event.getSource());

            super.handle(event);
        }
    }

}