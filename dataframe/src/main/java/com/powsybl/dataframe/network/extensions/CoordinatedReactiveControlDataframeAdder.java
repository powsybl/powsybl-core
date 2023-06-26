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
import com.powsybl.dataframe.update.StringSeries;
import com.powsybl.dataframe.update.UpdatingDataframe;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.CoordinatedReactiveControlAdder;

import java.util.Collections;
import java.util.List;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class CoordinatedReactiveControlDataframeAdder extends AbstractSimpleAdder {

    private static final List<SeriesMetadata> METADATA = List.of(
        SeriesMetadata.stringIndex("generator_id"),
        SeriesMetadata.doubles("q_percent")
    );

    @Override
    public List<List<SeriesMetadata>> getMetadata() {
        return Collections.singletonList(METADATA);
    }

    private static class CoordinatedReactiveControlSerie {
        private final StringSeries generatorId;
        private final DoubleSeries qPercent;

        CoordinatedReactiveControlSerie(UpdatingDataframe dataframe) {
            this.generatorId = dataframe.getStrings("generator_id");
            this.qPercent = dataframe.getDoubles("q_percent");
        }

        void create(Network network, int row) {
            String id = this.generatorId.get(row);
            Generator generator = network.getGenerator(id);
            if (generator == null) {
                throw new PowsyblException("Invalid generator id : could not find " + id);
            }
            CoordinatedReactiveControlAdder adder = generator.newExtension(CoordinatedReactiveControlAdder.class);
            SeriesUtils.applyIfPresent(qPercent, row, adder::withQPercent);
            adder.add();
        }
    }

    @Override
    public void addElements(Network network, UpdatingDataframe dataframe) {
        CoordinatedReactiveControlSerie series = new CoordinatedReactiveControlSerie(dataframe);
        for (int row = 0; row < dataframe.getRowCount(); row++) {
            series.create(network, row);
        }
    }
}
