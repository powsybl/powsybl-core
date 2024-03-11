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
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.HvdcAngleDroopActivePowerControlAdder;

import java.util.Collections;
import java.util.List;

import static com.powsybl.dataframe.network.adders.SeriesUtils.applyBooleanIfPresent;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@soft.it>
 */
public class HvdcAngleDroopActivePowerControlDataframeAdder extends AbstractSimpleAdder {

    private static final List<SeriesMetadata> METADATA = List.of(
        SeriesMetadata.stringIndex("id"),
        SeriesMetadata.doubles("droop"),
        SeriesMetadata.doubles("p0"),
        SeriesMetadata.booleans("enabled")
    );

    @Override
    public List<List<SeriesMetadata>> getMetadata() {
        return Collections.singletonList(METADATA);
    }

    private static class HvdcAngleDroopActivePowerControlSeries {

        private final StringSeries idSeries;
        private final DoubleSeries droop;
        private final DoubleSeries p0;
        private final IntSeries enabled;

        HvdcAngleDroopActivePowerControlSeries(UpdatingDataframe dataframe) {
            this.idSeries = dataframe.getStrings("id");
            this.droop = dataframe.getDoubles("droop");
            this.p0 = dataframe.getDoubles("p0");
            this.enabled = dataframe.getInts("enabled");
        }

        void create(Network network, int row) {
            String id = this.idSeries.get(row);
            HvdcLine l = network.getHvdcLine(id);
            if (l == null) {
                throw new PowsyblException("Invalid hvdc line id : could not find " + id);
            }
            var adder = l.newExtension(HvdcAngleDroopActivePowerControlAdder.class);
            SeriesUtils.applyIfPresent(droop, row, x -> adder.withDroop((float) x));
            SeriesUtils.applyIfPresent(p0, row, x -> adder.withP0((float) x));
            applyBooleanIfPresent(enabled, row, adder::withEnabled);
            adder.add();
        }
    }

    @Override
    public void addElements(Network network, UpdatingDataframe dataframe) {
        HvdcAngleDroopActivePowerControlSeries series = new HvdcAngleDroopActivePowerControlSeries(dataframe);
        for (int row = 0; row < dataframe.getRowCount(); row++) {
            series.create(network, row);
        }
    }
}
