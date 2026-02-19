/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.reducer;

import com.powsybl.iidm.network.*;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public class NetworkReducerObserverImpl extends DefaultNetworkReducerObserver {

    private int substationRemovedCount = 0;

    private int voltageLevelRemovedCount = 0;

    private int lineRemovedCount = 0;

    private int lineReplacedCount = 0;

    private int tieLineRemovedCount = 0;

    private int twoWindingsTransformerRemovedCount = 0;

    private int twoWindingsTransformerReplacedCount = 0;

    private int threeWindingsTransformerRemovedCount = 0;

    private int threeWindingsTransformerReplacedCount = 0;

    private int hvdcLineRemovedCount = 0;

    private int hvdcLineReplacedCount = 0;

    int getSubstationRemovedCount() {
        return substationRemovedCount;
    }

    int getVoltageLevelRemovedCount() {
        return voltageLevelRemovedCount;
    }

    int getLineRemovedCount() {
        return lineRemovedCount;
    }

    int getLineReplacedCount() {
        return lineReplacedCount;
    }

    public int getTieLineRemovedCount() {
        return tieLineRemovedCount;
    }

    int getTwoWindingsTransformerRemovedCount() {
        return twoWindingsTransformerRemovedCount;
    }

    int getTwoWindingsTransformerReplacedCount() {
        return twoWindingsTransformerReplacedCount;
    }

    int getThreeWindingsTransformerRemovedCount() {
        return threeWindingsTransformerRemovedCount;
    }

    int getThreeWindingsTransformerReplacedCount() {
        return threeWindingsTransformerReplacedCount;
    }

    int getHvdcLineRemovedCount() {
        return hvdcLineRemovedCount;
    }

    int getHvdcLineReplacedCount() {
        return hvdcLineReplacedCount;
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
    public void lineReplaced(Line line, Injection injection) {
        super.lineReplaced(line, injection);

        lineReplacedCount++;
    }

    @Override
    public void lineRemoved(Line line) {
        super.lineRemoved(line);

        lineRemovedCount++;
    }

    @Override
    public void tieLineRemoved(TieLine tieLine) {
        super.tieLineRemoved(tieLine);

        tieLineRemovedCount++;
    }

    @Override
    public void transformerReplaced(TwoWindingsTransformer transformer, Injection injection) {
        super.transformerReplaced(transformer, injection);

        twoWindingsTransformerReplacedCount++;
    }

    @Override
    public void transformerRemoved(TwoWindingsTransformer transformer) {
        super.transformerRemoved(transformer);

        twoWindingsTransformerRemovedCount++;
    }

    @Override
    public void transformerReplaced(ThreeWindingsTransformer transformer, Injection injection) {
        super.transformerReplaced(transformer, injection);

        threeWindingsTransformerReplacedCount++;
    }

    @Override
    public void transformerRemoved(ThreeWindingsTransformer transformer) {
        super.transformerRemoved(transformer);

        threeWindingsTransformerRemovedCount++;
    }

    @Override
    public void hvdcLineReplaced(HvdcLine hvdcLine, Injection injection) {
        super.hvdcLineReplaced(hvdcLine, injection);

        hvdcLineReplacedCount++;
    }

    @Override
    public void hvdcLineRemoved(HvdcLine hvdcLine) {
        super.hvdcLineRemoved(hvdcLine);

        hvdcLineRemovedCount++;
    }

}
