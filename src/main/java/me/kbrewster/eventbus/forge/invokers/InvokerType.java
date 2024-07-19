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
    interface SubscriberMethodBoolean
    {
        boolean invoke(Object event) throws Exception;
    }

    @FunctionalInterface
    interface SubscriberMethodInt
    {
        int invoke(Object event) throws Exception;
    }

    @FunctionalInterface
    interface SubscriberMethodFloat
    {
        float invoke(Object event) throws Exception;
    }

    @FunctionalInterface
    interface SubscriberMethodDouble
    {
        double invoke(Object event) throws Exception;
    }

    @FunctionalInterface
    interface SubscriberMethodLong
    {
        long invoke(Object event) throws Exception;
    }

    @FunctionalInterface
    interface SubscriberMethodShort
    {
        short invoke(Object event) throws Exception;
    }

    @FunctionalInterface
    interface SubscriberMethodByte
    {
        byte invoke(Object event) throws Exception;
    }

    @FunctionalInterface
    interface SubscriberMethodChar
    {
        char invoke(Object event) throws Exception;
    }

    @FunctionalInterface
    interface SubscriberMethodObject
    {
        Object invoke(Object event) throws Exception;
    }
}
