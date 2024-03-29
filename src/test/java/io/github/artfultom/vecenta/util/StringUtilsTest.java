package io.github.artfultom.vecenta.util;

import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class StringUtilsTest {

    @Test
    public void capitalizeFirstLetter() {
        assertEquals("Test", StringUtils.capitalizeFirstLetter("test"));
        assertEquals("TEST", StringUtils.capitalizeFirstLetter("TEST"));
        assertEquals("TEST", StringUtils.capitalizeFirstLetter("tEST"));
        assertEquals("", StringUtils.capitalizeFirstLetter(""));
        assertEquals("A", StringUtils.capitalizeFirstLetter("a"));
    }

    @Test
    public void getSimpleTypes() {
        assertEquals(Set.of("test"), StringUtils.getSimpleTypes("test"));
        assertEquals(Set.of("test"), StringUtils.getSimpleTypes("[test]"));
        assertEquals(Set.of("test", "test1"), StringUtils.getSimpleTypes("[test]test1"));
        assertEquals(Set.of("test", "test1"), StringUtils.getSimpleTypes("[test][test1]"));
    }

    @Test
    public void fillModelName() {
        assertEquals("int32", StringUtils.fillModelName(List.of("client", "entity"), "int32"));
        assertEquals("client.entity.Model", StringUtils.fillModelName(List.of("client", "entity"), "Model"));
        assertEquals("[int32]", StringUtils.fillModelName(List.of("client", "entity"), "[int32]"));
        assertEquals("[client.entity.Model]", StringUtils.fillModelName(List.of("client", "entity"), "[Model]"));
        assertEquals("[int32]int32", StringUtils.fillModelName(List.of("client", "entity"), "[int32]int32"));
        assertEquals("[client.entity.Model]client.entity.Model", StringUtils.fillModelName(List.of("client", "entity"), "[Model]Model"));
        assertEquals("[client.entity.Model][client.entity.Model]", StringUtils.fillModelName(List.of("client", "entity"), "[Model][Model]"));
        assertEquals("[test.test.test][test.test.test]", StringUtils.fillModelName(List.of("test", "test"), "[test][test]"));
    }

    @Test
    public void getExceptionName() {
        assertNull(StringUtils.getExceptionName(null));
        assertEquals("", StringUtils.getExceptionName(""));
        assertEquals("AException", StringUtils.getExceptionName("a"));
        assertEquals("AException", StringUtils.getExceptionName("A"));
        assertEquals("TestErrorException", StringUtils.getExceptionName("test error"));
        assertEquals("TestErrorException", StringUtils.getExceptionName("test error!"));
        assertEquals("TestErrorException", StringUtils.getExceptionName("test/error!"));
        assertEquals("TestErrorException", StringUtils.getExceptionName("test\\error!"));
    }

    @Test
    public void getMethodName() {
        assertEquals(
                "entity.method(a,b)->return",
                StringUtils.getMethodName("entity", "method", List.of("a", "b"), "return")
        );
        assertEquals(
                "entity.method()",
                StringUtils.getMethodName("entity", "method", null, null)
        );
    }
}