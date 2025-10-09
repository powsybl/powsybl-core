/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Network;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
class CimCharacteristicsAdderImpl extends AbstractExtensionAdder<Network, CimCharacteristics> implements CimCharacteristicsAdder {

    private CgmesTopologyKind cgmesTopologyKind;
    private int cimVersion = -1;

    CimCharacteristicsAdderImpl(Network extendable) {
        super(extendable);
    }

    @Override
    public CimCharacteristicsAdder setTopologyKind(CgmesTopologyKind cgmesTopologyKind) {
        this.cgmesTopologyKind = cgmesTopologyKind;
        return this;
    }

    @Override
    public CimCharacteristicsAdder setCimVersion(int cimVersion) {
        this.cimVersion = cimVersion;
        return this;
    }

    @Override
    protected CimCharacteristics createExtension(Network extendable) {
        if (cgmesTopologyKind == null) {
            throw new PowsyblException("CimCharacteristics.topologyKind is undefined");
        }
        if (cimVersion == -1) {
            throw new PowsyblException("CimCharacteristics.cimVersion is undefined");
        }
        return new CimCharacteristicsImpl(cgmesTopologyKind, cimVersion);
    }
}
