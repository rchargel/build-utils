package com.github.rchargel.build.common;

import org.junit.Test;

import static com.github.rchargel.build.common.ExceptionWrapper.ignoreException;
import static com.github.rchargel.build.common.ExceptionWrapper.wrap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class CanWrapAndIgnoreExceptionsTest {
    @Test
    public void canWrapThrowable() {
        wrap(IllegalArgumentException.class, () -> {
            assertTrue(true);
        });

        assertThrows(IllegalArgumentException.class, () -> wrap(IllegalArgumentException.class, () -> {
            assertTrue(false);
        }));
    }

    @Test
    public void canWrapSupplier() {
        assertEquals("value", wrap(IllegalArgumentException.class, () -> "value"));

        assertThrows(IllegalArgumentException.class, () -> wrap(IllegalArgumentException.class, () -> {
            assertTrue(false);
            return "value";
        }));
    }

    @Test
    public void canIgnoreRunnable() {
        ignoreException(() -> assertTrue(true));
        ignoreException(() -> assertTrue(false));
    }

    @Test
    public void canIgnoreSupplier() {
        assertEquals("value", ignoreException(() -> "value"));
        assertNull(ignoreException(() -> {
            assertTrue(false);
            return "value";
        }));
    }
}
