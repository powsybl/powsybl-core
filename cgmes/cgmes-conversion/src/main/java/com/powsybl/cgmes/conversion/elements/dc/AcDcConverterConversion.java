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

import java.util.Optional;

import static com.powsybl.cgmes.conversion.elements.dc.AbstractDCConductingEquipmentConversion.isDcTerminalConnected;
import static com.powsybl.cgmes.model.CgmesNames.VS_CONVERTER;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */
public class AcDcConverterConversion extends AbstractReactiveLimitsOwnerConversion {

    int numberOfAcTerminals;
    Terminal pccTerminal;
    static final double DEFAULT_POWER_FACTOR = 1.0 / Math.hypot(1.0, 0.5);  // Default power factor calculated with Q = P / 2

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
        }

        return true;
    }

    private boolean validPccTerminal() {
        String pccTerminalId = p.getId("PccTerminal");
        if (pccTerminalId == null || pccTerminalId.equals(p.getId(CgmesNames.TERMINAL))) {
            // PCC terminal is the local terminal.
            return true;
        }

        Terminal mappedPccTerminal = context.terminalMapping().get(pccTerminalId);
        if (mappedPccTerminal == null) {
            // The CGMES PCC terminal doesn't have an IIDM equivalent.
            // That happens for instance when it is the terminal of a Switch.
            // In that case, make the PCC terminal the local terminal.
            return false;
        }

        Connectable<?> pccEquipment = mappedPccTerminal.getConnectable();
        if (pccEquipment instanceof Branch<?> || pccEquipment instanceof ThreeWindingsTransformer) {
            pccTerminal = mappedPccTerminal;
            return true;
        }

        return false;
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
        connectWithOnlyEq(adder);
        connectDcWithOnlyEq(adder);
        setLoss(adder);
        setActivePowerControl(adder);
    }

    private void connectDcWithOnlyEq(AcDcConverterAdder<?, ?> adder) {
        String dcTerminal1 = p.getId(CgmesNames.DC_TERMINAL1);
        adder.setDcNode1(context.dcMapping().getDcNode(dcTerminal1));
        adder.setDcConnected1(true);

        String dcTerminal2 = p.getId(CgmesNames.DC_TERMINAL2);
        adder.setDcNode2(context.dcMapping().getDcNode(dcTerminal2));
        adder.setDcConnected2(true);
    }

    private void setLoss(AcDcConverterAdder<?, ?> adder) {
        adder.setIdleLoss(p.asPositiveDouble("idleLoss"));
        adder.setSwitchingLoss(p.asPositiveDouble("switchingLoss"));
        adder.setResistiveLoss(p.asPositiveDouble("resistiveLoss"));
    }

    private void setActivePowerControl(AcDcConverterAdder<?, ?> adder) {
        // The default is 0 MW active power at pcc terminal.
        adder.setControlMode(AcDcConverter.ControlMode.P_PCC);
        adder.setTargetP(0.0);

        if (pccTerminal != null) {
            adder.setPccTerminal(pccTerminal);
        }
    }

    private void setReactivePowerControl(VoltageSourceConverterAdder adder) {
        // The default is 0 MVar reactive power at pcc terminal.
        adder.setReactivePowerSetpoint(0.0)
                .setVoltageRegulatorOn(false);
    }

    private void setPowerFactor(LineCommutatedConverterAdder adder) {
        adder.setPowerFactor(DEFAULT_POWER_FACTOR);
    }

    private void commonPostAcDcConvert(AcDcConverter<?> converter) {
        convertedTerminals(converter);
        addAliasesAndProperties(converter);
    }

    private void convertedTerminals(AcDcConverter<?> converter) {
        if (numberOfAcTerminals == 1) {
            convertedTerminalsWithOnlyEq(converter.getTerminal1());
        } else {
            convertedTerminalsWithOnlyEq(converter.getTerminal1(), converter.getTerminal2().orElseThrow());
        }
    }

    private void addAliasesAndProperties(AcDcConverter<?> converter) {
        if (numberOfAcTerminals == 1) {
            converter.addAlias(p.getId(CgmesNames.TERMINAL), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL1, context.config().isEnsureIdAliasUnicity());
        } else {
            converter.addAlias(p.getId(CgmesNames.TERMINAL1), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL1, context.config().isEnsureIdAliasUnicity());
            converter.addAlias(p.getId(CgmesNames.TERMINAL2), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL2, context.config().isEnsureIdAliasUnicity());
        }
        converter.addAlias(p.getId(CgmesNames.DC_TERMINAL1), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.DC_TERMINAL1, context.config().isEnsureIdAliasUnicity());
        converter.addAlias(p.getId(CgmesNames.DC_TERMINAL2), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.DC_TERMINAL2, context.config().isEnsureIdAliasUnicity());
        converter.setProperty(Conversion.PROPERTY_CGMES_DC_CONVERTER_UNIT, p.getId("DCConverterUnit"));
    }

    public static void update(AcDcConverter<?> converter, PropertyBag cgmesData, Context context) {
        updateDcConnect(converter, context);
        updateActivePowerControl(converter, cgmesData, context);
        if (converter instanceof VoltageSourceConverter voltageSourceConverter) {
            updateReactivePowerControl(voltageSourceConverter, cgmesData, context);
        } else if (converter instanceof LineCommutatedConverter lineCommutatedConverter) {
            updatePowerFactor(lineCommutatedConverter, cgmesData, context);
        }
    }

    private static void updateDcConnect(AcDcConverter<?> converter, Context context) {
        Optional<Boolean> dcTerminalConnected1 = isDcTerminalConnected(converter, TwoSides.ONE, context);
        boolean defaultConnected1 = getDefaultValue(null, converter.getDcTerminal1().isConnected(), true, true, context);
        boolean connected1 = dcTerminalConnected1.orElse(defaultConnected1);

        Optional<Boolean> dcTerminalConnected2 = isDcTerminalConnected(converter, TwoSides.TWO, context);
        boolean defaultConnected2 = getDefaultValue(null, converter.getDcTerminal2().isConnected(), true, true, context);
        boolean connected2 = dcTerminalConnected2.orElse(defaultConnected2);

        converter.getDcTerminal1().setConnected(connected1);
        converter.getDcTerminal2().setConnected(connected2);
    }

    private static void updateActivePowerControl(AcDcConverter<?> converter, PropertyBag cgmesData, Context context) {
        // Retrieve control mode.
        AcDcConverter.ControlMode controlMode;
        String pPccControl = cgmesData.getLocal("pPccControl");
        if (pPccControl != null && (pPccControl.endsWith("activePower") || pPccControl.endsWith("pPcc"))) {
            controlMode = AcDcConverter.ControlMode.P_PCC;
        } else if (pPccControl != null && (pPccControl.endsWith("dcVoltage") || pPccControl.endsWith("udc"))) {
            controlMode = AcDcConverter.ControlMode.V_DC;
        } else {
            boolean isDefaultControlModePpcc = getDefaultValue(null, converter.getControlMode() == AcDcConverter.ControlMode.P_PCC, true, true, context);
            controlMode = isDefaultControlModePpcc ? AcDcConverter.ControlMode.P_PCC : AcDcConverter.ControlMode.V_DC;
        }

        if (controlMode == AcDcConverter.ControlMode.P_PCC) {
            double defaultTargetPpcc = getDefaultValue(null, converter.getTargetP(), 0.0, Double.NaN, context);
            double targetPpcc = cgmesData.asDouble("targetPpcc");
            double validTargetPpcc = isValidTargetValue(targetPpcc) ? targetPpcc : defaultTargetPpcc;

            // TargetPpcc must be valid before enabling regulation
            converter.setTargetP(validTargetPpcc)
                    .setControlMode(AcDcConverter.ControlMode.P_PCC)
                    .setTargetVdc(Double.NaN);
        } else {
            double defaultTargetVdc = getDefaultValue(null, converter.getTargetVdc(), converter.getDcTerminal1().getDcNode().getNominalV(), Double.NaN, context);
            double targetVdc = cgmesData.asDouble("targetUdc");
            double validTargetVdc = isValidTargetV(targetVdc) ? targetVdc : defaultTargetVdc;

            // TargetVdc must be valid before enabling regulation
            converter.setTargetVdc(validTargetVdc)
                    .setControlMode(AcDcConverter.ControlMode.V_DC)
                    .setTargetP(Double.NaN);
        }
    }

    private static void updateReactivePowerControl(VoltageSourceConverter vsc, PropertyBag cgmesData, Context context) {
        String qPccControl = cgmesData.getLocal("qPccControl");
        if (qPccControl != null && qPccControl.endsWith("voltagePcc")) {
            double defaultVoltageSetpoint = getDefaultValue(null, vsc.getVoltageSetpoint(), vsc.getTerminal1().getVoltageLevel().getNominalV(), Double.NaN, context);
            double voltageSetpoint = cgmesData.asDouble("targetUpcc");
            double validVoltageSetpoint = isValidTargetV(voltageSetpoint) ? voltageSetpoint : defaultVoltageSetpoint;

            // VoltageSetpoint must be valid before enabling regulation
            vsc.setVoltageSetpoint(validVoltageSetpoint)
                    .setVoltageRegulatorOn(true)
                    .setReactivePowerSetpoint(Double.NaN);
        } else if (qPccControl != null && qPccControl.endsWith("reactivePcc")) {
            double defaultReactivePowerSetpoint = getDefaultValue(null, vsc.getReactivePowerSetpoint(), 0.0, Double.NaN, context);
            double reactivePowerSetpoint = cgmesData.asDouble("targetQpcc");
            double validReactivePowerSetpoint = isValidTargetValue(reactivePowerSetpoint) ? reactivePowerSetpoint : defaultReactivePowerSetpoint;

            // ReactivePowerSetpoint must be valid before enabling regulation
            vsc.setReactivePowerSetpoint(validReactivePowerSetpoint)
                    .setVoltageRegulatorOn(false)
                    .setVoltageSetpoint(Double.NaN);
        }
    }

    private static void updatePowerFactor(LineCommutatedConverter lcc, PropertyBag cgmesData, Context context) {
        double defaultPowerFactor = getDefaultValue(null, lcc.getPowerFactor(), DEFAULT_POWER_FACTOR, Double.NaN, context);
        double pSsh = cgmesData.asDouble("p");
        double qSsh = cgmesData.asDouble("q");
        double powerFactor = Math.abs(pSsh / Math.hypot(pSsh, qSsh));
        double validPowerFactor = isValidPowerFactor(powerFactor) ? powerFactor : defaultPowerFactor;
        lcc.setPowerFactor(validPowerFactor);
    }

}
