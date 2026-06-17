/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.LineCouplingsAdder;
import com.powsybl.iidm.network.extensions.LineCouplings;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
@AutoService(ExtensionAdderProvider.class)
public class LineCouplingsAdderImplProvider implements ExtensionAdderProvider<Network, LineCouplings, LineCouplingsAdder> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return LineCouplings.NAME;
    }

    @Override
    public Class<LineCouplingsAdder> getAdderClass() {
        return LineCouplingsAdder.class;
    }

    @Override
    public LineCouplingsAdder newAdder(Network extendable) {
        return new LineCouplingsAdderImpl(extendable);
    }

}
