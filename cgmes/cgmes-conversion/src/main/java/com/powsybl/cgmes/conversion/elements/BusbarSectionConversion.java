/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.BusbarSectionAdder;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class BusbarSectionConversion extends AbstractConductingEquipmentConversion {

    public BusbarSectionConversion(PropertyBag bbs, Context context) {
        super("BusbarSection", bbs, context);
    }

    @Override
    public boolean valid() {
        // We only can convert busbar sections if we are in node-breaker context
        return context.nodeBreaker();
    }

    @Override
    public void convert() {
        BusbarSectionAdder bbsAdder = voltageLevel().getNodeBreakerView().newBusbarSection()
                .setId(iidmId())
                .setName(iidmName())
                .setEnsureIdUnicity(context.config().isEnsureIdAliasUnicity());
        bbsAdder.setNode(iidmNode());
        BusbarSection bbs = bbsAdder.add();
        addAliasesAndProperties(bbs);
        convertedTerminals(bbs.getTerminal());
    }
}
