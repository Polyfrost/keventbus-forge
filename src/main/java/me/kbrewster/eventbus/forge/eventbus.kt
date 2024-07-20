package me.kbrewster.eventbus.forge

import com.google.common.reflect.TypeToken
import me.kbrewster.eventbus.forge.collection.ConcurrentSubscriberArrayList
import me.kbrewster.eventbus.forge.collection.SubscriberArrayList
import me.kbrewster.eventbus.forge.exception.ExceptionHandler
import me.kbrewster.eventbus.forge.invokers.DirectInvoker
import me.kbrewster.eventbus.forge.invokers.InvokerType
import me.kbrewster.eventbus.forge.invokers.InvokerType.SubscriberMethod
import me.kbrewster.eventbus.forge.invokers.InvokerType.SubscriberMethodObject
import me.kbrewster.eventbus.forge.invokers.InvokerType.SubscriberMethodParent
import me.kbrewster.eventbus.forge.invokers.ReflectionInvoker
import net.minecraftforge.fml.common.eventhandler.Event
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
    private val threadSafety: Boolean = true, // set to false in forge
    private val busId: Int = 0
) {

    private val subscribers: AbstractMap<Class<*>, MutableList<Subscriber>> =
        if (threadSafety) ConcurrentHashMap() else HashMap()

    private val cachedEvents: AbstractMap<Class<*>, Event> =
        if (threadSafety) ConcurrentHashMap() else HashMap()

    private val cachedSuperclasses: AbstractMap<Class<*>, MutableList<Subscriber>> =
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
        val supers: Set<Class<*>?> = TypeToken.of(obj::class.java).getTypes().rawTypes()
        val methods = obj.javaClass.methods
        for (i in (methods.size - 1) downTo 0) {
            for (cls in supers) {
                if (cls == null) continue
                val method = try {
                    cls.getDeclaredMethod(methods[i].name, *methods[i].parameterTypes)
                } catch (e: NoSuchMethodException) {
                    continue
                }

                val sub: SubscribeEvent = method.getAnnotation(SubscribeEvent::class.java) ?: continue

                // verification
                val parameterClazz = method.parameterTypes[0]
                when {
                    method.parameterCount != 1 -> throw IllegalArgumentException("Subscribed method must only have one parameter.")
                    parameterClazz.isPrimitive -> throw IllegalArgumentException("Cannot subscribe method to a primitive.")
                    parameterClazz.modifiers and (Modifier.ABSTRACT or Modifier.INTERFACE) != 0 -> throw IllegalArgumentException(
                        "Cannot subscribe method to a polymorphic class."
                    )
                }

                val subscriberMethod = invokerType.setup(obj, obj.javaClass, parameterClazz, method)

                val subscriber = when (subscriberMethod) {
                    is SubscriberMethod -> SubscriberVoid(obj, sub.priority, sub.receiveCanceled, subscriberMethod)
                    is SubscriberMethodParent -> SubscriberVoidParent(obj, sub.priority, sub.receiveCanceled, subscriberMethod)
                    is SubscriberMethodObject -> SubscriberObject(obj, sub.priority, sub.receiveCanceled, subscriberMethod)
                    else -> throw IllegalArgumentException("Invalid subscriber method")
                }
                subscribers.putIfAbsent(
                    parameterClazz,
                    if (threadSafety) ConcurrentSubscriberArrayList() else SubscriberArrayList()
                )
                subscribers[parameterClazz]!!.add(subscriber)
                if (Event::class.java.isAssignableFrom(parameterClazz)) {
                    val constructor = parameterClazz.getConstructor()
                    constructor.isAccessible = true
                    val event = cachedEvents.getOrPut(parameterClazz) { constructor.newInstance() as Event }
                    event!!.listenerList.register(
                        busId,
                        subscriber.priority,
                        SubscriberFMLEventListener(subscriber)
                    )
                }

                for (cached in cachedSuperclasses) {
                    if (parameterClazz.isAssignableFrom(cached.key)) {
                        cached.setValue(null) // clear cache
                    }
                }
            }
        }
    }

    /**
     * Unsubscribes all `@Subscribe`'d methods inside of the `obj` instance.
     */
    fun unregister(obj: Any) {
        val supers: Set<Class<*>?> = TypeToken.of(obj::class.java).getTypes().rawTypes()
        val methods = obj.javaClass.methods
        for (i in (methods.size - 1) downTo 0) {
            for (cls in supers) {
                if (cls == null) continue
                val method = try {
                    cls.getDeclaredMethod(methods[i].name, *methods[i].parameterTypes)
                } catch (e: NoSuchMethodException) {
                    continue
                }
                if (method.getAnnotation(SubscribeEvent::class.java) == null) {
                    continue
                }
                val subscriber = if (method.returnType == Void.TYPE) if (invokerType is DirectInvoker) SubscriberVoidParent(obj, EventPriority.LOWEST, false, null) else SubscriberVoid(obj, EventPriority.LOWEST, false, null) else SubscriberObject(obj, EventPriority.LOWEST, false, null)
                val parameterClazz = method.parameterTypes[0]
                subscribers[parameterClazz]?.remove(subscriber)
                if (Event::class.java.isAssignableFrom(parameterClazz)) {
                    val constructor = parameterClazz.getConstructor()
                    constructor.isAccessible = true
                    val event = cachedEvents.getOrPut(parameterClazz) { constructor.newInstance() as Event }
                    event?.listenerList?.unregister(busId, SubscriberFMLEventListener(subscriber))
                }

                for (cached in cachedSuperclasses) {
                    if (parameterClazz.isAssignableFrom(cached.key)) {
                        cached.setValue(null) // clear cache
                    }
                }
            }
        }
    }

    /**
     * Posts the event instance given to all the subscribers
     * that are subscribed to the events class.
     */
    fun post(event: Event) {
        var events = cachedSuperclasses[event.javaClass]
        if (events == null) {
            events = subscribers[event.javaClass]
            var superclass = event.javaClass.superclass
            while (superclass != null) {
                subscribers[superclass]?.let { if (events != null) events!!.addAll(it) else events = it }
                superclass = superclass.superclass
            }
            if (events != null) cachedSuperclasses[event.javaClass] = events
            else cachedSuperclasses[event.javaClass] = mutableListOf() // we don't want to check again
        }
        if (events == null || events!!.size == 0) return
        // executed in descending order
        for (i in (events!!.size - 1) downTo 0) {
            try {
                val subscriber = events!![i]
                if (!event.isCancelable || !event.isCanceled || subscriber.receiveCancelled)
                    subscriber.invoke(event)
            } catch (e: Exception) {
                exceptionHandler.handle(e)
            }
        }
    }

    /**
     * Posts the event instance given to all the subscribers
     * that are subscribed to the events class.
     */
    fun post(event: Any) {
        var events = cachedSuperclasses[event.javaClass]
        if (events == null) {
            events = subscribers[event.javaClass]
            var superclass = event.javaClass.superclass
            while (superclass != null) {
                subscribers[superclass]?.let { if (events != null) events!!.addAll(it) else events = it }
                superclass = superclass.superclass
            }
            if (events != null) cachedSuperclasses[event.javaClass] = events
            else cachedSuperclasses[event.javaClass] = mutableListOf() // we don't want to check again
        }
        if (events == null) return
        // executed in descending order
        for (i in (events!!.size - 1) downTo 0) {
            try {
                events!![i].invoke(event)
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
        if (events.size == 0) return
        val event = supplier()
        // executed in descending order
        for (i in (events.size - 1) downTo 0) {
            events[i].invoke(event)
        }
    }

    fun getSubscribedEvents(clazz: Class<*>): MutableList<Subscriber>? {
        var events = cachedSuperclasses[clazz]
        if (events == null) {
            events = subscribers[clazz]
            var superclass = clazz.superclass
            while (superclass != null) {
                subscribers[superclass]?.let { if (events != null) events!!.addAll(it) else events = it }
                superclass = superclass.superclass
            }
            if (events != null) cachedSuperclasses[clazz] = events
            else cachedSuperclasses[clazz] = mutableListOf() // we don't want to check again
        }
        return events
    }
}
