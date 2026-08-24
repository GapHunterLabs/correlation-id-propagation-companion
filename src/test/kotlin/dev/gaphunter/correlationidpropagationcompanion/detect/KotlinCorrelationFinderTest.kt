package dev.gaphunter.correlationidpropagationcompanion.detect

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class KotlinCorrelationFinderTest : BasePlatformTestCase() {

    fun `test a correlation id received then never forwarded is flagged`() {
        val file = myFixture.configureByText(
            "OrderController.kt",
            """
            class OrderController {
                fun createOrder(@RequestHeader("X-Correlation-Id") correlationId: String, @RequestBody payload: String) {
                    restTemplate.postForObject("http://inventory/reserve", payload, Void::class.java)
                }
            }
            """.trimIndent(),
        )
        assertEquals(1, KotlinCorrelationFinder.findAll(file).size)
    }

    fun `test a correlation id forwarded into the outbound call is not flagged`() {
        val file = myFixture.configureByText(
            "OrderController.kt",
            """
            class OrderController {
                fun createOrder(@RequestHeader("X-Correlation-Id") correlationId: String, @RequestBody payload: String) {
                    val headers = HttpHeaders()
                    headers.set("X-Correlation-Id", correlationId)
                    restTemplate.postForObject("http://inventory/reserve", HttpEntity(payload, headers), Void::class.java)
                }
            }
            """.trimIndent(),
        )
        assertTrue(KotlinCorrelationFinder.findAll(file).isEmpty())
    }

    fun `test a method with no outbound HTTP call is not flagged`() {
        val file = myFixture.configureByText(
            "OrderController.kt",
            """
            class OrderController {
                fun createOrder(@RequestHeader("X-Correlation-Id") correlationId: String, @RequestBody payload: String) {
                    orderRepository.save(payload)
                }
            }
            """.trimIndent(),
        )
        assertTrue(KotlinCorrelationFinder.findAll(file).isEmpty())
    }

    fun `test a non-correlation header parameter is not flagged`() {
        val file = myFixture.configureByText(
            "OrderController.kt",
            """
            class OrderController {
                fun createOrder(@RequestHeader("Authorization") auth: String, @RequestBody payload: String) {
                    restTemplate.postForObject("http://inventory/reserve", payload, Void::class.java)
                }
            }
            """.trimIndent(),
        )
        assertTrue(KotlinCorrelationFinder.findAll(file).isEmpty())
    }
}
