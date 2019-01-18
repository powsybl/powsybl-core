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
public class NetworkReducerObserverImpl extends DefaultNetworkReducerObserver {

    private int substationRemovedCount = 0;

    private int voltageLevelRemovedCount = 0;

    private int lineRemovedCount = 0;

    private int lineReducedCount = 0;

    private int twoWindingsTransformerRemovedCount = 0;

    private int twoWindingsTransformerReducedCount = 0;

    private int threeWindingsTransformerRemovedCount = 0;

    private int threeWindingsTransformerReducedCount = 0;

    private int hvdcLineRemovedCount = 0;

    private int hvdcLineReducedCount = 0;

    int getSubstationRemovedCount() {
        return substationRemovedCount;
    }

    int getVoltageLevelRemovedCount() {
        return voltageLevelRemovedCount;
    }

    int getLineRemovedCount() {
        return lineRemovedCount;
    }

    int getLineReducedCount() {
        return lineReducedCount;
    }

    int getTwoWindingsTransformerRemovedCount() {
        return twoWindingsTransformerRemovedCount;
    }

    int getTwoWindingsTransformerReducedCount() {
        return twoWindingsTransformerReducedCount;
    }

    int getThreeWindingsTransformerRemovedCount() {
        return threeWindingsTransformerRemovedCount;
    }

    int getThreeWindingsTransformerReducedCount() {
        return threeWindingsTransformerReducedCount;
    }

    int getHvdcLineRemovedCount() {
        return hvdcLineRemovedCount;
    }

    int getHvdcLineReducedCount() {
        return hvdcLineReducedCount;
    }

    @Override
    public void substationRemoved(Substation substation) {
        super.substationRemoved(substation);

        substationRemovedCount++;
    }

    @Override
    public void voltageLevelRemoved(VoltageLevel voltageLevel) {
        super.voltageLevelRemoved(voltageLevel);

        voltageLevelRemovedCount++;
    }

    @Override
    public void lineReduced(Line line, Injection injection) {
        super.lineReduced(line, injection);

        lineReducedCount++;
    }

    @Override
    public void lineRemoved(Line line) {
        super.lineRemoved(line);

        lineRemovedCount++;
    }

    @Override
    public void transformerReduced(TwoWindingsTransformer transformer, Injection injection) {
        super.transformerReduced(transformer, injection);

        twoWindingsTransformerReducedCount++;
    }

    @Override
    public void transformerRemoved(TwoWindingsTransformer transformer) {
        super.transformerRemoved(transformer);

        twoWindingsTransformerRemovedCount++;
    }

    @Override
    public void transformerReduced(ThreeWindingsTransformer transformer, Injection injection) {
        super.transformerReduced(transformer, injection);

        threeWindingsTransformerReducedCount++;
    }

    @Override
    public void transformerRemoved(ThreeWindingsTransformer transformer) {
        super.transformerRemoved(transformer);

        threeWindingsTransformerRemovedCount++;
    }

    @Override
    public void hvdcLineReduced(HvdcLine hvdcLine, Injection injection) {
        super.hvdcLineReduced(hvdcLine, injection);

        hvdcLineReducedCount++;
    }

    @Override
    public void hvdcLineRemoved(HvdcLine hvdcLine) {
        super.hvdcLineRemoved(hvdcLine);

        hvdcLineRemovedCount++;
    }

}
