/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.extensions.DiscreteMeasurement;
import com.powsybl.iidm.network.extensions.DiscreteMeasurementAdder;
import com.powsybl.iidm.network.extensions.DiscreteMeasurements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
class DiscreteMeasurementsImpl<I extends Identifiable<I>> extends AbstractExtension<I> implements DiscreteMeasurements<I> {

    private final List<DiscreteMeasurementImpl> discreteMeasurements = new ArrayList<>();

    DiscreteMeasurementsImpl<I> add(DiscreteMeasurementImpl discreteMeasurement) {
        discreteMeasurements.add(discreteMeasurement);
        return this;
    }

    void remove(DiscreteMeasurementImpl discreteMeasurement) {
        discreteMeasurements.remove(discreteMeasurement);
    }

    @Override
    public Collection<DiscreteMeasurement> getDiscreteMeasurements() {
        return Collections.unmodifiableList(discreteMeasurements);
    }

    @Override
    public Collection<DiscreteMeasurement> getDiscreteMeasurements(DiscreteMeasurement.Type type) {
        return discreteMeasurements.stream().filter(dm -> dm.getType() == type).collect(Collectors.toList());
    }

    @Override
    public DiscreteMeasurement getDiscreteMeasurement(String id) {
        return discreteMeasurements.stream()
                .filter(d -> d.getId() != null && d.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public DiscreteMeasurementAdder newDiscreteMeasurement() {
        return new DiscreteMeasurementAdderImpl(this);
    }
}
