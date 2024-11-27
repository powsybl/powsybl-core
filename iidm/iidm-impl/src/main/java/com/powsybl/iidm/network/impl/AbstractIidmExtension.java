/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Terminal;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public abstract class AbstractIidmExtension<I extends Identifiable<I>> extends AbstractExtension<I> implements Referrer<Terminal> {

    protected AbstractIidmExtension(I extendable) {
        super(extendable);
    }

    @Override
    public void onReferencedRemoval(Terminal terminal) {
        // nothing by default
        // this is the place for terminal reference cleanup
    }
}
