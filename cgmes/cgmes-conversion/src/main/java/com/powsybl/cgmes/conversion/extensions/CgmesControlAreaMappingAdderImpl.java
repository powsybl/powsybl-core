/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.extensions;

import java.util.HashMap;
import java.util.Map;

import com.powsybl.cgmes.conversion.elements.areainterchange.CgmesControlArea;
import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Network;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class CgmesControlAreaMappingAdderImpl extends AbstractExtensionAdder<Network, CgmesControlAreaMapping> implements CgmesControlAreaMappingAdder {

    private Map<String, CgmesControlArea> cgmesControlAreas = new HashMap<>();

    @Override
    public CgmesControlAreaMappingAdder addTieFLow(PropertyBag tf) {
        String controlAreaId = tf.getId("ControlArea");
        String controlAreaName = tf.getLocal("controlAreaName");
        String energyIdentCodeEic = tf.getLocal("energyIdentCodeEic");
        double netInterchange = tf.asDouble("netInterchange");

        String tieFlowId = tf.getId("TieFlow");
        String tieFlowTerminal = tf.getLocal("terminal");

        cgmesControlAreas.computeIfAbsent(controlAreaId, s -> new CgmesControlArea(controlAreaId, controlAreaName, energyIdentCodeEic, netInterchange)).addTieFLow(tieFlowId, tieFlowTerminal);
        return this;
    }

    public CgmesControlAreaMappingAdderImpl(Network extendable) {
        super(extendable);
    }

    @Override
    protected CgmesControlAreaMapping createExtension(Network extendable) {
        return new CgmesControlAreaMappingImpl(cgmesControlAreas);
    }

}
