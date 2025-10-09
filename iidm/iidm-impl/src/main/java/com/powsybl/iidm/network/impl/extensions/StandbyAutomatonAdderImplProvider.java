/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.extensions.StandbyAutomaton;

/**
 * @author Jérémy Labous {@literal <jlabous at silicom.fr>}
 */
@AutoService(ExtensionAdderProvider.class)
public class StandbyAutomatonAdderImplProvider
        implements ExtensionAdderProvider<StaticVarCompensator, StandbyAutomaton, StandbyAutomatonAdderImpl> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return StandbyAutomaton.NAME;
    }

    @Override
    public Class<StandbyAutomatonAdderImpl> getAdderClass() {
        return StandbyAutomatonAdderImpl.class;
    }

    @Override
    public StandbyAutomatonAdderImpl newAdder(StaticVarCompensator extendable) {
        return new StandbyAutomatonAdderImpl(extendable);
    }
}
