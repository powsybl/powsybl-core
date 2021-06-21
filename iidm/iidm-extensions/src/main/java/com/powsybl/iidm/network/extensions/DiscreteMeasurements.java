/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Identifiable;

import java.util.Collection;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public interface DiscreteMeasurements<I extends Identifiable<I>> extends Extension<I> {

    @Override
    default String getName() {
        return "discreteMeasurements";
    }

    Collection<DiscreteMeasurement> getDiscreteMeasurements();

    DiscreteMeasurement getDiscreteMeasurement(String id);

    DiscreteMeasurementAdder newDiscreteMeasurement();

    default void cleanIfEmpty() {
        if (getDiscreteMeasurements().isEmpty()) {
            getExtendable().removeExtension(Measurements.class);
        }
    }
}
