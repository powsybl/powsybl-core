/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.HvdcConverterStation;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.Network;

import java.util.Objects;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public final class ImmutableHvdcLine extends AbstractImmutableIdentifiable<HvdcLine> implements HvdcLine {

    private final ImmutableCacheIndex cache;

    ImmutableHvdcLine(HvdcLine identifiable, ImmutableCacheIndex cache) {
        super(identifiable);
        this.cache = Objects.requireNonNull(cache);
    }

    @Override
    public Network getNetwork() {
        return cache.getNetwork();
    }

    @Override
    public ConvertersMode getConvertersMode() {
        return identifiable.getConvertersMode();
    }

    @Override
    public HvdcLine setConvertersMode(ConvertersMode mode) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getR() {
        return identifiable.getR();
    }

    @Override
    public HvdcLine setR(double r) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getNominalV() {
        return identifiable.getNominalV();
    }

    @Override
    public HvdcLine setNominalV(double nominalV) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getActivePowerSetpoint() {
        return identifiable.getActivePowerSetpoint();
    }

    @Override
    public HvdcLine setActivePowerSetpoint(double activePowerSetpoint) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getMaxP() {
        return identifiable.getMaxP();
    }

    @Override
    public HvdcLine setMaxP(double maxP) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public HvdcConverterStation<?> getConverterStation1() {
        return cache.getHvdcConverterStation(identifiable.getConverterStation1());
    }

    @Override
    public HvdcConverterStation<?> getConverterStation2() {
        return cache.getHvdcConverterStation(identifiable.getConverterStation2());
    }

    @Override
    public void remove() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }
}
