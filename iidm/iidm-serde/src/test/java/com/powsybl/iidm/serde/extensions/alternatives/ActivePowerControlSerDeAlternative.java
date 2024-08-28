/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions.alternatives;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionSerDeAlternative;
import com.powsybl.commons.extensions.ExtensionSerDeAlternative;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.extensions.ActivePowerControl;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
@AutoService(ExtensionSerDeAlternative.class)
public class ActivePowerControlSerDeAlternative<T extends Injection<T>> extends AbstractExtensionSerDeAlternative<T, ActivePowerControl<T>> {

    public ActivePowerControlSerDeAlternative() {
        super(ActivePowerControl.NAME, new LegacyActivePowerControlSerDe<>());
    }
}
