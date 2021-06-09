/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.extensions.AnalogMeasurement;
import com.powsybl.iidm.network.extensions.AnalogMeasurementAdder;
import com.powsybl.iidm.network.extensions.AnalogMeasurements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class AnalogMeasurementsImpl<C extends Connectable<C>> extends AbstractExtension<C> implements AnalogMeasurements<C> {

    private final List<AnalogMeasurement> analogMeasurements = new ArrayList<>();

    AnalogMeasurementsImpl<C> addAnalog(AnalogMeasurementImpl analog) {
        analogMeasurements.add(analog);
        return this;
    }

    @Override
    public Collection<AnalogMeasurement> getAnalogMeasurements() {
        return Collections.unmodifiableList(analogMeasurements);
    }

    @Override
    public AnalogMeasurement getAnalogMeasurement(String id) {
        return analogMeasurements.stream()
                .filter(a -> a.getId() != null && a.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public AnalogMeasurementAdder newAnalogMeasurement() {
        return new AnalogMeasurementAdderImpl(this);
    }
}
