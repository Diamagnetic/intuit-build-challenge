import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class Demo {
    public static void main(String[] args) throws InterruptedException {
        int producers = 1;
        int consumers = 1;
        int capacity;

        if(args.length > 0) {
            capacity = Integer.parseInt(args[0]);
        } else {
            Scanner sc = new Scanner(System.in);
            System.out.println("Enter queue capacity: ");
            capacity = sc.nextInt();
        }

        System.out.println("Queue capacity = " + capacity);
        System.out.println("Number of producers = " + producers);
        System.out.println("Number of consumers = " + consumers + "\n");

        SimpleBlockedQueue<Integer> buffer = new SimpleBlockedQueue<>(capacity);

        List<Integer> source1 = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        List<Integer> destination = 
            Collections.synchronizedList(new ArrayList<>());

        // Create a fixed-size thread pool to run all producers and consumers
        ExecutorService pool = 
            Executors.newFixedThreadPool(producers + consumers);

        // producer starts producing
        pool.submit(new Producer<Integer>(buffer, source1));

        // consumer starts consuming
        for(int i = 0; i < consumers; i++) {
            pool.submit(new Consumer<Integer>(buffer, destination));
        }

        // The thread pool waits for upto 6 seconds for tasks to complete
        // before shutting down thread pool
        pool.shutdown();
        pool.awaitTermination(6, TimeUnit.SECONDS);
        // Stop the buffer
        buffer.shutdown();

        System.out.println("\nFinal destination contents: " + destination);
        System.out.println("\nDemo complete\n");
    }
}
