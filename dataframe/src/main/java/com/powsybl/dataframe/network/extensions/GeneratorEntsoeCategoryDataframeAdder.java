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
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.GeneratorEntsoeCategoryAdder;

import java.util.Collections;
import java.util.List;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@soft.it>
 */
public class GeneratorEntsoeCategoryDataframeAdder extends AbstractSimpleAdder {

    private static final List<SeriesMetadata> METADATA = List.of(
        SeriesMetadata.stringIndex("id"),
        SeriesMetadata.ints("code")
    );

    @Override
    public List<List<SeriesMetadata>> getMetadata() {
        return Collections.singletonList(METADATA);
    }

    private static class GeneratorEntsoeCategorySeries {

        private final StringSeries idSeries;
        private final IntSeries code;

        GeneratorEntsoeCategorySeries(UpdatingDataframe dataframe) {
            this.idSeries = dataframe.getStrings("id");
            this.code = dataframe.getInts("code");
        }

        void create(Network network, int row) {
            String id = this.idSeries.get(row);
            Generator g = network.getGenerator(id);
            if (g == null) {
                throw new PowsyblException("Invalid generator id : could not find " + id);
            }
            var adder = g.newExtension(GeneratorEntsoeCategoryAdder.class);
            SeriesUtils.applyIfPresent(code, row, adder::withCode);
            adder.add();
        }
    }

    @Override
    public void addElements(Network network, UpdatingDataframe dataframe) {
        GeneratorEntsoeCategorySeries series = new GeneratorEntsoeCategorySeries(dataframe);
        for (int row = 0; row < dataframe.getRowCount(); row++) {
            series.create(network, row);
        }
    }
}
