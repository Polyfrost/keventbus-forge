package me.kbrewster.eventbus.forge.invokers.direct;

import java.util.HashMap;
import java.util.Map;

public final class DirectInvokerClassLoader extends ClassLoader {

    private final Map<String, Class<?>> classes = new HashMap<>();

    public DirectInvokerClassLoader(ClassLoader parent) {
        super(parent);
    }

    public Class<?> createClass(String className, byte[] bytes) {
        try {
            Class<?> clazz = this.defineClass(className, bytes, 0, bytes.length);
            if (clazz != null) {
                this.classes.put(className, clazz);
            }
            return clazz;
        } catch (LinkageError e) {
            return this.findLoadedClass(className);
        }
    }

    public Class<?> getClassIfPresent(String name) {
        return this.classes.get(name);
    }
}
