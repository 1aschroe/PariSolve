package parisolve;

import org.junit.Assert;

import org.junit.Test;

import parisolve.backend.algorithms.BigStepAlgorithm;

public class BigStepHelperTest {

    @Test
    public final void testImplication() {
        for (int c = 0; c < 100; c++) {
            Assert.assertEquals(BigStepAlgorithm.gamma(c + 1),
                    BigStepAlgorithm.gamma(c) + 1 - BigStepAlgorithm.beta(c), 0.001);
        }
    }
}
