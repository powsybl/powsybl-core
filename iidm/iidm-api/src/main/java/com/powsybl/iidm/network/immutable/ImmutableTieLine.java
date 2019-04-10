/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.immutable;

import com.powsybl.iidm.network.TieLine;

import java.util.Objects;

/**
 * An immutable {@link TieLine}.
 * It is a read-only object, any modification on it will throw a runtime exception.
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
final class ImmutableTieLine extends ImmutableLine implements TieLine {

    private  final TieLine tieLine;

    private final ImmutableHalfLine hl1;

    private final ImmutableHalfLine hl2;

    ImmutableTieLine(TieLine identifiable, ImmutableCacheIndex cache) {
        super(identifiable, cache);
        this.tieLine = identifiable;
        hl1 = new ImmutableHalfLine(identifiable.getHalf1());
        hl2 = new ImmutableHalfLine(identifiable.getHalf2());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUcteXnodeCode() {
        return tieLine.getUcteXnodeCode();
    }

    /**
     * {@inheritDoc}
     * @return Returns an {@link ImmutableHalfLine}
     */
    @Override
    public HalfLine getHalf1() {
        return hl1;
    }

    /**
     * {@inheritDoc}
     * @return Returns an {@link ImmutableHalfLine}
     */
    @Override
    public HalfLine getHalf2() {
        return hl2;
    }

    /**
     * An immutable {@link com.powsybl.iidm.network.TieLine.HalfLine}
     * It is a read-only object, any modification on it will throw a runtime exception.
     */
    public static class ImmutableHalfLine implements HalfLine {

        HalfLine hl;

        ImmutableHalfLine(HalfLine hl) {
            this.hl = Objects.requireNonNull(hl);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getId() {
            return hl.getId();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getName() {
            return hl.getName();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getXnodeP() {
            return hl.getXnodeP();
        }

        /**
         * Mutative operation is not allowed. It will throw an exception in runtime.
         * @return
         */
        @Override
        public HalfLine setXnodeP(double p) {
            throw ImmutableNetwork.createUnmodifiableNetworkException();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getXnodeQ() {
            return hl.getXnodeQ();
        }

        /**
         * Mutative operation is not allowed. It will throw an exception in runtime.
         * @return
         */
        @Override
        public HalfLine setXnodeQ(double q) {
            throw ImmutableNetwork.createUnmodifiableNetworkException();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getR() {
            return hl.getR();
        }

        /**
         * Mutative operation is not allowed. It will throw an exception in runtime.
         * @return
         */
        @Override
        public HalfLine setR(double r) {
            throw ImmutableNetwork.createUnmodifiableNetworkException();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getX() {
            return hl.getX();
        }

        /**
         * Mutative operation is not allowed. It will throw an exception in runtime.
         * @return
         */
        @Override
        public HalfLine setX(double x) {
            throw ImmutableNetwork.createUnmodifiableNetworkException();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getG1() {
            return hl.getG1();
        }

        /**
         * Mutative operation is not allowed. It will throw an exception in runtime.
         * @return
         */
        @Override
        public HalfLine setG1(double g1) {
            throw ImmutableNetwork.createUnmodifiableNetworkException();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getG2() {
            return hl.getG2();
        }

        /**
         * Mutative operation is not allowed. It will throw an exception in runtime.
         * @return
         */
        @Override
        public HalfLine setG2(double g2) {
            throw ImmutableNetwork.createUnmodifiableNetworkException();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getB1() {
            return hl.getB1();
        }

        /**
         * Mutative operation is not allowed. It will throw an exception in runtime.
         * @return
         */
        @Override
        public HalfLine setB1(double b1) {
            throw ImmutableNetwork.createUnmodifiableNetworkException();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getB2() {
            return hl.getB2();
        }

        /**
         * Mutative operation is not allowed. It will throw an exception in runtime.
         * @return
         */
        @Override
        public HalfLine setB2(double b2) {
            throw ImmutableNetwork.createUnmodifiableNetworkException();
        }
    }
}
