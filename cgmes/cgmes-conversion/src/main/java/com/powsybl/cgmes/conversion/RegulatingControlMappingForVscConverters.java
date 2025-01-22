/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VscConverterStation;
import com.powsybl.iidm.network.VscConverterStationAdder;
import com.powsybl.triplestore.api.PropertyBag;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */

public class RegulatingControlMappingForVscConverters {

    RegulatingControlMappingForVscConverters(Context context) {
        this.context = context;
        mapping = new HashMap<>();
    }

    public static void initialize(VscConverterStationAdder adder) {
        adder.setVoltageRegulatorOn(false)
                .setReactivePowerSetpoint(0.0);
    }

    public void add(String vscConverterId, PropertyBag sm) {
        if (mapping.containsKey(vscConverterId)) {
            throw new CgmesModelException("VscConverter already added, IIDM VscConverter Id: " + vscConverterId);
        }
        String pccTerminal = sm.getId("PccTerminal");
        mapping.put(vscConverterId, pccTerminal);
    }

    void applyRegulatingControls(Network network) {
        network.getVscConverterStationStream().forEach(this::apply);
    }

    private void apply(VscConverterStation vscConverter) {
        String pccTerminal = mapping.get(vscConverter.getId());
        apply(vscConverter, pccTerminal);
    }

    private void apply(VscConverterStation vscConverter, String pccTerminal) {
        if (pccTerminal == null) {
            return;
        }
        RegulatingTerminalMapper.TerminalAndSign mappedRegulatingTerminal = RegulatingTerminalMapper
                .mapForFlowControl(pccTerminal, context)
                .orElseGet(() -> new RegulatingTerminalMapper.TerminalAndSign(vscConverter.getTerminal(), 1));
        vscConverter
                .setRegulatingTerminal(mappedRegulatingTerminal.getTerminal())
                .setVoltageRegulatorOn(false);
        vscConverter.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "terminalSign", String.valueOf(mappedRegulatingTerminal.getSign()));
    }

    private final Map<String, String> mapping;
    private final Context context;
}
