/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
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
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */

public class RegulatingControlMappingForVscConverters {

    enum VscRegulation {
        REACTIVE_POWER,
        VOLTAGE
    }

    RegulatingControlMappingForVscConverters(RegulatingControlMapping parent, Context context) {
        this.parent = parent;
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

        CgmesRegulatingControlForVscConverter rd = new CgmesRegulatingControlForVscConverter();
        rd.vscRegulation = sm.getLocal("qPccControl");
        rd.voltageSetpoint = sm.asDouble("targetUpcc");
        rd.reactivePowerSetpoint = -sm.asDouble("targetQpcc");
        rd.pccTerminal = sm.getId("PccTerminal");
        mapping.put(vscConverterId, rd);
    }

    private static VscRegulation decodeVscRegulation(String qPccControl) {
        if (qPccControl != null) {
            if (qPccControl.endsWith("voltagePcc")) {
                return VscRegulation.VOLTAGE;
            } else if (qPccControl.endsWith("reactivePcc")) {
                return VscRegulation.REACTIVE_POWER;
            }
        }
        return null;
    }

    void applyRegulatingControls(Network network) {
        network.getVscConverterStationStream().forEach(this::apply);
    }

    private void apply(VscConverterStation vscConverter) {
        CgmesRegulatingControlForVscConverter rd = mapping.get(vscConverter.getId());
        apply(vscConverter, rd);
    }

    private void apply(VscConverterStation vscConverter, CgmesRegulatingControlForVscConverter rc) {
        if (rc == null) {
            return;
        }

        VscRegulation vscRegulation = decodeVscRegulation(rc.vscRegulation);
        if (vscRegulation == VscRegulation.VOLTAGE) {
            setRegulatingControlVoltage(rc, vscConverter);
        } else if (vscRegulation == VscRegulation.REACTIVE_POWER) {
            setRegulatingControlReactivePower(rc, vscConverter);
        } else {
            context.ignored(rc.vscRegulation, "Unsupported regulation mode for vscConverter " + vscConverter.getId());
        }
    }

    private void setRegulatingControlVoltage(CgmesRegulatingControlForVscConverter rc, VscConverterStation vscConverter) {

        vscConverter
            .setVoltageSetpoint(rc.voltageSetpoint)
            .setReactivePowerSetpoint(0.0)
            .setRegulatingTerminal(parent.getRegulatingTerminal(vscConverter, rc.pccTerminal))
            .setVoltageRegulatorOn(true);
    }

    private void setRegulatingControlReactivePower(CgmesRegulatingControlForVscConverter rc, VscConverterStation vscConverter) {

        vscConverter
            .setVoltageSetpoint(0.0)
            .setReactivePowerSetpoint(rc.reactivePowerSetpoint)
            .setRegulatingTerminal(parent.getRegulatingTerminal(vscConverter, rc.pccTerminal))
            .setVoltageRegulatorOn(false);
    }

    private static class CgmesRegulatingControlForVscConverter {
        String vscRegulation;
        double voltageSetpoint;
        double reactivePowerSetpoint;
        String pccTerminal;
    }

    private final RegulatingControlMapping parent;
    private final Map<String, CgmesRegulatingControlForVscConverter> mapping;
    private final Context context;
}
