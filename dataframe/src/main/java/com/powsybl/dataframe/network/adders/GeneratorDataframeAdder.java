/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dataframe.network.adders;

import com.powsybl.commons.reporter.Reporter;
import com.powsybl.dataframe.SeriesMetadata;
import com.powsybl.dataframe.update.DoubleSeries;
import com.powsybl.dataframe.update.IntSeries;
import com.powsybl.dataframe.update.StringSeries;
import com.powsybl.dataframe.update.UpdatingDataframe;
import com.powsybl.iidm.network.EnergySource;
import com.powsybl.iidm.network.GeneratorAdder;
import com.powsybl.iidm.network.Network;

import java.util.Collections;
import java.util.List;

import static com.powsybl.dataframe.network.adders.NetworkUtils.getVoltageLevelOrThrowWithBusOrBusbarSectionId;
import static com.powsybl.dataframe.network.adders.SeriesUtils.applyBooleanIfPresent;
import static com.powsybl.dataframe.network.adders.SeriesUtils.applyIfPresent;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 * @author Sylvain Leclerc <sylvain.leclerc@rte-france.com>
 */
public class GeneratorDataframeAdder extends AbstractSimpleAdder {

    private static final List<SeriesMetadata> METADATA = List.of(
        SeriesMetadata.stringIndex("id"),
        SeriesMetadata.strings("voltage_level_id"),
        SeriesMetadata.strings("bus_id"),
        SeriesMetadata.strings("connectable_bus_id"),
        SeriesMetadata.ints("node"),
        SeriesMetadata.strings("energy_source"),
        SeriesMetadata.doubles("max_p"),
        SeriesMetadata.doubles("min_p"),
        SeriesMetadata.doubles("target_p"),
        SeriesMetadata.doubles("target_q"),
        SeriesMetadata.doubles("rated_s"),
        SeriesMetadata.doubles("target_v"),
        SeriesMetadata.booleans("voltage_regulator_on")
    );

    @Override
    public List<List<SeriesMetadata>> getMetadata() {
        return Collections.singletonList(METADATA);
    }

    private static class GeneratorSeries extends InjectionSeries {

        private final StringSeries voltageLevels;
        private final DoubleSeries maxP;
        private final DoubleSeries minP;
        private final DoubleSeries targetP;
        private final DoubleSeries targetQ;
        private final DoubleSeries targetV;
        private final DoubleSeries ratedS;
        private final IntSeries voltageRegulatorOn;
        private final StringSeries energySource;
        private final StringSeries busOrBusbarSections;

        GeneratorSeries(UpdatingDataframe dataframe) {
            super(dataframe);
            this.voltageLevels = dataframe.getStrings("voltage_level_id");
            this.maxP = dataframe.getDoubles("max_p");
            this.minP = dataframe.getDoubles("min_p");
            this.targetP = dataframe.getDoubles("target_p");
            this.targetQ = dataframe.getDoubles("target_q");
            this.targetV = dataframe.getDoubles("target_v");
            this.ratedS = dataframe.getDoubles("rated_s");
            this.voltageRegulatorOn = dataframe.getInts("voltage_regulator_on");
            this.energySource = dataframe.getStrings("energy_source");
            this.busOrBusbarSections = dataframe.getStrings("bus_or_busbar_section_id");
        }

        GeneratorAdder createAdder(Network network, int row) {
            GeneratorAdder adder = getVoltageLevelOrThrowWithBusOrBusbarSectionId(network, row, voltageLevels,
                busOrBusbarSections)
                .newGenerator();
            setInjectionAttributes(adder, row);
            applyIfPresent(maxP, row, adder::setMaxP);
            applyIfPresent(minP, row, adder::setMinP);
            applyIfPresent(targetP, row, adder::setTargetP);
            applyIfPresent(targetQ, row, adder::setTargetQ);
            applyIfPresent(targetV, row, adder::setTargetV);
            applyIfPresent(ratedS, row, adder::setRatedS);
            applyBooleanIfPresent(voltageRegulatorOn, row, adder::setVoltageRegulatorOn);
            applyIfPresent(energySource, row, EnergySource.class, adder::setEnergySource);
            return adder;
        }
    }

    @Override
    public void addElements(Network network, UpdatingDataframe dataframe, AdditionStrategy addition,
                            boolean throwException, Reporter reporter) {
        GeneratorSeries series = new GeneratorSeries(dataframe);
        for (int row = 0; row < dataframe.getRowCount(); row++) {
            GeneratorAdder adder = series.createAdder(network, row);
            addition.add(network, dataframe, adder, row, throwException, reporter);
        }
    }

}
