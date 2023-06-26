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
import com.powsybl.dataframe.update.StringSeries;
import com.powsybl.dataframe.update.UpdatingDataframe;
import com.powsybl.entsoe.util.EntsoeAreaAdder;
import com.powsybl.entsoe.util.EntsoeGeographicalCode;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;

import java.util.Collections;
import java.util.List;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@soft.it>
 */
public class EntsoeAreaDataframeAdder extends AbstractSimpleAdder {

    private static final List<SeriesMetadata> METADATA = List.of(
        SeriesMetadata.stringIndex("id"),
        SeriesMetadata.strings("code")
    );

    @Override
    public List<List<SeriesMetadata>> getMetadata() {
        return Collections.singletonList(METADATA);
    }

    private static class EntsoeAreaSeries {

        private final StringSeries id;
        private final StringSeries code;

        EntsoeAreaSeries(UpdatingDataframe dataframe) {
            this.id = dataframe.getStrings("id");
            this.code = dataframe.getStrings("code");
        }

        void create(Network network, int row) {
            String id = this.id.get(row);
            Substation s = network.getSubstation(id);
            if (s == null) {
                throw new PowsyblException("Invalid substation id : could not find " + id);
            }
            var adder = s.newExtension(EntsoeAreaAdder.class);
            SeriesUtils.applyIfPresent(code, row, c -> adder.withCode(EntsoeGeographicalCode.valueOf(c)));
            adder.add();
        }
    }

    @Override
    public void addElements(Network network, UpdatingDataframe dataframe) {
        EntsoeAreaSeries series = new EntsoeAreaSeries(dataframe);
        for (int row = 0; row < dataframe.getRowCount(); row++) {
            series.create(network, row);
        }
    }
}
