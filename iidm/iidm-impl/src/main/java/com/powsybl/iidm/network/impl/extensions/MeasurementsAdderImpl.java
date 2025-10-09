/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.extensions.Measurements;
import com.powsybl.iidm.network.extensions.MeasurementsAdder;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
class MeasurementsAdderImpl<C extends Connectable<C>> extends AbstractExtensionAdder<C, Measurements<C>> implements MeasurementsAdder<C> {

    MeasurementsAdderImpl(C extendable) {
        super(extendable);
    }

    @Override
    protected MeasurementsImpl<C> createExtension(C extendable) {
        return new MeasurementsImpl<>();
    }
}
