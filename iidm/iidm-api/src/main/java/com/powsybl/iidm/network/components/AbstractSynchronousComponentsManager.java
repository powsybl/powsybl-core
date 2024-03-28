/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.network.components;

import com.powsybl.iidm.network.Component;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
public abstract class AbstractSynchronousComponentsManager<C extends Component> extends AbstractComponentsManager<C> {

    protected AbstractSynchronousComponentsManager() {
        super("Synchronous");
    }

}
