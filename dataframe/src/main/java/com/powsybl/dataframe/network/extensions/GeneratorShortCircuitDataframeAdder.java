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
import com.powsybl.dataframe.update.DoubleSeries;
import com.powsybl.dataframe.update.StringSeries;
import com.powsybl.dataframe.update.UpdatingDataframe;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.GeneratorShortCircuitAdder;

import java.util.Collections;
import java.util.List;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class GeneratorShortCircuitDataframeAdder extends AbstractSimpleAdder {

    private static final List<SeriesMetadata> METADATA = List.of(
        SeriesMetadata.stringIndex("id"),
        SeriesMetadata.doubles("direct_sub_trans_x"),
        SeriesMetadata.doubles("direct_trans_x"),
        SeriesMetadata.doubles("step_up_transformer_x")
    );

    private static class GeneratorShortCircuitSeries {

        private final StringSeries id;
        private final DoubleSeries directSubTransX;
        private final DoubleSeries directTransX;
        private final DoubleSeries stepUpTransformerX;

        GeneratorShortCircuitSeries(UpdatingDataframe dataframe) {
            this.id = dataframe.getStrings("id");
            this.directSubTransX = dataframe.getDoubles("direct_sub_trans_x");
            this.directTransX = dataframe.getDoubles("direct_trans_x");
            this.stepUpTransformerX = dataframe.getDoubles("step_up_transformer_x");
        }

        void create(Network network, int row) {
            String id = this.id.get(row);
            Generator g = network.getGenerator(id);
            if (g == null) {
                throw new PowsyblException("Invalid generator id : could not find " + id);
            }
            var adder = g.newExtension(GeneratorShortCircuitAdder.class);
            SeriesUtils.applyIfPresent(directSubTransX, row, adder::withDirectSubtransX);
            SeriesUtils.applyIfPresent(directTransX, row, adder::withDirectTransX);
            SeriesUtils.applyIfPresent(stepUpTransformerX, row, adder::withStepUpTransformerX);
            adder.add();
        }
    }

    @Override
    public void addElements(Network network, UpdatingDataframe dataframe) {
        GeneratorShortCircuitSeries series = new GeneratorShortCircuitSeries(dataframe);
        for (int row = 0; row < dataframe.getRowCount(); row++) {
            series.create(network, row);
        }
    }

    @Override
    public List<List<SeriesMetadata>> getMetadata() {
        return Collections.singletonList(METADATA);
    }
}
