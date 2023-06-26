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
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.IdentifiableShortCircuitAdder;

import java.util.Collections;
import java.util.List;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class IdentifiableShortCircuitDataframeAdder extends AbstractSimpleAdder {

    private static final List<SeriesMetadata> METADATA = List.of(
        SeriesMetadata.stringIndex("id"),
        SeriesMetadata.strings("element_type"),
        SeriesMetadata.doubles("ip_min"),
        SeriesMetadata.doubles("ip_max")
    );

    private static class IdentifiableShortCircuitSeries {

        private final StringSeries id;
        private final DoubleSeries ipMin;
        private final DoubleSeries ipMax;

        IdentifiableShortCircuitSeries(UpdatingDataframe dataframe) {
            this.id = dataframe.getStrings("id");
            this.ipMin = dataframe.getDoubles("ip_min");
            this.ipMax = dataframe.getDoubles("ip_max");
        }

        void create(Network network, int row) {
            String id = this.id.get(row);
            Identifiable identifiable = network.getIdentifiable(id);
            if (identifiable == null) {
                throw new PowsyblException("Invalid identifiable id : could not find " + id);
            }
            IdentifiableShortCircuitAdder adder = (IdentifiableShortCircuitAdder) identifiable.newExtension(
                IdentifiableShortCircuitAdder.class);
            SeriesUtils.applyIfPresent(ipMin, row, adder::withIpMin);
            SeriesUtils.applyIfPresent(ipMax, row, adder::withIpMax);
            adder.add();
        }
    }

    @Override
    public void addElements(Network network, UpdatingDataframe dataframe) {
        IdentifiableShortCircuitSeries series = new IdentifiableShortCircuitSeries(dataframe);
        for (int row = 0; row < dataframe.getRowCount(); row++) {
            series.create(network, row);
        }
    }

    @Override
    public List<List<SeriesMetadata>> getMetadata() {
        return Collections.singletonList(METADATA);
    }
}
