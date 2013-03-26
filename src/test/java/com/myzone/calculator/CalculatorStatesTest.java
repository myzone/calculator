package com.myzone.calculator;

import com.myzone.calculator.model.CalculatorModel;
import com.myzone.calculator.model.CalculatorStateFactory;
import com.myzone.calculator.model.Signal;
import com.myzone.calculator.view.CalculatorView;
import com.myzone.utils.statemachine.TestingEventStateMachine;
import org.junit.Before;
import org.junit.Test;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import static com.myzone.calculator.model.Signal.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @author: myzone
 * @date: 20.02.13 1:21
 */
public class CalculatorStatesTest {

    private CalculatorView view;
    private CalculatorModel model;
    private TestingEventStateMachine<Signal> stateMachine;

    @Before
    public void setUp() throws Exception {
        view = mock(CalculatorView.class);
        model = new CalculatorModel();
        stateMachine = new TestingEventStateMachine<>(new CalculatorStateFactory(model, view));
    }

    @Test
    public void testTwoMultiplyTwo() {
        assertEquals(0, stateMachine.run(
                DIGIT_2,
                MULTIPLY,
                DIGIT_2,
                EVALUATE
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("4", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testTwoMultiplyTwoWithManyEvaluations() {
        assertEquals(0, stateMachine.run(
                DIGIT_2,
                MULTIPLY,
                DIGIT_2,
                EVALUATE,
                EVALUATE,
                EVALUATE
        ).length);


        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("16", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testTwoMultiplyWithManyEvaluations() {
        assertEquals(0, stateMachine.run(
                DIGIT_2,
                MULTIPLY,
                EVALUATE,
                EVALUATE,
                EVALUATE
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("16", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testSquare() {
        assertEquals(0, stateMachine.run(
                DIGIT_2,
                MULTIPLY,
                EVALUATE
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("4", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testManyAdds() {
        assertEquals(0, stateMachine.run(
                PLUS,
                DIGIT_5,
                PLUS,
                DIGIT_5,
                PLUS,
                DIGIT_5,
                PLUS,
                DIGIT_5,
                PLUS,
                DIGIT_5,
                EVALUATE
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("25", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testManyAddsAndEvaluations() {
        assertEquals(0, stateMachine.run(
                PLUS,
                DIGIT_5,
                PLUS,
                DIGIT_5,
                EVALUATE,
                PLUS,
                DIGIT_5,
                EVALUATE,
                PLUS,
                DIGIT_5,
                EVALUATE,
                PLUS,
                DIGIT_5,
                EVALUATE
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("25", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testManyStrangeEvaluations() {
        assertEquals(0, stateMachine.run(
                PLUS,
                DIGIT_5,
                PLUS,
                DIGIT_5,
                PLUS,
                DIGIT_5,
                EVALUATE,
                DIGIT_1,
                DIGIT_5,
                EVALUATE,
                DIGIT_2,
                DIGIT_0,
                EVALUATE
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("25", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testManyEvaluations() {
        assertEquals(0, stateMachine.run(
                PLUS,
                DIGIT_5,
                PLUS,
                DIGIT_5,
                EVALUATE,
                DIGIT_3,
                PLUS,
                DIGIT_7,
                EVALUATE
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("10", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testManyEvaluationsWithDot() {
        assertEquals(0, stateMachine.run(
                PLUS,
                DIGIT_5,
                PLUS,
                DIGIT_5,
                EVALUATE,
                DOT,
                DIGIT_2,
                PLUS,
                DIGIT_3,
                EVALUATE
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("3.2", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testComplexEvaluationWithDot() {
        assertEquals(0, stateMachine.run(
                DOT,
                DIGIT_3,
                PLUS,
                DIGIT_0,
                DOT,
                DIGIT_2,
                DOT,
                DIGIT_3,
                DIGIT_5,
                DIVIDE,
                DIGIT_1,
                DIGIT_4,
                DIGIT_7,
                EVALUATE
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("0.0036394557823129", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testPercentAfterEvaluation() {
        assertEquals(0, stateMachine.run(
                DIGIT_1,
                DIGIT_0,
                PLUS,
                DIGIT_1,
                DIGIT_5,
                EVALUATE,
                PERCENT
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("6.25", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testEvaluationAfterPercentAfterEvaluation() {
        assertEquals(0, stateMachine.run(
                DIGIT_1,
                DIGIT_0,
                PLUS,
                DIGIT_1,
                DIGIT_5,
                EVALUATE,
                PERCENT,
                EVALUATE
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("21.25", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testPercentWithOneArg() {
        assertEquals(0, stateMachine.run(
                PLUS,
                DIGIT_1,
                PERCENT
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("0", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testPercent() {
        assertEquals(0, stateMachine.run(
                DIGIT_4,
                DIGIT_8,
                MINUS,
                DIGIT_2,
                PERCENT
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("0.96", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testEvaluationAfterPercent() {
        assertEquals(0, stateMachine.run(
                DIGIT_4,
                DIGIT_8,
                MINUS,
                DIGIT_2,
                PERCENT,
                EVALUATE
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("47.04", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testManyDots() {
        assertEquals(0, stateMachine.run(
                PLUS,
                DIGIT_0,
                DOT,
                DOT,
                DOT
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("0.", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testManyEvaluationsWithPositiveNumber() {
        assertEquals(0, stateMachine.run(
                DIGIT_5,
                DIGIT_6,
                EVALUATE,
                EVALUATE,
                EVALUATE
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("56", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testManyEvaluationsWithNegativeNumber() {
        assertEquals(0, stateMachine.run(
                DIGIT_5,
                DIGIT_6,
                REVERSE,
                EVALUATE,
                EVALUATE,
                EVALUATE
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("-56", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testZeroDivision() {
        assertEquals(0, stateMachine.run(
                DIGIT_5,
                DIGIT_6,
                DIVIDE,
                DIGIT_0,
                EVALUATE
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("ERR", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testSquareRootFromNegativeNumber() {
        assertEquals(0, stateMachine.run(
                DIGIT_5,
                DIGIT_6,
                REVERSE,
                SQUARE_ROOT
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("ERR", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testComplexSquareRootFromNegativeNumber() {
        assertEquals(0, stateMachine.run(
                DIGIT_5,
                DIGIT_6,
                SQUARE_ROOT,
                SQUARE_ROOT,
                MINUS,
                DIGIT_3,
                EVALUATE,
                SQUARE_ROOT
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("ERR", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testSquareRoot() {
        assertEquals(0, stateMachine.run(
                DIGIT_9,
                SQUARE_ROOT,
                PLUS,
                DIGIT_3,
                EVALUATE
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("6", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testNumberAfterSquareRoot() {
        assertEquals(0, stateMachine.run(
                DIGIT_9,
                SQUARE_ROOT,
                DIGIT_3,
                EVALUATE
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("3", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testDoubleSquareRoot() {
        assertEquals(0, stateMachine.run(
                DIGIT_5,
                DIGIT_6,
                SQUARE_ROOT,
                SQUARE_ROOT
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("2.735564799734761", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testTripleSquareRoot() {
        assertEquals(0, stateMachine.run(
                DIGIT_5,
                DIGIT_6,
                SQUARE_ROOT,
                SQUARE_ROOT,
                SQUARE_ROOT
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("1.653954291912192", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testMemory() {
        assertEquals(0, stateMachine.run(
                DIGIT_1,
                DIGIT_0,
                MEMORY_STORE,
                MULTIPLY,
                MEMORY_CLEAR,
                DIGIT_5,
                MEMORY_PLUS,
                EVALUATE,
                SQUARE_ROOT,
                PLUS,
                MEMORY_RESTORE,
                EVALUATE
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("12.07106781186548", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testBackspace() {
        assertEquals(0, stateMachine.run(
                DIGIT_5,
                DIGIT_5,
                DIGIT_5,
                DIGIT_5,
                DIGIT_5,
                BACK_SPACE,
                BACK_SPACE,
                BACK_SPACE,
                BACK_SPACE,
                BACK_SPACE
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("0", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testTooManyBackspace() {
        assertEquals(0, stateMachine.run(
                DIGIT_5,
                DIGIT_5,
                DIGIT_5,
                DIGIT_5,
                DIGIT_5,
                BACK_SPACE,
                BACK_SPACE,
                BACK_SPACE,
                BACK_SPACE,
                BACK_SPACE,
                BACK_SPACE,
                BACK_SPACE,
                DIGIT_7,
                BACK_SPACE,
                BACK_SPACE
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("0", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testBackspaceWithDottedNumber() {
        assertEquals(0, stateMachine.run(
                DIGIT_5,
                DIGIT_5,
                DIGIT_5,
                DIGIT_5,
                DIGIT_5,
                DOT,
                DOT,
                DIGIT_7,
                BACK_SPACE,
                BACK_SPACE,
                BACK_SPACE,
                DIGIT_5,
                DOT,
                DOT,
                DIGIT_2
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("55555.2", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testDoubleReverseAfterDot() {
        assertEquals(0, stateMachine.run(
                DIGIT_5,
                DIGIT_6,
                DOT,
                REVERSE,
                REVERSE
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("56.", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testPositiveOverflow() {
        assertEquals(0, stateMachine.run(
                DIGIT_5,
                DIGIT_5,
                DIGIT_5,
                DIGIT_5,
                DIGIT_5,
                DIGIT_5,
                DIGIT_5,
                DIGIT_5,
                DIGIT_5,
                DIGIT_5,
                DIGIT_5,
                DIGIT_5,
                DIGIT_5,
                DIGIT_5,
                DIGIT_5,
                DIGIT_5,
                PLUS,
                EVALUATE,
                EVALUATE
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("1.666666666666667e+16", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testNegativeOverflow() {
        assertEquals(0, stateMachine.run(
                DIGIT_5,
                DIGIT_5,
                DIGIT_5,
                DIGIT_5,
                DIGIT_5,
                DIGIT_5,
                DIGIT_5,
                DIGIT_5,
                DIGIT_5,
                DIGIT_5,
                DIGIT_5,
                DIGIT_5,
                DIGIT_5,
                DIGIT_5,
                DIGIT_5,
                DIGIT_5,
                REVERSE,
                MINUS,
                REVERSE,
                EVALUATE,
                EVALUATE
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("-1.666666666666667e+16", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testPrecision() {
        assertEquals(0, stateMachine.run(
                DOT,
                DIGIT_0,
                DIGIT_0,
                DIGIT_0,
                DIGIT_0,
                DIGIT_0,
                DIGIT_0,
                DIGIT_0,
                DIGIT_0,
                DIGIT_0,
                DIGIT_0,
                DIGIT_0,
                DIGIT_0,
                DIGIT_0,
                DIGIT_0,
                DIGIT_0,
                DIGIT_0,
                DIGIT_0,
                DIGIT_0,
                DIGIT_0,
                DIGIT_0,
                DIGIT_0,
                DIGIT_0,
                DIGIT_0,
                DIGIT_0,
                DIGIT_0,
                DIGIT_0,
                DIGIT_0,
                DIGIT_0,
                DIGIT_0,
                BACK_SPACE,
                DIGIT_2,
                DIVIDE,
                DIGIT_2,
                EVALUATE
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("0.0000000000000001", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testPercentAfterSign() {
        assertEquals(0, stateMachine.run(
                DIGIT_2,
                DIVIDE,
                PERCENT,
                PERCENT
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("0.0008", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testPrecision1() {
        assertEquals(0, stateMachine.run(
                DIGIT_2,
                SQUARE_ROOT,
                MINUS,
                DIGIT_1,
                DIGIT_0,
                PERCENT,
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("0.0000000000000002", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testManyNullsAtBeginning() {
        assertEquals(0, stateMachine.run(
                DIGIT_0,
                DIGIT_0,
                DIGIT_0,
                DIGIT_0,
                DIGIT_0,
                DIGIT_0
        ).length);

        model.getLock().lock();
        try {
            assertEquals("0", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testManyNullsAfterSingSelection() {
        assertEquals(0, stateMachine.run(
                DIGIT_1,
                PLUS,
                DIGIT_0,
                DIGIT_0,
                DIGIT_0,
                DIGIT_0,
                DIGIT_0
        ).length);

        model.getLock().lock();
        try {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("0", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testReverseNullsAtBeginning() {
        assertEquals(0, stateMachine.run(
                DIGIT_0,
                REVERSE
        ).length);

        model.getLock().lock();
        try {
            assertEquals("0", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testReverseNullsAfterSingSelection() {
        assertEquals(0, stateMachine.run(
                DIGIT_1,
                PLUS,
                DIGIT_0,
                REVERSE
        ).length);

        model.getLock().lock();
        try {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("0", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testManySquareRootsMultiplications2() {
        assertEquals(0, stateMachine.run(
                DIGIT_1,
                DIGIT_6,
                MULTIPLY,
                SQUARE_ROOT,
                MULTIPLY,
                SQUARE_ROOT,
                MULTIPLY,
                EVALUATE
        ).length);

        model.getLock().lock();
        try {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("262144", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testManyPercentsMultiplications() {
        assertEquals(0, stateMachine.run(
                DIGIT_5,
                DIGIT_0,
                MULTIPLY,
                PERCENT,
                MULTIPLY,
                PERCENT,
                MULTIPLY,
                EVALUATE
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("381469726562500", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testMemoryRecovery() {
        assertEquals(0, stateMachine.run(
                DIGIT_2,
                MEMORY_PLUS,
                CLEAR,
                DIGIT_2,
                MULTIPLY,
                MEMORY_RESTORE,
                PLUS
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("4", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testMinusZero() {
        assertEquals(0, stateMachine.run(
                DIGIT_0,
                DOT,
                DIGIT_0,
                DIGIT_0,
                DIGIT_0,
                DIGIT_0,
                REVERSE,
                BACK_SPACE,
                BACK_SPACE,
                BACK_SPACE,
                BACK_SPACE,
                BACK_SPACE
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("0", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testReverseSquareRoot() {
        assertEquals(0, stateMachine.run(
                DIGIT_2,
                SQUARE_ROOT,
                REVERSE
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("-1.414213562373095", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testDoubleInverse() {
        assertEquals(0, stateMachine.run(
                DIGIT_7,
                DIGIT_4,
                DIGIT_3,
                INVERSE,
                INVERSE
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("743", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testNormalize() {
        assertEquals(0, stateMachine.run(
                DOT,
                DIGIT_0,
                DIGIT_1,
                DIGIT_0,
                PLUS
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("0.01", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testNormalizeAfterPlus() {
        assertEquals(0, stateMachine.run(
                DOT,
                DIGIT_0,
                DIGIT_1,
                DIGIT_0,
                PLUS
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("0.01", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testBackspaceAfterDotInLArg1() {
        assertEquals(0, stateMachine.run(
                DIGIT_0,
                DOT,
                BACK_SPACE,
                DIGIT_9
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("9", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testBackspaceAfterDotInRArg1() {
        assertEquals(0, stateMachine.run(
                DIGIT_1,
                PLUS,
                DIGIT_0,
                DOT,
                BACK_SPACE,
                DIGIT_9
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("9", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }


    @Test
    public void testBackspaceAfterDotInLArg2() {
        assertEquals(0, stateMachine.run(
                DIGIT_1,
                DOT,
                BACK_SPACE,
                DIGIT_9
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("19", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testBackspaceAfterDotInRArg2() {
        assertEquals(0, stateMachine.run(
                DIGIT_1,
                PLUS,
                DIGIT_1,
                DOT,
                BACK_SPACE,
                DIGIT_9
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("19", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    @Test
    public void testRounding() {
        assertEquals(0, stateMachine.run(
                DOT,
                DIGIT_9,
                DIGIT_9,
                DIGIT_9,
                DIGIT_9,
                DIGIT_9,
                DIGIT_9,
                DIGIT_9,
                DIGIT_9,
                DIGIT_9,
                DIGIT_9,
                DIGIT_9,
                DIGIT_9,
                DIGIT_9,
                DIGIT_9,
                DIGIT_9,
                DIGIT_9,
                DIGIT_9,
                DIGIT_9,
                DIGIT_9,
                DIGIT_9,
                DIGIT_9,
                SQUARE_ROOT
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            assertEquals("1", model.getDisplayText());
        } finally {
            model.getLock().unlock();
        }
    }

    public void testTODON() {
        assertEquals(0, stateMachine.run(
                DIVIDE
        ).length);

        verify(view, atLeastOnce()).invalidate();
        model.getLock().lock();
        try {
            throw new NotImplementedException();
        } finally {
            model.getLock().unlock();
        }
    }

}
