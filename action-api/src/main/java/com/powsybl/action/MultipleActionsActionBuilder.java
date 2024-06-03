/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class MultipleActionsActionBuilder implements ActionBuilder<MultipleActionsActionBuilder> {

    private String id;
    private List<ActionBuilder> actionBuilders = new ArrayList<>();

    @Override
    public String getType() {
        return MultipleActionsAction.NAME;
    }

    @Override
    public MultipleActionsActionBuilder withId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public MultipleActionsActionBuilder withNetworkElementId(String elementId) {
        return null;
    }

    public MultipleActionsActionBuilder withActionBuilders(List<ActionBuilder> actionBuilders) {
        this.actionBuilders = actionBuilders;
        return this;
    }

    @Override
    public MultipleActionsAction build() {
        return new MultipleActionsAction(id, actionBuilders.stream().map(ActionBuilder::build).toList());
    }
}
