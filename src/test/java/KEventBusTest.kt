import me.kbrewster.eventbus.forge.eventbus
import me.kbrewster.eventbus.forge.invokers.DirectInvoker
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.junit.jupiter.api.*

class MessageReceivedEvent(val message: String)

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class KEventBusTest {

    private val eventBus = eventbus {
        invoker { DirectInvoker() }
        exceptionHandler { exception -> println("Error occurred in method: ${exception.message}")  }
        threadSafety { false }
    }

    private var gui = GuiThatImplementsModernGui()

    @Test
    @Order(0)
    fun `subscribing class`() {
        eventBus.register(this)
        eventBus.register(gui)
    }

    @SubscribeEvent
    fun `subscribed method`(event: MessageReceivedEvent) {
        // do something
    }

    @SubscribeEvent
    fun `subscribed method with inputevent`(event: InputEvent) {
        println("This should call, regardless of whether I've used KeyInputEvent or MouseInputEvent")
        when (event) {
            is InputEvent.KeyInputEvent -> println("KeyInputEvent")
            is InputEvent.MouseInputEvent -> println("MouseInputEvent")
        }
    }

    @SubscribeEvent
    fun `subscribed method with keyinputevent`(event: InputEvent.KeyInputEvent) {
        println("This should call, only when I've used KeyInputEvent")
    }

    @SubscribeEvent
    fun `subscribed method with mouseinputevent`(event: InputEvent.MouseInputEvent) {
        println("This should call, only when I've used MouseInputEvent")
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun `subscribed method except i run last`(event: MessageReceivedEvent): Char {
        println("3")
        return 'a'
    }

    @SubscribeEvent
    fun `subscribed method except its an object`(event: MessageReceivedEvent): Any {
        return Any()
    }

    @SubscribeEvent
    fun `subscribed method except its a class that extends Any`(event: MessageReceivedEvent): String {
        return ""
    }

    @SubscribeEvent
    fun `subscribed method except its an int`(event: MessageReceivedEvent): Int {
        return 0
    }

    @SubscribeEvent
    fun `subscribed method except its a boolean`(event: MessageReceivedEvent): Boolean {
        return false
    }

    @SubscribeEvent
    fun `subscribed method except its a float`(event: MessageReceivedEvent): Float {
        return 0.0f
    }

    @SubscribeEvent
    fun `subscribed method except its a double`(event: MessageReceivedEvent): Double {
        return 0.0
    }

    @SubscribeEvent
    fun `subscribed method except its a long`(event: MessageReceivedEvent): Long {
        return 0L
    }

    @SubscribeEvent
    fun `subscribed method except its a short`(event: MessageReceivedEvent): Short {
        return 0
    }

    @SubscribeEvent
    fun `subscribed method except its a byte`(event: MessageReceivedEvent): Byte {
        return 0
    }

    @SubscribeEvent
    fun `subscribed method except its a char`(event: MessageReceivedEvent): Char {
        println("2")
        return 'a'
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun `subscribed method except i run first`(event: MessageReceivedEvent): Char {
        println("1")
        return 'a'
    }

    @Test
    @Order(1)
    fun `posting event`() {
        eventBus.post { MessageReceivedEvent("Hello world") }
        eventBus.post(InputEvent.KeyInputEvent())
        eventBus.post(InputEvent.MouseInputEvent())
        eventBus.post(InputEvent.KeyInputEvent())
        eventBus.post(InputEvent.MouseInputEvent())
    }

    @Test
    @Order(2)
    fun `removing class`() {
        println("unregistering")
        eventBus.unregister(this)
        eventBus.unregister(gui)
        eventBus.post { MessageReceivedEvent("Hello world") }
        eventBus.post(InputEvent.KeyInputEvent())
        eventBus.post(InputEvent.MouseInputEvent())
    }

    @Test
    @Order(3)
    fun `reregistering class`() {
        println("re-registering")
        eventBus.register(this)
        eventBus.register(gui)
        eventBus.post { MessageReceivedEvent("Hello world") }
        eventBus.post(InputEvent.KeyInputEvent())
        eventBus.post(InputEvent.MouseInputEvent())
    }

    class GuiThatImplementsModernGui : ModernGui()

    abstract class ModernGui {
        @SubscribeEvent
        fun `subscribed method thats inherited`(event: MessageReceivedEvent) {
            println("Working (this shouldnt show twice)")
        }
    }

}