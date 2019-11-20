/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.HvdcConverterStation;
import com.powsybl.iidm.network.Terminal;

import java.util.List;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
abstract class AbstractHvdcConverterStationAdapter<I extends HvdcConverterStation<I>> extends AbstractIdentifiableAdapter<I> implements HvdcConverterStation<I> {

    AbstractHvdcConverterStationAdapter(I delegate, MergingViewIndex index) {
        super(delegate, index);
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public TerminalAdapter getTerminal() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public ConnectableType getType() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public List<? extends Terminal> getTerminals() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public void remove() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public HvdcLineAdapter getHvdcLine() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public HvdcType getHvdcType() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public float getLossFactor() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public I setLossFactor(float lossFactor) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }
}
