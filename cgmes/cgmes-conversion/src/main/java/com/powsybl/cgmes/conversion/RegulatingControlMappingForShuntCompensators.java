/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.conversion.RegulatingControlMapping.RegulatingControl;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.triplestore.api.PropertyBag;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class RegulatingControlMappingForShuntCompensators {

    private final RegulatingControlMapping parent;
    private final Context context;
    private final Map<String, CgmesRegulatingControlForShuntCompensator> mapping;

    RegulatingControlMappingForShuntCompensators(RegulatingControlMapping parent, Context context) {
        this.parent = parent;
        this.context = context;
        mapping = new HashMap<>();
    }

    public void add(String iidmId, PropertyBag p) {
        String rcId = RegulatingControlMapping.getRegulatingControlId(p);
        boolean controlEnabled = p.asBoolean("controlEnabled", false);

        if (mapping.containsKey(iidmId)) {
            throw new CgmesModelException(
                    "ShuntCompensator already added, IIDM ShuntCompensator Id: " + iidmId);
        }

        mapping.put(iidmId, new CgmesRegulatingControlForShuntCompensator(rcId, controlEnabled));
    }

    void applyRegulatingControls(Network network) {
        network.getShuntCompensatorStream().forEach(this::apply);
    }

    private void apply(ShuntCompensator shuntCompensator) {
        CgmesRegulatingControlForShuntCompensator cgmesRc = mapping.get(shuntCompensator.getId());
        apply(shuntCompensator, cgmesRc);
    }

    private void apply(ShuntCompensator shuntCompensator, CgmesRegulatingControlForShuntCompensator cgmesRc) {
        if (cgmesRc == null) {
            return;
        }

        String rcId = cgmesRc.regulatingControlId;
        if (rcId == null) {
            context.missing("Regulating Control ID not defined");
            return;
        }

        RegulatingControl rc = parent.cachedRegulatingControls().get(rcId);
        if (rc == null) {
            context.missing(String.format("Regulating control %s", rcId));
            return;
        }

        boolean okSet = setRegulatingControlVoltage(shuntCompensator, rcId, rc, cgmesRc.controlEnabled);
        rc.setCorrectlySet(okSet);
    }

    private boolean setRegulatingControlVoltage(ShuntCompensator shuntCompensator, String rcId, RegulatingControl control, boolean eqControlEnabled) {
        Terminal terminal = parent.getRegulatingTerminal(control.cgmesTerminal);
        double targetV = control.targetValue;
        double targetDeadband = control.targetDeadband;

        boolean valid = ValidationUtil.validRegulatingVoltageControl(terminal, targetV, targetDeadband, shuntCompensator.getNetwork());
        boolean voltageRegulatorOn = false;
        if (control.enabled && eqControlEnabled && valid) {
            voltageRegulatorOn = true;
        }
        shuntCompensator
            .setRegulatingTerminal(terminal)
            .setTargetV(targetV)
            .setTargetDeadband(targetDeadband)
            .setVoltageRegulatorOn(voltageRegulatorOn);

        shuntCompensator.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "RegulatingControl", rcId);

        return valid;
    }

    private static class CgmesRegulatingControlForShuntCompensator {
        String regulatingControlId;
        boolean controlEnabled;

        CgmesRegulatingControlForShuntCompensator(String regulatingControlId, boolean controlEnabled) {
            this.regulatingControlId = regulatingControlId;
            this.controlEnabled = controlEnabled;
        }
    }
}
