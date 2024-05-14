/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.results;

import com.powsybl.commons.extensions.AbstractExtendable;

import java.util.Objects;

/**
 * provide electrical information on a branch after a security analysis.
 * it belongs to pre and post Contingency results.
 * it is the result of the branch id specified in StateMonitor.
 *
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class BranchResult extends AbstractExtendable<BranchResult> {

    private final String branchId;

    private final double p1;

    private final double q1;

    private final double i1;

    private final double p2;

    private final double q2;

    private final double i2;

    /**
     * Flow transfer from the branch in contingency to the branch with id branchId.
     * <p>
     * It is a ratio computed as : <i>p1i,N-1 - p1i,N / p1j,N</i>, where :
     * <b>p1i,N</b> the active power flow on side 1 of the branch branchId at pre contingency stage.
     * <b>p1i,N-1</b> the active power flow on side 1 of the same branch at post contingency stage.
     * <b>p1j,N</b> the active power flow on side 1 of lost branch j at pre contingency stage.
     * Verifying : <i>p1i,N-1 = P1i,N + flow transfer(j->i) * p1j,N</i>
     */
    private final double flowTransfer;

    public BranchResult(String branchId, double p1, double q1, double i1, double p2, double q2, double i2) {
        this(branchId, p1, q1, i1, p2, q2, i2, Double.NaN);
    }

    public BranchResult(String branchId, double p1, double q1, double i1, double p2, double q2, double i2, double flowTransfer) {
        this.branchId = Objects.requireNonNull(branchId);
        this.p1 = p1;
        this.q1 = q1;
        this.i1 = i1;
        this.p2 = p2;
        this.q2 = q2;
        this.i2 = i2;
        this.flowTransfer = flowTransfer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BranchResult that = (BranchResult) o;
        return Double.compare(that.p1, p1) == 0 &&
            Double.compare(that.q1, q1) == 0 &&
            Double.compare(that.i1, i1) == 0 &&
            Double.compare(that.p2, p2) == 0 &&
            Double.compare(that.q2, q2) == 0 &&
            Double.compare(that.i2, i2) == 0 &&
            Objects.equals(branchId, that.branchId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(branchId, p1, q1, i1, p2, q2, i2);
    }

    public String getBranchId() {
        return branchId;
    }

    public double getI1() {
        return i1;
    }

    public double getI2() {
        return i2;
    }

    public double getP1() {
        return p1;
    }

    public double getP2() {
        return p2;
    }

    public double getQ1() {
        return q1;
    }

    public double getQ2() {
        return q2;
    }

    public double getFlowTransfer() {
        return flowTransfer;
    }

    @Override
    public String toString() {
        return "BranchResult{" +
            "branchId='" + branchId + '\'' +
            ", p1=" + p1 +
            ", q1=" + q1 +
            ", i1=" + i1 +
            ", p2=" + p2 +
            ", q2=" + q2 +
            ", i2=" + i2 +
            ", flowTransfer=" + flowTransfer +
            '}';
    }
}
