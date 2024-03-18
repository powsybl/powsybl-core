/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.json;

import com.powsybl.action.LoadAction;
import com.powsybl.action.LoadActionBuilder;

/**
 * @author Anne Tilloy {@literal <anne.tilloy@rte-france.com>}
 */
public class LoadActionDeserializer extends AbstractLoadActionDeserializer<LoadAction> {

    public LoadActionDeserializer() {
        super(LoadAction.class);
    }

    @Override
    protected LoadAction createAction(AbstractLoadActionDeserializer.ParsingContext context) {
        LoadActionBuilder loadActionBuilder = new LoadActionBuilder();
        loadActionBuilder
                .withId(context.id)
                .withLoadId(context.elementId)
                .withRelativeValue(context.relativeValue);
        if (context.activePowerValue != null) {
            loadActionBuilder.withActivePowerValue(context.activePowerValue);
        }
        if (context.reactivePowerValue != null) {
            loadActionBuilder.withReactivePowerValue(context.reactivePowerValue);
        }
        return loadActionBuilder.build();
    }
}
