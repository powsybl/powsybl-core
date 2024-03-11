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
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.InjectionObservabilityAdder;

import java.util.Collections;
import java.util.List;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class InjectionObservabilityDataframeAdder extends AbstractSimpleAdder {

    private static final List<SeriesMetadata> METADATA = List.of(
        SeriesMetadata.stringIndex("id"),
        SeriesMetadata.booleans("observable"),
        SeriesMetadata.doubles("p_standard_deviation"),
        SeriesMetadata.booleans("p_redundant"),
        SeriesMetadata.doubles("q_standard_deviation"),
        SeriesMetadata.booleans("q_redundant"),
        SeriesMetadata.doubles("v_standard_deviation"),
        SeriesMetadata.booleans("v_redundant")
    );

    @Override
    public List<List<SeriesMetadata>> getMetadata() {
        return Collections.singletonList(METADATA);
    }

    private static class InjectionObservabilitySeries {

        private final StringSeries idSeries;
        private final IntSeries observable;
        private final DoubleSeries pStandardDeviation;
        private final IntSeries pRedundant;
        private final DoubleSeries qStandardDeviation;
        private final IntSeries qRedundant;
        private final DoubleSeries vStandardDeviation;
        private final IntSeries vRedundant;

        InjectionObservabilitySeries(UpdatingDataframe dataframe) {
            this.idSeries = dataframe.getStrings("id");
            this.observable = dataframe.getInts("observable");
            this.pStandardDeviation = dataframe.getDoubles("p_standard_deviation");
            this.pRedundant = dataframe.getInts("p_redundant");
            this.qStandardDeviation = dataframe.getDoubles("q_standard_deviation");
            this.qRedundant = dataframe.getInts("q_redundant");
            this.vStandardDeviation = dataframe.getDoubles("v_standard_deviation");
            this.vRedundant = dataframe.getInts("v_redundant");
        }

        void create(Network network, int row) {
            String id = this.idSeries.get(row);
            Identifiable identifiable = network.getIdentifiable(id);
            if (identifiable == null) {
                throw new PowsyblException("Invalid injection id : could not find " + id);
            }
            if (!(identifiable instanceof Injection)) {
                throw new PowsyblException(id + " is not an injection");
            }
            Injection injection = (Injection) identifiable;
            InjectionObservabilityAdder adder = (InjectionObservabilityAdder) injection.newExtension(
                InjectionObservabilityAdder.class);
            SeriesUtils.applyBooleanIfPresent(observable, row, adder::withObservable);
            SeriesUtils.applyIfPresent(pStandardDeviation, row, adder::withStandardDeviationP);
            SeriesUtils.applyBooleanIfPresent(pRedundant, row, adder::withRedundantP);
            SeriesUtils.applyIfPresent(qStandardDeviation, row, adder::withStandardDeviationQ);
            SeriesUtils.applyBooleanIfPresent(qRedundant, row, adder::withRedundantQ);
            SeriesUtils.applyIfPresent(vStandardDeviation, row, adder::withStandardDeviationV);
            SeriesUtils.applyBooleanIfPresent(vRedundant, row, adder::withRedundantV);
            adder.add();
        }
    }

    @Override
    public void addElements(Network network, UpdatingDataframe dataframe) {
        InjectionObservabilitySeries series = new InjectionObservabilitySeries(dataframe);
        for (int row = 0; row < dataframe.getRowCount(); row++) {
            series.create(network, row);
        }
    }
}
