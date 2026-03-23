package com.atss.detection;

import com.atss.model.Aircraft;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Analyzes aircraft positions to detect potential mid-air collisions.
 *
 * <p>On each radar sweep, this detector computes the Euclidean distance between
 * every pair of tracked aircraft. If any pair is within the configured alert
 * threshold, a {@code [RED ALERT]} is issued to the console.</p>
 *
 * <p><strong>Complexity:</strong> The pairwise comparison is O(n^2/2) where n is
 * the number of aircraft. The nested-loop-with-offset pattern ({@code j = i + 1})
 * ensures each pair is checked exactly once, avoiding redundant and self-comparisons.</p>
 *
 * @author ATSS Development Team
 * @version 1.0
 */
public class CollisionDetector {

    /** Distance threshold in nautical miles below which a RED ALERT is triggered. */
    private static final double ALERT_THRESHOLD = 5.0;

    /** ANSI escape code for red text (supported by most modern terminals). */
    private static final String ANSI_RED = "\u001B[31m";

    /** ANSI escape code to reset text color. */
    private static final String ANSI_RESET = "\u001B[0m";

    /**
     * Checks all aircraft pairs for proximity violations.
     *
     * <p>Converts the input collection to an indexed list, then uses the
     * nested-loop-with-offset pattern to compare each unique pair exactly once:</p>
     * <pre>
     *   for i = 0 to n-1
     *     for j = i+1 to n-1
     *       compare(aircraft[i], aircraft[j])
     * </pre>
     *
     * <p>For n aircraft, this produces n(n-1)/2 comparisons instead of n^2.</p>
     *
     * @param aircraft the current collection of tracked aircraft
     * @return the number of collision alerts triggered during this check
     */
    public int checkCollisions(Collection<Aircraft> aircraft) {
        if (aircraft == null || aircraft.size() < 2) {
            return 0;
        }

        List<Aircraft> list = new ArrayList<>(aircraft);
        int alertCount = 0;

        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size(); j++) {
                Aircraft a = list.get(i);
                Aircraft b = list.get(j);

                double distance = calculateDistance(a, b);

                if (distance < ALERT_THRESHOLD) {
                    alertCount++;
                    printAlert(a, b, distance);
                }
            }
        }

        return alertCount;
    }

    /**
     * Calculates the Euclidean distance between two aircraft in 2D space.
     *
     * <p>Formula: d = sqrt((x2-x1)^2 + (y2-y1)^2)</p>
     *
     * <p>Uses direct multiplication ({@code dx * dx}) instead of
     * {@link Math#pow(double, double)} for squaring, as {@code pow()} is designed
     * for fractional exponents and carries unnecessary overhead for integer powers.</p>
     *
     * @param a first aircraft
     * @param b second aircraft
     * @return the distance in nautical miles between the two aircraft
     */
    static double calculateDistance(Aircraft a, Aircraft b) {
        double dx = b.getX() - a.getX();
        double dy = b.getY() - a.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Prints a formatted collision alert to the console with red ANSI coloring.
     *
     * @param a        first aircraft in the proximity violation
     * @param b        second aircraft in the proximity violation
     * @param distance the current distance between the two aircraft
     */
    private void printAlert(Aircraft a, Aircraft b, double distance) {
        System.out.printf(
                "%s  ██ [RED ALERT] COLLISION WARNING ██  %s ←→ %s  |  Distance: %.2f nm  (Threshold: %.1f nm)%s%n",
                ANSI_RED,
                a.getCallsign(),
                b.getCallsign(),
                distance,
                ALERT_THRESHOLD,
                ANSI_RESET
        );
    }
}
