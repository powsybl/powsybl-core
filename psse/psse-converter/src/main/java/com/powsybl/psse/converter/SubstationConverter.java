/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.converter;

import java.util.Objects;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.util.ContainersMapping;
import com.powsybl.psse.model.pf.PsseBus;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class SubstationConverter extends AbstractConverter {

    SubstationConverter(PsseBus psseBus, ContainersMapping containerMapping, Network network) {
        super(containerMapping, network);
        this.psseBus = Objects.requireNonNull(psseBus);
    }

    Substation create() {
        String voltageLevelId = getContainersMapping().getVoltageLevelId(psseBus.getI());
        String substationId = getContainersMapping().getSubstationId(voltageLevelId);

        Substation substation = getNetwork().getSubstation(substationId);
        if (substation == null) {
            substation = getNetwork().newSubstation()
                .setId(substationId)
                .add();
        }
        return substation;
    }

    private final PsseBus psseBus;
}
