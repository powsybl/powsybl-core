/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.conversion.RegulatingControlMapping.RegulatingControl;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.triplestore.api.PropertyBag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
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

        // This equipment is not participating in its regulating control
        // We will not create default regulation data
        // But will try to store information about corresponding regulating control
        if (!cgmesRc.controlEnabled) {
            if (rcId != null) {
                RegulatingControl rc = parent.cachedRegulatingControls().get(rcId);
                if (rc != null) {
                    setRegulatingControl(shuntCompensator, rcId, rc, cgmesRc.controlEnabled);
                }
            }
            return;
        }
        // The equipment is participating in regulating control (cgmesRc.controlEnabled)
        // But no regulating control information has been found
        // We create default regulation data
        if (rcId == null) {
            LOG.trace("Regulating control Id not present for shunt compensator {}", shuntCompensator.getId());
            setDefaultRegulatingControl(shuntCompensator);
            return;
        }
        RegulatingControl rc = parent.cachedRegulatingControls().get(rcId);
        if (rc == null) {
            context.missing(String.format("Regulating control %s", rcId));
            setDefaultRegulatingControl(shuntCompensator);
            return;
        }
        // Finally, equipment participates in it regulating control,
        // and the regulating control information is present in the CGMES model
        setRegulatingControl(shuntCompensator, rcId, rc, cgmesRc.controlEnabled);
        rc.setCorrectlySet(true);
    }

    private void setDefaultRegulatingControl(ShuntCompensator shuntCompensator) {
        shuntCompensator.setTargetV(Optional.ofNullable(shuntCompensator.getTerminal().getBusView().getBus())
                    .map(Bus::getV)
                    .filter(v -> !Double.isNaN(v))
                    .orElseGet(() -> shuntCompensator.getTerminal().getVoltageLevel().getNominalV()))
                .setTargetDeadband(0.0)
                .setVoltageRegulatorOn(true); // SSH controlEnabled attribute is true when this method is called
    }

    private void setRegulatingControl(ShuntCompensator shuntCompensator, String rcId, RegulatingControl rc, boolean controlEnabled) {
        shuntCompensator.setTargetV(rc.targetValue)
                .setTargetDeadband(rc.targetDeadband);
        if (rc.targetValue > 0) {
            // For the IIDM regulating control to be enabled
            // both the equipment participation in the control and
            // the regulating control itself should be enabled
            shuntCompensator.setVoltageRegulatorOn(rc.enabled && controlEnabled);
        } else {
            shuntCompensator.setVoltageRegulatorOn(false);
        }
        // Take default terminal if it has not been defined in CGMES files (it is never null)
        shuntCompensator.setRegulatingTerminal(RegulatingTerminalMapper
                .mapForVoltageControl(rc.cgmesTerminal, context)
                .orElse(shuntCompensator.getTerminal()));
        shuntCompensator.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "RegulatingControl", rcId);
    }

    private static class CgmesRegulatingControlForShuntCompensator {
        String regulatingControlId;
        boolean controlEnabled;

        CgmesRegulatingControlForShuntCompensator(String regulatingControlId, boolean controlEnabled) {
            this.regulatingControlId = regulatingControlId;
            this.controlEnabled = controlEnabled;
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(RegulatingControlMappingForShuntCompensators.class);
}
