package com.myzone.calculator;

import com.myzone.calculator.controller.CalculatorStateFactory;
import com.myzone.calculator.model.CalculatorModel;
import com.myzone.calculator.model.Signal;
import com.myzone.calculator.view.CalculatorView;
import com.myzone.utils.statemachine.TestingEventStateMachine;
import org.junit.Before;
import org.junit.Test;

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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("4", session.getDisplayText());
            assertEquals(4D, session.getDisplayData(), 0D);
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("16", session.getDisplayText());
            assertEquals(16D, session.getDisplayData(), 0D);
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("16", session.getDisplayText());
            assertEquals(16D, session.getDisplayData(), 0D);
        }
    }

    @Test
    public void testSquare() {
        assertEquals(0, stateMachine.run(
                DIGIT_2,
                MULTIPLY,
                EVALUATE
        ).length);

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("4", session.getDisplayText());
            assertEquals(4D, session.getDisplayData(), 0D);
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("25", session.getDisplayText());
            assertEquals(25D, session.getDisplayData(), 0D);
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("25", session.getDisplayText());
            assertEquals(25D, session.getDisplayData(), 0D);
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("25", session.getDisplayText());
            assertEquals(25D, session.getDisplayData(), 0D);
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("10", session.getDisplayText());
            assertEquals(10D, session.getDisplayData(), 0D);
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("3.2", session.getDisplayText());
            assertEquals(3.2D, session.getDisplayData(), 0D);
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("0.00363945578231", session.getDisplayText());
            assertEquals(0.0036394557823129247D, session.getDisplayData(), 0D);
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("6.25", session.getDisplayText());
            assertEquals(6.25D, session.getDisplayData(), 0D);
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("21.25", session.getDisplayText());
            assertEquals(21.25D, session.getDisplayData(), 0D);
        }
    }

    @Test
    public void testPercentWithOneArg() {
        assertEquals(0, stateMachine.run(
                PLUS,
                DIGIT_1,
                PERCENT
        ).length);

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("0", session.getDisplayText());
            assertEquals(0D, session.getDisplayData(), 0D);
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("0.96", session.getDisplayText());
            assertEquals(0.96D, session.getDisplayData(), 0D);
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("47.04", session.getDisplayText());
            assertEquals(47.04D, session.getDisplayData(), 0D);
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("0.", session.getDisplayText());
            assertEquals(0D, session.getDisplayData(), 0D);
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("56", session.getDisplayText());
            assertEquals(56D, session.getDisplayData(), 0D);
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("-56", session.getDisplayText());
            assertEquals(-56D, session.getDisplayData(), 0D);
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("ERR", session.getDisplayText());
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("ERR", session.getDisplayText());
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("ERR", session.getDisplayText());
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("6", session.getDisplayText());
            assertEquals(6D, session.getDisplayData(), 0D);
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("3", session.getDisplayText());
            assertEquals(3D, session.getDisplayData(), 0D);
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("2.7355647997348", session.getDisplayText());
            assertEquals(2.7355647997347607, session.getDisplayData(), 0D);
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("1.6539542919122", session.getDisplayText());
            assertEquals(1.6539542919121921, session.getDisplayData(), 0D);
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("12.071067811866", session.getDisplayText());
            assertEquals(12.0710678118655D, session.getDisplayData(), 0D);
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("0", session.getDisplayText());
            assertEquals(0D, session.getDisplayData(), 0D);
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("0", session.getDisplayText());
            assertEquals(0D, session.getDisplayData(), 0D);
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("55555.2", session.getDisplayText());
            assertEquals(55555.2D, session.getDisplayData(), 0D);
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("56.", session.getDisplayText());
            assertEquals(56.0D, session.getDisplayData(), 0D);
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("1.66666666666667e+16", session.getDisplayText());
            assertEquals(1.6666666666666664E16D , session.getDisplayData(), 0D);
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("-1.66666666666667e+16", session.getDisplayText());
            assertEquals(-1.6666666666666664E16D, session.getDisplayData(), 0D);
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("0.00000000000001", session.getDisplayText());
            assertEquals(1.0E-14D, session.getDisplayData(), 0D);
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("0.0008", session.getDisplayText());
            assertEquals(0.0008D, session.getDisplayData(), 0D);
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("0", session.getDisplayText());
            assertEquals(0D, session.getDisplayData(), 0D);
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

        try (CalculatorModel.Session session = model.createSession()) {
            assertEquals("0", session.getDisplayText());
            assertEquals(0D, session.getDisplayData(), 0D);
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("0", session.getDisplayText());
            assertEquals(0D, session.getDisplayData(), 0D);
        }
    }

    @Test
    public void testReverseNullsAtBeginning() {
        assertEquals(0, stateMachine.run(
                DIGIT_0,
                REVERSE
        ).length);


        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("0", session.getDisplayText());
            assertEquals(0D, session.getDisplayData(), 0D);
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("0", session.getDisplayText());
            assertEquals(0D, session.getDisplayData(), 0D);
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("262144", session.getDisplayText());
            assertEquals(262144D, session.getDisplayData(), 0D);
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("381469726562500", session.getDisplayText());
            assertEquals(381469726562500D, session.getDisplayData(), 0D);
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("4", session.getDisplayText());
            assertEquals(4D, session.getDisplayData(), 0D);
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("0", session.getDisplayText());
            assertEquals(0D, session.getDisplayData(), 0D);
        }
    }

    @Test
    public void testReverseSquareRoot() {
        assertEquals(0, stateMachine.run(
                DIGIT_2,
                SQUARE_ROOT,
                REVERSE
        ).length);

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("-1.4142135623731", session.getDisplayText());
            assertEquals(-1.4142135623731D, session.getDisplayData(), 0D);
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("743", session.getDisplayText());
            assertEquals(743D, session.getDisplayData(), 0D);
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("0.01", session.getDisplayText());
            assertEquals(0.01D, session.getDisplayData(), 0D);
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("0.01", session.getDisplayText());
            assertEquals(0.01D, session.getDisplayData(), 0D);
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


        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("9", session.getDisplayText());
            assertEquals(9D, session.getDisplayData(), 0D);
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("9", session.getDisplayText());
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("19", session.getDisplayText());
            assertEquals(19D, session.getDisplayData(), 0D);
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("19", session.getDisplayText());
            assertEquals(19D, session.getDisplayData(), 0D);
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("1", session.getDisplayText());
            assertEquals(0.999999999999995D, session.getDisplayData(), 0D);
        }
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("2", session.getDisplayText());
            assertEquals(2D, session.getDisplayData(), 0D);
        }
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("999999999999998", session.getDisplayText());
            assertEquals(9.99999999999998E14D, session.getDisplayData(), 0D);
        }
    }

    @Test
    public void testSquareRootAfterClearAfterError() {
        assertEquals(0, stateMachine.run(
                DIGIT_0,
                INVERSE,
                CLEAR,
                SQUARE_ROOT
        ).length);

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("0", session.getDisplayText());
            assertEquals(0D, session.getDisplayData(), 0D);
        }
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("9.99999999999997e+44", session.getDisplayText());
            assertEquals(9.99999999999997E44D, session.getDisplayData(), 0D);
        }
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("4", session.getDisplayText());
            assertEquals(3333D, session.getMemory(), 0D);
        }
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("4", session.getDisplayText());
            assertEquals(3333D, session.getMemory(), 0D);
        }
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("1.00000000000001e+15", session.getDisplayText());
            assertEquals(1000000000000001D, session.getDisplayData(), 0D);
        }
    }

    @Test
    public void testReverse() {
        assertEquals(0, stateMachine.run(
                DIGIT_9,
                REVERSE,
                DIGIT_9
        ).length);

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("-99", session.getDisplayText());
            assertEquals(-99D, session.getDisplayData(), 0D);
        }
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("238", session.getDisplayText());
            assertEquals(238D, session.getDisplayData(), 0D);
        }
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("238", session.getDisplayText());
            assertEquals(238D, session.getDisplayData(), 0D);
        }
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

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
            assertEquals("0", session.getDisplayText());
            assertEquals(1.1121061493012954E-15D, session.getDisplayData(), 0D);
        }
    }

    public void testTODON() {
        assertEquals(0, stateMachine.run(
                DIVIDE
        ).length);

        try (CalculatorModel.Session session = model.createSession()) {
            verify(view, atLeastOnce()).invalidate();
        }
    }
}
