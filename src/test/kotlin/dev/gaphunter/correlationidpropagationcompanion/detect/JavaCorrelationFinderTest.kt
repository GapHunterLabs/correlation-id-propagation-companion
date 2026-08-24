package dev.gaphunter.correlationidpropagationcompanion.detect

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class JavaCorrelationFinderTest : BasePlatformTestCase() {

    fun `test a correlation id received then never forwarded is flagged`() {
        val file = myFixture.configureByText(
            "OrderController.java",
            """
            class OrderController {
                void createOrder(@RequestHeader("X-Correlation-Id") String correlationId, @RequestBody String payload) {
                    restTemplate.postForObject("http://inventory/reserve", payload, Void.class);
                }
            }
            """.trimIndent(),
        )
        assertEquals(1, JavaCorrelationFinder.findAll(file).size)
    }

    fun `test a correlation id forwarded into the outbound call is not flagged`() {
        val file = myFixture.configureByText(
            "OrderController.java",
            """
            class OrderController {
                void createOrder(@RequestHeader("X-Correlation-Id") String correlationId, @RequestBody String payload) {
                    HttpHeaders headers = new HttpHeaders();
                    headers.set("X-Correlation-Id", correlationId);
                    restTemplate.postForObject("http://inventory/reserve", new HttpEntity<>(payload, headers), Void.class);
                }
            }
            """.trimIndent(),
        )
        assertTrue(JavaCorrelationFinder.findAll(file).isEmpty())
    }

    fun `test a method with no outbound HTTP call is not flagged`() {
        val file = myFixture.configureByText(
            "OrderController.java",
            """
            class OrderController {
                void createOrder(@RequestHeader("X-Correlation-Id") String correlationId, @RequestBody String payload) {
                    orderRepository.save(payload);
                }
            }
            """.trimIndent(),
        )
        assertTrue(JavaCorrelationFinder.findAll(file).isEmpty())
    }

    fun `test a non-correlation header parameter is not flagged`() {
        val file = myFixture.configureByText(
            "OrderController.java",
            """
            class OrderController {
                void createOrder(@RequestHeader("Authorization") String auth, @RequestBody String payload) {
                    restTemplate.postForObject("http://inventory/reserve", payload, Void.class);
                }
            }
            """.trimIndent(),
        )
        assertTrue(JavaCorrelationFinder.findAll(file).isEmpty())
    }

    fun `test correlation header matching is case-insensitive on the declared value`() {
        val file = myFixture.configureByText(
            "OrderController.java",
            """
            class OrderController {
                void createOrder(@RequestHeader("X-TRACE-ID") String traceId, @RequestBody String payload) {
                    restTemplate.postForObject("http://inventory/reserve", payload, Void.class);
                }
            }
            """.trimIndent(),
        )
        assertEquals(1, JavaCorrelationFinder.findAll(file).size)
    }
}
