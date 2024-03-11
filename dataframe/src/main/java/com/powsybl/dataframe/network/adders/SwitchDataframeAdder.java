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
import com.powsybl.iidm.network.*;

import java.util.Collections;
import java.util.List;

import static com.powsybl.dataframe.network.adders.NetworkUtils.getVoltageLevelOrThrow;
import static com.powsybl.dataframe.network.adders.SeriesUtils.applyBooleanIfPresent;
import static com.powsybl.dataframe.network.adders.SeriesUtils.applyIfPresent;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 * @author Sylvain Leclerc <sylvain.leclerc@rte-france.com>
 */
public class SwitchDataframeAdder extends AbstractSimpleAdder {

    private static final List<SeriesMetadata> METADATA = List.of(
        SeriesMetadata.stringIndex("id"),
        SeriesMetadata.strings("voltage_level_id"),
        SeriesMetadata.strings("bus1_id"),
        SeriesMetadata.strings("bus2_id"),
        SeriesMetadata.ints("node1"),
        SeriesMetadata.ints("node2"),
        SeriesMetadata.strings("name"),
        SeriesMetadata.strings("kind"),
        SeriesMetadata.booleans("open"),
        SeriesMetadata.booleans("retained"),
        SeriesMetadata.booleans("fictitious")
    );

    @Override
    public List<List<SeriesMetadata>> getMetadata() {
        return Collections.singletonList(METADATA);
    }

    private static class SwitchSeries extends IdentifiableSeries {

        private final StringSeries voltageLevels;
        private final StringSeries buses1;
        private final StringSeries buses2;
        private final IntSeries nodes1;
        private final IntSeries nodes2;
        private final StringSeries kinds;
        private final IntSeries open;
        private final IntSeries retained;
        private final IntSeries fictitious;

        SwitchSeries(UpdatingDataframe dataframe) {
            super(dataframe);
            this.voltageLevels = dataframe.getStrings("voltage_level_id");
            if (this.voltageLevels == null) {
                throw new PowsyblException("voltage_level_id is missing");
            }
            this.buses1 = dataframe.getStrings("bus1_id");
            this.buses2 = dataframe.getStrings("bus2_id");
            this.nodes1 = dataframe.getInts("node1");
            this.nodes2 = dataframe.getInts("node2");
            this.kinds = dataframe.getStrings("kind");
            this.open = dataframe.getInts("open");
            this.retained = dataframe.getInts("retained");
            this.fictitious = dataframe.getInts("fictitious");
        }

        void create(Network network, int row) {
            VoltageLevel vl = getVoltageLevelOrThrow(network, voltageLevels.get(row));
            TopologyKind kind = vl.getTopologyKind();
            if (kind == TopologyKind.NODE_BREAKER) {
                VoltageLevel.NodeBreakerView.SwitchAdder adder = vl.getNodeBreakerView().newSwitch();
                setIdentifiableAttributes(adder, row);
                applyIfPresent(kinds, row, adder::setKind);
                applyIfPresent(nodes1, row, adder::setNode1);
                applyIfPresent(nodes2, row, adder::setNode2);
                applyBooleanIfPresent(open, row, adder::setOpen);
                applyBooleanIfPresent(retained, row, adder::setRetained);
                applyBooleanIfPresent(fictitious, row, adder::setFictitious);
                adder.add();
            } else if (kind == TopologyKind.BUS_BREAKER) {
                VoltageLevel.BusBreakerView.SwitchAdder adder = vl.getBusBreakerView().newSwitch();
                setIdentifiableAttributes(adder, row);
                applyIfPresent(buses1, row, adder::setBus1);
                applyIfPresent(buses2, row, adder::setBus2);
                applyBooleanIfPresent(open, row, adder::setOpen);
                applyBooleanIfPresent(fictitious, row, adder::setFictitious);
                adder.add();
            }
        }
    }

    @Override
    public void addElements(Network network, UpdatingDataframe dataframe) {
        SwitchSeries series = new SwitchSeries(dataframe);
        for (int row = 0; row < dataframe.getRowCount(); row++) {
            series.create(network, row);
        }
    }
}
