package com.myzone.calculator.view;

import com.myzone.utils.statemachine.StateMachine;
import javafx.event.Event;
import javafx.event.EventHandler;
import org.jetbrains.annotations.NotNull;

/**
 * @author: myzone
 * @date: 22.02.13 0:15
 */
public class StimulusEmitter<S, E extends Event> implements EventHandler<E> {

    protected final StateMachine<S> stateMachine;
    protected final S stimulus;

    public StimulusEmitter(@NotNull StateMachine<S> stateMachine, @NotNull S stimulus) {
        this.stateMachine = stateMachine;
        this.stimulus = stimulus;
    }

    @Override
    public void handle(E e) {
        stateMachine.process(stimulus);
    }

}
