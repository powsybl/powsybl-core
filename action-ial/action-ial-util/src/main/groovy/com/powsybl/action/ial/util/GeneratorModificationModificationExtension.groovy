/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.ial.util

import com.google.auto.service.AutoService
import com.powsybl.action.ial.dsl.spi.DslModificationExtension
import com.powsybl.commons.PowsyblException
import com.powsybl.iidm.modification.GeneratorModification
import com.powsybl.iidm.modification.NetworkModification

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
@AutoService(DslModificationExtension.class)
class GeneratorModificationModificationExtension implements DslModificationExtension {
    @Override
    void addToSpec(MetaClass modificationsSpecMetaClass, List<NetworkModification> modifications, Binding binding) {
        modificationsSpecMetaClass.generatorModification = { String id, Closure<Void> closure ->
            def cloned = closure.clone()
            GeneratorModificationSpec spec = new GeneratorModificationSpec()
            cloned.delegate = spec
            cloned()
            if (spec.hasTargetP() && spec.hasDeltaTargetP()) {
                throw new PowsyblException("targetP/deltaTargetP actions are both found in generatorModification on '" + id + "'")
            }
            modifications.add(new GeneratorModification(id, spec.computeModifs()))
        }
    }

    /**
     * Describes the instructions usable in a "generatorModification" task
     */
    static class GeneratorModificationSpec {
        Double minP
        Double maxP
        Double targetP
        Double deltaTargetP
        Double targetV
        Double targetQ
        Boolean voltageRegulatorOn
        Boolean connected

        GeneratorModification.Modifs computeModifs() {
            GeneratorModification.Modifs modifs = new GeneratorModification.Modifs()
            modifs.setMinP(minP)
            modifs.setMaxP(maxP)
            modifs.setTargetP(targetP)
            modifs.setDeltaTargetP(deltaTargetP)
            modifs.setTargetV(targetV)
            modifs.setTargetQ(targetQ)
            modifs.setVoltageRegulatorOn(voltageRegulatorOn)
            modifs.setConnected(connected)
            return modifs
        }

        void minP(Double minP) {
            this.minP = minP
        }

        void maxP(Double maxP) {
            this.maxP = maxP
        }

        /**
         * <p>Changes the target power.</p>
         * <p>The resulting target power will respect the defined min and max powers, thus:
         * <ul><li>if targetP &gt; maxP, the target power will be set to maxP;</li>
         * <li>if targetP &lt; minP, the target power will be set to minP.</li></ul></p>
         * <p>This method connects the generator if it is'nt already connected, unless the same "generatorModification"
         * task contains a "connected false" instruction.</p>
         * @param targetP the target power
         */
        void targetP(Double targetP) {
            this.targetP = targetP
        }

        /**
         * <p>Changes the target power by specifying a variation.</p>
         * <p>The resulting target power will respect the defined min and max powers, thus:
         * <ul><li>if (targetP + deltaTargetP) &gt; maxP, the target power will be set to maxP;</li>
         * <li>if (targetP + deltaTargetP) &lt; minP, the target power will be set to minP.</li></ul></p>
         * <p>This method connects the generator if it is'nt already connected, unless the same "generatorModification"
         * task contains a "connected false" instruction.</p>
         * @param deltaTargetP a variation of the target power
         */
        void deltaTargetP(Double deltaTargetP) {
            this.deltaTargetP = deltaTargetP
        }

        void targetV(Double targetV) {
            this.targetV = targetV
        }

        void targetQ(Double targetQ) {
            this.targetQ = targetQ
        }

        void voltageRegulatorOn(Boolean voltageRegulatorOn) {
            this.voltageRegulatorOn = voltageRegulatorOn
        }

        /**
         * <p>Changes the connection state of the generator if needed.</p>
         * <p>If the generator is in voltage regulation mode, a "targetV" instruction is ignored
         * and its voltage is set to the bus voltage.</p>
         * @param connected the wanted connection state
         */
        void connected(Boolean connected) {
            this.connected = connected
        }

        private boolean hasTargetP() {
            return targetP != null
        }

        private boolean hasDeltaTargetP() {
            return deltaTargetP != null
        }
    }
}
