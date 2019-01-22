/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.reducer;

import com.powsybl.iidm.network.*;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class DefaultNetworkReducerObserver implements NetworkReducerObserver {

    @Override
    public void substationRemoved(Substation substation) {
        // Nothing to do
    }

    @Override
    public void voltageLevelRemoved(VoltageLevel voltageLevel) {
        // Nothing to do
    }

    @Override
    public void lineReduced(Line line, Injection injection) {
        // Nothing to do
    }

    @Override
    public void lineRemoved(Line line) {
        // Nothing to do
    }

    @Override
    public void transformerReduced(TwoWindingsTransformer transformer, Injection injection) {
        // Nothing to do
    }

    @Override
    public void transformerRemoved(TwoWindingsTransformer transformer) {
        // Nothing to do
    }

    @Override
    public void transformerReduced(ThreeWindingsTransformer transformer, Injection injection) {
        // Nothing to do
    }

    @Override
    public void transformerRemoved(ThreeWindingsTransformer transformer) {
        // Nothing to do
    }

    @Override
    public void hvdcLineReduced(HvdcLine hvdcLine, Injection injection) {
        // Nothing to do
    }

    @Override
    public void hvdcLineRemoved(HvdcLine hvdcLine) {
        // Nothing to do
    }
}
