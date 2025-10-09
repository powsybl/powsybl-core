/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.SlackTerminal;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
@AutoService(ExtensionAdderProvider.class)
public class SlackTerminalAdderImplProvider implements
    ExtensionAdderProvider<VoltageLevel, SlackTerminal, SlackTerminalAdderImpl> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return SlackTerminal.NAME;
    }

    @Override
    public Class<SlackTerminalAdderImpl> getAdderClass() {
        return SlackTerminalAdderImpl.class;
    }

    @Override
    public SlackTerminalAdderImpl newAdder(VoltageLevel extendable) {
        return new SlackTerminalAdderImpl(extendable);
    }
}
