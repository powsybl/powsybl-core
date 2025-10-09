/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.extensions.Measurement;
import com.powsybl.iidm.network.extensions.MeasurementAdder;
import com.powsybl.iidm.network.extensions.Measurements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
class MeasurementsImpl<C extends Connectable<C>> extends AbstractExtension<C> implements Measurements<C> {

    private final List<MeasurementImpl> measurements = new ArrayList<>();

    MeasurementsImpl<C> add(MeasurementImpl measurement) {
        measurements.add(measurement);
        return this;
    }

    void remove(MeasurementImpl measurement) {
        measurements.remove(measurement);
    }

    @Override
    public Collection<Measurement> getMeasurements() {
        return Collections.unmodifiableList(measurements);
    }

    @Override
    public Collection<Measurement> getMeasurements(Measurement.Type type) {
        return measurements.stream().filter(m -> m.getType() == type).collect(Collectors.toList());
    }

    @Override
    public Measurement getMeasurement(String id) {
        return measurements.stream()
                .filter(a -> a.getId() != null && a.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public MeasurementAdder newMeasurement() {
        return new MeasurementAdderImpl(this);
    }
}
