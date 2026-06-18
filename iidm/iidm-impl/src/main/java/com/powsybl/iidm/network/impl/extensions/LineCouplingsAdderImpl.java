/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.LineCouplingsAdder;
import com.powsybl.iidm.network.extensions.LineCouplings;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public class LineCouplingsAdderImpl extends AbstractExtensionAdder<Network, LineCouplings> implements LineCouplingsAdder {

    public LineCouplingsAdderImpl(Network network) {
        super(network);
    }

    @Override
    protected LineCouplingsImpl createExtension(Network network) {
        return new LineCouplingsImpl();
    }
}
