/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;

/**
 * An action consisting in an ordered list of actions.
 *
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class MultipleActionsAction extends AbstractAction {

    public static final String NAME = "MULTIPLE_ACTIONS";
    private final List<Action> actions;

    public MultipleActionsAction(String id, List<Action> actions) {
        super(id);
        this.actions = ImmutableList.copyOf(Objects.requireNonNull(actions));
    }

    @Override
    public String getType() {
        return NAME;
    }

    public List<Action> getActions() {
        return actions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        MultipleActionsAction that = (MultipleActionsAction) o;
        return Objects.equals(actions, that.actions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), actions);
    }
}
