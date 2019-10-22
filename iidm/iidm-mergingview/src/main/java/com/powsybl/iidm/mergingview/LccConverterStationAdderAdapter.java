/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.LccConverterStationAdder;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class LccConverterStationAdderAdapter extends AbstractAdapter<LccConverterStationAdder> implements LccConverterStationAdder {

    protected LccConverterStationAdderAdapter(final LccConverterStationAdder delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public LccConverterStationAdderAdapter setLossFactor(final float lossFactor) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public LccConverterStationAdderAdapter setNode(final int node) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public LccConverterStationAdderAdapter setBus(final String bus) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public LccConverterStationAdderAdapter setConnectableBus(final String connectableBus) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public LccConverterStationAdderAdapter setId(final String id) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public LccConverterStationAdderAdapter setEnsureIdUnicity(final boolean ensureIdUnicity) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public LccConverterStationAdderAdapter setName(final String name) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public LccConverterStationAdderAdapter setPowerFactor(final float powerFactor) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public LccConverterStationAdapter add() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }
}
