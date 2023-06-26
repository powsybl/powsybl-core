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
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.LoadDetailAdder;

import java.util.Collections;
import java.util.List;

import static com.powsybl.dataframe.network.extensions.LoadDetailDataframeProvider.*;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class LoadDetailDataframeAdder extends AbstractSimpleAdder {

    private static final List<SeriesMetadata> METADATA = List.of(
        SeriesMetadata.stringIndex("id"),
        SeriesMetadata.doubles(FIXED_P),
        SeriesMetadata.doubles(VARIABLE_P),
        SeriesMetadata.doubles(FIXED_Q),
        SeriesMetadata.doubles(VARIABLE_Q)
    );

    @Override
    public List<List<SeriesMetadata>> getMetadata() {
        return Collections.singletonList(METADATA);
    }

    private static class LoadDetailSeries {
        private final StringSeries id;
        private final DoubleSeries fixedActivePower;
        private final DoubleSeries variableActivePower;
        private final DoubleSeries fixedReactivePower;
        private final DoubleSeries variableReactivePower;

        LoadDetailSeries(UpdatingDataframe dataframe) {
            this.id = dataframe.getStrings("id");
            this.fixedActivePower = dataframe.getDoubles(FIXED_P);
            this.variableActivePower = dataframe.getDoubles(VARIABLE_P);
            this.fixedReactivePower = dataframe.getDoubles(FIXED_Q);
            this.variableReactivePower = dataframe.getDoubles(VARIABLE_Q);
        }

        void create(Network network, int row) {
            String id = this.id.get(row);
            Load l = network.getLoad(id);
            if (l == null) {
                throw new PowsyblException("Invalid load id : could not find " + id);
            }
            LoadDetailAdder adder = l.newExtension(LoadDetailAdder.class);
            SeriesUtils.applyIfPresent(fixedActivePower, row, adder::withFixedActivePower);
            SeriesUtils.applyIfPresent(variableActivePower, row, adder::withVariableActivePower);
            SeriesUtils.applyIfPresent(fixedReactivePower, row, adder::withFixedReactivePower);
            SeriesUtils.applyIfPresent(variableReactivePower, row, adder::withVariableReactivePower);
            adder.add();
        }
    }

    @Override
    public void addElements(Network network, UpdatingDataframe dataframe) {
        LoadDetailSeries series = new LoadDetailSeries(dataframe);
        for (int row = 0; row < dataframe.getRowCount(); row++) {
            series.create(network, row);
        }
    }
}
