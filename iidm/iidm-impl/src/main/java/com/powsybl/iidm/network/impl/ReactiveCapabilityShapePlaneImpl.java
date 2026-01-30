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
 * The inequality is of the form: Q + alpha * U + beta * P  {≤, ≥}  gamma.
 * P = Active Power (MW)
 * Q = Reactive Power (MVaR)
 * U = Voltage (KV)
 * </pre>
 *
 * @author Fabrice Buscaylet {@literal <fabrice.buscaylet at artelys.com>}
 */
public final class ReactiveCapabilityShapePlaneImpl implements ReactiveCapabilityShapePlane {

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
       GREATER_OR_EQUAL     // ≥  gamma
    }

    /**
     * The inequaility types
     */
    private InequalityType inequalityType;

    /**
     * private constructor for a ReactiveCapabilityShapePlane.
     *
     * @param alphaU    The coefficient for U.
     * @param betaP     The coefficient for P.
     */
    private ReactiveCapabilityShapePlaneImpl(double alphaU, double betaP) {
        this.alpha = alphaU;
        this.beta = betaP;
    }

    /**
     * Builder for a ReactiveCapabilityShapePlane.
     *
     * @param alphaU    The coefficient for U.
     * @param betaP     The coefficient for P.
     */
    public static ReactiveCapabilityShapePlane build(double alphaU, double betaP) {
        return new ReactiveCapabilityShapePlaneImpl(alphaU, betaP);
    }

    /**
     * Set the hyperplane constraint to a less or equal ≤ inequality with gamma right hand side
     * @param gamma the gamma right hand side
     * @return this
     */
    @Override
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
    @Override
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
        appendIfNonZero(alpha, sb, " * U");

        // Append P term (beta * P)
        appendIfNonZero(beta, sb, " * P");

        // Append relation and gamma
        sb.append(" ").append(inequalityType.equals(InequalityType.LESS_OR_EQUAL) ? '≤' : '≥').append(" ");
        sb.append(String.format("%.3f", gamma));

        return sb.toString();
    }

    private void appendIfNonZero(double coefficient, StringBuilder sb, String str) {
        if (Math.abs(coefficient) > EPSILON) { // Check if beta is non-zero
            sb.append(coefficient > 0 ? " + " : " - ");
            sb.append(String.format(Locale.ENGLISH, "%.3f", Math.abs(coefficient))).append(str);
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
    public double getAlpha() {
        return alpha;
    }

    @Override
    public double getBeta() {
        return beta;
    }

    @Override
    public double getGamma() {
        return gamma;
    }
}
