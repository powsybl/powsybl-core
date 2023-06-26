/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dataframe.network.adders;

import com.powsybl.commons.reporter.Reporter;
import com.powsybl.dataframe.SeriesMetadata;
import com.powsybl.dataframe.update.StringSeries;
import com.powsybl.dataframe.update.UpdatingDataframe;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TieLineAdder;

import java.util.Collections;
import java.util.List;

import static com.powsybl.dataframe.network.adders.SeriesUtils.applyIfPresent;

/**
 * @author Coline Piloquet <coline.piloquet@rte-france.com>
 */
public class TieLineDataframeAdder extends AbstractSimpleAdder {

    private static final List<SeriesMetadata> METADATA = List.of(
        SeriesMetadata.stringIndex("id"),
        SeriesMetadata.strings("name"),
        SeriesMetadata.strings("dangling_line1_id"),
        SeriesMetadata.strings("dangling_line2_id")
    );

    @Override
    public List<List<SeriesMetadata>> getMetadata() {
        return Collections.singletonList(METADATA);
    }

    private static class TieLineSeries extends IdentifiableSeries {

        private final StringSeries danglingLine1Ids;
        private final StringSeries danglingLine2Ids;

        TieLineSeries(UpdatingDataframe dataframe) {
            super(dataframe);
            this.danglingLine1Ids = dataframe.getStrings("dangling_line1_id");
            this.danglingLine2Ids = dataframe.getStrings("dangling_line2_id");
        }

        TieLineAdder create(Network network, int row) {
            TieLineAdder adder = network.newTieLine();
            setIdentifiableAttributes(adder, row);
            applyIfPresent(danglingLine1Ids, row, adder::setDanglingLine1);
            applyIfPresent(danglingLine2Ids, row, adder::setDanglingLine2);
            return adder;
        }
    }

    @Override
    public void addElements(Network network, UpdatingDataframe dataframe, AdditionStrategy additionStrategy,
                            boolean throwException, Reporter reporter) {
        TieLineSeries series = new TieLineSeries(dataframe);
        for (int row = 0; row < dataframe.getRowCount(); row++) {
            TieLineAdder adder = series.create(network, row);
            adder.add();
        }
    }
}
