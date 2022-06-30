/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.converter;

import java.util.Optional;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.powerfactory.converter.PowerFactoryImporter.ImportContext;
import com.powsybl.powerfactory.model.DataObject;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */

class VoltageAndAngle extends AbstractConverter {

    VoltageAndAngle(ImportContext importContext, Network network) {
        super(importContext, network);
    }

    void update(DataObject elmTerm) {
        if (!getImportContext().elmTermIdToNode.containsKey(elmTerm.getId())) {
            return;
        }
        Optional<Float> uknom = elmTerm.findFloatAttributeValue("uknom");
        Optional<Float> u = elmTerm.findFloatAttributeValue("m:u");
        Optional<Float> phiu = elmTerm.findFloatAttributeValue("m:phiu");

        if (uknom.isPresent() && u.isPresent() && phiu.isPresent()) {
            NodeRef nodeRef = getImportContext().elmTermIdToNode.get(elmTerm.getId());
            Terminal terminal = getNetwork().getVoltageLevel(nodeRef.voltageLevelId).getNodeBreakerView().getTerminal(nodeRef.node);
            if (terminal != null) {
                Bus bus = terminal.getBusView().getBus();
                if (bus != null) {
                    bus.setV(u.get() * uknom.get());
                    bus.setAngle(phiu.get());
                }
            }
        }
    }
}
