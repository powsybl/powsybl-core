/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dataframe.network.adders;

import com.powsybl.commons.PowsyblException;
import com.powsybl.dataframe.SeriesMetadata;
import com.powsybl.dataframe.update.IntSeries;
import com.powsybl.dataframe.update.StringSeries;
import com.powsybl.dataframe.update.UpdatingDataframe;
import com.powsybl.iidm.network.BusbarSectionAdder;
import com.powsybl.iidm.network.Network;

import java.util.Collections;
import java.util.List;

import static com.powsybl.dataframe.network.adders.NetworkUtils.getVoltageLevelOrThrow;
import static com.powsybl.dataframe.network.adders.SeriesUtils.applyIfPresent;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 * @author Sylvain Leclerc <sylvain.leclerc@rte-france.com>
 */
public class BusBarDataframeAdder extends AbstractSimpleAdder {

    private static final List<SeriesMetadata> METADATA = List.of(
        SeriesMetadata.stringIndex("id"),
        SeriesMetadata.strings("voltage_level_id"),
        SeriesMetadata.ints("node"),
        SeriesMetadata.strings("name")
    );

    @Override
    public List<List<SeriesMetadata>> getMetadata() {
        return Collections.singletonList(METADATA);
    }

    private static class BusBarSeries extends IdentifiableSeries {

        private final StringSeries voltageLevels;
        private final IntSeries nodes;

        BusBarSeries(UpdatingDataframe dataframe) {
            super(dataframe);
            this.voltageLevels = dataframe.getStrings("voltage_level_id");
            if (voltageLevels == null) {
                throw new PowsyblException("voltage_level_id is missing");
            }
            this.nodes = dataframe.getInts("node");
        }

        void createBusBar(Network network, int row) {
            BusbarSectionAdder adder = getVoltageLevelOrThrow(network, voltageLevels.get(row))
                .getNodeBreakerView()
                .newBusbarSection();
            setIdentifiableAttributes(adder, row);
            applyIfPresent(nodes, row, adder::setNode);
            adder.add();
        }
    }

    @Override
    public void addElements(Network network, UpdatingDataframe dataframe) {
        BusBarSeries series = new BusBarSeries(dataframe);
        for (int row = 0; row < dataframe.getRowCount(); row++) {
            series.createBusBar(network, row);
        }
    }
}
