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
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.HvdcOperatorActivePowerRangeAdder;

import java.util.Collections;
import java.util.List;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@soft.it>
 */
public class HvdcOperatorActivePowerRangeDataframeAdder extends AbstractSimpleAdder {

    private static final List<SeriesMetadata> METADATA = List.of(
        SeriesMetadata.stringIndex("id"),
        SeriesMetadata.doubles("opr_from_cs1_to_cs2"),
        SeriesMetadata.doubles("opr_from_cs2_to_cs1")
    );

    @Override
    public List<List<SeriesMetadata>> getMetadata() {
        return Collections.singletonList(METADATA);
    }

    private static class HvdcOperatorActivePowerRangeSeries {

        private final StringSeries id;
        private final DoubleSeries oprFromCS1toCS2;
        private final DoubleSeries oprFromCS2toCS1;

        HvdcOperatorActivePowerRangeSeries(UpdatingDataframe dataframe) {
            this.id = dataframe.getStrings("id");
            this.oprFromCS1toCS2 = dataframe.getDoubles("opr_from_cs1_to_cs2");
            this.oprFromCS2toCS1 = dataframe.getDoubles("opr_from_cs2_to_cs1");
        }

        void create(Network network, int row) {
            String id = this.id.get(row);
            HvdcLine l = network.getHvdcLine(id);
            if (l == null) {
                throw new PowsyblException("Invalid hvdc line id : could not find " + id);
            }
            var adder = l.newExtension(HvdcOperatorActivePowerRangeAdder.class);
            SeriesUtils.applyIfPresent(oprFromCS1toCS2, row, x -> adder.withOprFromCS1toCS2((float) x));
            SeriesUtils.applyIfPresent(oprFromCS2toCS1, row, x -> adder.withOprFromCS2toCS1((float) x));
            adder.add();
        }
    }

    @Override
    public void addElements(Network network, UpdatingDataframe dataframe) {
        HvdcOperatorActivePowerRangeSeries series = new HvdcOperatorActivePowerRangeSeries(dataframe);
        for (int row = 0; row < dataframe.getRowCount(); row++) {
            series.create(network, row);
        }
    }
}
