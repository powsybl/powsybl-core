package com.powsybl.iidm.network;

import static org.apache.commons.math3.util.Precision.EPSILON;

/**
 * Represents a plane (or half-space) defining one boundary of the convex polyhedron.
 * <pre>
 * The inequality is of the form: Q + alpha * U + beta * P  {≤, ≥}  gamma.
 * P = Active Power (MW)
 * Q = Reactive Power (MVaR)
 * U = Voltage (Volts)
 * </pre>
 */
public final class ReactiveCapabilityShapePlane {

    /**
     * Coefficient for U (Voltage)
     */
    private final double alpha;
    /**
     * Coefficient for P (Active Power)
      */
    private final double beta;
    /**
     *The constant limit on the right side
      */
    private double gamma;

    /**
     * ≤ or ≥ inequality types
     */
    public enum InequalityType {
       LESS_OR_EQUAL,       // ≤ gamma
        GREATER_OR_EQUAL    // ≥  gamma
    }

    /**
     * The inequaility types
     */
    InequalityType inequalityType;

    /**
     * private constructor for a ReactiveCapabilityShapePlane.
     *
     * @param alpha    The coefficient for U.
     * @param beta     The coefficient for P.
     */
    private ReactiveCapabilityShapePlane(double alpha, double beta) {
        this.alpha = alpha;
        this.beta = beta;
    }

    /**
     * Builder for a ReactiveCapabilityShapePlane.
     *
     * @param alpha    The coefficient for U.
     * @param beta     The coefficient for P.
     */
    public static ReactiveCapabilityShapePlane build(double alpha, double beta) {
        return new ReactiveCapabilityShapePlane(alpha, beta);
    }

    /**
     * Set the hyperplane constraint to a less or equal ≤ inequality with gamma right hand side
     * @param gamma the gamma right hand side
     * @return this
     */
    public ReactiveCapabilityShapePlane lessOrEqual(double gamma) {
        this.gamma = gamma;
        this.inequalityType = InequalityType.LESS_OR_EQUAL;
        return this;
    }

    /**
     * Set the hyperplane constraint to a greater or equal ≥ inequality with gamma right hand side
     * @param gamma the gamma right hand side
     * @return this
     */
    public ReactiveCapabilityShapePlane greaterOrEqual(double gamma) {
        this.gamma = gamma;
        this.inequalityType = InequalityType.GREATER_OR_EQUAL;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // Always include Q since it has an implicit coefficient of 1.0 in your formula
        sb.append("Q");

        // Append U term (alpha * U)
        if (Math.abs(alpha) > EPSILON) { // Check if alpha is non-zero
            sb.append(alpha > 0 ? " + " : " - ");
            sb.append(String.format("%.3f", Math.abs(alpha))).append(" * U");
        }

        // Append P term (beta * P)
        if (Math.abs(beta) > EPSILON) { // Check if beta is non-zero
            sb.append(beta > 0 ? " + " : " - ");
            sb.append(String.format("%.3f", Math.abs(beta))).append(" * P");
        }

        // Append relation and gamma
        sb.append(" ").append(inequalityType.equals(InequalityType.LESS_OR_EQUAL) ? '≤' : '≥').append(" ");
        sb.append(String.format("%.3f", gamma));

        if (sb.toString().startsWith("Q ")) {
            return sb.toString();
        }

        return sb.toString();
    }

    /**
     * @return true if the hyperplan is of '≤' type
     */
    public boolean isLessOrEqual() {
        return inequalityType == InequalityType.LESS_OR_EQUAL;
    }

    /**
     * @return true if the hyperplan is of '≥' type
     */
    public boolean isGreaterOrEqual() {
        return inequalityType == InequalityType.GREATER_OR_EQUAL;
    }

    /**
     * @return the alpha coefficient for the tension U
     */
    public double getAlpha() {
        return alpha;
    }

    /**
     * @return the beta coefficient for the active power P
     */
    public double getBeta() {
        return beta;
    }

    /**
     * @return the gamma Right hand side
     */
    public double getGamma() {
        return gamma;
    }
}
