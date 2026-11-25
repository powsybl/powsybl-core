/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import static com.powsybl.cgmes.model.CgmesNames.*;

/**
 * <p>The serialization of the CGMES Steady State Hypothesis profile.</p>
 *
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public final class SteadyStateHypothesisWriter extends AbstractCgmesWriter {

    /**
     * Create a {@link SteadyStateHypothesisWriter} instance for writing the CGMES Steady State Hypothesis profile.
     *
     * @param writer the {@link XMLStreamWriter} to serialize the CGMES Steady State Hypothesis profile.
     * @param context the {@link CgmesExportContext} containing the export context.
     */
    public SteadyStateHypothesisWriter(XMLStreamWriter writer, CgmesExportContext context) {
        super(writer, context);
    }

    /**
     * Write the given AsynchronousMachine to the CGMES Steady State Hypothesis profile.
     */
    public void writeAsynchronousMachine(CgmesRecords.AsynchronousMachineSsh asynchronousMachine) throws XMLStreamException {
        writeStartAbout(ASYNCHRONOUS_MACHINE, asynchronousMachine.rdfId());
        writeAttribute(cimNamespace, ROTATING_MACHINE_P, format(asynchronousMachine.p()));
        writeAttribute(cimNamespace, ROTATING_MACHINE_Q, format(asynchronousMachine.q()));
        writeAttribute(cimNamespace, REGULATING_COND_EQ_CONTROL_ENABLED, format(asynchronousMachine.controlEnabled()));
        writeResource(cimNamespace, ASYNCHRONOUS_MACHINE_TYPE,
            cimNamespace + ASYNCHRONOUS_MACHINE_KIND + asynchronousMachine.asynchronousMachineType());
        writeEnd();
    }

    /**
     * Write the given ControlArea to the CGMES Steady State Hypothesis profile.
     */
    public void writeControlArea(CgmesRecords.ControlAreaSsh controlArea) throws XMLStreamException {
        writeStartAbout(CONTROL_AREA, controlArea.rdfId());
        writeAttribute(cimNamespace, CONTROL_AREA_NET_INTERCHANGE, format(controlArea.netInterchange()));
        if (controlArea.pTolerance() != null) {
            writeAttribute(cimNamespace, CONTROL_AREA_P_TOLERANCE, format(controlArea.pTolerance()));
        }
        writeEnd();
    }

    /**
     * Write the given CsConverter to the CGMES Steady State Hypothesis profile.
     */
    public void writeCsConverter(CgmesRecords.CsConverterSsh csConverter) throws XMLStreamException {
        writeStartAbout(CS_CONVERTER, csConverter.rdfId());
        writeAcDcConverter(csConverter.targetPpcc(), csConverter.targetUdc(), csConverter.p(), csConverter.q());
        writeAttribute(cimNamespace, CS_CONVERTER_TARGET_ALPHA, format(csConverter.targetAlpha()));
        writeAttribute(cimNamespace, CS_CONVERTER_TARGET_GAMMA, format(csConverter.targetGamma()));
        writeAttribute(cimNamespace, CS_CONVERTER_TARGET_IDC, format(csConverter.targetIdc()));
        writeResource(cimNamespace, CS_CONVERTER_OPERATING_MODE,
            cimNamespace + CS_OPERATING_MODE_KIND + csConverter.operatingMode());
        writeResource(cimNamespace, CS_CONVERTER_P_PCC_CONTROL,
            cimNamespace + CS_PPCC_CONTROL_KIND + csConverter.pPccControl());
        writeEnd();
    }

    /**
     * Write the given DCTerminal to the CGMES Steady State Hypothesis profile.
     */
    public void writeDCTerminal(CgmesRecords.DCTerminalSsh dcTerminal) throws XMLStreamException {
        writeStartAbout(dcTerminal.className(), dcTerminal.rdfId());
        writeAttribute(cimNamespace, AC_DC_TERMINAL_CONNECTED, format(dcTerminal.connected()));
        writeEnd();
    }

    /**
     * Write the given EnergyConsumer to the CGMES Steady State Hypothesis profile.
     */
    public void writeEnergyConsumer(CgmesRecords.EnergyConsumerSsh energyConsumer) throws XMLStreamException {
        writeStartAbout(energyConsumer.className(), energyConsumer.rdfId());
        writeAttribute(cimNamespace, ENERGY_CONSUMER_P, format(energyConsumer.p()));
        writeAttribute(cimNamespace, ENERGY_CONSUMER_Q, format(energyConsumer.q()));
        writeEnd();
    }

    /**
     * Write the given EnergySource to the CGMES Steady State Hypothesis profile.
     */
    public void writeEnergySource(CgmesRecords.EnergySourceSsh energySource) throws XMLStreamException {
        writeStartAbout(ENERGY_SOURCE, energySource.rdfId());
        writeAttribute(cimNamespace, ENERGY_SOURCE_ACTIVE_POWER, format(energySource.activePower()));
        writeAttribute(cimNamespace, ENERGY_SOURCE_REACTIVE_POWER, format(energySource.reactivePower()));
        writeEnd();
    }

    /**
     * Write the given EquivalentInjection to the CGMES Steady State Hypothesis profile.
     */
    public void writeEquivalentInjection(CgmesRecords.EquivalentInjectionSsh equivalentInjection) throws XMLStreamException {
        writeStartAbout(EQUIVALENT_INJECTION, equivalentInjection.rdfId());
        writeAttribute(cimNamespace, EQUIVALENT_INJECTION_P, format(equivalentInjection.p()));
        writeAttribute(cimNamespace, EQUIVALENT_INJECTION_Q, format(equivalentInjection.q()));
        writeAttribute(cimNamespace, EQUIVALENT_INJECTION_REGULATION_STATUS, format(equivalentInjection.regulationStatus()));
        writeAttribute(cimNamespace, EQUIVALENT_INJECTION_REGULATION_TARGET, format(equivalentInjection.regulationTarget()));
        writeEnd();
    }

    /**
     * Write the given ExternalNetworkInjection to the CGMES Steady State Hypothesis profile.
     */
    public void writeExternalNetworkInjection(CgmesRecords.ExternalNetworkInjectionSsh externalNetworkInjection) throws XMLStreamException {
        writeStartAbout(EXTERNAL_NETWORK_INJECTION, externalNetworkInjection.rdfId());
        writeAttribute(cimNamespace, REGULATING_COND_EQ_CONTROL_ENABLED, format(externalNetworkInjection.controlEnabled()));
        writeAttribute(cimNamespace, EXTERNAL_NETWORK_INJECTION_P, format(externalNetworkInjection.p()));
        writeAttribute(cimNamespace, EXTERNAL_NETWORK_INJECTION_Q, format(externalNetworkInjection.q()));
        // reference priority is used for angle reference selection (slack)
        writeAttribute(cimNamespace, EXTERNAL_NETWORK_INJECTION_REFERENCE_PRIORITY, format(externalNetworkInjection.referencePriority()));
        writeEnd();
    }

    /**
     * Write the given GeneratingUnit to the CGMES Steady State Hypothesis profile.
     */
    public void writeGeneratingUnit(CgmesRecords.GeneratingUnitSsh generatingUnit) throws XMLStreamException {
        writeStartAbout(generatingUnit.className(), generatingUnit.rdfId());
        writeAttribute(cimNamespace, GENERATING_UNIT_NORMAL_PF, format(generatingUnit.normalPF()));
        writeEnd();
    }

    /**
     * Write the given RegulatingControl to the CGMES Steady State Hypothesis profile.
     */
    public void writeRegulatingControl(CgmesRecords.RegulatingControlSsh regulatingControl) throws XMLStreamException {
        writeStartAbout(regulatingControl.className(), regulatingControl.rdfId());
        writeAttribute(cimNamespace, REGULATING_CONTROL_DISCRETE, format(regulatingControl.discrete()));
        writeAttribute(cimNamespace, REGULATING_CONTROL_ENABLED, format(regulatingControl.enabled()));
        if (CgmesExportUtil.targetDeadbandIsDefined(regulatingControl.targetDeadband())) {
            writeAttribute(cimNamespace, REGULATING_CONTROL_TARGET_DEADBAND, format(regulatingControl.targetDeadband()));
        }
        writeAttribute(cimNamespace, REGULATING_CONTROL_TARGET_VALUE, format(regulatingControl.targetValue()));
        writeResource(cimNamespace, REGULATING_CONTROL_TARGET_VALUE_UNIT_MULTIPLIER,
            cimNamespace + UNIT_MULTIPLIER + regulatingControl.targetValueUnitMultiplier());
        writeEnd();
    }

    /**
     * Write the given ShuntCompensator to the CGMES Steady State Hypothesis profile.
     */
    public void writeShuntCompensator(CgmesRecords.ShuntCompensatorSsh shuntCompensator) throws XMLStreamException {
        writeStartAbout(shuntCompensator.className(), shuntCompensator.rdfId());
        writeAttribute(cimNamespace, SHUNT_COMPENSATOR_SECTIONS, format(shuntCompensator.sections()));
        writeAttribute(cimNamespace, REGULATING_COND_EQ_CONTROL_ENABLED, format(shuntCompensator.controlEnabled()));
        writeEnd();
    }

    /**
     * Write the given StaticVarCompensator to the CGMES Steady State Hypothesis profile.
     */
    public void writeStaticVarCompensator(CgmesRecords.StaticVarCompensatorSsh staticVarCompensator) throws XMLStreamException {
        writeStartAbout(STATIC_VAR_COMPENSATOR, staticVarCompensator.rdfId());
        writeAttribute(cimNamespace, REGULATING_COND_EQ_CONTROL_ENABLED, format(staticVarCompensator.controlEnabled()));
        writeAttribute(cimNamespace, STATIC_VAR_COMPENSATOR_Q, format(staticVarCompensator.q()));
        writeEnd();
    }

    /**
     * Write the given Switch to the CGMES Steady State Hypothesis profile.
     */
    public void writeSwitch(CgmesRecords.SwitchSsh switchSsh) throws XMLStreamException {
        writeStartAbout(switchSsh.className(), switchSsh.rdfId());
        writeAttribute(cimNamespace, SWITCH_OPEN, format(switchSsh.open()));
        writeEnd();
    }

    /**
     * Write the given SynchronousMachine to the CGMES Steady State Hypothesis profile.
     */
    public void writeSynchronousMachine(CgmesRecords.SynchronousMachineSsh synchronousMachine) throws XMLStreamException {
        writeStartAbout(SYNCHRONOUS_MACHINE, synchronousMachine.rdfId());
        writeAttribute(cimNamespace, REGULATING_COND_EQ_CONTROL_ENABLED, format(synchronousMachine.controlEnabled()));
        writeAttribute(cimNamespace, ROTATING_MACHINE_P, format(synchronousMachine.p()));
        writeAttribute(cimNamespace, ROTATING_MACHINE_Q, format(synchronousMachine.q()));
        // reference priority is used for angle reference selection (slack)
        writeAttribute(cimNamespace, SYNCHRONOUS_MACHINE_REFERENCE_PRIORITY, format(synchronousMachine.referencePriority()));
        writeResource(cimNamespace, SYNCHRONOUS_MACHINE_OPERATING_MODE,
            cimNamespace + SYNCHRONOUS_MACHINE_OPERATING_MODE_KIND + synchronousMachine.operatingMode());
        writeEnd();
    }

    /**
     * Write the given TapChanger to the CGMES Steady State Hypothesis profile.
     */
    public void writeTapChanger(CgmesRecords.TapChangerSsh tapChanger) throws XMLStreamException {
        writeStartAbout(tapChanger.className(), tapChanger.rdfId());
        writeAttribute(cimNamespace, TAP_CHANGER_CONTROL_ENABLED_ATTRIBUTE, format(tapChanger.controlEnabled()));
        writeAttribute(cimNamespace, TAP_CHANGER_STEP, format(tapChanger.step()));
        writeEnd();
    }

    /**
     * Write the given Terminal to the CGMES Steady State Hypothesis profile.
     */
    public void writeTerminal(CgmesRecords.TerminalSsh terminal) throws XMLStreamException {
        writeStartAbout(TERMINAL, terminal.rdfId());
        writeAttribute(cimNamespace, AC_DC_TERMINAL_CONNECTED, format(terminal.connected()));
        writeEnd();
    }

    /**
     * Write the given VsConverter to the CGMES Steady State Hypothesis profile.
     */
    public void writeVsConverter(CgmesRecords.VsConverterSsh vsConverter) throws XMLStreamException {
        writeStartAbout(VS_CONVERTER, vsConverter.rdfId());
        writeAcDcConverter(vsConverter.targetPpcc(), vsConverter.targetUdc(), vsConverter.p(), vsConverter.q());
        writeAttribute(cimNamespace, VS_CONVERTER_DROOP, format(vsConverter.droop()));
        writeAttribute(cimNamespace, VS_CONVERTER_DROOP_COMPENSATION, format(vsConverter.droopCompensation()));
        writeAttribute(cimNamespace, VS_CONVERTER_Q_SHARE, format(vsConverter.qShare()));
        writeAttribute(cimNamespace, VS_CONVERTER_TARGET_QPCC, format(vsConverter.targetQpcc()));
        writeAttribute(cimNamespace, VS_CONVERTER_TARGET_UPCC, format(vsConverter.targetUpcc()));
        writeResource(cimNamespace, VS_CONVERTER_P_PCC_CONTROL,
            cimNamespace + VS_PPCC_CONTROL_KIND + vsConverter.pPccControl());
        writeResource(cimNamespace, VS_CONVERTER_Q_PCC_CONTROL,
            cimNamespace + VS_QPCC_CONTROL_KIND + vsConverter.qPccControl());
        writeEnd();
    }

    private void writeAcDcConverter(double targetPpcc, double targetUdc, double p, double q) throws XMLStreamException {
        writeAttribute(cimNamespace, ACDC_CONVERTER_TARGET_PPCC, format(targetPpcc));
        writeAttribute(cimNamespace, ACDC_CONVERTER_TARGET_UDC, format(targetUdc));
        writeAttribute(cimNamespace, ACDC_CONVERTER_P, format(p));
        writeAttribute(cimNamespace, ACDC_CONVERTER_Q, format(q));
    }

    /**
     * Write the CGMES opening tag of the given element with an rdf:about reference.
     * The SSH profile describes objects that are defined in the EQ profile.
     */
    private void writeStartAbout(String className, String id) throws XMLStreamException {
        CgmesExportUtil.writeStartAbout(className, id, writer, context);
    }
}
