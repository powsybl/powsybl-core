/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.conversion.export.CgmesRecords.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import static com.powsybl.cgmes.model.CgmesNames.*;

/**
 * Writes the CGMES objects of the SSH profile.
 *
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public final class SteadyStateHypothesisWriter {

    private static final String ROTATING_MACHINE_P = ROTATING_MACHINE + ".p";
    private static final String ROTATING_MACHINE_Q = ROTATING_MACHINE + ".q";
    private static final String ASYNCHRONOUS_MACHINE_TYPE = ASYNCHRONOUS_MACHINE + ".asynchronousMachineType";
    private static final String ASYNCHRONOUS_MACHINE_KIND = "AsynchronousMachineKind.";
    private static final String CONTROL_AREA_NET_INTERCHANGE = CONTROL_AREA + ".netInterchange";
    private static final String CONTROL_AREA_P_TOLERANCE = CONTROL_AREA + "." + P_TOLERANCE;
    private static final String CS_CONVERTER_TARGET_ALPHA = CS_CONVERTER + ".targetAlpha";
    private static final String CS_CONVERTER_TARGET_GAMMA = CS_CONVERTER + ".targetGamma";
    private static final String CS_CONVERTER_TARGET_IDC = CS_CONVERTER + ".targetIdc";
    private static final String CS_CONVERTER_OPERATING_MODE = CS_CONVERTER + ".operatingMode";
    private static final String CS_OPERATING_MODE_KIND = "CsOperatingModeKind.";
    private static final String CS_CONVERTER_P_PCC_CONTROL = CS_CONVERTER + ".pPccControl";
    private static final String CS_PPCC_CONTROL_KIND = "CsPpccControlKind.";
    private static final String AC_DC_TERMINAL_CONNECTED = AC_DC_TERMINAL + "." + CONNECTED;
    private static final String ENERGY_CONSUMER_P = ENERGY_CONSUMER + ".p";
    private static final String ENERGY_CONSUMER_Q = ENERGY_CONSUMER + ".q";
    private static final String ENERGY_SOURCE_ACTIVE_POWER = ENERGY_SOURCE + ".activePower";
    private static final String ENERGY_SOURCE_REACTIVE_POWER = ENERGY_SOURCE + ".reactivePower";
    private static final String EQUIVALENT_INJECTION_P = EQUIVALENT_INJECTION + ".p";
    private static final String EQUIVALENT_INJECTION_Q = EQUIVALENT_INJECTION + ".q";
    private static final String EQUIVALENT_INJECTION_REGULATION_STATUS = EQUIVALENT_INJECTION + "." + REGULATION_STATUS;
    private static final String EQUIVALENT_INJECTION_REGULATION_TARGET = EQUIVALENT_INJECTION + "." + REGULATION_TARGET;
    private static final String EXTERNAL_NETWORK_INJECTION_P = EXTERNAL_NETWORK_INJECTION + ".p";
    private static final String EXTERNAL_NETWORK_INJECTION_Q = EXTERNAL_NETWORK_INJECTION + ".q";
    private static final String EXTERNAL_NETWORK_INJECTION_REFERENCE_PRIORITY = EXTERNAL_NETWORK_INJECTION + ".referencePriority";
    private static final String GENERATING_UNIT_NORMAL_PF = GENERATING_UNIT + "." + NORMAL_PF;
    private static final String REGULATING_CONTROL_DISCRETE = REGULATING_CONTROL + ".discrete";
    private static final String REGULATING_CONTROL_ENABLED = REGULATING_CONTROL + "." + ENABLED;
    private static final String REGULATING_CONTROL_TARGET_DEADBAND = REGULATING_CONTROL + "." + TARGET_DEADBAND;
    private static final String REGULATING_CONTROL_TARGET_VALUE = REGULATING_CONTROL + "." + TARGET_VALUE;
    private static final String REGULATING_CONTROL_TARGET_VALUE_UNIT_MULTIPLIER = REGULATING_CONTROL + ".targetValueUnitMultiplier";
    private static final String UNIT_MULTIPLIER = "UnitMultiplier.";
    private static final String SHUNT_COMPENSATOR_SECTIONS = SHUNT_COMPENSATOR + ".sections";
    private static final String STATIC_VAR_COMPENSATOR_Q = STATIC_VAR_COMPENSATOR + ".q";
    private static final String SWITCH_OPEN = SWITCH + "." + OPEN;
    private static final String SYNCHRONOUS_MACHINE_REFERENCE_PRIORITY = SYNCHRONOUS_MACHINE + ".referencePriority";
    private static final String SYNCHRONOUS_MACHINE_OPERATING_MODE = SYNCHRONOUS_MACHINE + ".operatingMode";
    private static final String SYNCHRONOUS_MACHINE_OPERATING_MODE_KIND = "SynchronousMachineOperatingMode.";
    private static final String TAP_CHANGER_CONTROL_ENABLED = TAP_CHANGER + "." + CONTROL_ENABLED;
    private static final String TAP_CHANGER_STEP = TAP_CHANGER + "." + STEP;
    private static final String VS_CONVERTER_DROOP = VS_CONVERTER + ".droop";
    private static final String VS_CONVERTER_DROOP_COMPENSATION = VS_CONVERTER + ".droopCompensation";
    private static final String VS_CONVERTER_Q_SHARE = VS_CONVERTER + ".qShare";
    private static final String VS_CONVERTER_TARGET_QPCC = VS_CONVERTER + ".targetQpcc";
    private static final String VS_CONVERTER_TARGET_UPCC = VS_CONVERTER + ".targetUpcc";
    private static final String VS_CONVERTER_P_PCC_CONTROL = VS_CONVERTER + ".pPccControl";
    private static final String VS_PPCC_CONTROL_KIND = "VsPpccControlKind.";
    private static final String VS_CONVERTER_Q_PCC_CONTROL = VS_CONVERTER + ".qPccControl";
    private static final String VS_QPCC_CONTROL_KIND = "VsQpccControlKind.";
    private static final String ACDC_CONVERTER_TARGET_PPCC = ACDC_CONVERTER + ".targetPpcc";
    private static final String ACDC_CONVERTER_TARGET_UDC = ACDC_CONVERTER + ".targetUdc";
    private static final String ACDC_CONVERTER_P = ACDC_CONVERTER + ".p";
    private static final String ACDC_CONVERTER_Q = ACDC_CONVERTER + ".q";

    private SteadyStateHypothesisWriter() {
    }

    public static void writeAsynchronousMachine(AsynchronousMachineSsh asynchronousMachine, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String namespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartAbout(ASYNCHRONOUS_MACHINE, asynchronousMachine.rdfId(), writer, context);
        CgmesExportUtil.writeAttribute(namespace, ROTATING_MACHINE_P, CgmesExportUtil.format(asynchronousMachine.p()), writer);
        CgmesExportUtil.writeAttribute(namespace, ROTATING_MACHINE_Q, CgmesExportUtil.format(asynchronousMachine.q()), writer);
        CgmesExportUtil.writeAttribute(namespace, REGULATING_COND_EQ_CONTROL_ENABLED, CgmesExportUtil.format(asynchronousMachine.controlEnabled()), writer);
        CgmesExportUtil.writeResource(namespace, ASYNCHRONOUS_MACHINE_TYPE,
            namespace + ASYNCHRONOUS_MACHINE_KIND + asynchronousMachine.asynchronousMachineType(), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    public static void writeControlArea(ControlAreaSsh controlArea, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String namespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartAbout(CONTROL_AREA, controlArea.rdfId(), writer, context);
        CgmesExportUtil.writeAttribute(namespace, CONTROL_AREA_NET_INTERCHANGE, CgmesExportUtil.format(controlArea.netInterchange()), writer);
        if (controlArea.pTolerance() != null) {
            CgmesExportUtil.writeAttribute(namespace, CONTROL_AREA_P_TOLERANCE, CgmesExportUtil.format(controlArea.pTolerance()), writer);
        }
        CgmesExportUtil.writeEnd(writer);
    }

    public static void writeCsConverter(CsConverterSsh csConverter, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String namespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartAbout(CS_CONVERTER, csConverter.rdfId(), writer, context);
        writeAcDcConverter(csConverter.targetPpcc(), csConverter.targetUdc(), csConverter.p(), csConverter.q(), writer, context);
        CgmesExportUtil.writeAttribute(namespace, CS_CONVERTER_TARGET_ALPHA, CgmesExportUtil.format(csConverter.targetAlpha()), writer);
        CgmesExportUtil.writeAttribute(namespace, CS_CONVERTER_TARGET_GAMMA, CgmesExportUtil.format(csConverter.targetGamma()), writer);
        CgmesExportUtil.writeAttribute(namespace, CS_CONVERTER_TARGET_IDC, CgmesExportUtil.format(csConverter.targetIdc()), writer);
        CgmesExportUtil.writeResource(namespace, CS_CONVERTER_OPERATING_MODE,
            namespace + CS_OPERATING_MODE_KIND + csConverter.operatingMode(), writer);
        CgmesExportUtil.writeResource(namespace, CS_CONVERTER_P_PCC_CONTROL,
            namespace + CS_PPCC_CONTROL_KIND + csConverter.pPccControl(), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    public static void writeDCTerminal(DCTerminalSsh dcTerminal, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String namespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartAbout(dcTerminal.className(), dcTerminal.rdfId(), writer, context);
        CgmesExportUtil.writeAttribute(namespace, AC_DC_TERMINAL_CONNECTED, CgmesExportUtil.format(dcTerminal.connected()), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    public static void writeEnergyConsumer(EnergyConsumerSsh energyConsumer, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String namespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartAbout(energyConsumer.className(), energyConsumer.rdfId(), writer, context);
        CgmesExportUtil.writeAttribute(namespace, ENERGY_CONSUMER_P, CgmesExportUtil.format(energyConsumer.p()), writer);
        CgmesExportUtil.writeAttribute(namespace, ENERGY_CONSUMER_Q, CgmesExportUtil.format(energyConsumer.q()), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    public static void writeEnergySource(EnergySourceSsh energySource, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String namespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartAbout(ENERGY_SOURCE, energySource.rdfId(), writer, context);
        CgmesExportUtil.writeAttribute(namespace, ENERGY_SOURCE_ACTIVE_POWER, CgmesExportUtil.format(energySource.activePower()), writer);
        CgmesExportUtil.writeAttribute(namespace, ENERGY_SOURCE_REACTIVE_POWER, CgmesExportUtil.format(energySource.reactivePower()), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    public static void writeEquivalentInjection(EquivalentInjectionSsh equivalentInjection, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String namespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartAbout(EQUIVALENT_INJECTION, equivalentInjection.rdfId(), writer, context);
        CgmesExportUtil.writeAttribute(namespace, EQUIVALENT_INJECTION_P, CgmesExportUtil.format(equivalentInjection.p()), writer);
        CgmesExportUtil.writeAttribute(namespace, EQUIVALENT_INJECTION_Q, CgmesExportUtil.format(equivalentInjection.q()), writer);
        CgmesExportUtil.writeAttribute(namespace, EQUIVALENT_INJECTION_REGULATION_STATUS, CgmesExportUtil.format(equivalentInjection.regulationStatus()), writer);
        CgmesExportUtil.writeAttribute(namespace, EQUIVALENT_INJECTION_REGULATION_TARGET, CgmesExportUtil.format(equivalentInjection.regulationTarget()), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    public static void writeExternalNetworkInjection(ExternalNetworkInjectionSsh externalNetworkInjection, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String namespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartAbout(EXTERNAL_NETWORK_INJECTION, externalNetworkInjection.rdfId(), writer, context);
        CgmesExportUtil.writeAttribute(namespace, REGULATING_COND_EQ_CONTROL_ENABLED, CgmesExportUtil.format(externalNetworkInjection.controlEnabled()), writer);
        CgmesExportUtil.writeAttribute(namespace, EXTERNAL_NETWORK_INJECTION_P, CgmesExportUtil.format(externalNetworkInjection.p()), writer);
        CgmesExportUtil.writeAttribute(namespace, EXTERNAL_NETWORK_INJECTION_Q, CgmesExportUtil.format(externalNetworkInjection.q()), writer);
        // reference priority is used for angle reference selection (slack)
        CgmesExportUtil.writeAttribute(namespace, EXTERNAL_NETWORK_INJECTION_REFERENCE_PRIORITY, CgmesExportUtil.format(externalNetworkInjection.referencePriority()), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    public static void writeGeneratingUnit(GeneratingUnitSsh generatingUnit, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String namespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartAbout(generatingUnit.className(), generatingUnit.rdfId(), writer, context);
        CgmesExportUtil.writeAttribute(namespace, GENERATING_UNIT_NORMAL_PF, CgmesExportUtil.format(generatingUnit.normalPF()), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    public static void writeRegulatingControl(RegulatingControlSsh regulatingControl, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String namespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartAbout(regulatingControl.className(), regulatingControl.rdfId(), writer, context);
        CgmesExportUtil.writeAttribute(namespace, REGULATING_CONTROL_DISCRETE, CgmesExportUtil.format(regulatingControl.discrete()), writer);
        CgmesExportUtil.writeAttribute(namespace, REGULATING_CONTROL_ENABLED, CgmesExportUtil.format(regulatingControl.enabled()), writer);
        if (CgmesExportUtil.targetDeadbandIsDefined(regulatingControl.targetDeadband())) {
            CgmesExportUtil.writeAttribute(namespace, REGULATING_CONTROL_TARGET_DEADBAND, CgmesExportUtil.format(regulatingControl.targetDeadband()), writer);
        }
        CgmesExportUtil.writeAttribute(namespace, REGULATING_CONTROL_TARGET_VALUE, CgmesExportUtil.format(regulatingControl.targetValue()), writer);
        CgmesExportUtil.writeResource(namespace, REGULATING_CONTROL_TARGET_VALUE_UNIT_MULTIPLIER,
            namespace + UNIT_MULTIPLIER + regulatingControl.targetValueUnitMultiplier(), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    public static void writeShuntCompensator(ShuntCompensatorSsh shuntCompensator, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String namespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartAbout(shuntCompensator.className(), shuntCompensator.rdfId(), writer, context);
        CgmesExportUtil.writeAttribute(namespace, SHUNT_COMPENSATOR_SECTIONS, CgmesExportUtil.format(shuntCompensator.sections()), writer);
        CgmesExportUtil.writeAttribute(namespace, REGULATING_COND_EQ_CONTROL_ENABLED, CgmesExportUtil.format(shuntCompensator.controlEnabled()), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    public static void writeStaticVarCompensator(StaticVarCompensatorSsh staticVarCompensator, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String namespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartAbout(STATIC_VAR_COMPENSATOR, staticVarCompensator.rdfId(), writer, context);
        CgmesExportUtil.writeAttribute(namespace, REGULATING_COND_EQ_CONTROL_ENABLED, CgmesExportUtil.format(staticVarCompensator.controlEnabled()), writer);
        CgmesExportUtil.writeAttribute(namespace, STATIC_VAR_COMPENSATOR_Q, CgmesExportUtil.format(staticVarCompensator.q()), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    public static void writeSwitch(SwitchSsh switchSsh, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String namespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartAbout(switchSsh.className(), switchSsh.rdfId(), writer, context);
        CgmesExportUtil.writeAttribute(namespace, SWITCH_OPEN, CgmesExportUtil.format(switchSsh.open()), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    public static void writeSynchronousMachine(SynchronousMachineSsh synchronousMachine, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String namespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartAbout(SYNCHRONOUS_MACHINE, synchronousMachine.rdfId(), writer, context);
        CgmesExportUtil.writeAttribute(namespace, REGULATING_COND_EQ_CONTROL_ENABLED, CgmesExportUtil.format(synchronousMachine.controlEnabled()), writer);
        CgmesExportUtil.writeAttribute(namespace, ROTATING_MACHINE_P, CgmesExportUtil.format(synchronousMachine.p()), writer);
        CgmesExportUtil.writeAttribute(namespace, ROTATING_MACHINE_Q, CgmesExportUtil.format(synchronousMachine.q()), writer);
        // reference priority is used for angle reference selection (slack)
        CgmesExportUtil.writeAttribute(namespace, SYNCHRONOUS_MACHINE_REFERENCE_PRIORITY, CgmesExportUtil.format(synchronousMachine.referencePriority()), writer);
        CgmesExportUtil.writeResource(namespace, SYNCHRONOUS_MACHINE_OPERATING_MODE,
            namespace + SYNCHRONOUS_MACHINE_OPERATING_MODE_KIND + synchronousMachine.operatingMode(), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    public static void writeTapChanger(TapChangerSsh tapChanger, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String namespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartAbout(tapChanger.className(), tapChanger.rdfId(), writer, context);
        CgmesExportUtil.writeAttribute(namespace, TAP_CHANGER_CONTROL_ENABLED, CgmesExportUtil.format(tapChanger.controlEnabled()), writer);
        CgmesExportUtil.writeAttribute(namespace, TAP_CHANGER_STEP, CgmesExportUtil.format(tapChanger.step()), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    public static void writeTerminal(TerminalSsh terminal, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String namespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartAbout(TERMINAL, terminal.rdfId(), writer, context);
        CgmesExportUtil.writeAttribute(namespace, AC_DC_TERMINAL_CONNECTED, CgmesExportUtil.format(terminal.connected()), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    public static void writeVsConverter(VsConverterSsh vsConverter, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String namespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartAbout(VS_CONVERTER, vsConverter.rdfId(), writer, context);
        writeAcDcConverter(vsConverter.targetPpcc(), vsConverter.targetUdc(), vsConverter.p(), vsConverter.q(), writer, context);
        CgmesExportUtil.writeAttribute(namespace, VS_CONVERTER_DROOP, CgmesExportUtil.format(vsConverter.droop()), writer);
        CgmesExportUtil.writeAttribute(namespace, VS_CONVERTER_DROOP_COMPENSATION, CgmesExportUtil.format(vsConverter.droopCompensation()), writer);
        CgmesExportUtil.writeAttribute(namespace, VS_CONVERTER_Q_SHARE, CgmesExportUtil.format(vsConverter.qShare()), writer);
        CgmesExportUtil.writeAttribute(namespace, VS_CONVERTER_TARGET_QPCC, CgmesExportUtil.format(vsConverter.targetQpcc()), writer);
        CgmesExportUtil.writeAttribute(namespace, VS_CONVERTER_TARGET_UPCC, CgmesExportUtil.format(vsConverter.targetUpcc()), writer);
        CgmesExportUtil.writeResource(namespace, VS_CONVERTER_P_PCC_CONTROL,
            namespace + VS_PPCC_CONTROL_KIND + vsConverter.pPccControl(), writer);
        CgmesExportUtil.writeResource(namespace, VS_CONVERTER_Q_PCC_CONTROL,
            namespace + VS_QPCC_CONTROL_KIND + vsConverter.qPccControl(), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    private static void writeAcDcConverter(double targetPpcc, double targetUdc, double p, double q,
                                           XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String namespace = context.getCim().getNamespace();
        CgmesExportUtil.writeAttribute(namespace, ACDC_CONVERTER_TARGET_PPCC, CgmesExportUtil.format(targetPpcc), writer);
        CgmesExportUtil.writeAttribute(namespace, ACDC_CONVERTER_TARGET_UDC, CgmesExportUtil.format(targetUdc), writer);
        CgmesExportUtil.writeAttribute(namespace, ACDC_CONVERTER_P, CgmesExportUtil.format(p), writer);
        CgmesExportUtil.writeAttribute(namespace, ACDC_CONVERTER_Q, CgmesExportUtil.format(q), writer);
    }
}
