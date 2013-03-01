package com.myzone.utils.statemachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author: myzone
 * @date: 03.02.13 15:24
 */
public class EventStateMachine<S> implements StateMachine<S> {

    protected static final int SIGNAL_QUEUE_LENGTH = 1024;

    private static final Logger logger = LoggerFactory.getLogger(EventStateMachine.class);

    protected final State.Factory<S> stateFactory;
    protected final BlockingQueue<S> stimulusesQueue;

    public EventStateMachine(State.Factory<S> stateFactory) {
        this.stateFactory = stateFactory;
        this.stimulusesQueue = new ArrayBlockingQueue<>(SIGNAL_QUEUE_LENGTH);
    }

    @Override
    public void run() {
        try {
            State<S> currentState = stateFactory.getStartState();
            while (!currentState.equals(stateFactory.getEndState())) {
                S stimulus = stimulusesQueue.take();
                logger.info("{} stimilus has been taken", stimulus);

                State<S> nextState = currentState.react(stimulus);
                if (!nextState.equals(currentState)) {
                    logger.info("Current state {} have been changed to {}", currentState, nextState);
                }

                currentState = nextState;
            }
        } catch (InterruptedException ignored) {
            // all is ok
            logger.warn("Current thread have been interrupted");
        }
    }

    @Override
    public void process(S signal) {
        stimulusesQueue.add(signal);
    }
}
