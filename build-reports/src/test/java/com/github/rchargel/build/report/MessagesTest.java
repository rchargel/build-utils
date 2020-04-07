package com.github.rchargel.build.report;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ResourceBundle;

import static org.junit.Assert.assertEquals;

public class MessagesTest {
    private static Messages underTest;

    @Test
    public void test1() {
        assertEquals("This is a message", underTest.text("message.1"));
    }

    @Test
    public void test2() {
        assertEquals("This is my second message", underTest.text("message.2", "second"));
    }

    @Test
    public void test3() {
        assertEquals("This is Hamster's third message", underTest.text("message.3", "Hamster's", "third"));
    }

    @BeforeClass
    public static void setup() {
        final ResourceBundle bundle = ResourceBundle.getBundle("testmessages");
        underTest = new Messages(bundle);
    }

}
