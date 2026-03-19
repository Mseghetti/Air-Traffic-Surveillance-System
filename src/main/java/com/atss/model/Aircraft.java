package com.atss.model;

/**
 * Represents a tracked aircraft within the Air Traffic Surveillance System.
 *
 * <p>Each aircraft has a unique callsign, a 2D position (x, y), a speed scalar,
 * and a heading in degrees (0 = North, 90 = East, 180 = South, 270 = West).
 * Position updates are derived from speed and heading using trigonometric
 * decomposition.</p>
 *
 * <p><strong>Thread Safety:</strong> This class is not inherently thread-safe.
 * External synchronization or a thread-safe registry must be used when
 * instances are shared across threads.</p>
 *
 * @author ATSS Development Team
 * @version 1.0
 */
public class Aircraft {

    /** Unique identifier for this aircraft (immutable after construction). */
    private final String callsign;

    /** Current X-axis position in nautical miles. */
    private double x;

    /** Current Y-axis position in nautical miles. */
    private double y;

    /** Speed in nautical miles per radar tick. */
    private double speed;

    /** Heading in degrees, where 0 = North and values increase clockwise. */
    private double heading;

    /**
     * Constructs a new Aircraft with the specified parameters.
     *
     * @param callsign unique identifier for this aircraft (cannot be null or blank)
     * @param x        initial X-axis position in nautical miles
     * @param y        initial Y-axis position in nautical miles
     * @param speed    speed in nautical miles per radar tick (must be non-negative)
     * @param heading  heading in degrees [0, 360)
     * @throws IllegalArgumentException if callsign is null/blank, speed is negative,
     *                                  or heading is outside [0, 360)
     */
    public Aircraft(String callsign, double x, double y, double speed, double heading) {
        if (callsign == null || callsign.isBlank()) {
            throw new IllegalArgumentException("Callsign must not be null or blank.");
        }
        if (speed < 0) {
            throw new IllegalArgumentException("Speed must be non-negative. Received: " + speed);
        }
        if (heading < 0 || heading >= 360) {
            throw new IllegalArgumentException("Heading must be in range [0, 360). Received: " + heading);
        }

        this.callsign = callsign;
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.heading = heading;
    }

    /**
     * Advances this aircraft's position by one tick based on current speed and heading.
     *
     * <p>Uses trigonometric decomposition to convert polar movement (speed + heading)
     * into Cartesian displacement:</p>
     * <ul>
     *   <li>deltaX = speed * sin(heading)</li>
     *   <li>deltaY = speed * cos(heading)</li>
     * </ul>
     *
     * <p>Heading is measured clockwise from North (Y-axis), which is why sine maps
     * to X and cosine maps to Y -- the reverse of standard math convention.</p>
     */
    public void updatePosition() {
        double headingRadians = Math.toRadians(this.heading);
        this.x += this.speed * Math.sin(headingRadians);
        this.y += this.speed * Math.cos(headingRadians);
    }

    // ========================================================================
    // Getters
    // ========================================================================

    /**
     * Returns the unique callsign of this aircraft.
     *
     * @return the callsign (never null)
     */
    public String getCallsign() {
        return callsign;
    }

    /**
     * Returns the current X-axis position.
     *
     * @return X position in nautical miles
     */
    public double getX() {
        return x;
    }

    /**
     * Returns the current Y-axis position.
     *
     * @return Y position in nautical miles
     */
    public double getY() {
        return y;
    }

    /**
     * Returns the current speed.
     *
     * @return speed in nautical miles per radar tick
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Returns the current heading.
     *
     * @return heading in degrees [0, 360)
     */
    public double getHeading() {
        return heading;
    }

    // ========================================================================
    // Setters (callsign is intentionally immutable -- no setter)
    // ========================================================================

    /**
     * Sets the X-axis position.
     *
     * @param x new X position in nautical miles
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Sets the Y-axis position.
     *
     * @param y new Y position in nautical miles
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * Sets the speed of this aircraft.
     *
     * @param speed new speed in nautical miles per radar tick (must be non-negative)
     * @throws IllegalArgumentException if speed is negative
     */
    public void setSpeed(double speed) {
        if (speed < 0) {
            throw new IllegalArgumentException("Speed must be non-negative. Received: " + speed);
        }
        this.speed = speed;
    }

    /**
     * Sets the heading of this aircraft.
     *
     * @param heading new heading in degrees [0, 360)
     * @throws IllegalArgumentException if heading is outside [0, 360)
     */
    public void setHeading(double heading) {
        if (heading < 0 || heading >= 360) {
            throw new IllegalArgumentException("Heading must be in range [0, 360). Received: " + heading);
        }
        this.heading = heading;
    }

    // ========================================================================
    // Object overrides
    // ========================================================================

    /**
     * Returns a formatted string representation suitable for radar console output.
     *
     * @return string in format: [CALLSIGN] Position(x, y) | Speed: s | Heading: h°
     */
    @Override
    public String toString() {
        return String.format("[%s] Position(%.2f, %.2f) | Speed: %.2f | Heading: %.1f°",
                callsign, x, y, speed, heading);
    }
}
