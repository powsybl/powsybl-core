/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Network;

import java.util.Objects;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
class CimCharacteristicsImpl extends AbstractExtension<Network> implements CimCharacteristics {

    private final CgmesTopologyKind cgmesTopologyKind;
    private final int cimVersion;

    CimCharacteristicsImpl(CgmesTopologyKind cgmesTopologyKind, int cimVersion) {
        this.cgmesTopologyKind = Objects.requireNonNull(cgmesTopologyKind);
        this.cimVersion = cimVersion;
    }

    @Override
    public CgmesTopologyKind getTopologyKind() {
        return cgmesTopologyKind;
    }

    @Override
    public int getCimVersion() {
        return cimVersion;
    }
}
