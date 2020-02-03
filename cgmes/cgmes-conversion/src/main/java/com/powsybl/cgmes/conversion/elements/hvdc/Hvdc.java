/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements.hvdc;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class Hvdc {

    private Set<HvdcEquipment> hvdcData;

    // The island includes dcTopologicalNodes and first acTopologicalNode
    Hvdc() {
        hvdcData = new HashSet<>();
    }

    void print() {
        LOG.info("Hvdc");
        hvdcData.forEach(h -> h.print());
    }

    static class HvdcEquipment {
        Set<String> transformersEnd1;
        Set<String> acDcConvertersEnd1;
        Set<String> dcLineSegmentsEnd;
        Set<String> transformersEnd2;
        Set<String> acDcConvertersEnd2;

        HvdcEquipment(Set<String> transformersEnd1, Set<String> acDcConvertersEnd1, Set<String> dcLineSegmentsEnd,
            Set<String> transformersEnd2, Set<String> acDcConvertersEnd2) {
            this.transformersEnd1 = transformersEnd1;
            this.acDcConvertersEnd1 = acDcConvertersEnd1;
            this.dcLineSegmentsEnd = dcLineSegmentsEnd;
            this.transformersEnd2 = transformersEnd2;
            this.acDcConvertersEnd2 = acDcConvertersEnd2;
        }

        void print() {
            LOG.info("    transformersEnd1: {}", this.transformersEnd1);
            LOG.info("    acDcConvertersEnd1: {}", this.acDcConvertersEnd1);
            LOG.info("    dcLineSegmentsEnd: {}", this.dcLineSegmentsEnd);
            LOG.info("    transformersEnd2: {}", this.transformersEnd2);
            LOG.info("    acDcConvertersEnd2: {}", this.acDcConvertersEnd2);
            LOG.info("---");
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(Hvdc.class);
}
