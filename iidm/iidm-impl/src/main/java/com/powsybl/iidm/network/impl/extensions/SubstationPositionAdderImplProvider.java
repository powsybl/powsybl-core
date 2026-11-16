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
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.extensions.SubstationPosition;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(ExtensionAdderProvider.class)
public class SubstationPositionAdderImplProvider implements
        ExtensionAdderProvider<Substation, SubstationPosition, SubstationPositionAdderImpl> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return SubstationPosition.NAME;
    }

    @Override
    public Class<SubstationPositionAdderImpl> getAdderClass() {
        return SubstationPositionAdderImpl.class;
    }

    @Override
    public SubstationPositionAdderImpl newAdder(Substation substation) {
        return new SubstationPositionAdderImpl(substation);
    }
}
