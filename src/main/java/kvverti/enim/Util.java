package kvverti.enim;

import java.io.*;
import java.util.regex.Matcher;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.*;

import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.relauncher.ReflectionHelper;

import kvverti.enim.entity.Entities;

/** Utility class */
public final class Util {

	public static final ResourceLocation MISSING_LOCATION = new ResourceLocation("missingno");

	private Util() { }

	public static Field findField(Class<?> declareClass, Class<?> returnType, String... names) {

		Field f = ReflectionHelper.findField(declareClass, names);
		assertThat(f.getType() == returnType,
			String.format("Type of field %s was %s, expected %s", f, f.getType(), returnType));
		return f;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getField(Object instance, Field field) {

		try { return (T) field.get(instance); }
		catch(IllegalAccessException e) {

			throw unchecked(e);
		}
	}

	public static int getIntField(Object instance, Field field) {

		try { return field.getInt(instance); }
		catch(IllegalAccessException e) {

			throw unchecked(e);
		}
	}

	public static void setField(Object instance, Field field, Object value) {

		try { field.set(instance, value); }
		catch(IllegalAccessException e) {

			throw unchecked(e);
		}
	}

	public static <T> Method findMethod(Class<T> declareClass, Class<?> returnType, String[] names, Class<?>... params) {

		Method m = ReflectionHelper.findMethod(declareClass, null, names, params);
		assertThat(m.getReturnType() == returnType,
			String.format("Type of method %s was %s, expected %s", m, m.getReturnType(), returnType));
		return m;
	}

	@SuppressWarnings("unchecked")
	public static <T> T invokeMethod(Object instance, Method method, Object... args) throws InvocationTargetException {

		try { return (T) method.invoke(instance, args); }
		catch(IllegalAccessException e) {

			throw unchecked(e);
		}
	}

	public static <T> T invokeUnchecked(Object instance, Method method, Object... args) {

		try { return invokeMethod(instance, method, args); }
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

	public static <E, X extends Exception>
		void validate(Iterable<E> coll, Predicate<? super E> check, Function<? super E, X> exc) throws X {

		for(E e : coll) { if(!check.test(e)) throw exc.apply(e); }
	}

	public static <E, X extends Exception>
		void validate(E[] arr, Predicate<? super E> check, Function<? super E, X> exc) throws X {

		for(E e : arr) { if(!check.test(e)) throw exc.apply(e); }
	}

	public static <E, X extends Exception>
		void validate(Iterable<E> coll, ThrowingConsumer<? super E, X> check) throws X {

		for(E e : coll) { check.acceptThrowing(e); }
	}

	public static void assertThat(boolean condition, Object message) {

		if(!condition) throw new AssertionError(message);
	}

	public static void assertFalse(Object message) {

		assertThat(false, message);
	}

	public static ResourceLocation getResourceLocation(String location, String prefix, String postfix) {

		Matcher m = Keys.RESOURCE_LOCATION_REGEX.matcher(location);
		return location != null && m.matches() ?
			new ResourceLocation(m.group("domain"), prefix + m.group("filepath") + postfix)
			: Util.MISSING_LOCATION;
	}

	public static Reader getReaderFor(ResourceLocation location) throws IOException {

		IResource resource = Entities.resourceManager().getResource(location);
		InputStream istream = resource.getInputStream();
		return new InputStreamReader(istream);
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

		public <X extends Throwable> X orElseWrap(Function<? super Throwable, X> cnstr) {

			return cnstr.apply(getCause());
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