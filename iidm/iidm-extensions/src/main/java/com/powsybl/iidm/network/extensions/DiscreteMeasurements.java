/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Identifiable;

import java.util.Collection;

/**
 * Measurements with discrete values associated with an equipment (the extended equipment).
 * See {@link DiscreteMeasurement}.
 *
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public interface DiscreteMeasurements<I extends Identifiable<I>> extends Extension<I> {

    String NAME = "discreteMeasurements";

    @Override
    default String getName() {
        return NAME;
    }

    Collection<DiscreteMeasurement> getDiscreteMeasurements();

    Collection<DiscreteMeasurement> getDiscreteMeasurements(DiscreteMeasurement.Type type);

    DiscreteMeasurement getDiscreteMeasurement(String id);

    DiscreteMeasurementAdder newDiscreteMeasurement();

    /**
     * Check if there is any discrete measurement associated with the extended equipment.
     * If not, remove the extension.
     */
    default void cleanIfEmpty() {
        if (getDiscreteMeasurements().isEmpty()) {
            getExtendable().removeExtension(DiscreteMeasurements.class);
        }
    }
}
