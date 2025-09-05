/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements.dc;

import com.powsybl.cgmes.conversion.CgmesReports;
import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.elements.AbstractReactiveLimitsOwnerConversion;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.*;
import com.powsybl.triplestore.api.PropertyBag;

import static com.powsybl.cgmes.model.CgmesNames.VS_CONVERTER;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */

public class AcDcConverterConversion extends AbstractReactiveLimitsOwnerConversion {

    int numberOfAcTerminals;
    Terminal pccTerminal;

    public AcDcConverterConversion(PropertyBag p, Context context) {
        super(CgmesNames.ACDC_CONVERTER, p, context);
        numberOfAcTerminals = 1;
    }

    public AcDcConverterConversion(PropertyBag p, Context context, int numberOfTerminals) {
        super(CgmesNames.ACDC_CONVERTER, p, context, numberOfTerminals);
        this.numberOfAcTerminals = numberOfTerminals;
    }

    @Override
    public boolean valid() {
        // Check AC connection(s).
        if (!super.valid()) {
            return false;
        }

        // Check DC connections.
        String dcNode1 = context.dcMapping().getDcNode(p.getId(CgmesNames.DC_TERMINAL1));
        String dcNode2 = context.dcMapping().getDcNode(p.getId(CgmesNames.DC_TERMINAL2));
        if (dcNode1 == null || dcNode2 == null) {
            return false;
        }

        // Check PCC terminal.
        if (!validPccTerminal()) {
            CgmesReports.invalidPccTerminalReport(context.getReportNode(), id);
            return false;
        }

        return true;
    }

    private boolean validPccTerminal() {
        String pccTerminalId = p.getId("PccTerminal");
        if (numberOfAcTerminals == 1 && (pccTerminalId == null || pccTerminalId.equals(p.getId(CgmesNames.TERMINAL)))) {
            // PCC terminal is the local terminal
            return true;
        } else if (numberOfAcTerminals != 1 && pccTerminalId == null) {
            return false;
        }

        Terminal mappedPccTerminal = context.terminalMapping().get(pccTerminalId);
        if (mappedPccTerminal == null) {
            return false;
        }

        Connectable<?> pccEquipment = mappedPccTerminal.getConnectable();
        if (pccEquipment instanceof Branch<?> || pccEquipment instanceof ThreeWindingsTransformer) {
            pccTerminal = mappedPccTerminal;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void convert() {
        if (VS_CONVERTER.equals(p.getLocal("type"))) {
            VoltageSourceConverterAdder vscAdder = voltageLevel().newVoltageSourceConverter();
            commonAcDcConvert(vscAdder);
            setReactivePowerControl(vscAdder);
            VoltageSourceConverter vsc = vscAdder.add();

            commonPostAcDcConvert(vsc);
            convertReactiveLimits(vsc);
        } else {
            LineCommutatedConverterAdder lccAdder = voltageLevel().newLineCommutatedConverter();
            commonAcDcConvert(lccAdder);
            setPowerFactor(lccAdder);
            LineCommutatedConverter lcc = lccAdder.add();

            commonPostAcDcConvert(lcc);
        }
    }

    private void commonAcDcConvert(AcDcConverterAdder<?, ?> adder) {
        identify(adder);
        connect(adder);
        connectDc(adder);
        setLoss(adder);
        setActivePowerControl(adder);
    }

    private void connectDc(AcDcConverterAdder<?, ?> adder) {
        String dcTerminal1 = p.getId(CgmesNames.DC_TERMINAL1);
        adder.setDcNode1(context.dcMapping().getDcNode(dcTerminal1));
        adder.setDcConnected1(context.dcMapping().isConnected(dcTerminal1));

        String dcTerminal2 = p.getId(CgmesNames.DC_TERMINAL2);
        adder.setDcNode2(context.dcMapping().getDcNode(dcTerminal2));
        adder.setDcConnected2(context.dcMapping().isConnected(dcTerminal2));
    }

    private void setLoss(AcDcConverterAdder<?, ?> adder) {
        adder.setIdleLoss(p.asPositiveDouble("idleLoss"));
        adder.setSwitchingLoss(p.asPositiveDouble("switchingLoss"));
        adder.setResistiveLoss(p.asPositiveDouble("resistiveLoss"));
    }

    private void setActivePowerControl(AcDcConverterAdder<?, ?> adder) {
        // Set control mode and associated target.
        // In case of inconsistent or missing data, the default is 0 MW active power at pcc terminal.
        AcDcConverter.ControlMode controlMode;
        String pPccControl = p.getLocal("pPccControl");
        if (pPccControl != null && (pPccControl.endsWith("dcVoltage") || pPccControl.endsWith("udc"))) {
            controlMode = AcDcConverter.ControlMode.V_DC;
        } else {
            controlMode = AcDcConverter.ControlMode.P_PCC;
        }
        adder.setControlMode(controlMode);

        if (controlMode == AcDcConverter.ControlMode.P_PCC) {
            double targetPpcc = p.asDouble("targetPpcc");
            targetPpcc = Double.isNaN(targetPpcc) ? 0.0 : targetPpcc;
            adder.setTargetP(targetPpcc);
        } else {
            adder.setTargetVdc(p.asDouble("targetUdc"));
        }

        if (pccTerminal != null) {
            adder.setPccTerminal(pccTerminal);
        }
    }

    private void setReactivePowerControl(VoltageSourceConverterAdder adder) {
        String qPccControl = p.getLocal("qPccControl");
        double voltageSetpoint = p.asDouble("targetUpcc");
        double reactivePowerSetpoint = p.asDouble("targetQpcc");
        reactivePowerSetpoint = Double.isNaN(reactivePowerSetpoint) ? 0.0 : reactivePowerSetpoint;

        if (qPccControl != null && qPccControl.endsWith("voltagePcc") && !Double.isNaN(voltageSetpoint)) {
            adder.setVoltageSetpoint(voltageSetpoint)
                    .setVoltageRegulatorOn(true);
        } else {
            adder.setReactivePowerSetpoint(reactivePowerSetpoint)
                    .setVoltageRegulatorOn(false);
        }
    }

    private void setPowerFactor(LineCommutatedConverterAdder adder) {
        double pSsh = p.asDouble("p");
        double qSsh = p.asDouble("q");
        double powerFactor = Math.abs(pSsh / Math.hypot(pSsh, qSsh));
        if (Double.isNaN(powerFactor)) {
            // Default power factor calculated with Q = P / 2
            powerFactor = 1.0 / Math.hypot(1.0, 0.5);
        }
        adder.setPowerFactor(powerFactor);
    }

    private void commonPostAcDcConvert(AcDcConverter<?> converter) {
        convertedTerminals(converter);
        addTerminalsAlias(converter);
    }

    private void convertedTerminals(AcDcConverter<?> converter) {
        if (numberOfAcTerminals == 1) {
            convertedTerminals(converter.getTerminal1());
        } else {
            convertedTerminals(converter.getTerminal1(), converter.getTerminal2().orElseThrow());
        }
    }

    private void addTerminalsAlias(AcDcConverter<?> converter) {
        if (numberOfAcTerminals == 1) {
            converter.addAlias(p.getId(CgmesNames.TERMINAL), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL1, context.config().isEnsureIdAliasUnicity());
        } else {
            converter.addAlias(p.getId(CgmesNames.TERMINAL1), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL1, context.config().isEnsureIdAliasUnicity());
            converter.addAlias(p.getId(CgmesNames.TERMINAL2), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL2, context.config().isEnsureIdAliasUnicity());
        }
        converter.addAlias(p.getId(CgmesNames.DC_TERMINAL1), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.DC_TERMINAL1, context.config().isEnsureIdAliasUnicity());
        converter.addAlias(p.getId(CgmesNames.DC_TERMINAL2), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.DC_TERMINAL2, context.config().isEnsureIdAliasUnicity());
    }

}
