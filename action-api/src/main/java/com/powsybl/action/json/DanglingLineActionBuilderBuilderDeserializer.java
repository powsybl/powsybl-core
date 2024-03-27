/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.json;

import com.powsybl.action.DanglingLineActionBuilder;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public class DanglingLineActionBuilderBuilderDeserializer extends AbstractLoadActionBuilderDeserializer<DanglingLineActionBuilder> {

    public DanglingLineActionBuilderBuilderDeserializer() {
        super(DanglingLineActionBuilder.class);
    }

    @Override
    protected DanglingLineActionBuilder createAction(ParsingContext context) {
        DanglingLineActionBuilder builder = new DanglingLineActionBuilder();
        builder.withId(context.id)
            .withDanglingLineId(context.elementId)
            .withRelativeValue(context.relativeValue);
        if (context.activePowerValue != null) {
            builder.withActivePowerValue(context.activePowerValue);
        }
        if (context.reactivePowerValue != null) {
            builder.withReactivePowerValue(context.reactivePowerValue);
        }
        return builder;
    }
}
