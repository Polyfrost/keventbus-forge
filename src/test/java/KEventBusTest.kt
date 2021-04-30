import me.kbrewster.eventbus.forge.eventbus
import me.kbrewster.eventbus.forge.invokers.LMFInvoker
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.junit.jupiter.api.*

class MessageReceivedEvent(val message: String)

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class KEventBusTest {

    private val eventBus = eventbus {
        invoker { LMFInvoker() }
        exceptionHandler { exception -> println("Error occurred in method: ${exception.message}")  }
        threadSafety { false }
    }

    @Test
    @Order(0)
    fun `subscribing class`() {
        eventBus.register(this)
    }

    @SubscribeEvent
    fun `subscribed method`(event: MessageReceivedEvent) {
        // do something
    }

    @Test
    @Order(1)
    fun `posting event`() {
        repeat(10_000_000) {
            eventBus.post { MessageReceivedEvent("Hello world") }
        }
    }

    @Test
    @Order(2)
    fun `removing class`() {
        eventBus.unregister(this)
    }

}