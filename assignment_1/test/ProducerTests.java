import java.util.List;

import static java.time.Duration.ofSeconds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;

public class ProducerTests {
    SimpleBlockedQueue<Integer> sharedQueue;
    Producer<Integer> producer;
    List<Integer> dataToProduce;

    @BeforeEach
    void init() {
        sharedQueue = new SimpleBlockedQueue<>(3);
        dataToProduce = List.of(10, 20, 30);
        producer = new Producer<>(sharedQueue, dataToProduce);
    }

    @Test
    @DisplayName("Test constructor initialization")
    void testConstructorInitialization() {
        assertTrue(sharedQueue.isActive(), "Shared queue should be active");
        assertEquals(0, sharedQueue.size(),
            "Producer should not have produced any data; queue should be empty"
        );
    }

    @Test
    @DisplayName("Producer should handle empty list")
    void testProducerHandlesEmptyList() throws InterruptedException {
        producer = new Producer<>(sharedQueue, List.of());

        Thread producerThread = new Thread(producer);
        producerThread.start();
        producerThread.join();

        assertEquals(0, sharedQueue.size(), "Shared queue should be empty");
    }

    @Test
    @DisplayName("Producer should block on full capacity queue")
    void testProducerBlockOnFullQueue() throws InterruptedException {
        sharedQueue = new SimpleBlockedQueue<>(1);
        producer = new Producer<>(sharedQueue, dataToProduce);

        Thread producerThread = new Thread(producer);
        producerThread.start();

        // Wait for 2 seconds to show that producerThread blocks on wait()
        assertTimeoutPreemptively(ofSeconds(2), () -> {
            assertTrue(producerThread.isAlive(),
                "Producer thread should be alive, and block on full queue"
            );

            producerThread.interrupt();
        });
    }

    @Nested
    @DisplayName("Producer thread running")
    class ProducerRunning {
        Thread producerThread;

        @BeforeEach
        void init() {
            // Start the producer in a thread
            producerThread = new Thread(producer);
            producerThread.start();
        }

        @Test
        @DisplayName("Producer should stop on queue shutdown")
        void testProducerStopOnShutdown() throws InterruptedException {
            sharedQueue.shutdown();

            // Wait for the producer thread to finish
            producerThread.join();

            assertFalse(producerThread.isAlive(),
                "Producer thread should stop after shutdown"
            );
        }

        @Test
        @DisplayName("Producer should produce multiple data in order")
        void testProduceresMultipleDataInOrder()
            throws InterruptedException {

            producerThread.join(1000);

            assertEquals(3, sharedQueue.size(), "Size of queue should be 3");
            assertEquals(10, sharedQueue.remove(), "Data removed should be 10");
            assertEquals(20, sharedQueue.remove(), "Data removed should be 20");
            assertEquals(30, sharedQueue.remove(), "Data removed should be 30");
        }

        @Test
        @DisplayName("Producer stops midway after queue shutdown")
        void testProducerStopsMidwayOnShutdown()
            throws InterruptedException {

            // put thread to sleep, so producer can produce meanwhile
            Thread.sleep(300);
            sharedQueue.shutdown();

            // Stop producer midway
            producerThread.join(500);

            assertTrue(sharedQueue.size() > 0,
                "Producer should have produced some data, and "
                + "queue should not be empty"
            );
        }

       @Test
        @DisplayName("Producer should stop on interrupt")
        void testProducerStopsOnInterrupt() throws InterruptedException {
            producerThread.interrupt();
            producerThread.join(500);

            assertFalse(producerThread.isAlive(),
                "Producer thread should exit on interrupt"
            );
        }
    }
}
