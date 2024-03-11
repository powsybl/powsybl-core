/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dataframe.network.adders;

import com.powsybl.commons.reporter.Reporter;
import com.powsybl.dataframe.SeriesMetadata;
import com.powsybl.dataframe.update.DoubleSeries;
import com.powsybl.dataframe.update.StringSeries;
import com.powsybl.dataframe.update.UpdatingDataframe;
import com.powsybl.iidm.network.LccConverterStationAdder;
import com.powsybl.iidm.network.Network;

import java.util.Collections;
import java.util.List;

import static com.powsybl.dataframe.network.adders.NetworkUtils.getVoltageLevelOrThrowWithBusOrBusbarSectionId;
import static com.powsybl.dataframe.network.adders.SeriesUtils.applyIfPresent;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 * @author Sylvain Leclerc <sylvain.leclerc@rte-france.com>
 */
public class LccStationDataframeAdder extends AbstractSimpleAdder {

    private static final List<SeriesMetadata> METADATA = List.of(
        SeriesMetadata.stringIndex("id"),
        SeriesMetadata.strings("voltage_level_id"),
        SeriesMetadata.strings("bus_id"),
        SeriesMetadata.strings("connectable_bus_id"),
        SeriesMetadata.ints("node"),
        SeriesMetadata.strings("name"),
        SeriesMetadata.doubles("power_factor"),
        SeriesMetadata.doubles("loss_factor")
    );

    @Override
    public List<List<SeriesMetadata>> getMetadata() {
        return Collections.singletonList(METADATA);
    }

    private static class LccStationSeries extends InjectionSeries {

        private final StringSeries voltageLevels;
        private final DoubleSeries powerFactors;
        private final DoubleSeries lossFactors;
        private final StringSeries busOrBusbarSections;

        LccStationSeries(UpdatingDataframe dataframe) {
            super(dataframe);
            this.voltageLevels = dataframe.getStrings("voltage_level_id");
            this.powerFactors = dataframe.getDoubles("power_factor");
            this.lossFactors = dataframe.getDoubles("loss_factor");
            this.busOrBusbarSections = dataframe.getStrings("bus_or_busbar_section_id");
        }

        LccConverterStationAdder createAdder(Network network, int row) {
            LccConverterStationAdder adder = getVoltageLevelOrThrowWithBusOrBusbarSectionId(network, row, voltageLevels,
                busOrBusbarSections)
                .newLccConverterStation();
            setInjectionAttributes(adder, row);
            applyIfPresent(lossFactors, row, f -> adder.setLossFactor((float) f));
            applyIfPresent(powerFactors, row, f -> adder.setPowerFactor((float) f));
            return adder;
        }
    }

    @Override
    public void addElements(Network network, UpdatingDataframe dataframe, AdditionStrategy additionStrategy,
                            boolean throwException, Reporter reporter) {
        LccStationSeries series = new LccStationSeries(dataframe);
        for (int row = 0; row < dataframe.getRowCount(); row++) {
            LccConverterStationAdder adder = series.createAdder(network, row);
            additionStrategy.add(network, dataframe, adder, row, throwException, reporter);
        }
    }
}
