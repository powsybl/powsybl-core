package com.powsybl.iidm.network;

/**
 * Represents a plane (or half-space) defining one boundary of the convex polyhedron.
 * <pre>
 * The inequality is of the form: Q + alpha * U + beta * P  {<, >}  gamma.
 * * P = Active Power (MW)
 * Q = Reactive Power (MVaR)
 * U = Voltage (Volts)
 * </pre>
 */
public class ReactiveShapePlane {

    /**
     * Coefficient for U (Voltage)
     */
    public final double alpha;
    /**
     * Coefficient for P (Active Power)
      */
    public final double beta;
    /**
     *The constant limit on the right side
      */
    public final double gamma;

    /**
     * '<' or '>' inequality
     */
    public final boolean isLowerThan;

    /**
     * Constructor for a bounding plane.
     *
     * @param alpha    The coefficient for U.
     * @param beta     The coefficient for P.
     * @param gamma    The boundary constant.
     * @param isLowerThan The inequality direction ('<' or '>').
     */
    public ReactiveShapePlane(double alpha, double beta, double gamma, boolean isLowerThan) {
        this.alpha = alpha;
        this.beta = beta;
        this.gamma = gamma;
        this.isLowerThan = isLowerThan;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // Always include Q since it has an implicit coefficient of 1.0 in your formula
        sb.append("Q");

        // Append U term (alpha * U)
        if (Math.abs(alpha) > 1e-9) { // Check if alpha is non-zero
            sb.append(alpha > 0 ? " + " : " - ");
            sb.append(String.format("%.3f", Math.abs(alpha))).append(" * U");
        }

        // Append P term (beta * P)
        if (Math.abs(beta) > 1e-9) { // Check if beta is non-zero
            sb.append(beta > 0 ? " + " : " - ");
            sb.append(String.format("%.3f", Math.abs(beta))).append(" * P");
        }

        // Append relation and gamma
        sb.append(" ").append(isLowerThan ? '≤':'≥').append(" ");
        sb.append(String.format("%.3f", gamma));

        // Tidy up if Q is the only term (e.g., "Q + 0.000 * U + 0.000 * P < 100.000")
        if (sb.toString().startsWith("Q ")) {
            return sb.toString();
        }

        return sb.toString();
    }
}
