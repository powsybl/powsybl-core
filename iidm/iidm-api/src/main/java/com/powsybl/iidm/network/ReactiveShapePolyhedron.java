package com.powsybl.iidm.network;

import com.powsybl.commons.PowsyblException;

import java.util.List;

/**
 * Defines a 3D convex polyhedron as the intersection of multiple half-spaces (Planes).
 * It provides the core logic to check if a point is within this operational envelope.
 */
public class ReactiveShapePolyhedron {

    private final List<ReactiveShapePlane> listOfPlanes;

    // Use large constants for initialization of Min/Max Q
    private static final double INITIAL_MIN_Q = Double.NEGATIVE_INFINITY;
    private static final double INITIAL_MAX_Q = Double.POSITIVE_INFINITY;

    /**
     * Constructor
     *
     * @param planes the list of Plane defining the convex polyhedron
     */
    public ReactiveShapePolyhedron(List<ReactiveShapePlane> planes) {
        this.listOfPlanes = planes;
    }

    // ----------------------------------------------------------------------
    // --- Implementation of OperatingLimits methods (Min/Max Q for fixed P, U)
    // ----------------------------------------------------------------------

    /**
     * Checks if a point (P, Q, U) is inside the convex polyhedron.
     * * @param p The Active Power (P in MW).
     *
     * @param q The Reactive Power (Q in MVaR).
     * @param u The Voltage (U in Volts).
     * @return true if the point satisfies ALL plane constraints, false otherwise.
     */
    public boolean isInside(double p, double q, double u) {
        // Iterate through all bounding planes
        for (ReactiveShapePlane plane : listOfPlanes) {

            // Calculate the left-hand side (LHS) expression: Q + alpha * U + beta * P
            // (Note: Using 'u' for the voltage variable from the method signature)
            double expression = q + plane.alpha * u + plane.beta * p;

            // Check the constraint for '<' relation (LHS must be strictly less than gamma)
            // The condition for OUTSIDE is: LHS > gamma
            if (plane.isLowerThan && expression > plane.gamma) {
                // Point fails the '<' constraint
                return false;
            }

            // Check the constraint for '>' relation (LHS must be strictly greater than gamma)
            // The condition for OUTSIDE is: LHS < gamma
            if (!plane.isLowerThan && expression < plane.gamma) {
                // Point fails the '>' constraint
                return false;
            }
        }

        // If the point satisfies ALL constraints, it is inside the convex polyhedron.
        return true;
    }

    /**
     * Gets the minimum feasible Reactive Power (Q) for a fixed Active Power (P) and Voltage (U).
     * This is the tightest lower bound imposed by the set of planes.
     */
    public double getMinQ(double p, double u) {
        double currentMinQ = INITIAL_MIN_Q; // Starts at negative infinity (no lower bound yet)

        for (ReactiveShapePlane plane : listOfPlanes) {

            // The plane constraint is: Q + alpha * U + beta * P  relation  gamma
            // Rearranging to isolate Q: Q  relation  gamma - (alpha * U + beta * P)
            double limit = plane.gamma - (plane.alpha * u + plane.beta * p);

            // A '>=' relation means Q MUST be greateror equal to the limit.
            // This defines a LOWER BOUND on Q.
            if (!plane.isLowerThan) {
                // The actual constraint is Q >= limit. This sets the floor.
                // We take the max of the current floor and this new limit.
                currentMinQ = Math.max(currentMinQ, limit);
            }

            // A '<=' relation means Q MUST be less or equal than the limit.
            // This defines an UPPER BOUND on Q. We ignore upper bounds when finding the minimum Q.
            // However, a case where currentMinQ > limit (i.e., the lower bound is higher than an upper bound)
            // implies the point (P, U) is outside the feasible region (empty Q range).
            if (plane.isLowerThan && currentMinQ > limit) {
                // If the required minimum Q is greater than an imposed maximum Q,
                // the P-U point is infeasible (empty intersection).
                return Double.NaN; // Or throw an exception for infeasible point
            }
        }

        // Check if the overall limit is still negative infinity (unbounded below)
        if (currentMinQ == INITIAL_MIN_Q) {
            return Double.NEGATIVE_INFINITY;
        }

        return currentMinQ;
    }

    /**
     * Gets the maximum feasible Reactive Power (Q) for a fixed Active Power (P) and Voltage (U).
     * This is the tightest upper bound imposed by the set of planes.
     */
    public double getMaxQ(double p, double u) {
        double currentMaxQ = INITIAL_MAX_Q; // Starts at positive infinity (no upper bound yet)

        for (ReactiveShapePlane plane : listOfPlanes) {

            // Rearranging to isolate Q: Q  relation  gamma - (alpha * U + beta * P)
            double limit = plane.gamma - (plane.alpha * u + plane.beta * p);

            // A '<' relation means Q MUST be less than the limit.
            // This defines an UPPER BOUND on Q.
            if (plane.isLowerThan) {
                // The actual constraint is Q < limit. This sets the ceiling.
                // We take the min of the current ceiling and this new limit.
                currentMaxQ = Math.min(currentMaxQ, limit);
            }

            // A '>' relation means Q MUST be greater than the limit.
            // This defines a LOWER BOUND on Q. We ignore lower bounds when finding the maximum Q.
            // However, a case where currentMaxQ < limit implies infeasibility.
            if (!plane.isLowerThan && currentMaxQ < limit) {
                // If the required maximum Q is less than an imposed minimum Q,
                // the P-U point is infeasible (empty intersection).
                return Double.NaN; // Or throw an exception for infeasible point
            }
        }

        // Check if the overall limit is still positive infinity (unbounded above)
        if (currentMaxQ == INITIAL_MAX_Q) {
            return Double.POSITIVE_INFINITY;
        }

        return currentMaxQ;
    }

    // ----------------------------------------------------------------------
    // --- Implementation for methods relying only on P
    // ----------------------------------------------------------------------

    /**
     * Finds the overall minimum Q for a fixed P, considering ALL feasible U values
     * within the polyhedron. This requires solving a 2D optimization subproblem.
     * * Since this is significantly more complex than the fixed-U case, this implementation
     * returns NaN to indicate it requires a full 2D LP search (Min/Max Q over the
     * feasible U range at a given P).
     */
    public double getMinQ(double p) {
        // This method requires an iteration over the feasible U range or a dedicated 2D LP solver.
        // It cannot be solved by simply iterating over the planes as we did for fixed U.
        throw new PowsyblException("Warning: getMinQ(P) requires solving a 2D Linear Program over the feasible U range.");
    }

    /**
     * Finds the overall maximum Q for a fixed P, considering ALL feasible U values
     * within the polyhedron.
     */
    public double getMaxQ(double p) {
        throw new PowsyblException("Warning: getMaxQ(P) requires solving a 2D Linear Program over the feasible U range.");
    }

    /**
     * Prints a formatted, human-readable list of all the constraints
     * that define the convex polyhedron into a String.
     * * @return A String containing the pretty-printed constraints.
     */
    public String toString() {
        // Using StringBuilder is generally preferred over StringBuffer for non-concurrent scenarios.
        StringBuilder sb = new StringBuilder();

        if (listOfPlanes.isEmpty()) {
            sb.append("The Polyhedron is unbounded and undefined (no constraints).");
            return sb.toString();
        }

        String separator = "=======================================================\n";

        sb.append("\n").append(separator);
        sb.append("  CONVEX POLYHEDRON CONSTRAINTS (P, Q, U Operating Envelope)\n");
        sb.append(separator);

        int index = 1;
        for (ReactiveShapePlane plane : listOfPlanes) {
            // Use the improved Plane.toString() method and append to the buffer
            sb.append(String.format("[%2d] %s%n", index++, plane.toString()));
        }

        sb.append(separator).append("\n");

        return sb.toString();
    }
}
