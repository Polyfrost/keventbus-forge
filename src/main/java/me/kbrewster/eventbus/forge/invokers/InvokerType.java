package me.kbrewster.eventbus.forge.invokers;

import java.lang.reflect.Method;

public interface InvokerType {

    Object setup(Object object, Class<?> clazz, Class<?> parameterClazz, Method method) throws Throwable;

    @FunctionalInterface
    interface SubscriberMethod
    {
        void invoke(Object event) throws Exception;
    }

    @FunctionalInterface
    interface SubscriberMethodObject
    {
        Object invoke(Object event) throws Exception;
    }
}
