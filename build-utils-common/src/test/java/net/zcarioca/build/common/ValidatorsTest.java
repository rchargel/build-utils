package net.zcarioca.build.common;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;

public class ValidatorsTest {

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Test
    public void testRequireNotNull() {
        Validators.requireNotNull("Object", "Won't be thrown");
        Validators.requireNotNull("", "Blank is still not null");
    }

    @Test
    public void testRequireNotNullStandard() {
        expected.expect(IllegalArgumentException.class);
        expected.expectMessage("The default error is an IllegalArgumentException");
        Validators.requireNotNull(null, "The default error is an IllegalArgumentException");
    }

    @Test
    public void testRequireNotNullIOE() throws IOException {
        expected.expect(IOException.class);
        expected.expectMessage("The exception class can be changed");
        Validators.requireNotNull(null, () -> new IOException("The exception class can be changed"));
    }

    @Test
    public void testRequireNotBlank() {
        expected.expect(IllegalArgumentException.class);
        expected.expectMessage("This is blank");
        Validators.requireNotBlank("not blank", "This is not blank");
        Validators.requireNotBlank(" ", "This is blank");
    }

    @Test
    public void testRequireNotBlankWithNPE() {
        expected.expect(NullPointerException.class);
        expected.expectMessage("Null is still blank");

        Validators.requireNotBlank(null, () -> new NullPointerException("Null is still blank"));
    }
}
