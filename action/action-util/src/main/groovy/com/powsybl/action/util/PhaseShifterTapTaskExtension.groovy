/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.util

import com.google.auto.service.AutoService
import com.powsybl.action.dsl.spi.DslTaskExtension
import com.powsybl.network.modification.NetworkModification

/**
 * @author Hamou AMROUN <hamou.amroun at rte-france.com>
 */
@AutoService(DslTaskExtension.class)
class PhaseShifterTapTaskExtension implements DslTaskExtension {

    @Override
    void addToSpec(MetaClass tasksSpecMetaClass, List<NetworkModification> tasks, Binding binding) {
        tasksSpecMetaClass.phaseShifterTap = { String id, int delta ->
            tasks.add(new PhaseShifterTapTask(id, delta))
        }
    }
}
