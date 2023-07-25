/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dataframe.network.adders;

import com.powsybl.commons.PowsyblException;
import com.powsybl.dataframe.SeriesMetadata;
import com.powsybl.dataframe.update.DoubleSeries;
import com.powsybl.dataframe.update.StringSeries;
import com.powsybl.dataframe.update.UpdatingDataframe;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ReactiveLimitsHolder;

import java.util.Collections;
import java.util.List;

import static com.powsybl.dataframe.network.adders.NetworkUtils.getIdentifiableOrThrow;
import static com.powsybl.dataframe.network.adders.SeriesUtils.getRequiredDoubles;
import static com.powsybl.dataframe.network.adders.SeriesUtils.getRequiredStrings;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@soft.it>
 */
public class MinMaxReactiveLimitsDataframeAdder implements NetworkElementAdder {

    private static final List<SeriesMetadata> METADATA = List.of(
        SeriesMetadata.stringIndex("id"),
        SeriesMetadata.doubles("min_q"),
        SeriesMetadata.doubles("max_q")
    );

    @Override
    public List<List<SeriesMetadata>> getMetadata() {
        return Collections.singletonList(METADATA);
    }

    private static final class MinMaxReactiveLimitsSeries {

        private final StringSeries elementIds;
        private final DoubleSeries minQs;
        private final DoubleSeries maxQs;

        MinMaxReactiveLimitsSeries(UpdatingDataframe dataframe) {
            this.elementIds = getRequiredStrings(dataframe, "id");
            this.minQs = getRequiredDoubles(dataframe, "min_q");
            this.maxQs = getRequiredDoubles(dataframe, "max_q");
        }

        public StringSeries getElementIds() {
            return elementIds;
        }

        public DoubleSeries getMinQs() {
            return minQs;
        }

        public DoubleSeries getMaxQs() {
            return maxQs;
        }
    }

    @Override
    public void addElements(Network network, List<UpdatingDataframe> dataframes) {
        UpdatingDataframe primaryTable = dataframes.get(0);
        MinMaxReactiveLimitsSeries series = new MinMaxReactiveLimitsSeries(primaryTable);
        for (int i = 0; i < primaryTable.getRowCount(); i++) {
            String elementId = series.getElementIds().get(i);
            double minQ = series.getMinQs().get(i);
            double maxQ = series.getMaxQs().get(i);
            createLimits(network, elementId, minQ, maxQ);
        }
    }

    private static void createLimits(Network network, String elementId, double minQ, double maxQ) {
        Identifiable<?> identifiable = getIdentifiableOrThrow(network, elementId);
        if (identifiable instanceof ReactiveLimitsHolder reactiveLimitsHolder) {
            reactiveLimitsHolder.newMinMaxReactiveLimits().setMinQ(minQ).setMaxQ(maxQ).add();
        } else {
            throw new PowsyblException("Element " + elementId + " does not have reactive limits.");
        }
    }
}
