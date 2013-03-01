package com.myzone.utils.statemachine;

import java.util.List;

/**
 * @author: myzone
 * @date: 03.02.13 15:33
 */
public interface State<S> {

    State<S> react(S stimulus);

    default void asd(S... ses) {
        for (S s : ses) {
            react(s);
        }
    }

    interface Factory<S> {

        State<S> getStartState();

        State<S> getEndState();

    }
}
