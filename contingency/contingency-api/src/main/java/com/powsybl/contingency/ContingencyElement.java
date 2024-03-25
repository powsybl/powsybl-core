/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.modification.tripping.Tripping;
import com.powsybl.iidm.network.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface ContingencyElement {

    String getId();

    ContingencyElementType getType();

    Tripping toModification();

    static ContingencyElement of(Identifiable<?> identifiable) {
        if (identifiable instanceof Line) {
            return new LineContingency(identifiable.getId());
        } else if (identifiable instanceof BusbarSection) {
            return new BusbarSectionContingency(identifiable.getId());
        } else if (identifiable instanceof TwoWindingsTransformer) {
            return new TwoWindingsTransformerContingency(identifiable.getId());
        } else if (identifiable instanceof ThreeWindingsTransformer) {
            return new ThreeWindingsTransformerContingency(identifiable.getId());
        } else if (identifiable instanceof Generator) {
            return new GeneratorContingency(identifiable.getId());
        } else if (identifiable instanceof Switch) {
            return new SwitchContingency(identifiable.getId());
        } else if (identifiable instanceof DanglingLine) {
            return new DanglingLineContingency(identifiable.getId());
        } else if (identifiable instanceof Load) {
            return new LoadContingency(identifiable.getId());
        } else if (identifiable instanceof HvdcLine) {
            return new HvdcLineContingency(identifiable.getId());
        } else if (identifiable instanceof ShuntCompensator) {
            return new ShuntCompensatorContingency(identifiable.getId());
        } else if (identifiable instanceof StaticVarCompensator) {
            return new StaticVarCompensatorContingency(identifiable.getId());
        } else if (identifiable instanceof Battery) {
            return new BatteryContingency(identifiable.getId());
        } else if (identifiable instanceof Bus) {
            return new BusContingency(identifiable.getId());
        } else if (identifiable instanceof TieLine) {
            return new TieLineContingency(identifiable.getId());
        } else {
            throw new PowsyblException(identifiable.getId() + " can not be a ContingencyElement");
        }
    }
}
