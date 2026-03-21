package com.atss.data;

import com.atss.model.Aircraft;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Thread-safe registry of all aircraft currently tracked by the ATSS.
 *
 * <p>Backed by a {@link ConcurrentHashMap} keyed on callsign, this registry
 * allows the main thread to inject new aircraft while the radar thread
 * concurrently iterates and updates positions -- without any explicit locking
 * or risk of {@link java.util.ConcurrentModificationException}.</p>
 *
 * <p><strong>Why ConcurrentHashMap?</strong></p>
 * <ul>
 *   <li>O(1) lookup by callsign for duplicate detection</li>
 *   <li>Weakly consistent iteration: the radar thread sees a safe, recent view</li>
 *   <li>O(1) amortized writes vs. CopyOnWriteArrayList's O(n) copy-on-every-write</li>
 *   <li>No global lock: reads and writes to different segments proceed in parallel</li>
 * </ul>
 *
 * @author ATSS Development Team
 * @version 1.0
 */
public class AircraftRegistry {

    /**
     * The core concurrent data structure.
     * Key: callsign (String), Value: Aircraft instance.
     */
    private final ConcurrentMap<String, Aircraft> registry = new ConcurrentHashMap<>();

    /**
     * Registers a new aircraft in the system.
     *
     * <p>Uses {@link ConcurrentMap#putIfAbsent} to atomically check-and-insert,
     * preventing duplicate callsigns without requiring external synchronization.</p>
     *
     * @param aircraft the aircraft to register (must not be null)
     * @return {@code true} if the aircraft was successfully added,
     *         {@code false} if a aircraft with the same callsign already exists
     * @throws IllegalArgumentException if aircraft is null
     */
    public boolean register(Aircraft aircraft) {
        if (aircraft == null) {
            throw new IllegalArgumentException("Aircraft must not be null.");
        }

        Aircraft existing = registry.putIfAbsent(aircraft.getCallsign(), aircraft);
        return existing == null;
    }

    /**
     * Removes an aircraft from the registry by callsign.
     *
     * @param callsign the callsign of the aircraft to remove
     * @return the removed {@link Aircraft}, or {@code null} if not found
     */
    public Aircraft deregister(String callsign) {
        if (callsign == null) {
            return null;
        }
        return registry.remove(callsign);
    }

    /**
     * Returns a live, weakly consistent view of all tracked aircraft.
     *
     * <p>This view is safe to iterate from the radar thread while the main
     * thread adds or removes aircraft. It will never throw
     * {@link java.util.ConcurrentModificationException}.</p>
     *
     * @return an unmodifiable-style view of aircraft values
     */
    public Collection<Aircraft> getAllAircraft() {
        return registry.values();
    }

    /**
     * Returns the number of aircraft currently tracked.
     *
     * @return the count of registered aircraft
     */
    public int size() {
        return registry.size();
    }

    /**
     * Checks if a callsign is already registered.
     *
     * @param callsign the callsign to check
     * @return {@code true} if the callsign exists in the registry
     */
    public boolean containsCallsign(String callsign) {
        return callsign != null && registry.containsKey(callsign);
    }
}
