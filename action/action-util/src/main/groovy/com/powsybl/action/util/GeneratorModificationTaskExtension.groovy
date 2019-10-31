/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.util

import com.google.auto.service.AutoService
import com.powsybl.action.dsl.spi.DslTaskExtension
import com.powsybl.commons.PowsyblException
import com.powsybl.contingency.tasks.ModificationTask

@AutoService(DslTaskExtension.class)
class GeneratorModificationTaskExtension implements DslTaskExtension {
    @Override
    void addToSpec(MetaClass tasksSpecMetaClass, List<ModificationTask> tasks, Binding binding) {
        tasksSpecMetaClass.generatorModification = { String id, Closure<Void> closure ->
            def cloned = closure.clone()
            GeneratorModificationSpec spec = new GeneratorModificationSpec()
            cloned.delegate = spec
            cloned()
            if (spec.hasTargetP() && spec.haspDelta()) {
                throw new PowsyblException("targetP/pDelta actions are both found in generatorModification on '" + id + "'");
            }

            tasks.add(new GeneratorModificationTask(id, spec.computeModifs()));
        }
    }

    static class GeneratorModificationSpec {
        private Double minP;
        private Double maxP;
        private Double targetP;
        private Double pDelta;
        private Double targetV;
        private Double targetQ;
        private Boolean voltageRegulatorOn;

        GeneratorModificationTask.Modifs computeModifs() {
            GeneratorModificationTask.Modifs modifs = new GeneratorModificationTask.Modifs();
            modifs.setMinP(minP);
            modifs.setMaxP(maxP);
            modifs.setTargetP(targetP);
            modifs.setpDelta(pDelta);
            modifs.setTargetV(targetV);
            modifs.setTargetQ(targetQ);
            modifs.setVoltageRegulatorOn(voltageRegulatorOn);
            return modifs;
        }

        void minP(Double minP) {
            this.minP = minP
        }
        void maxP(Double maxP) {
            this.maxP = maxP
        }
        void targetP(Double targetP) {
            this.targetP = targetP;
        }
        void pDelta(Double pDelta) {
            this.pDelta = pDelta;
        }
        void targetV(Double targetV) {
            this.targetV = targetV;
        }
        void targetQ(Double targetQ) {
            this.targetQ = targetQ;
        }
        void voltageRegulatorOn(Boolean voltageRegulatorOn) {
            this.voltageRegulatorOn = voltageRegulatorOn;
        }
        boolean hasTargetP() {
            return targetP != null;
        }
        boolean haspDelta() {
            return pDelta != null;
        }
    }
}
