/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.ReactiveCapabilityShapePlane;

import java.util.Locale;

import static org.apache.commons.math3.util.Precision.EPSILON;

/**
 * Represents a plane (or half-space) defining one boundary of the convex polyhedron.
 * <pre>
 * The inequality is of the form: coeffQ * Q + coeffU * U + coeffP * P  {≤, ≥}  rhs.
 * P = Active Power (MW)
 * Q = Reactive Power (MVAr)
 * U = Voltage (KV)
 * </pre>
 *
 * <p>The affine expression ({@code coeffU}, {@code coeffP}, {@code coeffQ}) is fixed at construction time
 * through {@link #build}, while the inequality direction and the {@code rhs} right-hand side are set
 * afterwards through {@link #lessOrEqual(double)} / {@link #greaterOrEqual(double)}. This split keeps the
 * geometric coefficients of the plane independent from its relational orientation, which mirrors the way
 * the half-space is later assembled into a {@code LinearConstraint}. Users of the public API never deal
 * with this two-step construction: {@code ReactiveCapabilityShapeAdder.addPlane(...)} takes the
 * coefficients, the inequality direction and {@code rhs} in a single call.</p>
 *
 * @author Fabrice Buscaylet {@literal <fabrice.buscaylet at artelys.com>}
 */
public final class ReactiveCapabilityShapePlaneImpl implements ReactiveCapabilityShapePlane {

    /**
     * Coefficient for U (Voltage)
     */
    private final double coeffU;
    /**
     * Coefficient for P (Active Power)
      */
    private final double coeffP;
    /**
     * Coefficient for Q (Reactive Power)
     */
    private final double coeffQ;
    /**
     *The constant limit on the right side
      */
    private double rhs;

    /**
     * ≤ or ≥ inequality types
     */
    public enum InequalityType {
       LESS_OR_EQUAL,       // ≤ rhs
       GREATER_OR_EQUAL     // ≥ rhs
    }

    /**
     * The inequaility types
     */
    private InequalityType inequalityType;

    /**
     * private constructor for a ReactiveCapabilityShapePlane.
     *
     * @param coeffU    The coefficient for U.
     * @param coeffP    The coefficient for P.
     * @param coeffQ    The coefficient for Q.
     */
    private ReactiveCapabilityShapePlaneImpl(double coeffU, double coeffP, double coeffQ) {
        this.coeffU = coeffU;
        this.coeffP = coeffP;
        this.coeffQ = coeffQ;
    }

    /**
     * Builder for a ReactiveCapabilityShapePlane.
     *
     * @param coeffU    The coefficient for U.
     * @param coeffP    The coefficient for P.
     */
    public static ReactiveCapabilityShapePlane build(double coeffU, double coeffP) {
        return new ReactiveCapabilityShapePlaneImpl(coeffU, coeffP, 1.0);
    }

    /**
     * Builder for a ReactiveCapabilityShapePlane.
     *
     * @param coeffU    The coefficient for U.
     * @param coeffP    The coefficient for P.
     * @param coeffQ    The coefficient for Q.
     */
    public static ReactiveCapabilityShapePlane build(double coeffU, double coeffP, double coeffQ) {
        return new ReactiveCapabilityShapePlaneImpl(coeffU, coeffP, coeffQ);
    }

    /**
     * Set the hyperplane constraint to a less or equal ≤ inequality with the given right hand side
     * @param rhs the right hand side
     * @return this
     */
    @Override
    public ReactiveCapabilityShapePlane lessOrEqual(double rhs) {
        this.rhs = rhs;
        this.inequalityType = InequalityType.LESS_OR_EQUAL;
        return this;
    }

    /**
     * Set the hyperplane constraint to a greater or equal ≥ inequality with the given right hand side
     * @param rhs the right hand side
     * @return this
     */
    @Override
    public ReactiveCapabilityShapePlane greaterOrEqual(double rhs) {
        this.rhs = rhs;
        this.inequalityType = InequalityType.GREATER_OR_EQUAL;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (coeffQ == 1.0) {
            sb.append("Q");
        } else {
            appendIfNonZero(coeffQ, sb, "Q");
        }

        // Append U term (coeffU * U)
        appendIfNonZero(coeffU, sb, "U");

        // Append P term (coeffP * P)
        appendIfNonZero(coeffP, sb, "P");

        // Append relation and rhs
        sb.append(" ").append(inequalityType.equals(InequalityType.LESS_OR_EQUAL) ? '≤' : '≥').append(" ");
        sb.append(String.format("%.3f", rhs));

        return sb.toString();
    }

    private void appendIfNonZero(double coefficient, StringBuilder sb, String variable) {
        if (coefficient == 1.0) {
            sb.append(" + ");
            sb.append(variable);
        } else if (Math.abs(coefficient) > EPSILON) {
            sb.append(coefficient > 0 ? " + " : " - ");
            sb.append(String.format(Locale.ENGLISH, "%.3f", Math.abs(coefficient))).append(" * ").append(variable);
        }
    }

    @Override
    public boolean isLessOrEqual() {
        return inequalityType == InequalityType.LESS_OR_EQUAL;
    }

    @Override
    public boolean isGreaterOrEqual() {
        return inequalityType == InequalityType.GREATER_OR_EQUAL;
    }

    @Override
    public double getCoeffU() {
        return coeffU;
    }

    @Override
    public double getCoeffP() {
        return coeffP;
    }

    @Override
    public double getCoeffQ() {
        return coeffQ;
    }

    @Override
    public double getRhs() {
        return rhs;
    }
}
