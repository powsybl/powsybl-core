/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dataframe.network.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.dataframe.SeriesMetadata;
import com.powsybl.dataframe.network.adders.AbstractSimpleAdder;
import com.powsybl.dataframe.network.adders.SeriesUtils;
import com.powsybl.dataframe.update.IntSeries;
import com.powsybl.dataframe.update.StringSeries;
import com.powsybl.dataframe.update.UpdatingDataframe;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.BusbarSectionPositionAdder;

import java.util.Collections;
import java.util.List;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class BusBarSectionPositionDataframeAdder extends AbstractSimpleAdder {

    private static final List<SeriesMetadata> METADATA = List.of(
        SeriesMetadata.stringIndex("id"),
        SeriesMetadata.ints("busbar_index"),
        SeriesMetadata.ints("section_index")
    );

    @Override
    public List<List<SeriesMetadata>> getMetadata() {
        return Collections.singletonList(METADATA);
    }

    private static class BusbarSectionPositionSerie {
        private final StringSeries id;
        private final IntSeries busbarIndex;
        private final IntSeries sectionIndex;

        BusbarSectionPositionSerie(UpdatingDataframe dataframe) {
            this.id = dataframe.getStrings("id");
            this.busbarIndex = dataframe.getInts("busbar_index");
            this.sectionIndex = dataframe.getInts("section_index");
        }

        void create(Network network, int row) {
            String id = this.id.get(row);
            BusbarSection busbarSection = network.getBusbarSection(id);
            if (busbarSection == null) {
                throw new PowsyblException("Invalid busbar id : could not find " + id);
            }
            BusbarSectionPositionAdder adder = busbarSection.newExtension(BusbarSectionPositionAdder.class);
            SeriesUtils.applyIfPresent(busbarIndex, row, adder::withBusbarIndex);
            SeriesUtils.applyIfPresent(sectionIndex, row, adder::withSectionIndex);
            adder.add();
        }
    }

    @Override
    public void addElements(Network network, UpdatingDataframe dataframe) {
        BusbarSectionPositionSerie series = new BusbarSectionPositionSerie(dataframe);
        for (int row = 0; row < dataframe.getRowCount(); row++) {
            series.create(network, row);
        }
    }
}
