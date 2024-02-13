/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.BusbarSectionAdder;
import com.powsybl.triplestore.api.PropertyBag;

import static com.powsybl.cgmes.conversion.Conversion.PROPERTY_BUSBAR_SECTION_TERMINALS;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class BusbarSectionConversion extends AbstractConductingEquipmentConversion {

    public BusbarSectionConversion(PropertyBag bbs, Context context) {
        super("BusbarSection", bbs, context);
    }

    @Override
    public boolean valid() {
        // Always try to convert busbar sections, even if located at invalid nodes or voltage levels
        return true;
    }

    @Override
    public void convert() {
        if (context.nodeBreaker()) {
            BusbarSectionAdder bbsAdder = voltageLevel().getNodeBreakerView().newBusbarSection()
                    .setId(iidmId())
                    .setName(iidmName())
                    .setEnsureIdUnicity(context.config().isEnsureIdAliasUnicity());
            bbsAdder.setNode(iidmNode());
            BusbarSection bbs = bbsAdder.add();
            addAliasesAndProperties(bbs);
            convertedTerminals(bbs.getTerminal());
        } else {
            // If we are reading CGMES input data as bus/branch,
            // we just keep track of this busbar section terminal
            // to use in case of an updated export
            addBusbarSectionTerminalToBus();
        }
    }

    private void addBusbarSectionTerminalToBus() {
        Bus bus = voltageLevel().getBusBreakerView().getBus(busId());
        if (bus != null) {
            String busbarSectionTerminals = bus.getProperty(PROPERTY_BUSBAR_SECTION_TERMINALS, "");
            if (!busbarSectionTerminals.isEmpty()) {
                busbarSectionTerminals += ",";
            }
            busbarSectionTerminals += terminalId();
            bus.setProperty(PROPERTY_BUSBAR_SECTION_TERMINALS, busbarSectionTerminals);
        }
    }
}
