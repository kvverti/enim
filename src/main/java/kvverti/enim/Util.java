package kvverti.enim;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.*;

/** Utility class */
public final class Util {

	private Util() { }

	@SuppressWarnings("unchecked")
	public static <T> T getField(Field field, Object instance) {

		try { return (T) field.get(instance); }
		catch(IllegalAccessException e) {

			throw new AssertionError("Field was not accessible!");
		}
	}

	public static int getIntField(Field field, Object instance) {

		try { return field.getInt(instance); }
		catch(IllegalAccessException e) {

			throw new AssertionError("Field was not accessible!");
		}
	}

	public static void setField(Field field, Object instance, Object value) {

		try { field.set(instance, value); }
		catch(IllegalAccessException e) {

			throw new AssertionError("Field was not accessible!");
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T invokeMethod(Method method, Object instance, Object... args) throws InvocationTargetException {

		try { return (T) method.invoke(instance, args); }
		catch(IllegalAccessException e) {

			throw new AssertionError("Method was not accessible!");
		}
	}

	public static <T> T invokeUnchecked(Method method, Object instance, Object... args) {

		try { return invokeMethod(method, instance, args); }
		catch(InvocationTargetException e) {

			throw unchecked(e.getCause());
		}
	}

	public static RuntimeException unchecked(Throwable e) {

		if(e instanceof RuntimeException)
			throw (RuntimeException) e;
		else if(e instanceof Error)
			throw (Error) e;
		else throw new WrappedCheckedException(e);
	}

	public static class WrappedCheckedException extends RuntimeException {

		private final Class<? extends Throwable> wrapType;

		WrappedCheckedException(Throwable cause) {

			super(cause);
			wrapType = cause.getClass();
		}

		public <X extends Throwable> WrappedCheckedException ifInstance(Class<X> cls) throws X {

			if(cls.isAssignableFrom(wrapType))
				throw cls.cast(getCause());
			return this;
		}

		public <X extends Throwable> X orElseWrap(Function<? super Throwable, X> cnstr) throws X {

			throw cnstr.apply(getCause());
		}
	}

	@FunctionalInterface
	public interface ThrowingConsumer<T, X extends Exception> extends Consumer<T> {

		void acceptThrowing(T t) throws X;

		@Override
		default void accept(T t) {

			try { acceptThrowing(t); }
			catch(Exception e) {

				throw unchecked(e);
			}
		}

		static <T> Consumer<T> of(ThrowingConsumer<T, ?> cons) {

			return cons;
		}
	}

	@FunctionalInterface
	public interface ThrowingFunction<T, R, X extends Exception> extends Function<T, R> {

		R applyThrowing(T t) throws X;

		@Override
		default R apply(T t) {

			try { return applyThrowing(t); }
			catch(Exception e) {

				throw unchecked(e);
			}
		}

		static <T, R> Function<T, R> of(ThrowingFunction<T, R, ?> func) {

			return func;
		}
	}
}