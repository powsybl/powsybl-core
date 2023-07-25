/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dataframe.network.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.dataframe.network.ExtensionInformation;
import com.powsybl.dataframe.network.NetworkDataframeMapper;
import com.powsybl.dataframe.network.NetworkDataframeMapperBuilder;
import com.powsybl.dataframe.network.adders.NetworkElementAdder;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.Measurement;
import com.powsybl.iidm.network.extensions.Measurements;
import org.jgrapht.alg.util.Pair;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
@AutoService(NetworkExtensionDataframeProvider.class)
public class MeasurementsDataframeProvider extends AbstractSingleDataframeNetworkExtension {

    public static final String ELEMENT_ID = "element_id";
    public static final String ID = "id";
    public static final String TYPE = "type";
    public static final String SIDE = "side";
    public static final String STANDARD_DEVIATION = "standard_deviation";
    public static final String VALUE = "value";
    public static final String VALID = "valid";

    @Override
    public String getExtensionName() {
        return Measurements.NAME;
    }

    @Override
    public ExtensionInformation getExtensionInformation() {
        return new ExtensionInformation(Measurements.NAME, "Provides measurement about a specific equipment",
            "index : element_id (str),id (str), type (str), standard_deviation (float), value (float), valid (bool)");
    }

    @Override
    public NetworkDataframeMapper createMapper() {
        return NetworkDataframeMapperBuilder.ofStream(this::itemsStream)
            .stringsIndex(ELEMENT_ID, MeasurementInformation::getElementId)
            .strings(ID, MeasurementInformation::getId)
            .strings(TYPE, info -> info.getType().toString())
            .strings(SIDE, info -> info.getSide() == null ? null : info.getSide().toString())
            .doubles(STANDARD_DEVIATION, MeasurementInformation::getStandardDeviation)
            .doubles(VALUE, MeasurementInformation::getValue)
            .booleans(VALID, MeasurementInformation::isValid)
            .build();
    }

    @Override
    public void removeExtensions(Network network, List<String> ids) {
        ids.stream().filter(Objects::nonNull)
            .map(network::getIdentifiable)
            .filter(Objects::nonNull)
            .filter(Connectable.class::isInstance)
            .forEach(c -> c.removeExtension(Measurements.class));
    }

    private Stream<MeasurementInformation> itemsStream(Network network) {
        return network.getConnectableStream()
            .filter(Objects::nonNull)
            .map(connectable -> Pair.of(connectable.getId(),
                (Measurements) connectable.getExtension(Measurements.class)))
            .filter(pair -> pair.getSecond() != null)
            .flatMap(pair -> ((Stream<Measurement>) pair.getSecond().getMeasurements().stream())
                .map(measurement -> new MeasurementInformation(measurement, pair.getFirst())));
    }

    @Override
    public NetworkElementAdder createAdder() {
        return new MeasurementsDataframeAdder();
    }

    private static class MeasurementInformation {
        private final String elementId;
        private final String id;
        private final Measurement.Type type;
        private final Measurement.Side side;
        private final double value;
        private final double standardDeviation;
        private final boolean valid;

        public MeasurementInformation(Measurement measurement, String elementId) {
            this.elementId = elementId;
            this.id = measurement.getId();
            this.type = measurement.getType();
            this.side = measurement.getSide();
            this.value = measurement.getValue();
            this.standardDeviation = measurement.getStandardDeviation();
            this.valid = measurement.isValid();
        }

        public String getId() {
            return id;
        }

        public String getElementId() {
            return elementId;
        }

        public Measurement.Side getSide() {
            return side;
        }

        public double getStandardDeviation() {
            return standardDeviation;
        }

        public double getValue() {
            return value;
        }

        public Measurement.Type getType() {
            return type;
        }

        public boolean isValid() {
            return valid;
        }
    }
}
