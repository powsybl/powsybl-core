/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf.io;

import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.psse.model.io.Context;
import com.powsybl.psse.model.io.LegacyTextReader;
import com.powsybl.psse.model.pf.PsseCaseIdentification;
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.pf.PssePowerFlowModel;
import com.powsybl.psse.model.PsseVersion;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * PSSE RAW data common to all versions
 * Should be able to read the case identification to obtain the version (case identification "rev" attribute)
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class PowerFlowRawDataAllVersions implements PowerFlowData {

    @Override
    public boolean isValidFile(ReadOnlyDataSource dataSource, String extension) throws IOException {
        try (BufferedReader bReader = new BufferedReader(new InputStreamReader(dataSource.newInputStream(null, extension)))) {
            LegacyTextReader reader = new LegacyTextReader(bReader);
            PsseCaseIdentification caseIdentification = new CaseIdentificationData().readHead(reader, new Context());
            caseIdentification.validate();
            return true;
        }
    }

    @Override
    public PsseVersion readVersion(ReadOnlyDataSource dataSource, String extension) throws IOException {
        try (BufferedReader bReader = new BufferedReader(new InputStreamReader(dataSource.newInputStream(null, extension)))) {
            LegacyTextReader reader = new LegacyTextReader(bReader);
            PsseCaseIdentification caseIdentification = new CaseIdentificationData().readHead(reader, new Context());
            caseIdentification.validate();
            return PsseVersion.fromRevision(caseIdentification.getRev());
        }
    }

    @Override
    public PssePowerFlowModel read(ReadOnlyDataSource dataSource, String ext, Context context) throws IOException {
        throw new PsseException("Here we don't know how to read a complete data file. Specific version instance is required");
    }

    @Override
    public void write(PssePowerFlowModel model, Context context, DataSource dataSource) throws IOException {
        throw new PsseException("Here we don't know how to write a complete data file. Specific version instance is required");
    }

    protected void readPsseData(PssePowerFlowModel model, Context context, LegacyTextReader reader, List<PsseDataClass> classes) throws IOException {
        for (PsseDataClass psseClass : classes) {
            switch (psseClass) {
                case BUS_DATA -> model.addBuses(new BusData().read(reader, context));
                case LOAD_DATA -> model.addLoads(new LoadData().read(reader, context));
                case FIXED_BUS_SHUNT_DATA -> model.addFixedShunts(new FixedBusShuntData().read(reader, context));
                case GENERATOR_DATA -> model.addGenerators(new GeneratorData().read(reader, context));
                case NON_TRANSFORMER_BRANCH_DATA -> model.addNonTransformerBranches(new NonTransformerBranchData().read(reader, context));
                case TRANSFORMER_DATA -> model.addTransformers(new TransformerData().read(reader, context));
                case AREA_INTERCHANGE_DATA -> model.addAreas(new AreaInterchangeData().read(reader, context));
                case TWO_TERMINAL_DC_TRANSMISSION_LINE_DATA -> model.addTwoTerminalDcTransmissionLines(new TwoTerminalDcTransmissionLineData().read(reader, context));
                case VOLTAGE_SOURCE_CONVERTER_DC_TRANSMISSION_LINE_DATA -> model.addVoltageSourceConverterDcTransmissionLines(new VoltageSourceConverterDcTransmissionLineData().read(reader, context));
                case TRANSFORMER_IMPEDANCE_CORRECTION_TABLES_DATA -> model.addTransformerImpedanceCorrections(new TransformerImpedanceCorrectionTablesData().read(reader, context));
                case MULTI_TERMINAL_DC_TRANSMISSION_LINE_DATA -> model.addMultiTerminalDcTransmissionLines(new MultiTerminalDcTransmissionLineData().read(reader, context));
                case MULTI_SECTION_LINE_GROUPING_DATA -> model.addLineGrouping(new MultiSectionLineGroupingData().read(reader, context));
                case ZONE_DATA -> model.addZones(new ZoneData().read(reader, context));
                case INTERAREA_TRANSFER_DATA -> model.addInterareaTransfer(new InterareaTransferData().read(reader, context));
                case OWNER_DATA -> model.addOwners(new OwnerData().read(reader, context));
                case FACTS_DEVICE_DATA -> model.addFacts(new FactsDeviceData().read(reader, context));
                case SWITCHED_SHUNT_DATA -> model.addSwitchedShunts(new SwitchedShuntData().read(reader, context));
                case GNE_DEVICE_DATA -> model.addGneDevice(new GneDeviceData().read(reader, context));
                case INDUCTION_MACHINE_DATA -> model.addInductionMachines(new InductionMachineData().read(reader, context));
                case SUBSTATION_DATA -> model.addSubstations(new SubstationData().read(reader, context));
                default -> throw new PsseException("Unknown class " + psseClass);
            }
        }
    }

    protected void writePsseData(PssePowerFlowModel model, Context context, BufferedOutputStream outputStream, List<PsseDataClass> classes) {
        for (PsseDataClass psseClass : classes) {
            switch (psseClass) {
                case BUS_DATA -> new BusData().write(model.getBuses(), context, outputStream);
                case LOAD_DATA -> new LoadData().write(model.getLoads(), context, outputStream);
                case FIXED_BUS_SHUNT_DATA -> new FixedBusShuntData().write(model.getFixedShunts(), context, outputStream);
                case GENERATOR_DATA -> new GeneratorData().write(model.getGenerators(), context, outputStream);
                case NON_TRANSFORMER_BRANCH_DATA -> new NonTransformerBranchData().write(model.getNonTransformerBranches(), context, outputStream);
                case TRANSFORMER_DATA -> new TransformerData().write(model.getTransformers(), context, outputStream);
                case AREA_INTERCHANGE_DATA -> new AreaInterchangeData().write(model.getAreas(), context, outputStream);
                case TWO_TERMINAL_DC_TRANSMISSION_LINE_DATA -> new TwoTerminalDcTransmissionLineData().write(model.getTwoTerminalDcTransmissionLines(), context, outputStream);
                case VOLTAGE_SOURCE_CONVERTER_DC_TRANSMISSION_LINE_DATA -> new VoltageSourceConverterDcTransmissionLineData().write(model.getVoltageSourceConverterDcTransmissionLines(), context, outputStream);
                case TRANSFORMER_IMPEDANCE_CORRECTION_TABLES_DATA -> new TransformerImpedanceCorrectionTablesData().write(model.getTransformerImpedanceCorrections(), context, outputStream);
                case MULTI_TERMINAL_DC_TRANSMISSION_LINE_DATA -> new MultiTerminalDcTransmissionLineData().write(model.getMultiTerminalDcTransmissionLines(), context, outputStream);
                case MULTI_SECTION_LINE_GROUPING_DATA -> new MultiSectionLineGroupingData().write(model.getLineGrouping(), context, outputStream);
                case ZONE_DATA -> new ZoneData().write(model.getZones(), context, outputStream);
                case INTERAREA_TRANSFER_DATA -> new InterareaTransferData().write(model.getInterareaTransfer(), context, outputStream);
                case OWNER_DATA -> new OwnerData().write(model.getOwners(), context, outputStream);
                case FACTS_DEVICE_DATA -> new FactsDeviceData().write(model.getFacts(), context, outputStream);
                case SWITCHED_SHUNT_DATA -> new SwitchedShuntData().write(model.getSwitchedShunts(), context, outputStream);
                case GNE_DEVICE_DATA -> new GneDeviceData().write(model.getGneDevice(), context, outputStream);
                case INDUCTION_MACHINE_DATA -> new InductionMachineData().write(model.getInductionMachines(), context, outputStream);
                case SUBSTATION_DATA -> new SubstationData().write(model.getSubstations(), context, outputStream);
                default -> throw new PsseException("Unknown class " + psseClass);
            }
        }
    }
}
