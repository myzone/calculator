package com.myzone.utils.statemachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * @author: myzone
 * @date: 20.02.13 1:36
 */
public class TestEventStateMachine<S> extends EventStateMachine<S> {

    private static final Logger logger = LoggerFactory.getLogger(TestEventStateMachine.class);

    public TestEventStateMachine(State.Factory<S> stateFactory) {
        super(stateFactory);
    }

    public int run(int stimulusesLeft) {
        try {
            State<S> currentState = stateFactory.getStartState();
            while (stimulusesLeft-- > 0 && !currentState.equals(stateFactory.getEndState())) {
                logger.info("{} test stimuluses left", stimulusesLeft + 1);

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

        if(stimulusesLeft + 1 > 0) {
            logger.warn("{} stimuluses have not processed", stimulusesLeft);
        }

        return stimulusesLeft + 1;
    }

    public S[] run(S... stimuluses) {
        for(S stimulus : stimuluses) {
            process(stimulus);
        }

        return Arrays.copyOfRange(stimuluses, stimuluses.length - run(stimuluses.length), stimuluses.length);
    }

}
