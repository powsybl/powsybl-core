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
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevelAdder;

import java.util.Collections;
import java.util.List;

import static com.powsybl.dataframe.network.adders.SeriesUtils.applyIfPresent;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 * @author Sylvain Leclerc <sylvain.leclerc@rte-france.com>
 */
public class VoltageLevelDataframeAdder extends AbstractSimpleAdder {

    private static final List<SeriesMetadata> METADATA = List.of(
        SeriesMetadata.stringIndex("id"),
        SeriesMetadata.strings("substation_id"),
        SeriesMetadata.strings("name"),
        SeriesMetadata.doubles("high_voltage_limit"),
        SeriesMetadata.doubles("low_voltage_limit"),
        SeriesMetadata.doubles("nominal_v"),
        SeriesMetadata.strings("topology_kind")
    );

    @Override
    public List<List<SeriesMetadata>> getMetadata() {
        return Collections.singletonList(METADATA);
    }

    private static class VoltageLevelSeries extends IdentifiableSeries {

        private final StringSeries substations;
        private final StringSeries topologyKind;
        private final DoubleSeries nominalV;
        private final DoubleSeries lowVoltageLimit;
        private final DoubleSeries highVoltageLimit;

        VoltageLevelSeries(UpdatingDataframe dataframe) {
            super(dataframe);
            this.substations = dataframe.getStrings("substation_id");
            this.topologyKind = dataframe.getStrings("topology_kind");
            this.nominalV = dataframe.getDoubles("nominal_v");
            this.lowVoltageLimit = dataframe.getDoubles("low_voltage_limit");
            this.highVoltageLimit = dataframe.getDoubles("high_voltage_limit");
        }

        void create(Network network, int row) {
            VoltageLevelAdder adder;
            if (this.substations != null) {
                adder = NetworkUtils.getSubstationOrThrow(network, substations.get(row))
                    .newVoltageLevel();
            } else {
                adder = network.newVoltageLevel();
            }
            setIdentifiableAttributes(adder, row);
            applyIfPresent(topologyKind, row, TopologyKind.class, adder::setTopologyKind);
            applyIfPresent(nominalV, row, adder::setNominalV);
            applyIfPresent(lowVoltageLimit, row, adder::setLowVoltageLimit);
            applyIfPresent(highVoltageLimit, row, adder::setHighVoltageLimit);
            adder.add();
        }
    }

    @Override
    public void addElements(Network network, UpdatingDataframe dataframe) {
        VoltageLevelSeries series = new VoltageLevelSeries(dataframe);
        for (int row = 0; row < dataframe.getRowCount(); row++) {
            series.create(network, row);
        }
    }
}
