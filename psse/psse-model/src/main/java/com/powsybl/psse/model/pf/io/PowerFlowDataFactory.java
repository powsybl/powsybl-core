/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf.io;

import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseVersion;
import com.powsybl.psse.model.io.Context;
import com.powsybl.psse.model.io.FileFormat;
import com.powsybl.psse.model.pf.PsseBus;
import com.powsybl.psse.model.pf.PsseCaseIdentification;
import com.powsybl.psse.model.pf.PsseFacts;
import com.powsybl.psse.model.pf.PsseFixedShunt;
import com.powsybl.psse.model.pf.PsseGenerator;
import com.powsybl.psse.model.pf.PsseLoad;
import com.powsybl.psse.model.pf.PsseNonTransformerBranch;
import com.powsybl.psse.model.pf.PsseSwitchedShunt;
import com.powsybl.psse.model.pf.PsseTransformer;
import com.powsybl.psse.model.pf.PsseTwoTerminalDcConverter;
import com.powsybl.psse.model.pf.PsseTwoTerminalDcTransmissionLine;
import com.powsybl.psse.model.pf.PsseVoltageSourceConverter;
import com.powsybl.psse.model.pf.PsseVoltageSourceConverterDcTransmissionLine;
import com.powsybl.psse.model.pf.internal.PsseSubstationNode;
import com.powsybl.psse.model.pf.internal.PsseSubstationRecord;
import com.powsybl.psse.model.pf.internal.TransformerImpedances;
import com.powsybl.psse.model.pf.internal.TransformerWindingRecord;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public final class PowerFlowDataFactory {

    private PowerFlowDataFactory() {
    }

    public static PowerFlowData create(String extension) {
        // When only extension is given,
        // We create PowerFlowData objects that are able to read the case identification
        // To determine if the file is valid and get its version
        if (extension.equalsIgnoreCase("rawx")) {
            return new PowerFlowRawxDataAllVersions();
        } else {
            return new PowerFlowRawDataAllVersions();
        }
    }

    public static PowerFlowData create(String extension, PsseVersion version) {
        if (extension.equalsIgnoreCase("rawx")) {
            switch (version.major()) {
                case V35 -> {
                    return new PowerFlowRawxData35();
                }
                default -> throw new PsseException("Unsupported version " + version);
            }
        } else {
            return switch (version.major()) {
                case V35 -> new PowerFlowRawData35();
                case V33 -> new PowerFlowRawData33();
                case V32 -> new PowerFlowRawData32();
            };
        }
    }

    public static Context createPsseContext(boolean rawFormat) {
        Context context = new Context();
        context.setVersion(PsseVersion.fromRevision(35));
        FileFormat fileFormat = rawFormat ? FileFormat.LEGACY_TEXT : FileFormat.JSON;
        context.setFileFormat(fileFormat);
        context.setFieldNames(PowerFlowRecordGroup.CASE_IDENTIFICATION, PsseCaseIdentification.getFieldNames());

        if (fileFormat == FileFormat.LEGACY_TEXT) {
            context.setFieldNames(PowerFlowRecordGroup.SUBSTATION, PsseSubstationRecord.getFieldNamesRaw());
            context.setFieldNames(PowerFlowRecordGroup.INTERNAL_SUBSTATION_NODE, PsseSubstationNode.getFieldNamesRaw());
            context.setFieldNames(PowerFlowRecordGroup.INTERNAL_SUBSTATION_SWITCHING_DEVICE, SubstationData.FIELD_NAMES_SUBSTATION_SWITCHING_DEVICES);
            context.setFieldNames(PowerFlowRecordGroup.INTERNAL_SUBSTATION_EQUIPMENT_TERMINAL_ONE_BUS, SubstationData.FIELD_NAMES_SUBSTATION_EQUIPMENT_TERMINALS_ONE_BUS);
            context.setFieldNames(PowerFlowRecordGroup.INTERNAL_SUBSTATION_EQUIPMENT_TERMINAL_TWO_BUSES, SubstationData.FIELD_NAMES_SUBSTATION_EQUIPMENT_TERMINALS_TWO_BUSES);
            context.setFieldNames(PowerFlowRecordGroup.INTERNAL_SUBSTATION_EQUIPMENT_TERMINAL_THREE_BUSES, SubstationData.FIELD_NAMES_SUBSTATION_EQUIPMENT_TERMINALS_THREE_BUSES);
        } else {
            context.setFieldNames(PowerFlowRecordGroup.SUBSTATION, PsseSubstationRecord.getFieldNamesRawx());
            context.setFieldNames(PowerFlowRecordGroup.INTERNAL_SUBSTATION_NODE, PsseSubstationNode.getFieldNamesRawx());
            context.setFieldNames(PowerFlowRecordGroup.INTERNAL_SUBSTATION_SWITCHING_DEVICE, SubstationData.FIELD_NAMES_SUBSTATION_SWITCHING_DEVICES_RAWX);
            context.setFieldNames(PowerFlowRecordGroup.INTERNAL_SUBSTATION_EQUIPMENT_TERMINAL, SubstationData.FIELD_NAMES_SUBSTATION_EQUIPMENT_TERMINALS_RAWX);
        }
        context.setFieldNames(PowerFlowRecordGroup.BUS, PsseBus.getFieldNames35());
        if (fileFormat == FileFormat.LEGACY_TEXT) {
            context.setFieldNames(PowerFlowRecordGroup.LOAD, PsseLoad.getFieldNames35());
        } else {
            context.setFieldNames(PowerFlowRecordGroup.LOAD, PsseLoad.getFieldNames35RawX());
        }
        context.setFieldNames(PowerFlowRecordGroup.FIXED_BUS_SHUNT, PsseFixedShunt.getFieldNames35());
        context.setFieldNames(PowerFlowRecordGroup.GENERATOR, PsseGenerator.getFieldNames35());
        context.setFieldNames(PowerFlowRecordGroup.NON_TRANSFORMER_BRANCH, PsseNonTransformerBranch.getFieldNames35());
        if (fileFormat == FileFormat.LEGACY_TEXT) {
            context.setFieldNames(PowerFlowRecordGroup.TRANSFORMER, PsseTransformer.getFieldNames35());
            context.setFieldNames(PowerFlowRecordGroup.INTERNAL_TRANSFORMER_IMPEDANCES, TransformerImpedances.getFieldNames35());
            context.setFieldNames(PowerFlowRecordGroup.INTERNAL_TRANSFORMER_WINDING, TransformerWindingRecord.getFieldNames35Wdg());
        } else {
            context.setFieldNames(PowerFlowRecordGroup.TRANSFORMER, PsseTransformer.getFieldNames35RawX());
        }
        context.setFieldNames(PowerFlowRecordGroup.FACTS_CONTROL_DEVICE, PsseFacts.getFieldNames35());
        if (fileFormat == FileFormat.LEGACY_TEXT) {
            context.setFieldNames(PowerFlowRecordGroup.TWO_TERMINAL_DC_TRANSMISSION_LINE, PsseTwoTerminalDcTransmissionLine.getFieldNames35());
            context.setFieldNames(PowerFlowRecordGroup.INTERNAL_TWO_TERMINAL_DC_TRANSMISSION_LINE_CONVERTER, PsseTwoTerminalDcConverter.getFieldNames35());
        } else {
            context.setFieldNames(PowerFlowRecordGroup.TWO_TERMINAL_DC_TRANSMISSION_LINE, PsseTwoTerminalDcTransmissionLine.getFieldNames35RawX());
        }
        if (fileFormat == FileFormat.LEGACY_TEXT) {
            context.setFieldNames(PowerFlowRecordGroup.VOLTAGE_SOURCE_CONVERTER_DC_TRANSMISSION_LINE, PsseVoltageSourceConverterDcTransmissionLine.getFieldNames35());
            context.setFieldNames(PowerFlowRecordGroup.INTERNAL_VOLTAGE_SOURCE_CONVERTER_DC_TRANSMISSION_LINE_CONVERTER, PsseVoltageSourceConverter.getFieldNames35());
        } else {
            context.setFieldNames(PowerFlowRecordGroup.VOLTAGE_SOURCE_CONVERTER_DC_TRANSMISSION_LINE, PsseVoltageSourceConverterDcTransmissionLine.getFieldNames35RawX());
        }
        if (fileFormat == FileFormat.LEGACY_TEXT) {
            context.setFieldNames(PowerFlowRecordGroup.SWITCHED_SHUNT, PsseSwitchedShunt.getFieldNames35());
        } else {
            context.setFieldNames(PowerFlowRecordGroup.SWITCHED_SHUNT, PsseSwitchedShunt.getFieldNames35RawX());
        }

        return context;
    }
}
