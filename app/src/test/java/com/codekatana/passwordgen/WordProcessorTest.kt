package com.codekatana.passwordgen

import org.junit.Test

import org.junit.Assert.*

class WordProcessorTest {

    @Test
    fun sprinkleNumbers() {
        assertNotEquals("Frame Correction", WordProcessor.sprinkleNumbers("Frame Correction"))
        assertNotEquals("Glorious", WordProcessor.sprinkleNumbers("Glorious"))
        assertNotEquals("dry", WordProcessor.sprinkleNumbers("dry"))
    }
}