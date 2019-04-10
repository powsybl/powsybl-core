/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.immutable;

import com.powsybl.iidm.network.HvdcConverterStation;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.Network;

/**
 * An immutable {@link HvdcLine}.
 * It is a read-only object, any modification on it will throw a runtime exception.
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
final class ImmutableHvdcLine extends AbstractImmutableIdentifiable<HvdcLine> implements HvdcLine {

    ImmutableHvdcLine(HvdcLine identifiable, ImmutableCacheIndex cache) {
        super(identifiable, cache);
    }

    /**
     * {@inheritDoc}
     * @return Returns an {@link ImmutableNetwork}
     */
    @Override
    public Network getNetwork() {
        return cache.getNetwork();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConvertersMode getConvertersMode() {
        return identifiable.getConvertersMode();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public HvdcLine setConvertersMode(ConvertersMode mode) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getR() {
        return identifiable.getR();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public HvdcLine setR(double r) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getNominalV() {
        return identifiable.getNominalV();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public HvdcLine setNominalV(double nominalV) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getActivePowerSetpoint() {
        return identifiable.getActivePowerSetpoint();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public HvdcLine setActivePowerSetpoint(double activePowerSetpoint) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getMaxP() {
        return identifiable.getMaxP();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public HvdcLine setMaxP(double maxP) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     * @return Returns an immutable {@link HvdcConverterStation}
     */
    @Override
    public HvdcConverterStation<?> getConverterStation1() {
        return cache.getHvdcConverterStation(identifiable.getConverterStation1());
    }

    /**
     * {@inheritDoc}
     * @return Returns an immutable {@link HvdcConverterStation}
     */
    @Override
    public HvdcConverterStation<?> getConverterStation2() {
        return cache.getHvdcConverterStation(identifiable.getConverterStation2());
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public void remove() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }
}
