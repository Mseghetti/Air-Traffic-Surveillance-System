package com.atss;

import com.atss.data.AircraftRegistry;
import com.atss.engine.RadarScanner;
import com.atss.model.Aircraft;

import java.util.Scanner;

/**
 * Entry point for the Air Traffic Surveillance System.
 *
 * <p>Demonstrates two threads sharing data safely:</p>
 * <ul>
 *   <li><strong>Main thread:</strong> Reads console input to inject new aircraft</li>
 *   <li><strong>Radar thread:</strong> Periodically scans and updates all aircraft positions</li>
 * </ul>
 *
 * <p>The {@link AircraftRegistry} (backed by ConcurrentHashMap) sits between them,
 * allowing concurrent reads and writes without locks or CME risk.</p>
 *
 * @author ATSS Development Team
 * @version 1.0
 */
public class Main {

    private static final String BANNER = """
            ╔══════════════════════════════════════════════════════╗
            ║     AIR TRAFFIC SURVEILLANCE SYSTEM (ATSS) v1.0     ║
            ║           Defense Aerospace Division                 ║
            ╚══════════════════════════════════════════════════════╝
            """;

    private static final String HELP_TEXT = """
              Commands:
                add    - Add a new aircraft to tracking
                list   - Show all tracked callsigns
                remove - Remove an aircraft by callsign
                quit   - Shutdown radar and exit
            """;

    /**
     * Launches the ATSS simulation.
     *
     * <p>Flow:</p>
     * <ol>
     *   <li>Creates the shared {@link AircraftRegistry}</li>
     *   <li>Starts the {@link RadarScanner} on a background thread</li>
     *   <li>Enters an interactive console loop on the main thread</li>
     *   <li>On "quit", shuts down the radar gracefully</li>
     * </ol>
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        System.out.println(BANNER);

        AircraftRegistry registry = new AircraftRegistry();
        RadarScanner radar = new RadarScanner(registry::getAllAircraft);

        seedTestAircraft(registry);

        radar.start();

        runConsoleLoop(registry, radar);
    }

    /**
     * Pre-loads a few aircraft so the radar has something to track immediately.
     *
     * @param registry the shared aircraft registry
     */
    private static void seedTestAircraft(AircraftRegistry registry) {
        registry.register(new Aircraft("EAGLE01", 0.0, 0.0, 2.5, 45.0));
        registry.register(new Aircraft("VIPER02", 10.0, 10.0, 1.8, 180.0));
        registry.register(new Aircraft("HAWK03", -5.0, 15.0, 3.0, 270.0));

        System.out.println("  Seeded " + registry.size() + " test aircraft: EAGLE01, VIPER02, HAWK03");
        System.out.println();
    }

    /**
     * Interactive console loop running on the main thread.
     *
     * <p>This is the key concurrency demonstration: while this method blocks
     * waiting for user input, the radar thread continues scanning and updating
     * aircraft positions through the shared registry.</p>
     *
     * @param registry the shared aircraft registry
     * @param radar    the radar scanner (for shutdown)
     */
    private static void runConsoleLoop(AircraftRegistry registry, RadarScanner radar) {
        Scanner scanner = new Scanner(System.in);
        System.out.println(HELP_TEXT);

        while (true) {
            System.out.print("ATSS> ");
            String input = scanner.nextLine().trim().toLowerCase();

            switch (input) {
                case "add" -> handleAdd(scanner, registry);
                case "list" -> handleList(registry);
                case "remove" -> handleRemove(scanner, registry);
                case "help" -> System.out.println(HELP_TEXT);
                case "quit", "exit" -> {
                    radar.shutdown();
                    System.out.println("ATSS terminated. Goodbye.");
                    scanner.close();
                    return;
                }
                default -> System.out.println("  Unknown command. Type 'help' for options.\n");
            }
        }
    }

    /**
     * Prompts the user for aircraft parameters and registers it.
     *
     * @param scanner  the console input scanner
     * @param registry the shared aircraft registry
     */
    private static void handleAdd(Scanner scanner, AircraftRegistry registry) {
        try {
            System.out.print("  Callsign: ");
            String callsign = scanner.nextLine().trim().toUpperCase();

            if (registry.containsCallsign(callsign)) {
                System.out.println("  [REJECTED] Callsign " + callsign + " is already tracked.\n");
                return;
            }

            System.out.print("  X position: ");
            double x = Double.parseDouble(scanner.nextLine().trim());

            System.out.print("  Y position: ");
            double y = Double.parseDouble(scanner.nextLine().trim());

            System.out.print("  Speed: ");
            double speed = Double.parseDouble(scanner.nextLine().trim());

            System.out.print("  Heading (0-359): ");
            double heading = Double.parseDouble(scanner.nextLine().trim());

            Aircraft aircraft = new Aircraft(callsign, x, y, speed, heading);
            boolean added = registry.register(aircraft);

            if (added) {
                System.out.println("  [CONFIRMED] " + callsign + " is now being tracked.\n");
            } else {
                System.out.println("  [REJECTED] Callsign " + callsign + " was added by another process.\n");
            }
        } catch (NumberFormatException e) {
            System.out.println("  [INPUT ERROR] Invalid number format. Aircraft not added.\n");
        } catch (IllegalArgumentException e) {
            System.out.println("  [INPUT ERROR] " + e.getMessage() + "\n");
        }
    }

    /**
     * Prints all currently tracked callsigns.
     *
     * @param registry the shared aircraft registry
     */
    private static void handleList(AircraftRegistry registry) {
        if (registry.size() == 0) {
            System.out.println("  No aircraft currently tracked.\n");
            return;
        }
        System.out.println("  Tracked aircraft (" + registry.size() + "):");
        for (Aircraft ac : registry.getAllAircraft()) {
            System.out.println("    - " + ac.getCallsign());
        }
        System.out.println();
    }

    /**
     * Removes an aircraft from tracking by callsign.
     *
     * @param scanner  the console input scanner
     * @param registry the shared aircraft registry
     */
    private static void handleRemove(Scanner scanner, AircraftRegistry registry) {
        System.out.print("  Callsign to remove: ");
        String callsign = scanner.nextLine().trim().toUpperCase();

        Aircraft removed = registry.deregister(callsign);
        if (removed != null) {
            System.out.println("  [REMOVED] " + callsign + " is no longer tracked.\n");
        } else {
            System.out.println("  [NOT FOUND] No aircraft with callsign " + callsign + ".\n");
        }
    }
}
