import java.util.List;
import java.util.ArrayList;

/**
 * Consumer continuously consumes from a shared SimpleBlockedQueue,
 * and stores it in consumedData
 * Stops when SimpleBlockedQueue.remove() returns null, indicating shutdown
 *
 * Thread-safety comes from SimpleBlockedQueue through the remove() method
 */
public class Consumer<T> implements Runnable {
    private final SimpleBlockedQueue<T> sharedQueue;
    private final List<T> consumedData;

    public Consumer(SimpleBlockedQueue<T> queue, List<T> destination) {
        sharedQueue = queue;
        consumedData = destination;
    }

    /**
     * - Continuously consume items by calling sharedQueue.remove()
     * - Stops is remove() returns null, indicating queue shutdown
     *
     * Termination:
     * - Normal termination when queue is shutdown
     * - Interrupted termination upon interrupt
     */
    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();

        try {
            while(true) {
                T data = sharedQueue.remove();

                // if sharedQueue is shutdown, it returns null
                // Stop consumer in this case
                if(data == null) {
                    System.out.println("Consumer thread " + threadName
                        + " stopped as queue is shutdown"
                    );

                    break;
                }

                consumedData.add(data);

                System.out.println("Consumer thread " + threadName
                    + " consumed data: " + data + ". Buffer size = "
                    + sharedQueue.size()
                );

                // Simulate work
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            System.out.println("Consumer thread " + threadName
                + " interrupted"
            );
        }
    }
}
