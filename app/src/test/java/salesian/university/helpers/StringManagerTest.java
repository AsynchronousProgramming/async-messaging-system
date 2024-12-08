package salesian.university.helpers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringManagerTest {
    private final StringManager stringManager = new StringManager();

    @Test
    void testGetLowerCaseString_WithUpperCaseInput() {
        String input = "HELLO WORLD";
        String expected = "hello world";
        String actual = stringManager.getLowerCaseString(input);

        assertEquals(expected, actual);
    }

    @Test
    void testGetLowerCaseString_WithMixedCaseInput() {
        String input = "HeLLo WoRLd";
        String expected = "hello world";
        String actual = stringManager.getLowerCaseString(input);

        assertEquals(expected, actual);
    }

    @Test
    void testGetLowerCaseString_WithLowerCaseInput() {
        String input = "hello world";
        String expected = "hello world";
        String actual = stringManager.getLowerCaseString(input);

        assertEquals(expected, actual);
    }

    @Test
    void testGetLowerCaseString_WithNumbersAndSymbols() {
        String input = "1234!@#$";
        String expected = "1234!@#$";
        String actual = stringManager.getLowerCaseString(input);

        assertEquals(expected, actual);
    }

    @Test
    void testGetLowerCaseString_WithEmptyString() {
        String input = "";
        String expected = "";
        String actual = stringManager.getLowerCaseString(input);

        assertEquals(expected, actual);
    }

    @Test
    void testGetLowerCaseString_WithNullInput() {
        assertThrows(NullPointerException.class, () -> stringManager.getLowerCaseString(null));
    }
}
