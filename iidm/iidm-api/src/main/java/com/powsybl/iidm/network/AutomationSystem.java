/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public interface AutomationSystem<I extends AutomationSystem<I>> extends Identifiable<I> {

    /**
     * Says if the system is active or not.
     * @return <code>true</code> is the automation system is enabled
     */
    boolean isEnabled();

    /**
     * Change the state of the automation system
     * @param enabled <code>true</code> to enable the automation system
     */
    void setEnabled(boolean enabled);

    /**
     * Remove the automation system from the network.
     */
    void remove();
}
