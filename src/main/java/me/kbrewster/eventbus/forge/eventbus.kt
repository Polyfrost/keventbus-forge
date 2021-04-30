package me.kbrewster.eventbus.forge

import me.kbrewster.eventbus.forge.collection.ConcurrentSubscriberArrayList
import me.kbrewster.eventbus.forge.collection.SubscriberArrayList
import me.kbrewster.eventbus.forge.exception.ExceptionHandler
import me.kbrewster.eventbus.forge.invokers.InvokerType
import me.kbrewster.eventbus.forge.invokers.ReflectionInvoker
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.lang.reflect.Modifier
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class KEventBus @JvmOverloads constructor(
    private val invokerType: InvokerType = ReflectionInvoker(),
    private val exceptionHandler: ExceptionHandler = object : ExceptionHandler {
        override fun handle(exception: Exception) {
            throw exception
        }
    },
    private val threadSafety: Boolean = true
) {

    class Subscriber(private val obj: Any, val priority: EventPriority, private val invoker: InvokerType.SubscriberMethod?) {

        @Throws(Exception::class)
        operator fun invoke(arg: Any?) {
            invoker!!.invoke(arg)
        }

        override fun equals(other: Any?): Boolean {
            return other.hashCode() == this.hashCode()
        }

        override fun hashCode(): Int {
            return obj.hashCode()
        }

    }

    private val subscribers: AbstractMap<Class<*>, MutableList<Subscriber>> =
        if (threadSafety) ConcurrentHashMap() else HashMap()

    /**
     * Subscribes all of the methods marked with the `@Subscribe` annotation
     * within the `obj` instance provided to th methods first parameter class
     *
     * e.g. registering an instance which includes the method below will invoke
     * that method every time EventBus#post(MessageReceivedEvent()) is called.
     * @Subscribe
     * fun messageReceivedEvent(event: MessageReceivedEvent) {
     * }
     *
     */
    fun register(obj: Any) {
        val methods = obj.javaClass.declaredMethods
        for (i in (methods.size - 1) downTo 0) {
            val method = methods[i]
            val sub: SubscribeEvent = method.getAnnotation(SubscribeEvent::class.java) ?: continue

            // verification
            val parameterClazz = method.parameterTypes[0]
            when {
                method.parameterCount != 1 -> throw IllegalArgumentException("Subscribed method must only have one parameter.")
                method.returnType != Void.TYPE -> throw IllegalArgumentException("Subscribed method must be of type 'Void'. ")
                parameterClazz.isPrimitive -> throw IllegalArgumentException("Cannot subscribe method to a primitive.")
                parameterClazz.modifiers and (Modifier.ABSTRACT or Modifier.INTERFACE) != 0 -> throw IllegalArgumentException(
                    "Cannot subscribe method to a polymorphic class."
                )
            }

            val subscriberMethod = invokerType.setup(obj, obj.javaClass, parameterClazz, method)

            val subscriber = Subscriber(obj, sub.priority, subscriberMethod)
            subscribers.putIfAbsent(
                parameterClazz,
                if (threadSafety) ConcurrentSubscriberArrayList() else SubscriberArrayList()
            )
            subscribers[parameterClazz]!!.add(subscriber)
        }
    }

    /**
     * Unsubscribes all `@Subscribe`'d methods inside of the `obj` instance.
     */
    fun unregister(obj: Any) {
        val methods = obj.javaClass.declaredMethods
        for (i in (methods.size - 1) downTo 0) {
            val method = methods[i]
            if (method.getAnnotation(SubscribeEvent::class.java) == null) {
                continue
            }
            subscribers[method.parameterTypes[0]]?.remove(Subscriber(obj, EventPriority.LOWEST, null))
        }
    }

    /**
     * Posts the event instance given to all the subscribers
     * that are subscribed to the events class.
     */
    fun post(event: Any) {
        val events = subscribers[event.javaClass] ?: return
        // executed in descending order
        for (i in (events.size - 1) downTo 0) {
            try {
                events[i].invoke(event)
            } catch (e: Exception) {
                exceptionHandler.handle(e)
            }
        }
    }

    /**
     * Supplier is only used if there are subscribers listening to
     * the event.
     *
     * Example usage: EventBus#post { ComputationallyHeavyEvent() }
     *
     * This allows events to only be constructed if needed.
     */
    inline fun <reified T> post(supplier: () -> T) {
        val events = getSubscribedEvents(T::class.java) ?: return
        val event = supplier()
        // executed in descending order
        for (i in (events.size - 1) downTo 0) {
            events[i].invoke(event)
        }
    }

    fun getSubscribedEvents(clazz: Class<*>) = subscribers[clazz]

    private inline fun iterateSubclasses(obj: Any, body: (Class<*>) -> Unit) {
        var postClazz: Class<*>? = obj.javaClass
        do {
            body(postClazz!!)
            postClazz = postClazz.superclass
        } while (postClazz != null)
    }
}
