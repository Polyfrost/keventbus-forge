package me.kbrewster.eventbus.forge.collection

import me.kbrewster.eventbus.forge.Subscriber
import java.util.Comparator
import java.util.concurrent.CopyOnWriteArrayList

class ConcurrentSubscriberArrayList : CopyOnWriteArrayList<Subscriber>() {
    override fun add(element: Subscriber): Boolean {
        if (size == 0) {
            super.add(element)
        } else {
            var index = this.binarySearch(element, Comparator.comparing { obj: Subscriber -> obj.priority })
            if (index < 0) index = -(index + 1)
            super.add(index, element)
        }
        return true
    }
}
class SubscriberArrayList : ArrayList<Subscriber>() {
    override fun add(element: Subscriber): Boolean {
        if (size == 0) {
            super.add(element)
        } else {
            var index = this.binarySearch(element, Comparator.comparing { obj: Subscriber -> obj.priority })
            if (index < 0) index = -(index + 1)
            super.add(index, element)
        }
        return true
    }
}
