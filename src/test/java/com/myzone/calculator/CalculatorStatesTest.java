package com.myzone.calculator;

import com.myzone.calculator.controller.CalculatorStateFactory;
import com.myzone.calculator.model.CalculatorModel;
import com.myzone.calculator.model.Signal;
import com.myzone.calculator.view.CalculatorView;
import com.myzone.utils.BigFraction;
import com.myzone.utils.statemachine.TestingEventStateMachine;
import org.junit.Before;
import org.junit.Test;

import static com.myzone.calculator.model.Signal.*;
import static com.myzone.utils.BigFraction.valueOf;
import static org.junit.Assert.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

/**
 * @author: myzone
 * @date: 20.02.13 1:21
 */
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
        assertEmpty(stateMachine.run(
                DIGIT_2,
                MULTIPLY,
                DIGIT_2,
                EVALUATE
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("4", view.getDisplayText());

        assertEquals(valueOf(4), model.getDisplayData());
    }

    @Test
    public void testTwoMultiplyTwoWithManyEvaluations() {
        assertEmpty(stateMachine.run(
                DIGIT_2,
                MULTIPLY,
                DIGIT_2,
                EVALUATE,
                EVALUATE,
                EVALUATE
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("16", view.getDisplayText());

        assertEquals(valueOf(16), model.getDisplayData());
    }

    @Test
    public void testTwoMultiplyWithManyEvaluations() {
        assertEmpty(stateMachine.run(
                DIGIT_2,
                MULTIPLY,
                EVALUATE,
                EVALUATE,
                EVALUATE
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("16", view.getDisplayText());

        assertEquals(valueOf(16), model.getDisplayData());
    }

    @Test
    public void testSquare() {
        assertEmpty(stateMachine.run(
                DIGIT_2,
                MULTIPLY,
                EVALUATE
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("4", view.getDisplayText());

        assertEquals(valueOf(4), model.getDisplayData());
    }

    @Test
    public void testManyAdds() {
        assertEmpty(stateMachine.run(
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
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("25", view.getDisplayText());

        assertEquals(valueOf(25), model.getDisplayData());
    }

    @Test
    public void testManyAddsAndEvaluations() {
        assertEmpty(stateMachine.run(
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
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("25", view.getDisplayText());

        assertEquals(valueOf(25), model.getDisplayData());
    }

    @Test
    public void testManyStrangeEvaluations() {
        assertEmpty(stateMachine.run(
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
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("25", view.getDisplayText());

        assertEquals(valueOf(25), model.getDisplayData());
    }

    @Test
    public void testManyEvaluations() {
        assertEmpty(stateMachine.run(
                PLUS,
                DIGIT_5,
                PLUS,
                DIGIT_5,
                EVALUATE,
                DIGIT_3,
                PLUS,
                DIGIT_7,
                EVALUATE
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("10", view.getDisplayText());

        assertEquals(valueOf(10), model.getDisplayData());
    }

    @Test
    public void testManyEvaluationsWithDot() {
        assertEmpty(stateMachine.run(
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
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("3.2", view.getDisplayText());

        assertEquals(valueOf(16, 5), model.getDisplayData());
    }

    @Test
    public void testComplexEvaluationWithDot() {
        assertEmpty(stateMachine.run(
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
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("0.00363945578231", view.getDisplayText());

        assertEquals(valueOf(107, 29400), model.getDisplayData());
    }

    @Test
    public void testPercentAfterEvaluation() {
        assertEmpty(stateMachine.run(
                DIGIT_1,
                DIGIT_0,
                PLUS,
                DIGIT_1,
                DIGIT_5,
                EVALUATE,
                PERCENT
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("6.25", view.getDisplayText());

        assertEquals(valueOf(25, 4), model.getDisplayData());
    }

    @Test
    public void testEvaluationAfterPercentAfterEvaluation() {
        assertEmpty(stateMachine.run(
                DIGIT_1,
                DIGIT_0,
                PLUS,
                DIGIT_1,
                DIGIT_5,
                EVALUATE,
                PERCENT,
                EVALUATE
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("21.25", view.getDisplayText());

        assertEquals(valueOf(85, 4), model.getDisplayData());
    }

    @Test
    public void testPercentWithOneArg() {
        assertEmpty(stateMachine.run(
                PLUS,
                DIGIT_1,
                PERCENT
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("0", view.getDisplayText());

        assertEquals(valueOf(0), model.getDisplayData());
    }

    @Test
    public void testPercent() {
        assertEmpty(stateMachine.run(
                DIGIT_4,
                DIGIT_8,
                MINUS,
                DIGIT_2,
                PERCENT
        ));


        assertFalse(view.hasMemoryFlag());
        assertEquals("0.96", view.getDisplayText());

        assertEquals(valueOf(24, 25), model.getDisplayData());
    }

    @Test
    public void testEvaluationAfterPercent() {
        assertEmpty(stateMachine.run(
                DIGIT_4,
                DIGIT_8,
                MINUS,
                DIGIT_2,
                PERCENT,
                EVALUATE
        ));


        assertFalse(view.hasMemoryFlag());
        assertEquals("47.04", view.getDisplayText());

        assertEquals(valueOf(1176, 25), model.getDisplayData());
    }

    @Test
    public void testManyDots() {
        assertEmpty(stateMachine.run(
                PLUS,
                DIGIT_0,
                DOT,
                DOT,
                DOT
        ));


        assertFalse(view.hasMemoryFlag());
        assertEquals("0.", view.getDisplayText());

        assertEquals(valueOf(0), model.getDisplayData());
    }

    @Test
    public void testManyEvaluationsWithPositiveNumber() {
        assertEmpty(stateMachine.run(
                DIGIT_5,
                DIGIT_6,
                EVALUATE,
                EVALUATE,
                EVALUATE
        ));


        assertFalse(view.hasMemoryFlag());
        assertEquals("56", view.getDisplayText());

        assertEquals(valueOf(56), model.getDisplayData());
    }

    @Test
    public void testManyEvaluationsWithNegativeNumber() {
        assertEmpty(stateMachine.run(
                DIGIT_5,
                DIGIT_6,
                REVERSE,
                EVALUATE,
                EVALUATE,
                EVALUATE
        ));


        assertFalse(view.hasMemoryFlag());
        assertEquals("-56", view.getDisplayText());

        assertEquals(valueOf(-56), model.getDisplayData());
    }

    @Test
    public void testZeroDivision() {
        assertEmpty(stateMachine.run(
                DIGIT_5,
                DIGIT_6,
                DIVIDE,
                DIGIT_0,
                EVALUATE
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("ERR", view.getDisplayText());
    }

    @Test
    public void testSquareRootFromNegativeNumber() {
        assertEmpty(stateMachine.run(
                DIGIT_5,
                DIGIT_6,
                REVERSE,
                SQUARE_ROOT
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("ERR", view.getDisplayText());
    }

    @Test
    public void testComplexSquareRootFromNegativeNumber() {
        assertEmpty(stateMachine.run(
                DIGIT_5,
                DIGIT_6,
                SQUARE_ROOT,
                SQUARE_ROOT,
                MINUS,
                DIGIT_3,
                EVALUATE,
                SQUARE_ROOT
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("ERR", view.getDisplayText());
    }

    @Test
    public void testSquareRoot() {
        assertEmpty(stateMachine.run(
                DIGIT_9,
                SQUARE_ROOT,
                PLUS,
                DIGIT_3,
                EVALUATE
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("6", view.getDisplayText());

        assertEquals(valueOf(6), model.getDisplayData());
    }

    @Test
    public void testNumberAfterSquareRoot() {
        assertEmpty(stateMachine.run(
                DIGIT_9,
                SQUARE_ROOT,
                DIGIT_3,
                EVALUATE
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("3", view.getDisplayText());

        assertEquals(valueOf(3), model.getDisplayData());
    }

    @Test
    public void testDoubleSquareRoot() {
        assertEmpty(stateMachine.run(
                DIGIT_5,
                DIGIT_6,
                SQUARE_ROOT,
                SQUARE_ROOT
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("2.7355647997348", view.getDisplayText());

        assertEquals(valueOf(6159944306366657L, 2251799813685248L), model.getDisplayData());
    }

    @Test
    public void testTripleSquareRoot() {
        assertEmpty(stateMachine.run(
                DIGIT_5,
                DIGIT_6,
                SQUARE_ROOT,
                SQUARE_ROOT,
                SQUARE_ROOT
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("1.6539542919122", view.getDisplayText());

        assertEquals(valueOf(1862186983185895L, 1125899906842624L), model.getDisplayData());
    }

    @Test
    public void testMemory() {
        assertEmpty(stateMachine.run(
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
        ));

        assertTrue(view.hasMemoryFlag());
        assertEquals("12.071067811865", view.getDisplayText());
    }

    @Test
    public void testBackspace() {
        assertEmpty(stateMachine.run(
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
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("0", view.getDisplayText());

        assertEquals(valueOf(0), model.getDisplayData());
    }

    @Test
    public void testTooManyBackspace() {
        assertEmpty(stateMachine.run(
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
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("0", view.getDisplayText());

        assertEquals(valueOf(0), model.getDisplayData());
    }

    @Test
    public void testBackspaceWithDottedNumber() {
        assertEmpty(stateMachine.run(
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
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("55555.2", view.getDisplayText());

        assertEquals(valueOf(277776, 5), model.getDisplayData());
    }

    @Test
    public void testDoubleReverseAfterDot() {
        assertEmpty(stateMachine.run(
                DIGIT_5,
                DIGIT_6,
                DOT,
                REVERSE,
                REVERSE
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("56.", view.getDisplayText());

        assertEquals(valueOf(56), model.getDisplayData());
    }

    @Test
    public void testPositiveOverflow() {
        assertEmpty(stateMachine.run(
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
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("1.66666666666667e+16", view.getDisplayText());

        assertEquals(valueOf(16666666666666650L, 1L), model.getDisplayData());
    }

    @Test
    public void testNegativeOverflow() {
        assertEmpty(stateMachine.run(
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
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("-1.66666666666667e+16", view.getDisplayText());

        assertEquals(valueOf(-16666666666666650L, 1L), model.getDisplayData());
    }

    @Test
    public void testPrecision() {
        assertEmpty(stateMachine.run(
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
        ));


        assertFalse(view.hasMemoryFlag());
        assertEquals("0.00000000000001", view.getDisplayText());

        assertEquals(valueOf(1L, 100000000000000L), model.getDisplayData());
    }

    @Test
    public void testPercentAfterSign() {
        assertEmpty(stateMachine.run(
                DIGIT_2,
                DIVIDE,
                PERCENT,
                PERCENT
        ));


        assertFalse(view.hasMemoryFlag());
        assertEquals("0.0008", view.getDisplayText());

        assertEquals(valueOf(1, 1250), model.getDisplayData());
    }

    @Test
    public void testPrecision1() {
        assertEmpty(stateMachine.run(
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
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("0", view.getDisplayText());

        assertEquals(valueOf(0), model.getDisplayData());
    }

    @Test
    public void testManyNullsAtBeginning() {
        assertEmpty(stateMachine.run(
                DIGIT_0,
                DIGIT_0,
                DIGIT_0,
                DIGIT_0,
                DIGIT_0,
                DIGIT_0
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("0", view.getDisplayText());

        assertEquals(valueOf(0), model.getDisplayData());
    }

    @Test
    public void testManyNullsAfterSingSelection() {
        assertEmpty(stateMachine.run(
                DIGIT_1,
                PLUS,
                DIGIT_0,
                DIGIT_0,
                DIGIT_0,
                DIGIT_0,
                DIGIT_0
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("0", view.getDisplayText());

        assertEquals(valueOf(0), model.getDisplayData());
    }

    @Test
    public void testReverseNullsAtBeginning() {
        assertEmpty(stateMachine.run(
                DIGIT_0,
                REVERSE
        ));


        assertFalse(view.hasMemoryFlag());
        assertEquals("0", view.getDisplayText());

        assertEquals(valueOf(0), model.getDisplayData());
    }

    @Test
    public void testReverseNullsAfterSingSelection() {
        assertEmpty(stateMachine.run(
                DIGIT_1,
                PLUS,
                DIGIT_0,
                REVERSE
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("0", view.getDisplayText());

        assertEquals(valueOf(0), model.getDisplayData());
    }

    @Test
    public void testManySquareRootsMultiplications2() {
        assertEmpty(stateMachine.run(
                DIGIT_1,
                DIGIT_6,
                MULTIPLY,
                SQUARE_ROOT,
                MULTIPLY,
                SQUARE_ROOT,
                MULTIPLY,
                EVALUATE
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("262144", view.getDisplayText());

        assertEquals(valueOf(262144), model.getDisplayData());
    }

    @Test
    public void testManyPercentsMultiplications() {
        assertEmpty(stateMachine.run(
                DIGIT_5,
                DIGIT_0,
                MULTIPLY,
                PERCENT,
                MULTIPLY,
                PERCENT,
                MULTIPLY,
                EVALUATE
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("381469726562500", view.getDisplayText());

        assertEquals(valueOf(381469726562500L), model.getDisplayData());
    }

    @Test
    public void testMemoryRecovery() {
        assertEmpty(stateMachine.run(
                DIGIT_2,
                MEMORY_PLUS,
                CLEAR,
                DIGIT_2,
                MULTIPLY,
                MEMORY_RESTORE,
                PLUS
        ));

        assertTrue(view.hasMemoryFlag());
        assertEquals("4", view.getDisplayText());

        assertEquals(valueOf(4), model.getDisplayData());
    }

    @Test
    public void testMinusZero() {
        assertEmpty(stateMachine.run(
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
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("0", view.getDisplayText());

        assertEquals(valueOf(0), model.getDisplayData());
    }

    @Test
    public void testReverseSquareRoot() {
        assertEmpty(stateMachine.run(
                DIGIT_2,
                SQUARE_ROOT,
                REVERSE
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("-1.4142135623731", view.getDisplayText());

        assertEquals(valueOf(-6369051672525773L, 4503599627370496L), model.getDisplayData());
    }

    @Test
    public void testDoubleInverse() {
        assertEmpty(stateMachine.run(
                DIGIT_7,
                DIGIT_4,
                DIGIT_3,
                INVERSE,
                INVERSE
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("743", view.getDisplayText());

        assertEquals(valueOf(743), model.getDisplayData());
    }

    @Test
    public void testNormalize() {
        assertEmpty(stateMachine.run(
                DOT,
                DIGIT_0,
                DIGIT_1,
                DIGIT_0,
                PLUS
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("0.01", view.getDisplayText());

        assertEquals(valueOf(1, 100), model.getDisplayData());
    }

    @Test
    public void testNormalizeAfterPlus() {
        assertEmpty(stateMachine.run(
                DOT,
                DIGIT_0,
                DIGIT_1,
                DIGIT_0,
                PLUS
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("0.01", view.getDisplayText());

        assertEquals(valueOf(1, 100), model.getDisplayData());
    }

    @Test
    public void testBackspaceAfterDotInLArg1() {
        assertEmpty(stateMachine.run(
                DIGIT_0,
                DOT,
                BACK_SPACE,
                DIGIT_9
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("9", view.getDisplayText());

        assertEquals(valueOf(9), model.getDisplayData());
    }

    @Test
    public void testBackspaceAfterDotInRArg1() {
        assertEmpty(stateMachine.run(
                DIGIT_1,
                PLUS,
                DIGIT_0,
                DOT,
                BACK_SPACE,
                DIGIT_9
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("9", view.getDisplayText());

        assertEquals(valueOf(9), model.getDisplayData());
    }


    @Test
    public void testBackspaceAfterDotInLArg2() {
        assertEmpty(stateMachine.run(
                DIGIT_1,
                DOT,
                BACK_SPACE,
                DIGIT_9
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("19", view.getDisplayText());

        assertEquals(valueOf(19), model.getDisplayData());
    }

    @Test
    public void testBackspaceAfterDotInRArg2() {
        assertEmpty(stateMachine.run(
                DIGIT_1,
                PLUS,
                DIGIT_1,
                DOT,
                BACK_SPACE,
                DIGIT_9
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("19", view.getDisplayText());

        assertEquals(valueOf(19), model.getDisplayData());
    }

    @Test
    public void testRounding() {
        assertEmpty(stateMachine.run(
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
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("1", view.getDisplayText());

        assertEquals(valueOf(9007199254740947L, 9007199254740992L), model.getDisplayData());
    }

    @Test
    public void testManyEvaluationsAfterDot() {
        assertEmpty(stateMachine.run(
                DIGIT_2,
                DOT,
                EVALUATE,
                EVALUATE,
                EVALUATE
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("2", view.getDisplayText());

        assertEquals(valueOf(2), model.getDisplayData());
    }

    @Test
    public void testDecrementMaxNormalized() {
        assertEmpty(stateMachine.run(
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
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("999999999999998", view.getDisplayText());

        assertEquals(valueOf(999999999999998L), model.getDisplayData());
    }

    @Test
    public void testSquareRootAfterClearAfterError() {
        assertEmpty(stateMachine.run(
                DIGIT_0,
                INVERSE,
                CLEAR,
                SQUARE_ROOT
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("0", view.getDisplayText());

        assertEquals(valueOf(0), model.getDisplayData());
    }

    @Test
    public void testScientificNumberModification() {
        assertEmpty(stateMachine.run(
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
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("9.99999999999997e+44", view.getDisplayText());

        assertEquals(valueOf("999999999999997000000000000002999999999999999/1"), model.getDisplayData());
    }

    @Test
    public void testModificationAfterMemorySetInLArg() {
        assertEmpty(stateMachine.run(
                DIGIT_3,
                DIGIT_3,
                DIGIT_3,
                DIGIT_3,
                MEMORY_STORE,
                DIGIT_4
        ));


        assertTrue(view.hasMemoryFlag());
        assertEquals("4", view.getDisplayText());

        assertEquals(valueOf(4), model.getDisplayData());
    }

    @Test
    public void testModificationAfterMemorySetInRArg() {
        assertEmpty(stateMachine.run(
                MULTIPLY,
                DIGIT_3,
                DIGIT_3,
                DIGIT_3,
                DIGIT_3,
                MEMORY_STORE,
                DIGIT_4
        ));

        assertTrue(view.hasMemoryFlag());
        assertEquals("4", view.getDisplayText());

        assertEquals(valueOf(4), model.getDisplayData());
    }

    @Test
    public void testTransitionToScientificNotation() {
        assertEmpty(stateMachine.run(
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
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("1.00000000000001e+15", view.getDisplayText());

        assertEquals(valueOf(1000000000000001L, 1L), model.getDisplayData());
    }

    @Test
    public void testReverse() {
        assertEmpty(stateMachine.run(
                DIGIT_9,
                REVERSE,
                DIGIT_9
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("-99", view.getDisplayText());

        assertEquals(valueOf(-99), model.getDisplayData());
    }

    @Test
    public void testBackspaceAfterSignSelection() {
        assertEmpty(stateMachine.run(
                DIGIT_2,
                DIGIT_3,
                DIGIT_8,
                DIVIDE,
                BACK_SPACE
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("238", view.getDisplayText());

        assertEquals(valueOf(238), model.getDisplayData());
    }

    @Test
    public void testBackspaceAfterEvaluation() {
        assertEmpty(stateMachine.run(
                DIGIT_2,
                DIGIT_3,
                DIGIT_8,
                DIVIDE,
                DIGIT_1,
                EVALUATE,
                BACK_SPACE
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("238", view.getDisplayText());

        assertEquals(valueOf(238), model.getDisplayData());
    }

    @Test
    public void testPrecision2() {
        assertEmpty(stateMachine.run(
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
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("0", view.getDisplayText());

        assertEquals(valueOf(1L, 899194740203776L), model.getDisplayData());
    }

    @Test
    public void testClearAfterEvaluation() {
        assertEmpty(stateMachine.run(
                DIGIT_2,
                PLUS,
                DIGIT_1,
                EVALUATE,
                CLEAR,
                DIGIT_0,
                EVALUATE
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("0", view.getDisplayText());

        assertEquals(valueOf(0), model.getDisplayData());
    }


    @Test
    public void testNormalizeInRArg() {
        assertEmpty(stateMachine.run(
                DIGIT_0,
                MINUS,
                DIGIT_0,
                DOT,
                EVALUATE
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("0", view.getDisplayText());

        assertEquals(valueOf(0), model.getDisplayData());
    }

    @Test
    public void testDotInInitialState() {
        assertEmpty(stateMachine.run(
                DIGIT_2,
                DOT,
                EVALUATE,
                DOT,
                DIGIT_3,
                DIGIT_3
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("0.33", view.getDisplayText());

        assertEquals(valueOf(33, 100), model.getDisplayData());
    }

    @Test
    public void testNormalizationAfterMemoryOperations() {
        assertEmpty(stateMachine.run(
                DIGIT_2,
                DIGIT_5,
                MEMORY_PLUS,
                REVERSE,
                DOT,
                MEMORY_MINUS
        ));

        assertTrue(view.hasMemoryFlag());
        assertEquals("0", view.getDisplayText());

        assertEquals(valueOf(0), model.getDisplayData());
    }

    @Test
    public void testBackspaceAfterSquareRootInLArg() {
        assertEmpty(stateMachine.run(
                DIGIT_2,
                SQUARE_ROOT,
                BACK_SPACE
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("1.4142135623731", view.getDisplayText());

        assertEquals(valueOf(6369051672525773L, 4503599627370496L), model.getDisplayData());
    }

    @Test
    public void testBackspaceAfterSquareRootInRArg() {
        assertEmpty(stateMachine.run(
                DIGIT_2,
                PLUS,
                DIGIT_2,
                SQUARE_ROOT,
                BACK_SPACE
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("1.4142135623731", view.getDisplayText());

        assertEquals(valueOf(6369051672525773L, 4503599627370496L), model.getDisplayData());
    }

    @Test
    public void testBackspaceAfterMemoryRestoreInLArg() {
        assertEmpty(stateMachine.run(
                DIGIT_2,
                DIGIT_2,
                MEMORY_PLUS,
                MEMORY_RESTORE,
                BACK_SPACE
        ));

        assertTrue(view.hasMemoryFlag());
        assertEquals("22", view.getDisplayText());

        assertEquals(valueOf(22), model.getDisplayData());
    }

    @Test
    public void testBackspaceAfterMemoryRestoreInRArg() {
        assertEmpty(stateMachine.run(
                PLUS,
                DIGIT_2,
                DIGIT_2,
                MEMORY_PLUS,
                MEMORY_RESTORE,
                BACK_SPACE
        ));

        assertTrue(view.hasMemoryFlag());
        assertEquals("22", view.getDisplayText());

        assertEquals(valueOf(22), model.getDisplayData());
    }

    @Test
    public void testBackspaceAfterMemoryPlusInRArg() {
        assertEmpty(stateMachine.run(
                PLUS,
                DIGIT_2,
                DIGIT_2,
                MEMORY_PLUS,
                BACK_SPACE
        ));

        assertTrue(view.hasMemoryFlag());
        assertEquals("22", view.getDisplayText());

        assertEquals(valueOf(22), model.getDisplayData());
    }

    @Test
    public void testPlusAfterMemoryPlus() {
        assertEmpty(stateMachine.run(
                DIGIT_5,
                MEMORY_PLUS,
                PLUS,
                DOT,
                DIGIT_3,
                PLUS
        ));

        assertTrue(view.hasMemoryFlag());
        assertEquals("5.3", view.getDisplayText());

        assertEquals(valueOf(53, 10), model.getDisplayData());
    }

    @Test
    public void testSmallDigitRounding() {
        assertEmpty(stateMachine.run(
                DIGIT_3,
                MINUS,
                DOT,
                DIGIT_6,
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE,
                EVALUATE
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("0", view.getDisplayText());

        assertEquals(valueOf(0), model.getDisplayData());
    }

    @Test
    public void testRound() {
        assertEmpty(stateMachine.run(
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
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("1333333332.6667", view.getDisplayText());

        assertEquals(valueOf(3999999998L, 3L), model.getDisplayData());
    }

    @Test
    public void testManyEvaluationsAfterDot2() {
        assertEmpty(stateMachine.run(
                DIGIT_5,
                MULTIPLY,
                DIGIT_3,
                EVALUATE,
                DOT,
                DIGIT_2,
                EVALUATE
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("0.6", view.getDisplayText());

        assertEquals(valueOf(3, 5), model.getDisplayData());
    }

    @Test
    public void testNegativeZeroSquareRoot() {
        assertEmpty(stateMachine.run(
                DIGIT_0,
                DOT,
                REVERSE,
                SQUARE_ROOT
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("0", view.getDisplayText());

        assertEquals(valueOf(0), model.getDisplayData());
    }

    @Test
    public void testNegativeZeroNormalization() {
        assertEmpty(stateMachine.run(
                DIGIT_0,
                DOT,
                REVERSE,
                MULTIPLY
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("0", view.getDisplayText());

        assertEquals(valueOf(0), model.getDisplayData());
    }

    @Test
    public void testSquareRoot2() {
        assertEmpty(stateMachine.run(
                DIGIT_5,
                DIGIT_5,
                SQUARE_ROOT,
                MULTIPLY,
                EVALUATE
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("55", view.getDisplayText());
    }


    @Test
    public void testSquareRoot3() {
        assertEmpty(stateMachine.run(
                DIGIT_2,
                DIGIT_2,
                DIGIT_2,
                DIGIT_2,
                DIGIT_2,
                DIGIT_2,
                DIGIT_2,
                DIGIT_2,
                DIGIT_2,
                DIGIT_2,
                DIGIT_2,
                DIGIT_2,
                DIGIT_5,
                DIGIT_5,
                DIGIT_5,
                SQUARE_ROOT,
                MULTIPLY,
                EVALUATE
        ));

        assertFalse(view.hasMemoryFlag());
        assertEquals("222222222222555", view.getDisplayText());
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

    private static <T> void assertEmpty(T[] arr) {
        assertEquals(0, arr.length);
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
