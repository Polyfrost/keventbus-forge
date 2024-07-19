import me.kbrewster.eventbus.forge.eventbus
import me.kbrewster.eventbus.forge.invokers.DirectInvoker
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
        return 'a'
    }

    @Test
    @Order(1)
    fun `posting event`() {
        eventBus.post { MessageReceivedEvent("Hello world") }
    }

    @Test
    @Order(2)
    fun `removing class`() {
        eventBus.unregister(this)
        eventBus.unregister(gui)
        eventBus.post { MessageReceivedEvent("Hello world") }
    }

    class GuiThatImplementsModernGui : ModernGui()

    abstract class ModernGui {
        @SubscribeEvent
        fun `subscribed method thats inherited`(event: MessageReceivedEvent) {
            println("Working (this shouldnt show twice)")
        }
    }

}