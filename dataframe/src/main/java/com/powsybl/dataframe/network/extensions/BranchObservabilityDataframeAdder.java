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
import com.powsybl.dataframe.update.IntSeries;
import com.powsybl.dataframe.update.StringSeries;
import com.powsybl.dataframe.update.UpdatingDataframe;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.BranchObservabilityAdder;

import java.util.Collections;
import java.util.List;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class BranchObservabilityDataframeAdder extends AbstractSimpleAdder {

    private static final List<SeriesMetadata> METADATA = List.of(
        SeriesMetadata.stringIndex("id"),
        SeriesMetadata.booleans("observable"),
        SeriesMetadata.doubles("p1_standard_deviation"),
        SeriesMetadata.booleans("p1_redundant"),
        SeriesMetadata.doubles("p2_standard_deviation"),
        SeriesMetadata.booleans("p2_redundant"),
        SeriesMetadata.doubles("q1_standard_deviation"),
        SeriesMetadata.booleans("q1_redundant"),
        SeriesMetadata.doubles("q2_standard_deviation"),
        SeriesMetadata.booleans("q2_redundant")
    );

    @Override
    public List<List<SeriesMetadata>> getMetadata() {
        return Collections.singletonList(METADATA);
    }

    private static class BranchObservabilitySeries {

        private final StringSeries idSeries;
        private final IntSeries observable;
        private final DoubleSeries p1StandardDeviation;
        private final IntSeries p1Redundant;
        private final DoubleSeries p2StandardDeviation;
        private final IntSeries p2Redundant;
        private final DoubleSeries q1StandardDeviation;
        private final IntSeries q1Redundant;
        private final DoubleSeries q2StandardDeviation;
        private final IntSeries q2Redundant;

        BranchObservabilitySeries(UpdatingDataframe dataframe) {
            this.idSeries = dataframe.getStrings("id");
            this.observable = dataframe.getInts("observable");
            this.p1StandardDeviation = dataframe.getDoubles("p1_standard_deviation");
            this.p1Redundant = dataframe.getInts("p1_redundant");
            this.p2StandardDeviation = dataframe.getDoubles("p2_standard_deviation");
            this.p2Redundant = dataframe.getInts("p2_redundant");
            this.q1StandardDeviation = dataframe.getDoubles("q1_standard_deviation");
            this.q1Redundant = dataframe.getInts("q1_redundant");
            this.q2StandardDeviation = dataframe.getDoubles("q2_standard_deviation");
            this.q2Redundant = dataframe.getInts("q2_redundant");
        }

        void create(Network network, int row) {
            String id = this.idSeries.get(row);
            Branch branch = network.getBranch(id);
            if (branch == null) {
                throw new PowsyblException("Invalid branch id : could not find " + id);
            }
            BranchObservabilityAdder adder = (BranchObservabilityAdder) branch.newExtension(
                BranchObservabilityAdder.class);
            SeriesUtils.applyBooleanIfPresent(observable, row, adder::withObservable);
            SeriesUtils.applyIfPresent(p1StandardDeviation, row, adder::withStandardDeviationP1);
            SeriesUtils.applyBooleanIfPresent(p1Redundant, row, adder::withRedundantP1);
            SeriesUtils.applyIfPresent(p2StandardDeviation, row, adder::withStandardDeviationP2);
            SeriesUtils.applyBooleanIfPresent(p2Redundant, row, adder::withRedundantP2);
            SeriesUtils.applyIfPresent(q1StandardDeviation, row, adder::withStandardDeviationQ1);
            SeriesUtils.applyBooleanIfPresent(q1Redundant, row, adder::withRedundantQ1);
            SeriesUtils.applyIfPresent(q2StandardDeviation, row, adder::withStandardDeviationQ2);
            SeriesUtils.applyBooleanIfPresent(q2Redundant, row, adder::withRedundantQ2);
            adder.add();
        }
    }

    @Override
    public void addElements(Network network, UpdatingDataframe dataframe) {
        BranchObservabilitySeries series = new BranchObservabilitySeries(dataframe);
        for (int row = 0; row < dataframe.getRowCount(); row++) {
            series.create(network, row);
        }
    }
}
