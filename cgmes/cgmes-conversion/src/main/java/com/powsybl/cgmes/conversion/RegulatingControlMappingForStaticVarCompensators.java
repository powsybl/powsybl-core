/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.conversion.RegulatingControlMapping.RegulatingControl;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.StaticVarCompensatorAdder;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.triplestore.api.PropertyBag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */

public class RegulatingControlMappingForStaticVarCompensators {

    RegulatingControlMappingForStaticVarCompensators(RegulatingControlMapping parent, Context context) {
        this.parent = parent;
        this.context = context;
        mapping = new HashMap<>();
    }

    public static void initialize(StaticVarCompensatorAdder adder) {
        adder.setRegulationMode(StaticVarCompensator.RegulationMode.OFF);
    }

    public void add(String iidmId, PropertyBag sm) {
        String rcId = RegulatingControlMapping.getRegulatingControlId(sm);
        double defaultTargetVoltage = sm.asDouble("voltageSetPoint");
        String defaultRegulationMode = sm.getId("controlMode");

        if (mapping.containsKey(iidmId)) {
            throw new CgmesModelException(
                    "StaticVarCompensator already added, IIDM StaticVarCompensator Id: " + iidmId);
        }

        CgmesRegulatingControlForStaticVarCompensator rc = new CgmesRegulatingControlForStaticVarCompensator();
        rc.regulatingControlId = rcId;
        rc.defaultTargetVoltage = defaultTargetVoltage;
        rc.defaultRegulationMode = defaultRegulationMode;

        mapping.put(iidmId, rc);
    }

    void applyRegulatingControls(Network network) {
        network.getStaticVarCompensatorStream().forEach(this::apply);
    }

    private void apply(StaticVarCompensator svc) {
        CgmesRegulatingControlForStaticVarCompensator rd = mapping.get(svc.getId());
        apply(svc, rd);
    }

    private void apply(StaticVarCompensator svc, CgmesRegulatingControlForStaticVarCompensator rc) {
        if (rc == null) {
            return;
        }

        String controlId = rc.regulatingControlId;
        if (controlId == null) {
            LOG.trace("Regulating control Id not present for static var compensator {}", svc.getId());
            setDefaultRegulatingControl(rc, svc);
            return;
        }

        RegulatingControl control = parent.cachedRegulatingControls().get(controlId);
        if (control == null) {
            context.missing(String.format("Regulating control %s", controlId));
            setDefaultRegulatingControl(rc, svc);
            return;
        }

        control.setCorrectlySet(setRegulatingControl(rc, control, svc));
    }

    private boolean setRegulatingControl(CgmesRegulatingControlForStaticVarCompensator rc, RegulatingControl control, StaticVarCompensator svc) {
        setDefaultRegulatingControl(rc, svc);
        svc.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "RegulatingControl", rc.regulatingControlId);
        svc.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "mode", control.mode);

        // Take default terminal if it has not been defined in CGMES files (it is never null)
        Terminal regulatingTerminal = RegulatingTerminalMapper
                .mapForVoltageControl(control.cgmesTerminal, context)
                .orElse(svc.getTerminal());

        svc.setRegulatingTerminal(regulatingTerminal);

        return true;
    }

    private void setDefaultRegulatingControl(CgmesRegulatingControlForStaticVarCompensator rc, StaticVarCompensator svc) {
        svc.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "defaultRegulationMode", rc.defaultRegulationMode);
        svc.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "defaultTargetVoltage", String.valueOf(rc.defaultTargetVoltage));
    }

    private static final class CgmesRegulatingControlForStaticVarCompensator {
        String regulatingControlId;
        double defaultTargetVoltage;
        String defaultRegulationMode;
    }

    private final RegulatingControlMapping parent;
    private final Map<String, CgmesRegulatingControlForStaticVarCompensator> mapping;
    private final Context context;

    private static final Logger LOG = LoggerFactory.getLogger(RegulatingControlMappingForStaticVarCompensators.class);
}
