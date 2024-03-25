/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.ial.util

import com.google.auto.service.AutoService
import com.powsybl.action.ial.dsl.spi.DslModificationExtension
import com.powsybl.iidm.modification.CloseSwitch
import com.powsybl.iidm.modification.NetworkModification

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
@AutoService(DslModificationExtension.class)
class CloseSwitchModificationExtension implements DslModificationExtension {
    @Override
    void addToSpec(MetaClass modificationsSpecMetaClass, List<NetworkModification> modifications, Binding binding) {
        modificationsSpecMetaClass.closeSwitch = { String id ->
            modifications.add(new CloseSwitch(id))
        }
    }
}
