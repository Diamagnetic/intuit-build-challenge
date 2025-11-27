import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Thread-safe blocking queue implementation using wait/notifyAll
 *
 * Features:
 *  - Uses a queue internally
 *  - Producer blocks when queue is full.
 *  - Consumer blocks when queue is empty.
 *
 * Thread-safety:
 * synchronized public methods to allow mutual exclusive access to the queue
 */
public class SimpleBlockedQueue<T> {
    private final Queue<T> blockedQueue;
    private final int capacity;
    private boolean isRunning;

    /**
     * Creates a blocking queue with given capacity
     * @throws IllegalArgumentException when capacity <= 0
     */
    public SimpleBlockedQueue(int capacity) {
        if(capacity <= 0) {
            throw new IllegalArgumentException(
                "SimpleBlockedQueue capacity must be greater than 0"
            );
        }

        this.capacity = capacity;
        blockedQueue = new ArrayDeque<>(this.capacity);
        isRunning = true;
    }

    /**
     * Inserts data into the queue
     * Blocks if the queue is full, and if the queue is active.
     * Returns upon shutdown without insertion.
     */
    public synchronized void put(T data) throws InterruptedException {
        while(isRunning
            && this.capacity == blockedQueue.size()) {

            wait(); // wait till queue has some space
        }

        if(!isRunning) return;

        blockedQueue.offer(data);
        notifyAll();
    }

    /**
     * Removes data from the queue
     * Blocks if the queue is empty, and if the queue is active.
     * Returns null upon shutdown without removal.
     */
    public synchronized T remove() throws InterruptedException {
        while(isRunning
            && blockedQueue.isEmpty()) {

            wait(); // wait till queue has some data
        }

        if(!isRunning) return null;

        T data = blockedQueue.poll();
        notifyAll();

        return data;
    }

    /**
     * Returns the size of the queue
     */
    public synchronized int size() {
        return blockedQueue.size();
    }

    /**
     * Returns true if the queue is active, false otherwise
     */
    public synchronized boolean isActive() {
        return isRunning;
    }

    /**
     * Shuts down the queue
     * Wakes all waiting threads to exit gracefully
     */
    public synchronized void shutdown() {
        isRunning = false;
        notifyAll();
    }
}
