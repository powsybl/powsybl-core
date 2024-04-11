/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf.io;

import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.io.*;
import com.powsybl.psse.model.pf.PsseSubstation;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.powsybl.psse.model.pf.PsseSubstation.*;
import static com.powsybl.psse.model.pf.io.PowerFlowRecordGroup.*;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class SubstationData extends AbstractRecordGroup<PsseSubstation> {

    SubstationData() {
        super(SUBSTATION);
        withIO(FileFormat.LEGACY_TEXT, new IOLegacyText(this));
        withIO(FileFormat.JSON, new IOJson(this));
    }

    @Override
    protected Class<PsseSubstation> psseTypeClass() {
        return PsseSubstation.class;
    }

    private static class IOLegacyText extends RecordGroupIOLegacyText<PsseSubstation> {

        IOLegacyText(AbstractRecordGroup<PsseSubstation> recordGroup) {
            super(recordGroup);
        }

        @Override
        public List<PsseSubstation> read(LegacyTextReader reader, Context context) throws IOException {

            SubstationRecordData recordData = new SubstationRecordData();
            List<PsseSubstation> substationList = new ArrayList<>();

            if (!reader.isQRecordFound()) {
                String line = reader.readRecordLine();
                while (!reader.endOfBlock(line)) {
                    PsseSubstationRecord record = recordData.readFromStrings(Collections.singletonList(line), context).get(0);

                    List<PsseSubstationNode> nodeList = new SubstationNodeData().read(reader, context);
                    List<PsseSubstationSwitchingDevice> switchingDeviceList = new SubstationSwitchingDeviceData().read(reader, context);
                    List<PsseSubstationEquipmentTerminal> equipmentTerminalList = readEquipmentTerminalData(reader, context);

                    PsseSubstation substation = new PsseSubstation(record, nodeList, switchingDeviceList, equipmentTerminalList);
                    substationList.add(substation);

                    line = reader.readRecordLine();
                }
            }

            return substationList;
        }

        private List<PsseSubstationEquipmentTerminal> readEquipmentTerminalData(LegacyTextReader reader, Context context) throws IOException {
            SubstationEquipmentTerminalDataCommonStart commonStartData = new SubstationEquipmentTerminalDataCommonStart();
            List<String> equipmentTerminalOneBusRecords = new ArrayList<>();
            List<String> equipmentTerminalTwoBusesRecords = new ArrayList<>();
            List<String> equipmentTerminalThreeBusesRecords = new ArrayList<>();

            String line = reader.readRecordLine();
            while (!reader.endOfBlock(line)) {
                PsseSubstationEquipmentTerminalCommonStart commonStart = commonStartData.readFromStrings(Collections.singletonList(line), context).get(0);
                if (isOneBus(commonStart.getType())) {
                    equipmentTerminalOneBusRecords.add(line);
                } else if (isTwoBuses(commonStart.getType())) {
                    equipmentTerminalTwoBusesRecords.add(line);
                } else if (isThreeBuses(commonStart.getType())) {
                    equipmentTerminalThreeBusesRecords.add(line);
                } else {
                    throw new PsseException("Unexpected equipment terminal type: " + commonStart.getType());
                }
                line = reader.readRecordLine();
            }

            List<PsseSubstationEquipmentTerminalOneBus> equipmentTerminalOneBusList = new SubstationEquipmentTerminalDataOneBus().readFromStrings(equipmentTerminalOneBusRecords, context);
            List<PsseSubstationEquipmentTerminalTwoBuses> equipmentTerminalTwoBusesList = new SubstationEquipmentTerminalDataTwoBuses().readFromStrings(equipmentTerminalTwoBusesRecords, context);
            List<PsseSubstationEquipmentTerminalThreeBuses> equipmentTerminalThreeBusesList = new SubstationEquipmentTerminalDataThreeBuses().readFromStrings(equipmentTerminalThreeBusesRecords, context);

            List<PsseSubstationEquipmentTerminal> equipmentTerminalList = equipmentTerminalOneBusList.stream().map(PsseSubstationEquipmentTerminalOneBus::convertToEquipmentTerminal).collect(Collectors.toList());
            equipmentTerminalList.addAll(equipmentTerminalTwoBusesList.stream().map(PsseSubstationEquipmentTerminalTwoBuses::convertToEquipmentTerminal).toList());
            equipmentTerminalList.addAll(equipmentTerminalThreeBusesList.stream().map(PsseSubstationEquipmentTerminalThreeBuses::convertToEquipmentTerminal).toList());

            return equipmentTerminalList;
        }

        @Override
        public void write(List<PsseSubstation> substationList, Context context, OutputStream outputStream) {

            writeBegin(outputStream);

            substationList.forEach(substation -> {

                SubstationRecordData recordData = new SubstationRecordData();
                write(recordData.buildRecords(Collections.singletonList(substation.getRecord()), context.getFieldNames(SUBSTATION), recordData.quotedFields(), context), outputStream);

                writeComment(" BEGIN SUBSTATION NODE DATA", outputStream);

                SubstationNodeData nodeData = new SubstationNodeData();
                write(nodeData.buildRecords(substation.getNodes(), context.getFieldNames(INTERNAL_SUBSTATION_NODE), nodeData.quotedFields(), context), outputStream);
                writeEndComment(" END OF SUBSTATION NODE DATA, BEGIN SUBSTATION SWITCHING DEVICE DATA", outputStream);

                SubstationSwitchingDeviceData switchingDeviceData = new SubstationSwitchingDeviceData();
                write(switchingDeviceData.buildRecords(substation.getSwitchingDevices(), context.getFieldNames(INTERNAL_SUBSTATION_SWITCHING_DEVICE), nodeData.quotedFields(), context), outputStream);
                writeEndComment(" END OF SUBSTATION SWITCHING DEVICE DATA, BEGIN SUBSTATION EQUIPMENT TERMINAL DATA", outputStream);

                write(writeEquipmentTerminalData(substation.getEquipmentTerminals(), context), outputStream);
                writeEndComment(" END OF SUBSTATION EQUIPMENT TERMINAL DATA", outputStream);
            });

            writeEnd(outputStream);
        }

        private List<String> writeEquipmentTerminalData(List<PsseSubstationEquipmentTerminal> equipmentTerminalList, Context context) {

            List<PsseSubstationEquipmentTerminalOneBus> eqOneBus = equipmentTerminalList.stream().filter(eq -> isOneBus(eq.getType())).map(PsseSubstationEquipmentTerminal::convertToEquipmentTerminalOneBus).collect(Collectors.toList());
            List<PsseSubstationEquipmentTerminalTwoBuses> eqTwoBuses = equipmentTerminalList.stream().filter(eq -> isTwoBuses(eq.getType())).map(PsseSubstationEquipmentTerminal::convertToEquipmentTerminalTwoBuses).collect(Collectors.toList());
            List<PsseSubstationEquipmentTerminalThreeBuses> eqThreeBuses = equipmentTerminalList.stream().filter(eq -> isThreeBuses(eq.getType())).map(PsseSubstationEquipmentTerminal::convertToEquipmentTerminalThreeBuses).collect(Collectors.toList());

            SubstationEquipmentTerminalDataOneBus oneBusData = new SubstationEquipmentTerminalDataOneBus();
            List<String> strings = oneBusData.buildRecords(eqOneBus, context.getFieldNames(INTERNAL_SUBSTATION_EQUIPMENT_TERMINAL_ONE_BUS), oneBusData.quotedFields(), context);

            SubstationEquipmentTerminalDataTwoBuses twoBusesData = new SubstationEquipmentTerminalDataTwoBuses();
            strings.addAll(twoBusesData.buildRecords(eqTwoBuses, context.getFieldNames(INTERNAL_SUBSTATION_EQUIPMENT_TERMINAL_TWO_BUSES), twoBusesData.quotedFields(), context));

            SubstationEquipmentTerminalDataThreeBuses threeBusesData = new SubstationEquipmentTerminalDataThreeBuses();
            strings.addAll(threeBusesData.buildRecords(eqThreeBuses, context.getFieldNames(INTERNAL_SUBSTATION_EQUIPMENT_TERMINAL_THREE_BUSES), threeBusesData.quotedFields(), context));

            return strings;
        }

        private static class SubstationNodeData extends AbstractRecordGroup<PsseSubstationNode> {
            SubstationNodeData() {
                super(INTERNAL_SUBSTATION_NODE, "ni", "name", "i", "status", "vm", "va");
                withQuotedFields(QUOTED_FIELDS);
            }

            @Override
            public Class<PsseSubstationNode> psseTypeClass() {
                return PsseSubstationNode.class;
            }
        }

        private static class SubstationSwitchingDeviceData extends AbstractRecordGroup<PsseSubstationSwitchingDevice> {
            SubstationSwitchingDeviceData() {
                super(INTERNAL_SUBSTATION_SWITCHING_DEVICE, "ni", "nj", "ckt", "name", "type", "status", "nstat", "x", "rate1", "rate2", "rate3");
                withQuotedFields(QUOTED_FIELDS);
            }

            @Override
            public Class<PsseSubstationSwitchingDevice> psseTypeClass() {
                return PsseSubstationSwitchingDevice.class;
            }
        }

        private static class SubstationEquipmentTerminalDataCommonStart extends AbstractRecordGroup<PsseSubstationEquipmentTerminalCommonStart> {
            SubstationEquipmentTerminalDataCommonStart() {
                super(INTERNAL_SUBSTATION_EQUIPMENT_TERMINAL_COMMON_START, "i", "ni", "type");
                withQuotedFields(QUOTED_FIELDS);
            }

            @Override
            public Class<PsseSubstationEquipmentTerminalCommonStart> psseTypeClass() {
                return PsseSubstationEquipmentTerminalCommonStart.class;
            }
        }

        private static class SubstationEquipmentTerminalDataOneBus extends AbstractRecordGroup<PsseSubstationEquipmentTerminalOneBus> {
            SubstationEquipmentTerminalDataOneBus() {
                super(INTERNAL_SUBSTATION_EQUIPMENT_TERMINAL_ONE_BUS, "i", "ni", "type", "id");
                withQuotedFields(QUOTED_FIELDS);
            }

            @Override
            public Class<PsseSubstationEquipmentTerminalOneBus> psseTypeClass() {
                return PsseSubstationEquipmentTerminalOneBus.class;
            }
        }

        private static class SubstationEquipmentTerminalDataTwoBuses extends AbstractRecordGroup<PsseSubstationEquipmentTerminalTwoBuses> {
            SubstationEquipmentTerminalDataTwoBuses() {
                super(INTERNAL_SUBSTATION_EQUIPMENT_TERMINAL_TWO_BUSES, "i", "ni", "type", "j", "id");
                withQuotedFields(QUOTED_FIELDS);
            }

            @Override
            public Class<PsseSubstationEquipmentTerminalTwoBuses> psseTypeClass() {
                return PsseSubstationEquipmentTerminalTwoBuses.class;
            }
        }

        private static class SubstationEquipmentTerminalDataThreeBuses extends AbstractRecordGroup<PsseSubstationEquipmentTerminalThreeBuses> {
            SubstationEquipmentTerminalDataThreeBuses() {
                super(INTERNAL_SUBSTATION_EQUIPMENT_TERMINAL_THREE_BUSES, "i", "ni", "type", "j", "k", "id");
                withQuotedFields(QUOTED_FIELDS);
            }

            @Override
            public Class<PsseSubstationEquipmentTerminalThreeBuses> psseTypeClass() {
                return PsseSubstationEquipmentTerminalThreeBuses.class;
            }
        }
    }

    private static class IOJson extends RecordGroupIOJson<PsseSubstation> {
        IOJson(AbstractRecordGroup<PsseSubstation> recordGroup) {
            super(recordGroup);
        }

        @Override
        public List<PsseSubstation> read(LegacyTextReader reader, Context context) throws IOException {
            if (reader != null) {
                throw new PsseException("Unexpected reader. Should be null");
            }
            List<PsseSubstationRecord> recordList = new SubstationRecordData().read(null, context);
            List<PsseSubstationNodex> nodexList = new SubstationNodexData().read(null, context);
            List<PsseSubstationSwitchingDevicex> switchingDevicexList = new SubstationSwitchingDevicexData().read(null, context);
            List<PsseSubstationEquipmentTerminalx> equipmentTerminalxList = new SubstationEquipmentTerminalxData().read(null, context);
            return convertToSubstationList(recordList, nodexList, switchingDevicexList, equipmentTerminalxList);
        }

        @Override
        public void write(List<PsseSubstation> substationList, Context context, OutputStream outputStream) {
            if (outputStream != null) {
                throw new PsseException("Unexpected outputStream. Should be null");
            }
            List<PsseSubstationRecord> recordList = substationList.stream().map(PsseSubstation::getRecord).collect(Collectors.toList());
            new SubstationRecordData().write(recordList, context, null);

            List<PsseSubstationNodex> nodexList = new ArrayList<>();
            substationList.forEach(substation -> substation.getNodes().forEach(node -> nodexList.add(
                    new PsseSubstationNodex(substation.getRecord().getIs(), node))));
            new SubstationNodexData().write(nodexList, context, null);

            List<PsseSubstationSwitchingDevicex> switchingDevicexList = new ArrayList<>();
            substationList.forEach(substation -> substation.getSwitchingDevices().forEach(switchingDevice -> switchingDevicexList.add(
                    new PsseSubstationSwitchingDevicex(substation.getRecord().getIs(), switchingDevice))));
            new SubstationSwitchingDevicexData().write(switchingDevicexList, context, null);

            List<PsseSubstationEquipmentTerminalx> equipmentTerminalxList = new ArrayList<>();
            substationList.forEach(substation -> substation.getEquipmentTerminals().forEach(equipmentTerminal -> equipmentTerminalxList.add(
                    new PsseSubstationEquipmentTerminalx(substation.getRecord().getIs(), equipmentTerminal))));
            new SubstationEquipmentTerminalxData().write(equipmentTerminalxList, context, null);
        }

        private static List<PsseSubstation> convertToSubstationList(List<PsseSubstationRecord> recordList,
                                                                    List<PsseSubstationNodex> nodexList, List<PsseSubstationSwitchingDevicex> switchingDevicexList,
                                                                    List<PsseSubstationEquipmentTerminalx> equipmentTerminalxList) {

            List<PsseSubstation> substationList = new ArrayList<>();
            for (PsseSubstationRecord record : recordList) {
                List<PsseSubstationNode> nodeList = nodexList.stream().filter(n -> n.getIsub() == record.getIs()).map(PsseSubstationNodex::getNode).collect(Collectors.toList());
                List<PsseSubstationSwitchingDevice> switchingDeviceList = switchingDevicexList.stream().filter(sd -> sd.getIsub() == record.getIs()).map(PsseSubstationSwitchingDevicex::getSwitchingDevice).collect(Collectors.toList());
                List<PsseSubstationEquipmentTerminal> equipmentTerminalList = equipmentTerminalxList.stream().filter(eq -> eq.getIsub() == record.getIs()).map(PsseSubstationEquipmentTerminalx::getEquipmentTerminal).collect(Collectors.toList());

                substationList.add(new PsseSubstation(record, nodeList, switchingDeviceList, equipmentTerminalList));
            }
            return substationList;
        }

        private static class SubstationNodexData extends AbstractRecordGroup<PsseSubstationNodex> {
            SubstationNodexData() {
                super(INTERNAL_SUBSTATION_NODE);
                withQuotedFields(QUOTED_FIELDS);
            }

            @Override
            public Class<PsseSubstationNodex> psseTypeClass() {
                return PsseSubstationNodex.class;
            }
        }

        private static class SubstationSwitchingDevicexData extends AbstractRecordGroup<PsseSubstationSwitchingDevicex> {
            SubstationSwitchingDevicexData() {
                super(INTERNAL_SUBSTATION_SWITCHING_DEVICE);
                withQuotedFields(QUOTED_FIELDS);
            }

            @Override
            public Class<PsseSubstationSwitchingDevicex> psseTypeClass() {
                return PsseSubstationSwitchingDevicex.class;
            }
        }

        private static class SubstationEquipmentTerminalxData extends AbstractRecordGroup<PsseSubstationEquipmentTerminalx> {
            SubstationEquipmentTerminalxData() {
                super(INTERNAL_SUBSTATION_EQUIPMENT_TERMINAL);
                withQuotedFields(QUOTED_FIELDS);
            }

            @Override
            public Class<PsseSubstationEquipmentTerminalx> psseTypeClass() {
                return PsseSubstationEquipmentTerminalx.class;
            }
        }
    }

    private static class SubstationRecordData extends AbstractRecordGroup<PsseSubstationRecord> {
        SubstationRecordData() {
            super(SUBSTATION, "is", "name", "lati", "long", "srg");
            withQuotedFields(QUOTED_FIELDS);
        }

        @Override
        public Class<PsseSubstationRecord> psseTypeClass() {
            return PsseSubstationRecord.class;
        }
    }

    private static final String[] QUOTED_FIELDS = {"name", "type", "id", "ckt", "eqid"};
}
