/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dataframe.network.adders;

import com.powsybl.dataframe.SeriesMetadata;
import com.powsybl.dataframe.update.DoubleSeries;
import com.powsybl.dataframe.update.StringSeries;
import com.powsybl.dataframe.update.UpdatingDataframe;
import com.powsybl.iidm.network.*;

import java.util.Collections;
import java.util.List;

import static com.powsybl.dataframe.network.adders.SeriesUtils.applyIfPresent;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 * @author Sylvain Leclerc <sylvain.leclerc@rte-france.com>
 */
public class HvdcDataframeAdder extends AbstractSimpleAdder {

    private static final List<SeriesMetadata> METADATA = List.of(
        SeriesMetadata.stringIndex("id"),
        SeriesMetadata.strings("name"),
        SeriesMetadata.strings("converter_station1_id"),
        SeriesMetadata.strings("converter_station2_id"),
        SeriesMetadata.doubles("max_p"),
        SeriesMetadata.strings("converters_mode"),
        SeriesMetadata.doubles("target_p"),
        SeriesMetadata.doubles("r"),
        SeriesMetadata.doubles("nominal_v")
    );

    @Override
    public List<List<SeriesMetadata>> getMetadata() {
        return Collections.singletonList(METADATA);
    }

    private static class HvdcLineSeries extends IdentifiableSeries {

        private final StringSeries converterStations1;
        private final StringSeries converterStations2;
        private final DoubleSeries maxP;
        private final StringSeries convertersModes;
        private final DoubleSeries targetP;
        private final DoubleSeries r;
        private final DoubleSeries nominalV;

        HvdcLineSeries(UpdatingDataframe dataframe) {
            super(dataframe);
            this.converterStations1 = dataframe.getStrings("converter_station1_id");
            this.converterStations2 = dataframe.getStrings("converter_station2_id");
            this.maxP = dataframe.getDoubles("max_p");
            this.convertersModes = dataframe.getStrings("converters_mode");
            this.targetP = dataframe.getDoubles("target_p");
            this.r = dataframe.getDoubles("r");
            this.nominalV = dataframe.getDoubles("nominal_v");
        }

        void create(Network network, int row) {
            HvdcLineAdder adder = network.newHvdcLine();
            setIdentifiableAttributes(adder, row);
            applyIfPresent(converterStations1, row, adder::setConverterStationId1);
            applyIfPresent(converterStations2, row, adder::setConverterStationId2);
            applyIfPresent(maxP, row, adder::setMaxP);
            applyIfPresent(convertersModes, row, HvdcLine.ConvertersMode.class, adder::setConvertersMode);
            applyIfPresent(targetP, row, adder::setActivePowerSetpoint);
            applyIfPresent(r, row, adder::setR);
            applyIfPresent(nominalV, row, adder::setNominalV);
            adder.add();
        }
    }

    @Override
    public void addElements(Network network, UpdatingDataframe dataframe) {
        HvdcLineSeries series = new HvdcLineSeries(dataframe);
        for (int row = 0; row < dataframe.getRowCount(); row++) {
            series.create(network, row);
        }
    }
}
