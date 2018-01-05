package kvverti.enim;

import java.io.*;
import java.util.regex.Matcher;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.*;

import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.relauncher.ReflectionHelper;

import com.google.common.util.concurrent.UncheckedExecutionException;

import kvverti.enim.entity.Entities;

/** Utility class */
public final class Util {

    public static final ResourceLocation MISSING_LOCATION = new ResourceLocation("missingno");

    private Util() { }

    public static Field findField(Class<?> declareClass, Class<?> returnType, String... names) {

        Field f = ReflectionHelper.findField(declareClass, names);
        assert f.getType() == returnType
            : String.format("Type of field %s was %s, expected %s", f, f.getType(), returnType);
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

    public static Method findMethod(Class<?> declareClass, Class<?> returnType, String name, String obfName, Class<?>... params) {

        Method m = ReflectionHelper.findMethod(declareClass, name, obfName, params);
        assert m.getReturnType() == returnType
            : String.format("Type of method %s was %s, expected %s", m, m.getReturnType(), returnType);
        return m;
    }

    public static Method findMethod(Class<?> declareClass, Class<?> returnType, String name, Class<?>... params) {

        return findMethod(declareClass, returnType, name, name, params);
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
        else throw new UncheckedExecutionException(e);
    }

    public static <E, X extends Exception>
        void validate(Iterable<E> coll, Predicate<? super E> check, Function<? super E, X> exc) throws X {

        for(E e : coll) { if(!check.test(e)) throw exc.apply(e); }
    }

    public static <E, X extends Exception>
        void validate(E[] arr, Predicate<? super E> check, Function<? super E, X> exc) throws X {

        for(E e : arr) { if(!check.test(e)) throw exc.apply(e); }
    }

    public static ResourceLocation getResourceLocation(String location, String prefix, String postfix) {

        Matcher m = Keys.RESOURCE_LOCATION_REGEX.matcher(location);
        return location != null && m.matches() ?
            new ResourceLocation(m.group("domain"), prefix + m.group("filepath") + postfix)
            : Util.MISSING_LOCATION;
    }

    public static Reader getReaderFor(IResourceManager manager, ResourceLocation location) throws IOException {

        IResource resource = manager.getResource(location);
        InputStream istream = resource.getInputStream();
        return new BufferedReader(new InputStreamReader(istream));
    }

    public static Reader getReaderFor(ResourceLocation location) throws IOException {

        return getReaderFor(Entities.resourceManager(), location);
    }
}