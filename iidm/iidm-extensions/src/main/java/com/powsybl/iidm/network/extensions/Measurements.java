/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Connectable;

import java.util.Collection;

/**
 * Measurements with continuous numeric values associated with an equipment (the extended equipment).
 * See {@link Measurement}.
 *
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public interface Measurements<C extends Connectable<C>> extends Extension<C> {

    String NAME = "measurements";

    @Override
    default String getName() {
        return NAME;
    }

    Collection<Measurement> getMeasurements();

    Collection<Measurement> getMeasurements(Measurement.Type type);

    Measurement getMeasurement(String id);

    MeasurementAdder newMeasurement();

    /**
     * Check if there is any measurement with continuous numeric values associated with the extended equipment.
     * If not, remove the extension.
     */
    default void cleanIfEmpty() {
        if (getMeasurements().isEmpty()) {
            getExtendable().removeExtension(Measurements.class);
        }
    }
}
