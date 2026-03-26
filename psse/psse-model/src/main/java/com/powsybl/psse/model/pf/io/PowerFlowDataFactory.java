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
        context.setFieldNames(PowerFlowRecordGroup.CASE_IDENTIFICATION, CaseIdentificationData.FIELD_NAMES_CASE_IDENTIFICATION);

        if (fileFormat == FileFormat.LEGACY_TEXT) {
            context.setFieldNames(PowerFlowRecordGroup.SUBSTATION, SubstationData.FIELD_NAMES_SUBSTATION);
            context.setFieldNames(PowerFlowRecordGroup.INTERNAL_SUBSTATION_NODE, SubstationData.FIELD_NAMES_SUBSTATION_NODE);
            context.setFieldNames(PowerFlowRecordGroup.INTERNAL_SUBSTATION_SWITCHING_DEVICE, SubstationData.FIELD_NAMES_SUBSTATION_SWITCHING_DEVICES);
            context.setFieldNames(PowerFlowRecordGroup.INTERNAL_SUBSTATION_EQUIPMENT_TERMINAL_ONE_BUS, SubstationData.FIELD_NAMES_SUBSTATION_EQUIPMENT_TERMINALS_ONE_BUS);
            context.setFieldNames(PowerFlowRecordGroup.INTERNAL_SUBSTATION_EQUIPMENT_TERMINAL_TWO_BUSES, SubstationData.FIELD_NAMES_SUBSTATION_EQUIPMENT_TERMINALS_TWO_BUSES);
            context.setFieldNames(PowerFlowRecordGroup.INTERNAL_SUBSTATION_EQUIPMENT_TERMINAL_THREE_BUSES, SubstationData.FIELD_NAMES_SUBSTATION_EQUIPMENT_TERMINALS_THREE_BUSES);
        } else {
            context.setFieldNames(PowerFlowRecordGroup.SUBSTATION, SubstationData.FIELD_NAMES_SUBSTATION_RAWX);
            context.setFieldNames(PowerFlowRecordGroup.INTERNAL_SUBSTATION_NODE, SubstationData.FIELD_NAMES_SUBSTATION_NODE_RAWX);
            context.setFieldNames(PowerFlowRecordGroup.INTERNAL_SUBSTATION_SWITCHING_DEVICE, SubstationData.FIELD_NAMES_SUBSTATION_SWITCHING_DEVICES_RAWX);
            context.setFieldNames(PowerFlowRecordGroup.INTERNAL_SUBSTATION_EQUIPMENT_TERMINAL, SubstationData.FIELD_NAMES_SUBSTATION_EQUIPMENT_TERMINALS_RAWX);
        }
        context.setFieldNames(PowerFlowRecordGroup.BUS, BusData.FIELD_NAMES_BUS_35);
        context.setFieldNames(PowerFlowRecordGroup.LOAD, LoadData.FIELD_NAMES_LOAD_35);
        context.setFieldNames(PowerFlowRecordGroup.FIXED_BUS_SHUNT, FixedBusShuntData.FIELD_NAMES_35);
        context.setFieldNames(PowerFlowRecordGroup.GENERATOR, GeneratorData.FIELD_NAMES_35);
        context.setFieldNames(PowerFlowRecordGroup.NON_TRANSFORMER_BRANCH, NonTransformerBranchData.FIELD_NAMES_35);
        if (fileFormat == FileFormat.LEGACY_TEXT) {
            context.setFieldNames(PowerFlowRecordGroup.TRANSFORMER, TransformerData.FIELD_NAMES_35);
            context.setFieldNames(PowerFlowRecordGroup.INTERNAL_TRANSFORMER_IMPEDANCES, TransformerData.FIELD_NAMES_IMPEDANCES_35);
            context.setFieldNames(PowerFlowRecordGroup.INTERNAL_TRANSFORMER_WINDING, TransformerData.FIELD_NAMES_WINDING_35);
        } else {
            context.setFieldNames(PowerFlowRecordGroup.TRANSFORMER, TransformerData.FIELD_NAMES_35_RAWX);
        }
        context.setFieldNames(PowerFlowRecordGroup.FACTS_CONTROL_DEVICE, FactsDeviceData.FIELD_NAMES_35);
        if (fileFormat == FileFormat.LEGACY_TEXT) {
            context.setFieldNames(PowerFlowRecordGroup.TWO_TERMINAL_DC_TRANSMISSION_LINE, TwoTerminalDcTransmissionLineData.FIELD_NAMES_35);
            context.setFieldNames(PowerFlowRecordGroup.INTERNAL_TWO_TERMINAL_DC_TRANSMISSION_LINE_CONVERTER, TwoTerminalDcTransmissionLineData.FIELD_NAMES_CONVERTER_35);
        } else {
            context.setFieldNames(PowerFlowRecordGroup.TWO_TERMINAL_DC_TRANSMISSION_LINE, TwoTerminalDcTransmissionLineData.FIELD_NAMES_35_RAWX);
        }
        if (fileFormat == FileFormat.LEGACY_TEXT) {
            context.setFieldNames(PowerFlowRecordGroup.VOLTAGE_SOURCE_CONVERTER_DC_TRANSMISSION_LINE, VoltageSourceConverterDcTransmissionLineData.FIELD_NAMES_35);
            context.setFieldNames(PowerFlowRecordGroup.INTERNAL_VOLTAGE_SOURCE_CONVERTER_DC_TRANSMISSION_LINE_CONVERTER, VoltageSourceConverterDcTransmissionLineData.FIELD_NAMES_CONVERTER_35);
        } else {
            context.setFieldNames(PowerFlowRecordGroup.VOLTAGE_SOURCE_CONVERTER_DC_TRANSMISSION_LINE, VoltageSourceConverterDcTransmissionLineData.FIELD_NAMES_35_RAWX);
        }
        if (fileFormat == FileFormat.LEGACY_TEXT) {
            context.setFieldNames(PowerFlowRecordGroup.SWITCHED_SHUNT, SwitchedShuntData.FIELD_NAMES_35);
        } else {
            context.setFieldNames(PowerFlowRecordGroup.SWITCHED_SHUNT, SwitchedShuntData.FIELD_NAMES_35_RAWX);
        }

        return context;
    }
}
