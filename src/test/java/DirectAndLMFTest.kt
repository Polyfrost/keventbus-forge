import me.kbrewster.eventbus.forge.eventbus
import me.kbrewster.eventbus.forge.invokers.DirectInvoker
import me.kbrewster.eventbus.forge.invokers.LMFInvoker
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.junit.jupiter.api.*
import kotlin.system.measureTimeMillis

class TickStartEvent(val message: String)

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class DirectAndLMFTest {

    private val directBus = eventbus {
        invoker { DirectInvoker() }
        exceptionHandler { exception -> println("Error occurred in method: ${exception.message}")  }
        threadSafety { false }
    }
    private val lmfBus = eventbus {
        invoker { LMFInvoker() }
        exceptionHandler { exception -> println("Error occurred in method: ${exception.message}")  }
        threadSafety { false }
    }

    @Test
    @Order(0)
    fun `subscribing class`() {
        println("Direct bus subscribing: ${
            measureTimeMillis {
                repeat(1_000) {
                    directBus.register(this, 1)
                }
            }
        }ms")
        println("LMF bus subscribing: ${
            measureTimeMillis {
                repeat(1_000) {
                    lmfBus.register(this, 1)
                }
            }
        }ms")
    }

    @SubscribeEvent
    fun `subscribed method`(event: TickStartEvent) {
        // do something
    }

    @SubscribeEvent
    fun `subscribed method except its an object`(event: TickStartEvent): Any {
        return Any()
    }

    @SubscribeEvent
    fun `subscribed method except its a class that extends Any`(event: TickStartEvent): String {
        return ""
    }

    @SubscribeEvent
    fun `subscribed method except its an int`(event: TickStartEvent): Int {
        return 0
    }

    @SubscribeEvent
    fun `subscribed method except its a boolean`(event: TickStartEvent): Boolean {
        return false
    }

    @SubscribeEvent
    fun `subscribed method except its a float`(event: TickStartEvent): Float {
        return 0.0f
    }

    @SubscribeEvent
    fun `subscribed method except its a double`(event: TickStartEvent): Double {
        return 0.0
    }

    @SubscribeEvent
    fun `subscribed method except its a long`(event: TickStartEvent): Long {
        return 0L
    }

    @SubscribeEvent
    fun `subscribed method except its a short`(event: TickStartEvent): Short {
        return 0
    }

    @SubscribeEvent
    fun `subscribed method except its a byte`(event: TickStartEvent): Byte {
        return 0
    }

    @SubscribeEvent
    fun `subscribed method except its a char`(event: TickStartEvent): Char {
        return 'a'
    }

    @Test
    @Order(1)
    fun `posting event`() {
        println("Direct bus posting: ${
            measureTimeMillis {
                repeat(1_000) {
                    directBus.post { TickStartEvent("Hello world") }
                }
            }
        }ms")
        println("LMF bus posting: ${
            measureTimeMillis {
                repeat(1_000) {
                    lmfBus.post { TickStartEvent("Hello world") }
                }
            }
        }ms")
    }

    @Test
    @Order(2)
    fun `removing class`() {
        println("Direct bus removing: ${
            measureTimeMillis {
                repeat(1_000) {
                    directBus.unregister(this, 1)
                }
            }
        }ms")
        println("LMF bus removing: ${
            measureTimeMillis {
                repeat(1_000) {
                    lmfBus.unregister(this, 1)
                }
            }
        }ms")
    }

}