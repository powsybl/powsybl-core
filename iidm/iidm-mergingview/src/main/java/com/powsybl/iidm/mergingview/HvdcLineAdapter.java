/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.HvdcConverterStation;
import com.powsybl.iidm.network.HvdcLine;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class HvdcLineAdapter extends AbstractIdentifiableAdapter<HvdcLine> implements HvdcLine {

    HvdcLineAdapter(final HvdcLine delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public HvdcConverterStation<?> getConverterStation1() {
        return getIndex().getHvdcConverterStation(getDelegate().getConverterStation1());
    }

    @Override
    public HvdcConverterStation<?> getConverterStation2() {
        return getIndex().getHvdcConverterStation(getDelegate().getConverterStation2());
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public ConvertersMode getConvertersMode() {
        return getDelegate().getConvertersMode();
    }

    @Override
    public HvdcLine setConvertersMode(final ConvertersMode mode) {
        getDelegate().setConvertersMode(mode);
        return this;
    }

    @Override
    public double getR() {
        return getDelegate().getR();
    }

    @Override
    public HvdcLine setR(final double r) {
        getDelegate().setR(r);
        return this;
    }

    @Override
    public double getNominalV() {
        return getDelegate().getNominalV();
    }

    @Override
    public HvdcLine setNominalV(final double nominalV) {
        getDelegate().setNominalV(nominalV);
        return this;
    }

    @Override
    public double getActivePowerSetpoint() {
        return getDelegate().getActivePowerSetpoint();
    }

    @Override
    public HvdcLine setActivePowerSetpoint(final double activePowerSetpoint) {
        getDelegate().setActivePowerSetpoint(activePowerSetpoint);
        return this;
    }

    @Override
    public double getMaxP() {
        return getDelegate().getMaxP();
    }

    @Override
    public HvdcLine setMaxP(final double maxP) {
        getDelegate().setMaxP(maxP);
        return this;
    }

    @Override
    public void remove() {
        getDelegate().remove();
    }
}
