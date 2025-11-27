import java.util.List;

/**
 * Producer produces from a list, and puts it in a sharedQueue
 * Stops when all items are produced, or due to early shutdown
 *
 * Thread-safety comes from SimpleBlockedQueue through the put() method
 */
public class Producer<T> implements Runnable {
    private final SimpleBlockedQueue<T> sharedQueue;
    private final List<T> dataToProduce;

    public Producer(SimpleBlockedQueue<T> queue, List<T> list) {
        sharedQueue = queue;
        dataToProduce = list;
    }

     /**
     * - Produce items from dataProduce and put in sharedQueue using put()
     *
     * Termination:
     * - Normal termination when all items are produced
     * - Early termination if queue is shutdown
     * - Interrupted termination upon interrupt
     */

    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();

        try {
            for(T data : dataToProduce) {
                // stop producing when queue is inactive
                if(!sharedQueue.isActive()) {
                    System.out.println("Producer thread "
                        + threadName + " stopped as queue is shutdown"
                    );

                    break;
                }

                sharedQueue.put(data);

                System.out.println("Producer thread "
                    + threadName + " produced data: " + data
                    + ". Buffer size = " + sharedQueue.size()
                );

                // Simulated work
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            System.out.println("Producer thread "
                + threadName + " interrupted"
            );
        }
    }
}
