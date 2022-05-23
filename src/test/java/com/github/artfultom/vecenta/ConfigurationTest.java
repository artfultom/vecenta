package com.github.artfultom.vecenta;

import org.junit.Test;

import static org.junit.Assert.*;

public class ConfigurationTest {

    @Test
    public void getInt() {
        int value = Configuration.getInt("send.attempt_count");
        assertEquals(10, value);
    }

    @Test
    public void getIntError() {
        try {
            Configuration.getInt("wrong_prop");
            fail();
        } catch (Exception e) {
            assertEquals("property wrong_prop not found", e.getMessage());
        }
    }
}