package com.github.rchargel.build.common

object ExceptionWrapper {

    @JvmStatic
    fun <R> ignoreException(action: ThrowingSupplier<R>): R? = wrap(null as Class<Exception>?, action)

    @JvmStatic
    fun ignoreException(action: ThrowingRunnable): Unit = wrap(null as Class<Exception>?, action)

    @JvmStatic
    fun <E : Throwable, R> wrap(wrappedException: Class<E>?, action: ThrowingSupplier<R>): R? = try {
        action.get()
    } catch (e: Throwable) {
        if (wrappedException != null) {
            if (wrappedException.isAssignableFrom(e.javaClass))
                throw e
            else
                throw wrapException(e, wrappedException)
        } else null
    }

    @JvmStatic
    fun <E : Throwable> wrap(wrappedException: Class<E>?, action: ThrowingRunnable): Unit = try {
        action.run()
    } catch (e: Throwable) {
        if (wrappedException != null) {
            if (wrappedException.isAssignableFrom(e.javaClass)) {
                throw e
            } else {
                throw wrapException(e, wrappedException)
            }
        } else {
        }
    }

    fun <R> ignoreError(action: () -> R): R? = ignoreException(ThrowingSupplier(action))

    fun <E : Throwable, R> wrap(wrappedException: Class<E>?, action: () -> R): R? =
            wrap(wrappedException, ThrowingSupplier(action))

    private fun <E : Throwable> wrapException(e: Throwable, wrappedException: Class<E>): E {
        val constructor = wrappedException.getConstructor(Throwable::class.java)
        return constructor.newInstance(e)
    }
}