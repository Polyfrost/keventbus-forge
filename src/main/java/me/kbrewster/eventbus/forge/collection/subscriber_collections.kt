package me.kbrewster.eventbus.forge.collection

import me.kbrewster.eventbus.forge.KEventBus
import java.util.Comparator
import java.util.concurrent.CopyOnWriteArrayList

class ConcurrentSubscriberArrayList : CopyOnWriteArrayList<KEventBus.Subscriber>() {
    override fun add(element: KEventBus.Subscriber): Boolean {
        if (size == 0) {
            super.add(element)
        } else {
            var index = this.binarySearch(element, Comparator.comparing { obj: KEventBus.Subscriber -> obj.priority })
            if (index < 0) index = -(index + 1)
            super.add(index, element)
        }
        return true
    }
}
class SubscriberArrayList : ArrayList<KEventBus.Subscriber>() {
    override fun add(element: KEventBus.Subscriber): Boolean {
        if (size == 0) {
            super.add(element)
        } else {
            var index = this.binarySearch(element, Comparator.comparing { obj: KEventBus.Subscriber -> obj.priority })
            if (index < 0) index = -(index + 1)
            super.add(index, element)
        }
        return true
    }
}
