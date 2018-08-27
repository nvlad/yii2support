package com.nvlad.yii2support.common;

import org.junit.Test;

import static org.junit.Assert.*;

public class StringUtilsTest {
    @Test
    public void camelToId() {
        assertEquals(StringUtils.CamelToId("testId"), "test_id");
    }

    @Test
    public void camelToIdWithSeparator() {
        assertEquals(StringUtils.CamelToId("testId", "+"), "test+id");

        assertEquals(StringUtils.CamelToId("testId", ""), "testid");

        assertEquals(StringUtils.CamelToId("testId", "-"), "test-id");

        assertEquals(StringUtils.CamelToId("testId", "_"), "test_id");
    }

}