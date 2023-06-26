/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dataframe.network.adders;

import com.powsybl.dataframe.SeriesMetadata;
import com.powsybl.dataframe.update.StringSeries;
import com.powsybl.dataframe.update.UpdatingDataframe;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.SubstationAdder;

import java.util.Collections;
import java.util.List;

import static com.powsybl.dataframe.network.adders.SeriesUtils.applyIfPresent;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 * @author Sylvain Leclerc <sylvain.leclerc@rte-france.com>
 */
public class SubstationDataframeAdder extends AbstractSimpleAdder {

    private static final List<SeriesMetadata> METADATA = List.of(
        SeriesMetadata.stringIndex("id"),
        SeriesMetadata.strings("name"),
        SeriesMetadata.strings("country"),
        SeriesMetadata.strings("tso"),
        SeriesMetadata.strings("TSO")
    );

    private static class SubstationSeries extends IdentifiableSeries {
        private final StringSeries countries;
        private final StringSeries tsos;

        SubstationSeries(UpdatingDataframe dataframe) {
            super(dataframe);
            this.countries = dataframe.getStrings("country");
            if (dataframe.getStrings("tso") != null) {
                this.tsos = dataframe.getStrings("tso");
            } else {
                this.tsos = dataframe.getStrings("TSO");
            }
        }

        void createSubstation(Network network, int row) {
            SubstationAdder adder = network.newSubstation();
            setIdentifiableAttributes(adder, row);
            applyIfPresent(countries, row, Country.class, adder::setCountry);
            applyIfPresent(tsos, row, adder::setTso);
            adder.add();
        }
    }

    @Override
    public List<List<SeriesMetadata>> getMetadata() {
        return Collections.singletonList(METADATA);
    }

    @Override
    public void addElements(Network network, UpdatingDataframe dataframe) {
        SubstationSeries series = new SubstationSeries(dataframe);
        for (int row = 0; row < dataframe.getRowCount(); row++) {
            series.createSubstation(network, row);
        }
    }
}
