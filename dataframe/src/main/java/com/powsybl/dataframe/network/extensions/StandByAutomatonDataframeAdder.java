/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dataframe.network.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.dataframe.SeriesMetadata;
import com.powsybl.dataframe.network.adders.AbstractSimpleAdder;
import com.powsybl.dataframe.network.adders.SeriesUtils;
import com.powsybl.dataframe.update.DoubleSeries;
import com.powsybl.dataframe.update.IntSeries;
import com.powsybl.dataframe.update.StringSeries;
import com.powsybl.dataframe.update.UpdatingDataframe;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.extensions.StandbyAutomatonAdder;

import java.util.Collections;
import java.util.List;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class StandByAutomatonDataframeAdder extends AbstractSimpleAdder {

    private static final List<SeriesMetadata> METADATA = List.of(
        SeriesMetadata.stringIndex("id"),
        SeriesMetadata.booleans("standby"),
        SeriesMetadata.doubles("b0"),
        SeriesMetadata.doubles("low_voltage_threshold"),
        SeriesMetadata.doubles("low_voltage_setpoint"),
        SeriesMetadata.doubles("high_voltage_threshold"),
        SeriesMetadata.doubles("high_voltage_setpoint")
    );

    private static class StandByAutomatonSeries {
        private final StringSeries idSeries;
        private final IntSeries standBy;
        private final DoubleSeries b0;
        private final DoubleSeries lowVoltageThreshold;
        private final DoubleSeries lowVoltageSetpoint;
        private final DoubleSeries highVoltageThreshold;
        private final DoubleSeries highVoltageSetpoint;

        StandByAutomatonSeries(UpdatingDataframe dataframe) {
            this.idSeries = dataframe.getStrings("id");
            this.standBy = dataframe.getInts("standby");
            this.b0 = dataframe.getDoubles("b0");
            this.lowVoltageThreshold = dataframe.getDoubles("low_voltage_threshold");
            this.lowVoltageSetpoint = dataframe.getDoubles("low_voltage_setpoint");
            this.highVoltageThreshold = dataframe.getDoubles("high_voltage_threshold");
            this.highVoltageSetpoint = dataframe.getDoubles("high_voltage_setpoint");
        }

        void create(Network network, int row) {
            String id = this.idSeries.get(row);
            StaticVarCompensator staticVarCompensator = network.getStaticVarCompensator(id);
            if (staticVarCompensator == null) {
                throw new PowsyblException("Invalid static var compensator id : could not find " + id);
            }
            StandbyAutomatonAdder adder = staticVarCompensator.newExtension(StandbyAutomatonAdder.class);
            SeriesUtils.applyIfPresent(b0, row, x -> adder.withB0((float) x));
            SeriesUtils.applyIfPresent(lowVoltageThreshold, row, adder::withLowVoltageThreshold);
            SeriesUtils.applyIfPresent(lowVoltageSetpoint, row, adder::withLowVoltageSetpoint);
            SeriesUtils.applyIfPresent(highVoltageThreshold, row, adder::withHighVoltageThreshold);
            SeriesUtils.applyIfPresent(highVoltageSetpoint, row, adder::withHighVoltageSetpoint);
            SeriesUtils.applyBooleanIfPresent(standBy, row, adder::withStandbyStatus);
            adder.add();
        }
    }

    @Override
    public void addElements(Network network, UpdatingDataframe dataframe) {
        StandByAutomatonSeries series = new StandByAutomatonSeries(dataframe);
        for (int row = 0; row < dataframe.getRowCount(); row++) {
            series.create(network, row);
        }
    }

    @Override
    public List<List<SeriesMetadata>> getMetadata() {
        return Collections.singletonList(METADATA);
    }
}
