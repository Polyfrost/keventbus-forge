package me.kbrewster.eventbus.forge.invokers;

import java.lang.reflect.Method;

public class ReflectionInvoker implements InvokerType {

    @Override
    public SubscriberMethod setup(Object object, Class clazz, Class parameterClazz, Method method) {
        method.setAccessible(true);
        return (paramObject) -> method.invoke(object, paramObject);
    }
}
