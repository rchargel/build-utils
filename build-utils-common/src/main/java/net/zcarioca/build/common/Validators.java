package net.zcarioca.build.common;

import org.apache.commons.lang3.StringUtils;

import java.util.function.Supplier;

public class Validators {

    private Validators() {
        // cannot be instantiated
    }

    public static void requireNotNull(final Object t, final String message) throws IllegalArgumentException {
        requireNotNull(t, () -> new IllegalArgumentException(message));
    }

    public static <E extends Throwable> void requireNotNull(final Object t, final Supplier<E> errorSupplier) throws E {
        if (t == null) {
            throw errorSupplier.get();
        }
    }

    public static void requireNotBlank(final String s, final String message) throws IllegalArgumentException {
        requireNotBlank(s, () -> new IllegalArgumentException(message));
    }

    public static <E extends Throwable> void requireNotBlank(final String s, final Supplier<E> errorSupplier) throws E {
        if (StringUtils.isBlank(s)) {
            throw errorSupplier.get();
        }
    }
}
