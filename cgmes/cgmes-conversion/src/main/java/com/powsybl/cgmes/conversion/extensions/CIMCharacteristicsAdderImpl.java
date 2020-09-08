/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.extensions;

import com.powsybl.cgmes.conversion.elements.CgmesTopologyKind;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Network;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class CIMCharacteristicsAdderImpl extends AbstractExtensionAdder<Network, CIMCharacteristics> implements CIMCharacteristicsAdder {

    private CgmesTopologyKind cgmesTopologyKind;
    private int cimVersion = -1;

    CIMCharacteristicsAdderImpl(Network extendable) {
        super(extendable);
    }

    @Override
    public CIMCharacteristicsAdder setTopologyKind(CgmesTopologyKind cgmesTopologyKind) {
        this.cgmesTopologyKind = cgmesTopologyKind;
        return this;
    }

    @Override
    public CIMCharacteristicsAdder setCimVersion(int cimVersion) {
        this.cimVersion = cimVersion;
        return this;
    }

    @Override
    protected CIMCharacteristics createExtension(Network extendable) {
        if (cgmesTopologyKind == null) {
            throw new PowsyblException("CIMCharacteristics.topologyKind is undefined");
        }
        if (cimVersion == -1) {
            throw new PowsyblException("CIMCharacteristics.cimVersion is undefined");
        }
        return new CIMCharacteristicsImpl(cgmesTopologyKind, cimVersion);
    }
}
