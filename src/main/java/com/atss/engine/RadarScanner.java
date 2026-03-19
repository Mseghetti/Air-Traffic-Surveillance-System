package com.atss.engine;

import com.atss.model.Aircraft;

import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Background radar engine that periodically scans all tracked aircraft,
 * updates their positions, and prints a radar sweep to the console.
 *
 * <p>Uses a {@link ScheduledExecutorService} with fixed-rate scheduling to
 * ensure consistent 2-second tick intervals regardless of scan duration.
 * This is the defense-standard approach over raw {@code Thread.sleep()} loops,
 * which suffer from timing drift, silent exception death, and no clean shutdown.</p>
 *
 * <p><strong>Thread Model:</strong> The scanner runs on a dedicated daemon thread
 * named "ATSS-RadarThread-N". It reads aircraft state from a supplier that must
 * return a thread-safe snapshot or a concurrent collection.</p>
 *
 * @author ATSS Development Team
 * @version 1.0
 */
public class RadarScanner {

    /** Interval between radar sweeps in seconds. */
    private static final int SCAN_INTERVAL_SECONDS = 2;

    /** Thread-safe counter for naming radar threads across instances. */
    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger(1);

    /** Executor that manages the scheduled radar sweep thread. */
    private final ScheduledExecutorService executor;

    /**
     * Supplier providing the current collection of aircraft to scan.
     * Must return a thread-safe view (e.g., from ConcurrentHashMap.values()).
     */
    private final Supplier<Collection<Aircraft>> aircraftSupplier;

    /** Running count of completed radar sweeps. */
    private final AtomicInteger sweepCount = new AtomicInteger(0);

    /**
     * Constructs a new RadarScanner bound to the given aircraft data source.
     *
     * @param aircraftSupplier supplier returning the current aircraft collection;
     *                         must be thread-safe for concurrent reads
     * @throws IllegalArgumentException if aircraftSupplier is null
     */
    public RadarScanner(Supplier<Collection<Aircraft>> aircraftSupplier) {
        if (aircraftSupplier == null) {
            throw new IllegalArgumentException("Aircraft supplier must not be null.");
        }

        this.aircraftSupplier = aircraftSupplier;
        this.executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("ATSS-RadarThread-" + THREAD_COUNTER.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * Starts the radar scanner, scheduling sweeps at a fixed rate.
     *
     * <p>The first sweep begins immediately (0-second initial delay), and
     * subsequent sweeps occur every {@value #SCAN_INTERVAL_SECONDS} seconds.</p>
     */
    public void start() {
        System.out.println("=== ATSS RADAR ONLINE ===");
        System.out.println("Sweep interval: " + SCAN_INTERVAL_SECONDS + "s");
        System.out.println();

        executor.scheduleAtFixedRate(
                this::performSweep,
                0,
                SCAN_INTERVAL_SECONDS,
                TimeUnit.SECONDS
        );
    }

    /**
     * Gracefully shuts down the radar scanner.
     *
     * <p>Allows the current sweep to finish, then terminates the executor.
     * If the executor does not terminate within 5 seconds, it is forcibly shut down.</p>
     */
    public void shutdown() {
        System.out.println("\n=== ATSS RADAR SHUTTING DOWN ===");
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Executes a single radar sweep: updates all aircraft positions and
     * prints their current state to the console.
     *
     * <p>This method is called by the scheduled executor on the radar thread.
     * Any exception thrown here is caught by the executor and will cancel
     * future executions, so we wrap in try-catch for resilience.</p>
     */
    private void performSweep() {
        try {
            int sweep = sweepCount.incrementAndGet();
            Collection<Aircraft> aircraft = aircraftSupplier.get();

            System.out.println("─────────────────────────────────────────────");
            System.out.printf("  RADAR SWEEP #%d | Tracking %d aircraft%n", sweep, aircraft.size());
            System.out.println("─────────────────────────────────────────────");

            if (aircraft.isEmpty()) {
                System.out.println("  No aircraft in range.");
            } else {
                for (Aircraft ac : aircraft) {
                    ac.updatePosition();
                    System.out.println("  " + ac);
                }
            }

            System.out.println();
        } catch (Exception e) {
            System.err.println("[RADAR ERROR] Sweep failed: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    /**
     * Returns the number of completed radar sweeps.
     *
     * @return sweep count
     */
    public int getSweepCount() {
        return sweepCount.get();
    }
}
