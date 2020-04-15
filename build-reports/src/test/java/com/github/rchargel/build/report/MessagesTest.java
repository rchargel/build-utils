package com.github.rchargel.build.report;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.Assert.assertEquals;

public class MessagesTest {
    private static Messages english;
    private static Messages portuguese;

    @Test
    public void test1() {
        assertEquals("This is a message", english.text("message.1"));
    }

    @Test
    public void test2() {
        assertEquals("This is my second message", english.text("message.2", "second"));
    }

    @Test
    public void test3() {
        assertEquals("This is Hamster's third message", english.text("message.3", "Hamster's", "third"));
    }

    @Test
    public void test4() {
        assertEquals("Esta é a minha mensagem", portuguese.text("message.1"));
    }

    @BeforeClass
    public static void setup() {
        final ResourceBundle bundle = ResourceBundle.getBundle("testmessages");
        english = Messages.loadMessages("testMessages", Locale.ENGLISH);

        portuguese = Messages.loadMessages("testmessages", new Locale("pt", "BR"));
    }

}
