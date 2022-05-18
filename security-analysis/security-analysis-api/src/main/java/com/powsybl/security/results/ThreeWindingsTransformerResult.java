/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.results;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.powsybl.commons.extensions.AbstractExtendable;

import java.util.Objects;

/**
 *
 * provide electrical information on a three windings transformer
 * after a security analysis. it belongs to pre and post Contingency results.
 * it is the result of the three windings transformer id specified in StateMonitor.
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class ThreeWindingsTransformerResult extends AbstractExtendable<ThreeWindingsTransformerResult> {

    private final String threeWindingsTransformerId;

    private final double p1;

    private final double q1;

    private final double i1;

    private final double p2;

    private final double q2;

    private final double i2;

    private final double p3;

    private final double q3;

    private final double i3;

    public ThreeWindingsTransformerResult(@JsonProperty("threeWindingsTransformerId") String threeWindingsTransformerId, @JsonProperty("p1") double p1, @JsonProperty("q1") double q1,
                                          @JsonProperty("i1") double i1, @JsonProperty("p2") double p2, @JsonProperty("q2") double q2,
                                          @JsonProperty("i2") double i2, @JsonProperty("p3") double p3, @JsonProperty("q3") double q3, @JsonProperty("i3") double i3) {
        this.threeWindingsTransformerId = Objects.requireNonNull(threeWindingsTransformerId);
        this.p1 = Objects.requireNonNull(p1);
        this.q1 = Objects.requireNonNull(q1);
        this.i1 = Objects.requireNonNull(i1);
        this.p2 = Objects.requireNonNull(p2);
        this.q2 = Objects.requireNonNull(q2);
        this.i2 = Objects.requireNonNull(i2);
        this.p3 = Objects.requireNonNull(p3);
        this.q3 = Objects.requireNonNull(q3);
        this.i3 = Objects.requireNonNull(i3);
    }

    public String getThreeWindingsTransformerId() {
        return threeWindingsTransformerId;
    }

    public double getI1() {
        return i1;
    }

    public double getP1() {
        return p1;
    }

    public double getQ1() {
        return q1;
    }

    public double getI2() {
        return i2;
    }

    public double getP2() {
        return p2;
    }

    public double getQ2() {
        return q2;
    }

    public double getI3() {
        return i3;
    }

    public double getP3() {
        return p3;
    }

    public double getQ3() {
        return q3;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ThreeWindingsTransformerResult that = (ThreeWindingsTransformerResult) o;
        return Double.compare(that.p1, p1) == 0 &&
            Double.compare(that.q1, q1) == 0 &&
            Double.compare(that.i1, i1) == 0 &&
            Double.compare(that.p2, p2) == 0 &&
            Double.compare(that.q2, q2) == 0 &&
            Double.compare(that.i2, i2) == 0 &&
            Double.compare(that.p3, p3) == 0 &&
            Double.compare(that.q3, q3) == 0 &&
            Double.compare(that.i3, i3) == 0 &&
            Objects.equals(threeWindingsTransformerId, that.threeWindingsTransformerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(threeWindingsTransformerId, p1, q1, i1, p2, q2, i2, p3, q3, i3);
    }

    @Override
    public String toString() {
        return "ThreeWindingsTransformerResult{" +
            "threeWindingsTransformerId='" + threeWindingsTransformerId + '\'' +
            ", p1=" + p1 +
            ", q1=" + q1 +
            ", i1=" + i1 +
            ", p2=" + p2 +
            ", q2=" + q2 +
            ", i2=" + i2 +
            ", p3=" + p3 +
            ", q3=" + q3 +
            ", i3=" + i3 +
            '}';
    }
}
