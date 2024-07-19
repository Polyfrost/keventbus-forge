package me.kbrewster.eventbus.forge

import me.kbrewster.eventbus.forge.invokers.InvokerType
import net.minecraftforge.fml.common.eventhandler.EventPriority

open class Subscriber(private val obj: Any, val priority: EventPriority) {

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

class SubscriberVoid(obj: Any, priority: EventPriority, private val invoker: InvokerType.SubscriberMethod?) : Subscriber(obj, priority) {
    override fun invoke(arg: Any?) {
        invoker!!.invoke(arg)
    }
}

class SubscriberBoolean(obj: Any, priority: EventPriority, private val invoker: InvokerType.SubscriberMethodBoolean?) : Subscriber(obj, priority) {
    override fun invoke(arg: Any?) {
        invoker!!.invoke(arg)
    }
}

class SubscriberInt(obj: Any, priority: EventPriority, private val invoker: InvokerType.SubscriberMethodInt?) : Subscriber(obj, priority) {
    override fun invoke(arg: Any?) {
        invoker!!.invoke(arg)
    }
}

class SubscriberFloat(obj: Any, priority: EventPriority, private val invoker: InvokerType.SubscriberMethodFloat?) : Subscriber(obj, priority) {
    override fun invoke(arg: Any?) {
        invoker!!.invoke(arg)
    }
}

class SubscriberDouble(obj: Any, priority: EventPriority, private val invoker: InvokerType.SubscriberMethodDouble?) : Subscriber(obj, priority) {
    override fun invoke(arg: Any?) {
        invoker!!.invoke(arg)
    }
}

class SubscriberLong(obj: Any, priority: EventPriority, private val invoker: InvokerType.SubscriberMethodLong?) : Subscriber(obj, priority) {
    override fun invoke(arg: Any?) {
        invoker!!.invoke(arg)
    }
}

class SubscriberShort(obj: Any, priority: EventPriority, private val invoker: InvokerType.SubscriberMethodShort?) : Subscriber(obj, priority) {
    override fun invoke(arg: Any?) {
        invoker!!.invoke(arg)
    }
}

class SubscriberByte(obj: Any, priority: EventPriority, private val invoker: InvokerType.SubscriberMethodByte?) : Subscriber(obj, priority) {
    override fun invoke(arg: Any?) {
        invoker!!.invoke(arg)
    }
}

class SubscriberChar(obj: Any, priority: EventPriority, private val invoker: InvokerType.SubscriberMethodChar?) : Subscriber(obj, priority) {
    override fun invoke(arg: Any?) {
        invoker!!.invoke(arg)
    }
}

class SubscriberObject(obj: Any, priority: EventPriority, private val invoker: InvokerType.SubscriberMethodObject?) : Subscriber(obj, priority) {
    override fun invoke(arg: Any?) {
        invoker!!.invoke(arg)
    }
}