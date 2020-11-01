package com.codekatana.passwordgen

import org.junit.Assert.assertNotEquals
import org.junit.Test

class WordProcessorTest {

    @Test
    fun sprinkleNumbers() {
        assertNotEquals("Frame Correction", WordProcessor.sprinkleNumbers("Frame Correction"))
        assertNotEquals("Glorious", WordProcessor.sprinkleNumbers("Glorious"))
        assertNotEquals("dry", WordProcessor.sprinkleNumbers("dry"))
    }


    @Test
    fun specializers() {
        assertNotEquals("Frame Correction", WordProcessor.specialize("Frame Correction"))
        assertNotEquals("Glorious", WordProcessor.specialize("Glorious"))
        assertNotEquals("dry", WordProcessor.specialize("dry"))
    }
}