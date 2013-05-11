package com.myzone.calculator;

import com.myzone.calculator.controller.CalculatorStateFactory;
import com.myzone.calculator.model.CalculatorModel;
import com.myzone.calculator.model.Signal;
import com.myzone.calculator.view.CalculatorView;
import com.myzone.utils.BigFraction;
import com.myzone.utils.statemachine.TestingEventStateMachine;
import com.myzone.utils.testing.LoggingRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.myzone.calculator.model.Signal.*;
import static com.myzone.utils.BigFraction.valueOf;
import static org.junit.Assert.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

/**
 * @author: myzone
 * @date: 20.02.13 1:21
 */
@RunWith(LoggingRunner.class)
public class CalculatorStatesTest {

    private volatile TestingCalculatorView view;
    private volatile TestingCalculatorModel model;
    private volatile TestingEventStateMachine<Signal> stateMachine;

    @Before
    public void setUp() throws Exception {
        model = new TestingCalculatorModel();
        view = new TestingCalculatorView();

        CalculatorView mockedView = mock(CalculatorView.class);
        doAnswer(invocation -> {
            try (CalculatorModel.Session session = model.createSession()) {
                view = new TestingCalculatorView(
                        !session.getMemory().equals(BigFraction.ZERO),
                        session.getDisplayText()
                );
            }

            return null;
        }).when(mockedView).invalidate();

        stateMachine = new TestingEventStateMachine<>(new CalculatorStateFactory(model, mockedView));
    }

    @Test
    public void testTwoMultiplyTwo() {
        assertEquals(0, stateMachine.run(
                DIGIT_2,
                MULTIPLY,
                DIGIT_2,
                EVALUATE
        ).length);

        assertFalse(view.hasMemoryFlag());
        assertEquals("4", view.getDisplayText());

        assertEquals(valueOf(4), model.getDisplayData());
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

        assertFalse(view.hasMemoryFlag());
        assertEquals("16", view.getDisplayText());

        assertEquals(valueOf(16), model.getDisplayData());
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

        assertFalse(view.hasMemoryFlag());
        assertEquals("16", view.getDisplayText());

        assertEquals(valueOf(16), model.getDisplayData());
    }

    @Test
    public void testSquare() {
        assertEquals(0, stateMachine.run(
                DIGIT_2,
                MULTIPLY,
                EVALUATE
        ).length);

        assertFalse(view.hasMemoryFlag());
        assertEquals("4", view.getDisplayText());

        assertEquals(valueOf(4), model.getDisplayData());
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

        assertFalse(view.hasMemoryFlag());
        assertEquals("25", view.getDisplayText());

        assertEquals(valueOf(25), model.getDisplayData());
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

        assertFalse(view.hasMemoryFlag());
        assertEquals("25", view.getDisplayText());

        assertEquals(valueOf(25), model.getDisplayData());
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

        assertFalse(view.hasMemoryFlag());
        assertEquals("25", view.getDisplayText());

        assertEquals(valueOf(25), model.getDisplayData());
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

        assertFalse(view.hasMemoryFlag());
        assertEquals("10", view.getDisplayText());

        assertEquals(valueOf(10), model.getDisplayData());
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

        assertFalse(view.hasMemoryFlag());
        assertEquals("3.2", view.getDisplayText());

        assertEquals(valueOf(16, 5), model.getDisplayData());
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

        assertFalse(view.hasMemoryFlag());
        assertEquals("0.00363945578231", view.getDisplayText());

        assertEquals(valueOf(107, 29400), model.getDisplayData());
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

        assertFalse(view.hasMemoryFlag());
        assertEquals("6.25", view.getDisplayText());

        assertEquals(valueOf(25, 4), model.getDisplayData());
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

        assertFalse(view.hasMemoryFlag());
        assertEquals("21.25", view.getDisplayText());

        assertEquals(valueOf(85, 4), model.getDisplayData());
    }

    @Test
    public void testPercentWithOneArg() {
        assertEquals(0, stateMachine.run(
                PLUS,
                DIGIT_1,
                PERCENT
        ).length);

        assertFalse(view.hasMemoryFlag());
        assertEquals("0", view.getDisplayText());

        assertEquals(valueOf(0), model.getDisplayData());
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


        assertFalse(view.hasMemoryFlag());
        assertEquals("0.96", view.getDisplayText());

        assertEquals(valueOf(24, 25), model.getDisplayData());
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


        assertFalse(view.hasMemoryFlag());
        assertEquals("47.04", view.getDisplayText());

        assertEquals(valueOf(1176, 25), model.getDisplayData());
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


        assertFalse(view.hasMemoryFlag());
        assertEquals("0.", view.getDisplayText());

        assertEquals(valueOf(0), model.getDisplayData());
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


        assertFalse(view.hasMemoryFlag());
        assertEquals("56", view.getDisplayText());

        assertEquals(valueOf(56), model.getDisplayData());
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


        assertFalse(view.hasMemoryFlag());
        assertEquals("-56", view.getDisplayText());

        assertEquals(valueOf(-56), model.getDisplayData());
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

        assertFalse(view.hasMemoryFlag());
        assertEquals("ERR", view.getDisplayText());
    }

    @Test
    public void testSquareRootFromNegativeNumber() {
        assertEquals(0, stateMachine.run(
                DIGIT_5,
                DIGIT_6,
                REVERSE,
                SQUARE_ROOT
        ).length);

        assertFalse(view.hasMemoryFlag());
        assertEquals("ERR", view.getDisplayText());
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

        assertFalse(view.hasMemoryFlag());
        assertEquals("ERR", view.getDisplayText());
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

        assertFalse(view.hasMemoryFlag());
        assertEquals("6", view.getDisplayText());

        assertEquals(valueOf(6), model.getDisplayData());
    }

    @Test
    public void testNumberAfterSquareRoot() {
        assertEquals(0, stateMachine.run(
                DIGIT_9,
                SQUARE_ROOT,
                DIGIT_3,
                EVALUATE
        ).length);

        assertFalse(view.hasMemoryFlag());
        assertEquals("3", view.getDisplayText());

        assertEquals(valueOf(3), model.getDisplayData());
    }

    @Test
    public void testDoubleSquareRoot() {
        assertEquals(0, stateMachine.run(
                DIGIT_5,
                DIGIT_6,
                SQUARE_ROOT,
                SQUARE_ROOT
        ).length);

        assertFalse(view.hasMemoryFlag());
        assertEquals("2.7355647997348", view.getDisplayText());

        assertEquals(valueOf(6159944306366657L, 2251799813685248L), model.getDisplayData());
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

        assertFalse(view.hasMemoryFlag());
        assertEquals("1.6539542919122", view.getDisplayText());

        assertEquals(valueOf(10534120349184526L, 6369051672525773L), model.getDisplayData());
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

        assertTrue(view.hasMemoryFlag());
        assertEquals("12.071067811866", view.getDisplayText());

        assertEquals(valueOf(24142135623731L, 2000000000000L), model.getDisplayData());
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

        assertFalse(view.hasMemoryFlag());
        assertEquals("0", view.getDisplayText());

        assertEquals(valueOf(0), model.getDisplayData());
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

        assertFalse(view.hasMemoryFlag());
        assertEquals("0", view.getDisplayText());

        assertEquals(valueOf(0), model.getDisplayData());
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

        assertFalse(view.hasMemoryFlag());
        assertEquals("55555.2", view.getDisplayText());

        assertEquals(valueOf(277776, 5), model.getDisplayData());
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

        assertFalse(view.hasMemoryFlag());
        assertEquals("56.", view.getDisplayText());

        assertEquals(valueOf(56), model.getDisplayData());
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
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE,
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

        assertFalse(view.hasMemoryFlag());
        assertEquals("1.66666666666667e+16", view.getDisplayText());

        assertEquals(valueOf(16666666666666650L, 1L), model.getDisplayData());
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
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE,
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

        assertFalse(view.hasMemoryFlag());
        assertEquals("-1.66666666666667e+16", view.getDisplayText());

        assertEquals(valueOf(-16666666666666650L, 1L), model.getDisplayData());
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


        assertFalse(view.hasMemoryFlag());
        assertEquals("0.00000000000001", view.getDisplayText());

        assertEquals(valueOf(1L, 100000000000000L), model.getDisplayData());
    }

    @Test
    public void testPercentAfterSign() {
        assertEquals(0, stateMachine.run(
                DIGIT_2,
                DIVIDE,
                PERCENT,
                PERCENT
        ).length);


        assertFalse(view.hasMemoryFlag());
        assertEquals("0.0008", view.getDisplayText());

        assertEquals(valueOf(1, 1250), model.getDisplayData());
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

        assertFalse(view.hasMemoryFlag());
        assertEquals("0", view.getDisplayText());

        assertEquals(valueOf(0), model.getDisplayData());
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

        assertFalse(view.hasMemoryFlag());
        assertEquals("0", view.getDisplayText());

        assertEquals(valueOf(0), model.getDisplayData());
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

        assertFalse(view.hasMemoryFlag());
        assertEquals("0", view.getDisplayText());

        assertEquals(valueOf(0), model.getDisplayData());
    }

    @Test
    public void testReverseNullsAtBeginning() {
        assertEquals(0, stateMachine.run(
                DIGIT_0,
                REVERSE
        ).length);


        assertFalse(view.hasMemoryFlag());
        assertEquals("0", view.getDisplayText());

        assertEquals(valueOf(0), model.getDisplayData());
    }

    @Test
    public void testReverseNullsAfterSingSelection() {
        assertEquals(0, stateMachine.run(
                DIGIT_1,
                PLUS,
                DIGIT_0,
                REVERSE
        ).length);

        assertFalse(view.hasMemoryFlag());
        assertEquals("0", view.getDisplayText());

        assertEquals(valueOf(0), model.getDisplayData());
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

        assertFalse(view.hasMemoryFlag());
        assertEquals("262144", view.getDisplayText());

        assertEquals(valueOf(262144), model.getDisplayData());
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

        assertFalse(view.hasMemoryFlag());
        assertEquals("381469726562500", view.getDisplayText());

        assertEquals(valueOf(381469726562500L), model.getDisplayData());
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

        assertTrue(view.hasMemoryFlag());
        assertEquals("4", view.getDisplayText());

        assertEquals(valueOf(4), model.getDisplayData());
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

        assertFalse(view.hasMemoryFlag());
        assertEquals("0", view.getDisplayText());

        assertEquals(valueOf(0), model.getDisplayData());
    }

    @Test
    public void testReverseSquareRoot() {
        assertEquals(0, stateMachine.run(
                DIGIT_2,
                SQUARE_ROOT,
                REVERSE
        ).length);

        assertFalse(view.hasMemoryFlag());
        assertEquals("-1.4142135623731", view.getDisplayText());

        assertEquals(valueOf(-6369051672525773L, 4503599627370496L), model.getDisplayData());
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

        assertFalse(view.hasMemoryFlag());
        assertEquals("743", view.getDisplayText());

        assertEquals(valueOf(743), model.getDisplayData());
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

        assertFalse(view.hasMemoryFlag());
        assertEquals("0.01", view.getDisplayText());

        assertEquals(valueOf(1, 100), model.getDisplayData());
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

        assertFalse(view.hasMemoryFlag());
        assertEquals("0.01", view.getDisplayText());

        assertEquals(valueOf(1, 100), model.getDisplayData());
    }

    @Test
    public void testBackspaceAfterDotInLArg1() {
        assertEquals(0, stateMachine.run(
                DIGIT_0,
                DOT,
                BACK_SPACE,
                DIGIT_9
        ).length);

        assertFalse(view.hasMemoryFlag());
        assertEquals("9", view.getDisplayText());

        assertEquals(valueOf(9), model.getDisplayData());
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

        assertFalse(view.hasMemoryFlag());
        assertEquals("9", view.getDisplayText());

        assertEquals(valueOf(9), model.getDisplayData());
    }


    @Test
    public void testBackspaceAfterDotInLArg2() {
        assertEquals(0, stateMachine.run(
                DIGIT_1,
                DOT,
                BACK_SPACE,
                DIGIT_9
        ).length);

        assertFalse(view.hasMemoryFlag());
        assertEquals("19", view.getDisplayText());

        assertEquals(valueOf(19), model.getDisplayData());
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

        assertFalse(view.hasMemoryFlag());
        assertEquals("19", view.getDisplayText());

        assertEquals(valueOf(19), model.getDisplayData());
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

        assertFalse(view.hasMemoryFlag());
        assertEquals("1", view.getDisplayText());

        assertEquals(valueOf(5368709119999973L, 5368709120000000L), model.getDisplayData());
    }

    @Test
    public void testManyEvaluationsAfterDot() {
        assertEquals(0, stateMachine.run(
                DIGIT_2,
                DOT,
                EVALUATE,
                EVALUATE,
                EVALUATE
        ).length);

        assertFalse(view.hasMemoryFlag());
        assertEquals("2", view.getDisplayText());

        assertEquals(valueOf(2), model.getDisplayData());
    }

    @Test
    public void testDecrementMaxNormalized() {
        assertEquals(0, stateMachine.run(
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
                MINUS,
                DIGIT_1,
                EVALUATE
        ).length);

        assertFalse(view.hasMemoryFlag());
        assertEquals("999999999999998", view.getDisplayText());

        assertEquals(valueOf(999999999999998L), model.getDisplayData());
    }

    @Test
    public void testSquareRootAfterClearAfterError() {
        assertEquals(0, stateMachine.run(
                DIGIT_0,
                INVERSE,
                CLEAR,
                SQUARE_ROOT
        ).length);

        assertFalse(view.hasMemoryFlag());
        assertEquals("0", view.getDisplayText());

        assertEquals(valueOf(0), model.getDisplayData());
    }

    @Test
    public void testScientificNumberModification() {
        assertEquals(0, stateMachine.run(
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
                MULTIPLY,
                EVALUATE,
                EVALUATE,
                BACK_SPACE
        ).length);

        assertFalse(view.hasMemoryFlag());
        assertEquals("9.99999999999997e+44", view.getDisplayText());

        assertEquals(valueOf("999999999999997000000000000002999999999999999/1"), model.getDisplayData());
    }

    @Test
    public void testModificationAfterMemorySetInLArg() {
        assertEquals(0, stateMachine.run(
                DIGIT_3,
                DIGIT_3,
                DIGIT_3,
                DIGIT_3,
                MEMORY_STORE,
                DIGIT_4
        ).length);


        assertTrue(view.hasMemoryFlag());
        assertEquals("4", view.getDisplayText());

        assertEquals(valueOf(4), model.getDisplayData());
    }

    @Test
    public void testModificationAfterMemorySetInRArg() {
        assertEquals(0, stateMachine.run(
                MULTIPLY,
                DIGIT_3,
                DIGIT_3,
                DIGIT_3,
                DIGIT_3,
                MEMORY_STORE,
                DIGIT_4
        ).length);

        assertTrue(view.hasMemoryFlag());
        assertEquals("4", view.getDisplayText());

        assertEquals(valueOf(4), model.getDisplayData());
    }

    @Test
    public void testTransitionToScientificNotation() {
        assertEquals(0, stateMachine.run(
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
                BACK_SPACE,
                DIGIT_7,
                PLUS,
                DIGIT_1,
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE
        ).length);

        assertFalse(view.hasMemoryFlag());
        assertEquals("1.00000000000001e+15", view.getDisplayText());

        assertEquals(valueOf(1000000000000001L, 1L), model.getDisplayData());
    }

    @Test
    public void testReverse() {
        assertEquals(0, stateMachine.run(
                DIGIT_9,
                REVERSE,
                DIGIT_9
        ).length);

        assertFalse(view.hasMemoryFlag());
        assertEquals("-99", view.getDisplayText());

        assertEquals(valueOf(-99), model.getDisplayData());
    }

    @Test
    public void testBackspaceAfterSignSelection() {
        assertEquals(0, stateMachine.run(
                DIGIT_2,
                DIGIT_3,
                DIGIT_8,
                DIVIDE,
                BACK_SPACE
        ).length);

        assertFalse(view.hasMemoryFlag());
        assertEquals("238", view.getDisplayText());

        assertEquals(valueOf(238), model.getDisplayData());
    }

    @Test
    public void testBackspaceAfterEvaluation() {
        assertEquals(0, stateMachine.run(
                DIGIT_2,
                DIGIT_3,
                DIGIT_8,
                DIVIDE,
                DIGIT_1,
                EVALUATE,
                BACK_SPACE
        ).length);

        assertFalse(view.hasMemoryFlag());
        assertEquals("238", view.getDisplayText());

        assertEquals(valueOf(238), model.getDisplayData());
    }

    @Test
    public void testPrecision2() {
        assertEquals(0, stateMachine.run(
                DIGIT_1,
                DIVIDE,
                DIGIT_7,
                DIGIT_4,
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE
        ).length);

        assertFalse(view.hasMemoryFlag());
        assertEquals("0", view.getDisplayText());

        assertEquals(valueOf(1L, 899194740203776L), model.getDisplayData());
    }

    @Test
    public void testClearAfterEvaluation() {
        assertEquals(0, stateMachine.run(
                DIGIT_2,
                PLUS,
                DIGIT_1,
                EVALUATE,
                CLEAR,
                DIGIT_0,
                EVALUATE
        ).length);

        assertFalse(view.hasMemoryFlag());
        assertEquals("0", view.getDisplayText());

        assertEquals(valueOf(0), model.getDisplayData());
    }


    @Test
    public void testNormalizeInRArg() {
        assertEquals(0, stateMachine.run(
                DIGIT_0,
                MINUS,
                DIGIT_0,
                DOT,
                EVALUATE
        ).length);

        assertFalse(view.hasMemoryFlag());
        assertEquals("0", view.getDisplayText());

        assertEquals(valueOf(0), model.getDisplayData());
    }

    @Test
    public void testDotInInitialState() {
        assertEquals(0, stateMachine.run(
                DIGIT_2,
                DOT,
                EVALUATE,
                DOT,
                DIGIT_3,
                DIGIT_3
        ).length);

        assertFalse(view.hasMemoryFlag());
        assertEquals("0.33", view.getDisplayText());

        assertEquals(valueOf(33, 100), model.getDisplayData());
    }

    @Test
    public void testNormalizationAfterMemoryOperations() {
        assertEquals(0, stateMachine.run(
                DIGIT_2,
                DIGIT_5,
                MEMORY_PLUS,
                REVERSE,
                DOT,
                MEMORY_MINUS
        ).length);

        assertTrue(view.hasMemoryFlag());
        assertEquals("0", view.getDisplayText());

        assertEquals(valueOf(0), model.getDisplayData());
    }

    @Test
    public void testBackspaceAfterSquareRootInLArg() {
        assertEquals(0, stateMachine.run(
                DIGIT_2,
                SQUARE_ROOT,
                BACK_SPACE
        ).length);

        assertFalse(view.hasMemoryFlag());
        assertEquals("1.4142135623731", view.getDisplayText());

        assertEquals(valueOf(6369051672525773L, 4503599627370496L), model.getDisplayData());
    }

    @Test
    public void testBackspaceAfterSquareRootInRArg() {
        assertEquals(0, stateMachine.run(
                DIGIT_2,
                PLUS,
                DIGIT_2,
                SQUARE_ROOT,
                BACK_SPACE
        ).length);

        assertFalse(view.hasMemoryFlag());
        assertEquals("1.4142135623731", view.getDisplayText());

        assertEquals(valueOf(6369051672525773L, 4503599627370496L), model.getDisplayData());
    }

    @Test
    public void testBackspaceAfterMemoryRestoreInLArg() {
        assertEquals(0, stateMachine.run(
                DIGIT_2,
                DIGIT_2,
                MEMORY_PLUS,
                MEMORY_RESTORE,
                BACK_SPACE
        ).length);

        assertTrue(view.hasMemoryFlag());
        assertEquals("22", view.getDisplayText());

        assertEquals(valueOf(22), model.getDisplayData());
    }

    @Test
    public void testBackspaceAfterMemoryRestoreInRArg() {
        assertEquals(0, stateMachine.run(
                PLUS,
                DIGIT_2,
                DIGIT_2,
                MEMORY_PLUS,
                MEMORY_RESTORE,
                BACK_SPACE
        ).length);

        assertTrue(view.hasMemoryFlag());
        assertEquals("22", view.getDisplayText());

        assertEquals(valueOf(22), model.getDisplayData());
    }

    @Test
    public void testBackspaceAfterMemoryPlusInRArg() {
        assertEquals(0, stateMachine.run(
                PLUS,
                DIGIT_2,
                DIGIT_2,
                MEMORY_PLUS,
                BACK_SPACE
        ).length);

        assertTrue(view.hasMemoryFlag());
        assertEquals("22", view.getDisplayText());

        assertEquals(valueOf(22), model.getDisplayData());
    }

    @Test
    public void testPlusAfterMemoryPlus() {
        assertEquals(0, stateMachine.run(
                DIGIT_5,
                MEMORY_PLUS,
                PLUS,
                DOT,
                DIGIT_3,
                PLUS
        ).length);

        assertTrue(view.hasMemoryFlag());
        assertEquals("5.3", view.getDisplayText());

        assertEquals(valueOf(53, 10), model.getDisplayData());
    }

    @Test
    public void testSmallDigitRounding() {
        assertEquals(0, stateMachine.run(
                DIGIT_3,
                MINUS,
                DOT,
                DIGIT_6,
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE
        ).length);

        assertFalse(view.hasMemoryFlag());
        assertEquals("0", view.getDisplayText());

        assertEquals(valueOf(0), model.getDisplayData());
    }

    @Test
    public void testRound() {
        assertEquals(0, stateMachine.run(
                DIGIT_1,
                DIVIDE,
                DIGIT_3,
                PLUS,
                DIGIT_6,
                DIGIT_6,
                DIGIT_6,
                DIGIT_6,
                DIGIT_6,
                DIGIT_6,
                DIGIT_6,
                DIGIT_6,
                DIGIT_6,
                MULTIPLY,
                DIGIT_2,
                EVALUATE
        ).length);

        assertFalse(view.hasMemoryFlag());
        assertEquals("1333333332.6667", view.getDisplayText());

        assertEquals(valueOf(3999999998L, 3L), model.getDisplayData());
    }

    private static class TestingCalculatorModel extends CalculatorModel {

        @Override
        public BigFraction getlArg() {
            return super.getlArg();
        }

        @Override
        public void setlArg(BigFraction lArg) {
            super.setlArg(lArg);
        }

        @Override
        public BigFraction getrArg() {
            return super.getrArg();
        }

        @Override
        public void setrArg(BigFraction rArg) {
            super.setrArg(rArg);
        }

        @Override
        public BigFraction getMemory() {
            return super.getMemory();
        }

        @Override
        public void setMemory(BigFraction memory) {
            super.setMemory(memory);
        }

        @Override
        public String getDisplayText() {
            return super.getDisplayText();
        }

        @Override
        public void setDisplayText(String displayText) {
            super.setDisplayText(displayText);
        }

        @Override
        public BigFraction getDisplayData() {
            return super.getDisplayData();
        }

        @Override
        public void setDisplayData(BigFraction displayData) {
            super.setDisplayData(displayData);
        }

        @Override
        public Operation getOperation() {
            return super.getOperation();
        }

        @Override
        public void setOperation(Operation operation) {
            super.setOperation(operation);
        }
    }

    private static class TestingCalculatorView {

        protected final boolean memoryFlag;
        protected final String displayText;

        private TestingCalculatorView() {
            memoryFlag = false;
            displayText = "0";
        }

        private TestingCalculatorView(boolean memoryFlag, String displayText) {
            this.memoryFlag = memoryFlag;
            this.displayText = displayText;
        }

        public boolean hasMemoryFlag() {
            return memoryFlag;
        }

        public String getDisplayText() {
            return displayText;
        }

    }

}
