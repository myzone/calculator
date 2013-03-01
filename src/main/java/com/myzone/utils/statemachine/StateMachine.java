package com.myzone.utils.statemachine;

/**
 * @author: myzone
 * @date: 03.02.13 15:15
 */
public interface StateMachine<S> extends Runnable {

    void run();

    void process(S signal);

}
