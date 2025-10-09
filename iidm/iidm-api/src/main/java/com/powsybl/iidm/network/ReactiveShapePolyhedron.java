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
 * Defines a 3D convex polyhedron as the intersection of multiple half-spaces (Planes).
 * It provides the core logic to check if a point is within this operational envelope.
 */
public class ReactiveShapePolyhedron {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReactiveShapePolyhedron.class);

    private final List<ReactiveShapePlane> listOfPlanes;


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
        return getOptimalQ(p,GoalType.MINIMIZE,Collections.singletonList(new LinearConstraint(new double[]{0,1,0},Relationship.EQ,u)));
    }

    /**
     * Gets the maximum feasible Reactive Power (Q) for a fixed Active Power (P) and Voltage (U).
     * This is the tightest upper bound imposed by the set of planes.
     */
    public double getMaxQ(double p, double u) {
        return getOptimalQ(p,GoalType.MAXIMIZE,Collections.singletonList(new LinearConstraint(new double[]{0,1,0},Relationship.EQ,u)));
    }

    // ----------------------------------------------------------------------
    // --- Implementation for methods relying only on P
    // ----------------------------------------------------------------------


    private Collection<LinearConstraint> getLinearConstraints(double p) {
        Collection<LinearConstraint> constraints = new ArrayList<>();
        for (ReactiveShapePlane plane : listOfPlanes) {
            constraints.add(
                    new LinearConstraint(
                            new double[]{1.0, plane.alpha, plane.beta},
                            plane.isLowerThan ? Relationship.LEQ : Relationship.GEQ,
                            plane.gamma
                    )
            );
        }

        // Fix active power at the given value of p
        constraints.add(
                new LinearConstraint(new double[]{0.0, 0.0, 1.0}, Relationship.EQ, p)
        );

        return constraints;
    }

    public double getOptimalQ(double p, GoalType goalType, List<LinearConstraint> additionalConstraints) {
        try {
            Collection<LinearConstraint> constraints = getLinearConstraints(p);
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
     * Finds the overall minimum Q for a fixed P, considering ALL feasible U values
     * within the polyhedron. This requires solving a 2D optimization subproblem.
     * * Since this is significantly more complex than the fixed-U case, this implementation
     * returns NaN to indicate it requires a full 2D LP search (Min/Max Q over the
     * feasible U range at a given P).
     */
    public double getMinQ(double p) {
        return getOptimalQ(p,GoalType.MINIMIZE, Collections.emptyList());
    }

    /**
     * Finds the overall maximum Q for a fixed P, considering ALL feasible U values
     * within the polyhedron.
     */
    public double getMaxQ(double p) {
        return getOptimalQ(p,GoalType.MAXIMIZE , Collections.emptyList());
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
