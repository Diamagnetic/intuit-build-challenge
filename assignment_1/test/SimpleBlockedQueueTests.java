import static java.time.Duration.ofSeconds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;

class SimpleBlockedQueueTests {
    SimpleBlockedQueue<Integer> q;

    @BeforeEach
    void init() {
        q = new SimpleBlockedQueue<>(1);
    }

    @Test
    @DisplayName("Test constructor initialization")
    void testConstructorInitialState() {
        assertEquals(0, q.size(), "Size should be 0 at initialization");
        assertTrue(q.isActive(), "The buffer should be running/active");
    }

    @Test
    @DisplayName("Test for invalid capacity")
    void testConstructorInvalidCapacity() {
        assertThrows(IllegalArgumentException.class, () -> {
            new SimpleBlockedQueue<>(0);
        }, "Capacity 0 should not be allowed");

        assertThrows(IllegalArgumentException.class, () -> {
            new SimpleBlockedQueue<>(-1);
        }, "Capacity -1 should not be allowed");
    }

    @Test
    @DisplayName("Test isActive method")
    void testIsActive() {
        assertTrue(q.isActive(), "Blocked queue should be active/running");
    }

    @Test
    @DisplayName("Test shutdown method")
    void testShutdown() throws InterruptedException {
        assertTrue(q.isActive(), "Blocked queue should be active");

        q.shutdown();

        assertFalse(q.isActive(), "Blocked queue should not be active");
    }

    @Test
    @DisplayName("Test size method")
    void testSize() throws InterruptedException {
        assertEquals(0, q.size(), "Size should be 0 at initialization");
    }

    @Test
    @DisplayName("Test remove method when blocked queue is empty")
    void testRemoveWhenQueueEmpty() {
        // Wait for 2 seconds to see remove() in wait state
        assertTimeoutPreemptively(ofSeconds(2), () -> {
            Thread t = new Thread(() -> {
                try {
                    q.remove();
                } catch(InterruptedException e) {}
            });

            t.start();

            assertTrue(
                t.isAlive(),
                "Thread should be active as it is in the wait state"
            );

            t.interrupt();
        });
    }

    @Nested
    @DisplayName("After put method")
    class AfterPut {
        @BeforeEach
        void putData() throws InterruptedException {
            q.put(10);
        }

        @Test
        @DisplayName("Test put method")
        void testPut() {
            assertEquals(1, q.size(), "Size should be 1 after 1 put call");
        }

        @Test
        @DisplayName("Test put method when blocked queue is full")
        void testPutWhenQueueFull() {
            // Queue capacity reached, but put() is blocked at wait()
            assertTimeoutPreemptively(ofSeconds(2), () -> {
                Thread t = new Thread(() -> {
                    try {
                        q.put(20);
                    } catch(InterruptedException e) {}
                });

                t.start();

                assertTrue(
                    t.isAlive(),
                    "Thread should be active as it is in the wait state"
                );

                t.interrupt();
            });
        }

        @Test
        @DisplayName("Test remove method")
        void testRemove() throws InterruptedException {
            assertEquals(1, q.size(), "Size should be 1 after 1 put call");

            assertEquals(10, q.remove(), "Element 10 should have been removed");
            assertEquals(0, q.size(), "Size should be 0 after removal");
        }
    }

    @Test
    @DisplayName("Test Producer and Consumer interaction")
    public void testProducerConsumerInteraction() throws InterruptedException {
        q = new SimpleBlockedQueue<>(3);

        // producer thread to populate buffer
        Thread producer = new Thread(() -> {
            try {
                q.put(10);
                q.put(20);
                q.put(30);

                assertEquals(3, q.size(), "Queue size should be 3");
            } catch(InterruptedException e) {}
        });

        // consumer thread to consumer from buffer
        Thread consumer = new Thread(() -> {
            try {
                Thread.sleep(300);
                assertEquals(10, q.remove(),
                    "Element 10 should have been removed"
                );

                Thread.sleep(300);
                assertEquals(20, q.remove(),
                    "Element 20 should have been removed"
                );
            } catch(InterruptedException e) {}
        });

        producer.start();
        consumer.start();

        producer.join(1000);
        consumer.join(1000);

        assertEquals(30, q.remove(), "Element 30 should have been removed");
    }
}
