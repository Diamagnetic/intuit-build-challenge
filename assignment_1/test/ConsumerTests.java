import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import static java.time.Duration.ofSeconds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;

public class ConsumerTests {
    SimpleBlockedQueue<Integer> sharedQueue;
    Consumer<Integer> consumer;
    List<Integer> destination;

    @BeforeEach
    void init() {
        sharedQueue = new SimpleBlockedQueue<>(3);
        // thread-safe list for consumer to write at
        destination = Collections.synchronizedList(new ArrayList<>());
        consumer = new Consumer<>(sharedQueue, destination);
    }

    @Test
    @DisplayName("Test constructor initialization")
    void testConstructorInitialization() {
        assertTrue(sharedQueue.isActive(), "Shared queue should be active");
        assertTrue(destination.isEmpty(),
            "Consumer should not have consumed any data"
        );
    }

    @Nested
    @DisplayName("Consumer thread running")
    class ConsumerRunning {
        Thread consumerThread;

        @BeforeEach
        void init() {
            // Start the consumer in a thread
            consumerThread = new Thread(consumer);
            consumerThread.start();
        }

        @Test
        @DisplayName("Consumer should block on empty queue")
        void testConsumerBlockOnEmptyQueue () {
            // Wait for 2 seconds to show that consumerThread blocks on wait()
            assertTimeoutPreemptively(ofSeconds(2), () -> {
                assertTrue(consumerThread.isAlive(),
                    "Consumer thread should be alive, and block on empty queue"
                );

                consumerThread.interrupt();
            });
        }

        @Test
        @DisplayName("Consumer should stop on queue shutdown")
        void testConsumerStopOnShutdown() throws InterruptedException {
            sharedQueue.shutdown();

            // Wait for the consumer thread to finish
            consumerThread.join();

            assertFalse(consumerThread.isAlive(),
                "Consumer thread should stop after shutdown"
            );
        }

        @Test
        @DisplayName("Consumer should consume multiple data in order")
        void testConsumerConsumesMultipleDataInOrder()
            throws InterruptedException {

            // populate buffer for consumer to consume
            sharedQueue.put(10);
            sharedQueue.put(20);
            sharedQueue.put(30);

            // Wait before shutting down so that consumer can consume
            Thread.sleep(1000);

            sharedQueue.shutdown();
            consumerThread.join();

            assertEquals(List.of(10, 20, 30), destination,
                "Correct order should be [10, 20, 30]"
            );
        }

        @Test
        @DisplayName("Consumer stops midway after queue shutdown")
        void testConsumerStopsMidwayOnShutdown()
            throws InterruptedException {

            // populate buffer
            sharedQueue.put(10);
            sharedQueue.put(20);
            sharedQueue.put(30);

            // Wait before shutting down so that consumer can consume some data
            Thread.sleep(300);
            sharedQueue.shutdown();
            consumerThread.join(500);

            assertTrue(sharedQueue.size() < 3,
                "Consumer should have consumed some data, and "
                + "queue should not be full"
            );
        }

        @Test
        @DisplayName("Consumer should stop on interrupt")
        void testConsumerStopsOnInterrupt() throws InterruptedException {
            consumerThread.interrupt();
            consumerThread.join(500);

            assertFalse(consumerThread.isAlive(),
                "Consumer thread should exit on interrupt"
            );
        }
    }
}
