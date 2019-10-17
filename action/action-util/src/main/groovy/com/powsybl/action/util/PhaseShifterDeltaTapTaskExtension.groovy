package com.powsybl.action.util

import com.google.auto.service.AutoService
import com.powsybl.action.dsl.spi.DslTaskExtension
import com.powsybl.contingency.tasks.ModificationTask

@AutoService(DslTaskExtension.class)
class PhaseShifterDeltaTapTaskExtension implements DslTaskExtension {

    @Override
    void addToSpec(MetaClass tasksSpecMetaClass, List<ModificationTask> tasks, Binding binding) {
        tasksSpecMetaClass.phaseShifterFixedTap = { String id, int delta ->
            tasks.add(new PhaseShifterDeltaTapTask(id, delta))
        }
    }
}
