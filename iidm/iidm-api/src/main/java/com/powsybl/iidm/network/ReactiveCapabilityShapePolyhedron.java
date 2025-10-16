package com.powsybl.iidm.network;

import com.powsybl.commons.PowsyblException;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.linear.*;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Defines a P,Q,V convex polyhedron as the intersection of multiple half-spaces (Hyperplanes).
 * It provides the core logic to check if a point is within this operational envelope.
 * It provides computation method for Minimal or maximal value of reactive power Q inside the polytope
 */
public final class ReactiveCapabilityShapePolyhedron {
    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ReactiveCapabilityShapePolyhedron.class);
    /**
     * The convex polyhedron hyperplanes
     */
    private final List<ReactiveCapabilityShapePlane> listOfPlanes;
    /**
     * Lower bound for reactive power in MVaR (default to Double.NaN for inactive)
     */
    private double minQ = Double.NaN;
    /**
     * Upper bound for reactive power in MVaR (default to Double.NaN for inactive)
     */
    private double maxQ = Double.NaN;
    /**
     * Lower bound for active power in MW (default to Double.NaN for inactive)
     */
    private double minP = Double.NaN;
    /**
     * Upper bound for active power in MW  (default to Double.NaN for inactive)
     */
    private double maxP = Double.NaN;
    /**
     * Lower bound for voltage in KV (default to Double.NaN for inactive)
     */
    private double minV = Double.NaN;
    /**
     * Upper bound for voltage in KV (default to Double.NaN for inactive)
     */
    private double maxV = Double.NaN;

    /**
     * Constructor
     */
    private ReactiveCapabilityShapePolyhedron(final List<ReactiveCapabilityShapePlane> planes) {
        this.listOfPlanes = planes;
    }

    /**
     * Set the list of hyperplanes forming the convex polyhedron
     * @param planes the list of Plane defining the convex polyhedron
     * @return this
     */
    public static ReactiveCapabilityShapePolyhedron build(final List<ReactiveCapabilityShapePlane> planes) {
        return new ReactiveCapabilityShapePolyhedron(planes);
    }

    /**
     * Add lower and upper bound for reactive power
     * @param minQ the lower bound for the reactive power in MVaR (can be set to Double.NaN for disabling)
     * @param maxQ the upper bound for the reactive power n MVaR (can be set to Double.NaN for disabling)
     * @return this
     */
    public ReactiveCapabilityShapePolyhedron withReactivePowerBounds(final double minQ, final double maxQ) {
        this.minQ = minQ;
        this.maxQ = maxQ;
        return this;
    }

    /**
     * Add lower and upper bound for active power
     * @param minP the lower bound for the active power in MW (can be set to Double.NaN for disabling)
     * @param maxP the upper bound for the active power in MW(can be set to Double.NaN for disabling)
     * @return this
     */
    public ReactiveCapabilityShapePolyhedron withActivePowerBounds(final double minP, final double maxP) {
        this.minP = minP;
        this.maxP = maxP;
        return this;
    }

    /**
     * Add lower and upper bound for voltage
     * @param minV the lower bound for the voltage in KV (can be set to Double.NaN for disabling)
     * @param maxV the upper bound for the voltage in KV (can be set to Double.NaN for disabling)
     * @return this
     */
    public ReactiveCapabilityShapePolyhedron withVoltageBounds(final double minV, final double maxV) {
        this.minV = minV;
        this.maxV = maxV;
        return this;
    }

    /**
     * Checks if a point (P, Q, U) is inside the convex polyhedron.
     * @param p The Active Power (P in MW).
     * @param q The Reactive Power (Q in MVaR).
     * @param u The Voltage (U in KV).
     * @return true if the point satisfies ALL plane constraints, false otherwise.
     */
    public boolean isInside(final double p, final double q, final double u) {
        boolean insideBounds = isInsideBounds(p, q, u);
        if (insideBounds) {
            // Iterate through all bounding planes
            for (ReactiveCapabilityShapePlane plane : listOfPlanes) {

                // Calculate the left-hand side (LHS) expression: Q + alpha * U + beta * P
                // (Note: Using 'u' for the voltage variable from the method signature)
                double expression = q + plane.getAlpha() * u + plane.getBeta() * p;

                // Check the constraint for '<' relation (LHS must be strictly less than gamma)
                // The condition for OUTSIDE is: LHS > gamma
                if (plane.isLessOrEqual() && expression > plane.getGamma()) {
                    // Point fails the '<' constraint
                    return false;
                }

                // Check the constraint for '>' relation (LHS must be strictly greater than gamma)
                // The condition for OUTSIDE is: LHS < gamma
                if (!plane.isLessOrEqual() && expression < plane.getGamma()) {
                    // Point fails the '>' constraint
                    return false;
                }
            }

            // If the point satisfies ALL constraints, it is inside the convex polyhedron.
            return true;
        } else {
            return false;
        }
    }

    /**
     * Return true if the p, q, u point is inside the polytope and respects the bounds constraints
     * @param p the active power  in MW
     * @param q the reactive power in MVaR
     * @param u the tension in KV
     * @return true iff the p, q, u point is inside the polytope and respects the bounds constraints
     */
    private boolean isInsideBounds(final double p, final double q, final double u) {
        return (p <= maxP || Double.isNaN(maxP))
                && (p >= minP || Double.isNaN(minP))
                && (u <= maxV || Double.isNaN(maxV))
                && (u >= minV || Double.isNaN(minV))
                && (q <= maxQ || Double.isNaN(maxQ))
                && (q >= minQ || Double.isNaN(minQ));
    }

    /**
     * Gets the minimum feasible Reactive Power (Q in MVaR) for a fixed Active Power (P in MW) and Voltage (U in KV).
     * This is the tightest lower bound imposed by the set of planes.
     */
    public double getMinQ(final double p, final double u) {
        return getOptimalQ(p, GoalType.MINIMIZE, Collections.singletonList(new LinearConstraint(new double[]{0, 1.0, 0}, Relationship.EQ, u)));
    }

    /**
     * Gets the maximum feasible Reactive Power (Q in MVaR) for a fixed Active Power (P in MW) and Voltage (U in KV).
     * This is the tightest upper bound imposed by the set of planes.
     */
    public double getMaxQ(final double p, final double u) {
        return getOptimalQ(p, GoalType.MAXIMIZE, Collections.singletonList(new LinearConstraint(new double[]{0, 1.0, 0}, Relationship.EQ, u)));
    }

    /**
     * Utility method using a linear program solver to compute minimal or maximal possible values for reactive power Q
     * @param p The fixed active power in KW
     * @param goalType Either GoalType.MINIMIZE for minimization of Q or GoalType.MAXIMIZE for maximization of Q
     * @param additionalConstraints Optional set of additional constrains
     * @return the optimal value for the reactive power Q in MVaR
     */
    public double getOptimalQ(final double p, final GoalType goalType, final List<LinearConstraint> additionalConstraints) {
        try {
            Collection<LinearConstraint> constraints = new ArrayList<>();
            // Addd lower / upper bounds constraints
            addBoundsConstraints(constraints);
            // Add the convex polytope hyperplane constraints
            for (ReactiveCapabilityShapePlane plane : listOfPlanes) {
                constraints.add(
                        new LinearConstraint(
                                new double[]{1.0, plane.getAlpha(), plane.getBeta()},
                                plane.isLessOrEqual() ? Relationship.LEQ : Relationship.GEQ,
                                plane.getGamma()
                        )
                );
            }
            // Fix active power at the given value of p
            constraints.add(new LinearConstraint(new double[]{0.0, 0.0, 1.0}, Relationship.EQ, p));
            if (additionalConstraints != null) {
                constraints.addAll(additionalConstraints);
            }
            // Objective: optimize Q (first variable)
            LinearObjectiveFunction objective = new LinearObjectiveFunction(new double[]{1.0, 0.0, 0.0}, 0);

            // Solve using Simplex
            SimplexSolver solver = new SimplexSolver();
            PointValuePair solution = solver.optimize(
                    new MaxIter(200),
                    objective,
                    new LinearConstraintSet(constraints),
                    goalType,
                    new NonNegativeConstraint(false)  // allow negative Q, U, etc.
            );

            double[] vars = solution.getPoint();
            double q = vars[0];
            double u = vars[1];

            LOGGER.debug("Optimal Q={} at U={} (P fixed at {})", q, u, p);
            return q;

        } catch (NoFeasibleSolutionException e) {
            throw new PowsyblException("Reactive envelope infeasible at P=" + p, e);
        } catch (Exception e) {
            throw new PowsyblException("Error solving for Q at P=" + p, e);
        }
    }

    /**
     * Convert upper / lower bounds for Q, P and V to linear constraints for the linear solver
     * @param constraints a collection to fill with linear constraints corresponding to upper / lower bounds for Q, P and V
     */
    private void addBoundsConstraints(final Collection<LinearConstraint> constraints) {
        // Check the bounds activities before adding them to the linear program
        if (!Double.isNaN(this.minQ)) {
            constraints.add(new LinearConstraint(new double[]{1.0, 0.0, 0.0}, Relationship.GEQ, this.minQ));
        }
        if (!Double.isNaN(this.maxQ)) {
            constraints.add(new LinearConstraint(new double[]{1.0, 0.0, 0.0}, Relationship.LEQ, this.maxQ));
        }
        if (!Double.isNaN(this.minP)) {
            constraints.add(new LinearConstraint(new double[]{0.0, 0.0, 1.0}, Relationship.GEQ, this.minP));
        }
        if (!Double.isNaN(this.maxP)) {
            constraints.add(new LinearConstraint(new double[]{0.0, 0.0, 1.0}, Relationship.LEQ, this.maxP));
        }
        if (!Double.isNaN(this.minV)) {
            constraints.add(new LinearConstraint(new double[]{0.0, 1.0, 0.0}, Relationship.GEQ, this.minV));
        }
        if (!Double.isNaN(this.maxV)) {
            constraints.add(new LinearConstraint(new double[]{0.0, 1.0, 0.0}, Relationship.LEQ, this.maxV));
        }
    }

    /**
     * Finds the overall minimum Q in MVaR for a fixed P in MW, considering ALL feasible U in KV value within the polyhedron.
     */
    public double getMinQ(final double p) {
        return getOptimalQ(p, GoalType.MINIMIZE, Collections.emptyList());
    }

    /**
     * Finds the overall maximum Q in MVaR for a fixed P in MW, considering ALL feasible U in KV values within the polyhedron.
     */
    public double getMaxQ(final double p) {
        return getOptimalQ(p, GoalType.MAXIMIZE, Collections.emptyList());
    }

    /**
     * Prints a formatted, human-readable list of all the constraints
     * that define the convex polyhedron into a String.
     * @return A String containing the pretty-printed constraints.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (listOfPlanes.isEmpty()) {
            sb.append("The Polyhedron is unbounded and undefined (no constraints).");
            return sb.toString();
        }
        String separator = "=======================================================\n";
        sb.append("\n").append(separator);
        sb.append("  UPPER AND LOWER BOUNDS ON (P, Q, U)\n");
        if (!Double.isNaN(this.minQ)) {
            sb.append(" Q ≥ ").append(this.minQ).append(" MVaR");
        }
        if (!Double.isNaN(this.maxQ)) {
            sb.append(" Q ≤ ").append(this.maxQ).append(" MVaR");
        }
        if (!Double.isNaN(this.minP)) {
            sb.append(" P ≥ ").append(this.minP).append(" MW");
        }
        if (!Double.isNaN(this.maxP)) {
            sb.append(" P ≤ ").append(this.maxP).append(" MW");
        }
        if (!Double.isNaN(this.minV)) {
            sb.append(" U ≥ ").append(this.minV).append(" KV");
        }
        if (!Double.isNaN(this.maxV)) {
            sb.append(" U ≤ ").append(this.maxV).append(" KV");
        }
        sb.append("  CONVEX POLYHEDRON CONSTRAINTS (P, Q, U Operating Envelope)\n");
        sb.append(separator);

        int index = 1;
        for (ReactiveCapabilityShapePlane plane : listOfPlanes) {
            // Use the improved Plane.toString() method and append to the buffer
            sb.append(String.format("[%2d] %s%n", index++, plane.toString()));
        }
        sb.append(separator).append("\n");
        return sb.toString();
    }
}
