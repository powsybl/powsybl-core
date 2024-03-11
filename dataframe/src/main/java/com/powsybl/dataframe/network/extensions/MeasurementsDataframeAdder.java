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
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.Measurement;
import com.powsybl.iidm.network.extensions.MeasurementAdder;
import com.powsybl.iidm.network.extensions.Measurements;
import com.powsybl.iidm.network.extensions.MeasurementsAdder;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.powsybl.dataframe.network.extensions.MeasurementsDataframeProvider.*;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class MeasurementsDataframeAdder extends AbstractSimpleAdder {

    private static final List<SeriesMetadata> METADATA = List.of(
        SeriesMetadata.stringIndex(ELEMENT_ID),
        SeriesMetadata.strings(ID),
        SeriesMetadata.strings(TYPE),
        SeriesMetadata.strings(SIDE),
        SeriesMetadata.doubles(STANDARD_DEVIATION),
        SeriesMetadata.doubles(VALUE),
        SeriesMetadata.booleans(VALID)
    );

    private static class MeasurementsSeries {
        private final StringSeries id;
        private final StringSeries elementIdSeries;
        private final StringSeries type;
        private final StringSeries side;
        private final DoubleSeries value;
        private final DoubleSeries standardDeviation;
        private final IntSeries valid;
        private final Map<String, Measurements> measurementsMap = new HashMap<>();

        MeasurementsSeries(UpdatingDataframe dataframe) {
            this.elementIdSeries = dataframe.getStrings(ELEMENT_ID);
            this.id = dataframe.getStrings(ID);
            this.type = dataframe.getStrings(TYPE);
            this.side = dataframe.getStrings(SIDE);
            this.value = dataframe.getDoubles(STANDARD_DEVIATION);
            this.standardDeviation = dataframe.getDoubles(VALUE);
            this.valid = dataframe.getInts(VALID);
        }

        void create(int row) {
            String elementId = this.elementIdSeries.get(row);
            Measurements measurements = measurementsMap.get(elementId);
            MeasurementAdder adder = measurements.newMeasurement();
            SeriesUtils.applyIfPresent(id, row, adder::setId);
            SeriesUtils.applyIfPresent(type, row, t -> adder.setType(Measurement.Type.valueOf(t)));
            SeriesUtils.applyIfPresent(side, row, s -> adder.setSide(Measurement.Side.valueOf(s)));
            SeriesUtils.applyIfPresent(value, row, adder::setValue);
            SeriesUtils.applyIfPresent(standardDeviation, row, adder::setStandardDeviation);
            SeriesUtils.applyBooleanIfPresent(valid, row, adder::setValid);
            adder.add();
        }

        void removeAndInitialize(Network network, int row) {
            String elementId = this.elementIdSeries.get(row);
            if (!this.measurementsMap.containsKey(elementId)) {
                Identifiable identifiable = network.getIdentifiable(elementId);
                if (identifiable == null) {
                    throw new PowsyblException("Invalid element id : could not find " + elementId);
                }
                if (!(identifiable instanceof Connectable)) {
                    throw new PowsyblException("element : " + elementId + " is not a connectable");
                }
                Connectable connectable = (Connectable) identifiable;
                connectable.removeExtension(Measurements.class);
                MeasurementsAdder adder = (MeasurementsAdder) connectable.newExtension(MeasurementsAdder.class);
                adder.add();
                measurementsMap.put(elementId, (Measurements) connectable.getExtension(Measurements.class));
            }
        }
    }

    @Override
    public void addElements(Network network, UpdatingDataframe dataframe) {
        MeasurementsSeries series = new MeasurementsSeries(dataframe);
        for (int row = 0; row < dataframe.getRowCount(); row++) {
            series.removeAndInitialize(network, row);
        }
        for (int row = 0; row < dataframe.getRowCount(); row++) {
            series.create(row);
        }
    }

    @Override
    public List<List<SeriesMetadata>> getMetadata() {
        return Collections.singletonList(METADATA);
    }
}
