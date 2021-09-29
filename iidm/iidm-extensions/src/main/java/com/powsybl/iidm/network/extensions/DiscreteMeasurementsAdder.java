/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.Identifiable;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public interface DiscreteMeasurementsAdder<I extends Identifiable<I>> extends ExtensionAdder<I, DiscreteMeasurements<I>> {

    @Override
    default Class<DiscreteMeasurements> getExtensionClass() {
        return DiscreteMeasurements.class;
    }
}
