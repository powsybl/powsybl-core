/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.cgmes.conversion.elements.CgmesTopologyKind;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.mergingview.extensions.ExtensionMergingView;
import com.powsybl.iidm.network.Network;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
@AutoService(ExtensionMergingView.class)
public class CimCharacteristicsMergingView implements ExtensionMergingView<Network, CimCharacteristics> {

    @Override
    public Class<? extends CimCharacteristics> getExtensionClass() {
        return CimCharacteristics.class;
    }

    @Override
    public String getName() {
        return "cimCharacteristics";
    }

    @Override
    public void merge(Network extendable, CimCharacteristics extension) {
        CimCharacteristics original = extendable.getExtension(CimCharacteristics.class);
        if (original == null) {
            extendable.newExtension(CimCharacteristicsAdder.class)
                    .setCimVersion(extension.getCimVersion())
                    .setTopologyKind(extension.getTopologyKind())
                    .add();
        } else {
            checkCimVersions(original.getCimVersion(), extension.getCimVersion());
            extendable.newExtension(CimCharacteristicsAdder.class)
                    .setCimVersion(extension.getCimVersion())
                    .setTopologyKind(getMergedTopologyKind(original.getTopologyKind(), extension.getTopologyKind()))
                    .add();
        }
    }

    private static void checkCimVersions(int cimVersion1, int cimVersion2) {
        if (cimVersion1 != cimVersion2) {
            throw new PowsyblException("Networks are from different CIM versions (" + cimVersion1 + " and " + cimVersion2 + ")");
        }
    }

    private static CgmesTopologyKind getMergedTopologyKind(CgmesTopologyKind topologyKind1, CgmesTopologyKind topologyKind2) {
        if (topologyKind1 == CgmesTopologyKind.NODE_BREAKER || topologyKind2 == CgmesTopologyKind.NODE_BREAKER) {
            return CgmesTopologyKind.NODE_BREAKER;
        }
        return CgmesTopologyKind.BUS_BRANCH;
    }
}
