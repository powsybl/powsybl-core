/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.json.action;

import com.powsybl.security.action.DanglingLineAction;
import com.powsybl.security.action.DanglingLineActionBuilder;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public class DanglingLineActionDeserializer extends AbstractLoadActionDeserializer<DanglingLineAction> {

    public DanglingLineActionDeserializer() {
        super(DanglingLineAction.class);
    }

    @Override
    protected DanglingLineAction createAction(ParsingContext context) {
        DanglingLineActionBuilder builder = new DanglingLineActionBuilder();
        builder.withId(context.id).withDanglingLineId(context.elementId).withRelativeValue(context.relativeValue);
        if (context.activePowerValue != null) {
            builder.withActivePowerValue(context.activePowerValue);
        }
        if (context.reactivePowerValue != null) {
            builder.withReactivePowerValue(context.reactivePowerValue);
        }
        return builder.build();
    }
}
