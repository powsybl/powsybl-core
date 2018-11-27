/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.util

import com.google.auto.service.AutoService
import com.powsybl.action.dsl.spi.DslTaskExtension
import com.powsybl.contingency.tasks.ModificationTask

/**
 * @author Olivier Bretteville <olivier.bretteville at rte-france.com>
 */
@AutoService(DslTaskExtension.class)
class LoadQ0TaskExtension implements DslTaskExtension {
    @Override
    void addToSpec(MetaClass tasksSpecMetaClass, List<ModificationTask> tasks, Binding binding) {
        tasksSpecMetaClass.loadQ0 = { String id, double q0 ->
            tasks.add(new LoadQ0Task(id, q0))
        }
    }
}
