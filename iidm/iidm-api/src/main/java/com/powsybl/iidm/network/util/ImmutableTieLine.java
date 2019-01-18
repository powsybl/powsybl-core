/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.TieLine;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public final class ImmutableTieLine extends ImmutableLine implements TieLine {

    private static final Map<TieLine, ImmutableTieLine> CACHE = new HashMap<>();

    TieLine tieLine;

    private ImmutableTieLine(TieLine identifiable) {
        super(identifiable);
        this.tieLine = identifiable;
    }

    static ImmutableTieLine ofNullable(TieLine line) {
        return null == line ? null : CACHE.computeIfAbsent(line, k -> new ImmutableTieLine(line));
    }

    @Override
    public String getUcteXnodeCode() {
        return tieLine.getUcteXnodeCode();
    }

    @Override
    public HalfLine getHalf1() {
        return new ImmutableHalfLine(tieLine.getHalf1());
    }

    @Override
    public HalfLine getHalf2() {
        return new ImmutableHalfLine(tieLine.getHalf2());
    }

    public static class ImmutableHalfLine implements HalfLine {

        HalfLine hl;

        ImmutableHalfLine(HalfLine hl) {
            this.hl = Objects.requireNonNull(hl);
        }

        @Override
        public String getId() {
            return hl.getId();
        }

        @Override
        public String getName() {
            return hl.getName();
        }

        @Override
        public double getXnodeP() {
            return hl.getXnodeP();
        }

        @Override
        public HalfLine setXnodeP(double p) {
            throw ImmutableNetwork.createUnmodifiableNetworkException();
        }

        @Override
        public double getXnodeQ() {
            return hl.getXnodeQ();
        }

        @Override
        public HalfLine setXnodeQ(double q) {
            throw ImmutableNetwork.createUnmodifiableNetworkException();
        }

        @Override
        public double getR() {
            return hl.getR();
        }

        @Override
        public HalfLine setR(double r) {
            throw ImmutableNetwork.createUnmodifiableNetworkException();
        }

        @Override
        public double getX() {
            return hl.getX();
        }

        @Override
        public HalfLine setX(double x) {
            throw ImmutableNetwork.createUnmodifiableNetworkException();
        }

        @Override
        public double getG1() {
            return hl.getG1();
        }

        @Override
        public HalfLine setG1(double g1) {
            throw ImmutableNetwork.createUnmodifiableNetworkException();
        }

        @Override
        public double getG2() {
            return hl.getG2();
        }

        @Override
        public HalfLine setG2(double g2) {
            throw ImmutableNetwork.createUnmodifiableNetworkException();
        }

        @Override
        public double getB1() {
            return hl.getB1();
        }

        @Override
        public HalfLine setB1(double b1) {
            throw ImmutableNetwork.createUnmodifiableNetworkException();
        }

        @Override
        public double getB2() {
            return hl.getB2();
        }

        @Override
        public HalfLine setB2(double b2) {
            throw ImmutableNetwork.createUnmodifiableNetworkException();
        }
    }
}
