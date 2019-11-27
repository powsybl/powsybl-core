/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.HvdcConverterStation;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.Terminal;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
abstract class AbstractHvdcConverterStationAdapter<I extends HvdcConverterStation<I>> extends AbstractIdentifiableAdapter<I> implements HvdcConverterStation<I> {

    protected AbstractHvdcConverterStationAdapter(I delegate, MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public Terminal getTerminal() {
        return getIndex().getTerminal(getDelegate().getTerminal());
    }

    @Override
    public HvdcLine getHvdcLine() {
        return getIndex().getHvdcLine(getDelegate().getHvdcLine());
    }

    @Override
    public I setLossFactor(float lossFactor) {
        getDelegate().setLossFactor(lossFactor);
        return (I) this;
    }

    @Override
    public List<? extends Terminal> getTerminals() {
        return getDelegate().getTerminals().stream()
                .map(getIndex()::getTerminal)
                .collect(Collectors.toList());
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public ConnectableType getType() {
        return getDelegate().getType();
    }

    @Override
    public HvdcType getHvdcType() {
        return getDelegate().getHvdcType();
    }

    @Override
    public float getLossFactor() {
        return getDelegate().getLossFactor();
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public void remove() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }
}
