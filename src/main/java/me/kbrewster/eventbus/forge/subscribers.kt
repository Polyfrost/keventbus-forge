package me.kbrewster.eventbus.forge

import me.kbrewster.eventbus.forge.invokers.InvokerType
import net.minecraftforge.fml.common.eventhandler.Event
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.IEventListener

open class Subscriber(protected val obj: Any, val priority: EventPriority, val receiveCancelled: Boolean = false) {

    @Throws(Exception::class)
    open operator fun invoke(arg: Any?) {
        throw UnsupportedOperationException("This method should be overridden")
    }

    override fun equals(other: Any?): Boolean {
        return other.hashCode() == this.hashCode()
    }

    override fun hashCode(): Int {
        return obj.hashCode()
    }

}

class SubscriberVoid(obj: Any, priority: EventPriority, receiveCancelled: Boolean, private val invoker: InvokerType.SubscriberMethod?) : Subscriber(obj, priority, receiveCancelled) {
    override fun invoke(arg: Any?) {
        invoker!!.invoke(arg)
    }
}

class SubscriberVoidParent(obj: Any, priority: EventPriority, receiveCancelled: Boolean, private val invoker: InvokerType.SubscriberMethodParent?) : Subscriber(obj, priority, receiveCancelled) {
    override fun invoke(arg: Any?) {
        invoker!!.invoke(arg, obj)
    }
}

class SubscriberObject(obj: Any, priority: EventPriority, receiveCancelled: Boolean, private val invoker: InvokerType.SubscriberMethodObject?) : Subscriber(obj, priority, receiveCancelled) {
    override fun invoke(arg: Any?) {
        invoker!!.invoke(arg)
    }
}

class SubscriberFMLEventListener(private val subscriber: Subscriber) : IEventListener {

    override fun invoke(event: Event) {
        subscriber.invoke(event)
    }

    override fun equals(other: Any?): Boolean {
        return other is SubscriberFMLEventListener && other.hashCode() == this.hashCode()
    }

    override fun hashCode(): Int {
        return subscriber.hashCode()
    }
}