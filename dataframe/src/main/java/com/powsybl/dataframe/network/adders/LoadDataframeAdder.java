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
import com.powsybl.iidm.network.LoadAdder;
import com.powsybl.iidm.network.LoadType;
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
public class LoadDataframeAdder extends AbstractSimpleAdder {

    private static final List<SeriesMetadata> METADATA = List.of(
        SeriesMetadata.stringIndex("id"),
        SeriesMetadata.strings("voltage_level_id"),
        SeriesMetadata.strings("bus_id"),
        SeriesMetadata.strings("connectable_bus_id"),
        SeriesMetadata.ints("node"),
        SeriesMetadata.strings("name"),
        SeriesMetadata.strings("type"),
        SeriesMetadata.doubles("p0"),
        SeriesMetadata.doubles("q0")
    );

    @Override
    public List<List<SeriesMetadata>> getMetadata() {
        return Collections.singletonList(METADATA);
    }

    private static class LoadSeries extends InjectionSeries {

        private final StringSeries voltageLevels;
        private final DoubleSeries p0;
        private final DoubleSeries q0;
        private final StringSeries type;
        private final StringSeries busOrBusbarSections;

        LoadSeries(UpdatingDataframe dataframe) {
            super(dataframe);
            this.voltageLevels = dataframe.getStrings("voltage_level_id");
            this.p0 = dataframe.getDoubles("p0");
            this.q0 = dataframe.getDoubles("q0");
            this.type = dataframe.getStrings("type");
            this.busOrBusbarSections = dataframe.getStrings("bus_or_busbar_section_id");
        }

        LoadAdder createAdder(Network network, int row) {
            LoadAdder adder = getVoltageLevelOrThrowWithBusOrBusbarSectionId(network, row, voltageLevels,
                busOrBusbarSections)
                .newLoad();
            setInjectionAttributes(adder, row);
            applyIfPresent(p0, row, adder::setP0);
            applyIfPresent(q0, row, adder::setQ0);
            applyIfPresent(type, row, LoadType.class, adder::setLoadType);
            return adder;
        }
    }

    @Override
    public void addElements(Network network, UpdatingDataframe dataframe, AdditionStrategy addition,
                            boolean throwException, Reporter reporter) {
        LoadSeries series = new LoadSeries(dataframe);
        for (int row = 0; row < dataframe.getRowCount(); row++) {
            LoadAdder adder = series.createAdder(network, row);
            addition.add(network, dataframe, adder, row, throwException, reporter);
        }
    }
}
