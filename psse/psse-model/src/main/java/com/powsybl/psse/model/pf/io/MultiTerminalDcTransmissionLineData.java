/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.pf.io;

import static com.powsybl.psse.model.PsseVersion.Major.V33;
import static com.powsybl.psse.model.PsseVersion.Major.V35;
import static com.powsybl.psse.model.pf.io.PowerFlowRecordGroup.MULTI_TERMINAL_DC_TRANSMISSION_LINE;
import static com.powsybl.psse.model.pf.io.PowerFlowRecordGroup.INTERNAL_MULTI_TERMINAL_DC_CONVERTER;
import static com.powsybl.psse.model.pf.io.PowerFlowRecordGroup.INTERNAL_MULTI_TERMINAL_DC_BUS;
import static com.powsybl.psse.model.pf.io.PowerFlowRecordGroup.INTERNAL_MULTI_TERMINAL_DC_LINK;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.io.AbstractRecordGroup;
import com.powsybl.psse.model.io.Context;
import com.powsybl.psse.model.io.FileFormat;
import com.powsybl.psse.model.io.RecordGroupIOJson;
import com.powsybl.psse.model.io.RecordGroupIOLegacyText;
import com.powsybl.psse.model.pf.PsseMultiTerminalDcTransmissionLine;
import com.powsybl.psse.model.pf.PsseMultiTerminalDcTransmissionLine.PsseMultiTerminalDcBus;
import com.powsybl.psse.model.pf.PsseMultiTerminalDcTransmissionLine.PsseMultiTerminalDcBusx;
import com.powsybl.psse.model.pf.PsseMultiTerminalDcTransmissionLine.PsseMultiTerminalDcConverter;
import com.powsybl.psse.model.pf.PsseMultiTerminalDcTransmissionLine.PsseMultiTerminalDcConverterx;
import com.powsybl.psse.model.pf.PsseMultiTerminalDcTransmissionLine.PsseMultiTerminalDcLink;
import com.powsybl.psse.model.pf.PsseMultiTerminalDcTransmissionLine.PsseMultiTerminalDcLinkx;
import com.powsybl.psse.model.pf.PsseMultiTerminalDcTransmissionLine.PsseMultiTerminalDcMain;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class MultiTerminalDcTransmissionLineData extends AbstractRecordGroup<PsseMultiTerminalDcTransmissionLine> {

    MultiTerminalDcTransmissionLineData() {
        super(MULTI_TERMINAL_DC_TRANSMISSION_LINE);
        withIO(FileFormat.LEGACY_TEXT, new IOLegacyText(this));
        withIO(FileFormat.JSON, new IOJson(this));
    }

    @Override
    public Class<PsseMultiTerminalDcTransmissionLine> psseTypeClass() {
        return PsseMultiTerminalDcTransmissionLine.class;
    }

    private static class IOLegacyText extends RecordGroupIOLegacyText<PsseMultiTerminalDcTransmissionLine> {

        IOLegacyText(AbstractRecordGroup<PsseMultiTerminalDcTransmissionLine> recordGroup) {
            super(recordGroup);
        }

        @Override
        public List<PsseMultiTerminalDcTransmissionLine> read(BufferedReader reader, Context context) throws IOException {
            List<String> records = readRecords(reader);

            MultiTerminalDcMainData mainData = new MultiTerminalDcMainData();
            MultiTerminalDcConverterData converterData = new MultiTerminalDcConverterData();
            MultiTerminalDcBusData busData = new MultiTerminalDcBusData();
            MultiTerminalDcLinkData linkData = new MultiTerminalDcLinkData();

            List<PsseMultiTerminalDcTransmissionLine> multiTerminalList = new ArrayList<>();

            int i = 0;
            while (i < records.size()) {
                PsseMultiTerminalDcMain main = mainData.readFromStrings(Collections.singletonList(records.get(i++)), context).get(0);

                List<String> converterRecords = new ArrayList<>();
                int nConverter = 0;
                while (nConverter < main.getNconv()) {
                    converterRecords.add(records.get(i++));
                    nConverter++;
                }
                List<String> busRecords = new ArrayList<>();
                int nBus = 0;
                while (nBus < main.getNdcbs()) {
                    busRecords.add(records.get(i++));
                    nBus++;
                }
                List<String> linkRecords = new ArrayList<>();
                int nLinks = 0;
                while (nLinks < main.getNdcln()) {
                    linkRecords.add(records.get(i++));
                    nLinks++;
                }

                PsseMultiTerminalDcTransmissionLine multiTerminal = new PsseMultiTerminalDcTransmissionLine(main);
                multiTerminal.getDcConverters().addAll(converterData.readFromStrings(converterRecords, context));
                multiTerminal.getDcBuses().addAll(busData.readFromStrings(busRecords, context));
                multiTerminal.getDcLinks().addAll(linkData.readFromStrings(linkRecords, context));
                multiTerminalList.add(multiTerminal);
            }
            return multiTerminalList;
        }

        @Override
        public void write(List<PsseMultiTerminalDcTransmissionLine> multiTerminalList, Context context, OutputStream outputStream) {
            MultiTerminalDcMainData mainData = new MultiTerminalDcMainData();
            MultiTerminalDcConverterData converterData = new MultiTerminalDcConverterData();
            MultiTerminalDcBusData busData = new MultiTerminalDcBusData();
            MultiTerminalDcLinkData linkData = new MultiTerminalDcLinkData();

            writeBegin(outputStream);
            multiTerminalList.forEach(multiTerminal -> {
                write(String.format("%s%n", mainData.buildRecord(multiTerminal.getMain(), mainData.fieldNames(context.getVersion()), mainData.quotedFields(), context)), outputStream);
                write(converterData.buildRecords(multiTerminal.getDcConverters(), converterData.fieldNames(context.getVersion()), converterData.quotedFields(), context), outputStream);
                write(busData.buildRecords(multiTerminal.getDcBuses(), busData.fieldNames(context.getVersion()), busData.quotedFields(), context), outputStream);
                write(linkData.buildRecords(multiTerminal.getDcLinks(), linkData.fieldNames(context.getVersion()), linkData.quotedFields(), context), outputStream);
            });
            writeEnd(outputStream);
        }

        private static class MultiTerminalDcConverterData extends AbstractRecordGroup<PsseMultiTerminalDcConverter> {
            MultiTerminalDcConverterData() {
                super(INTERNAL_MULTI_TERMINAL_DC_CONVERTER);
                withFieldNames(V33, FIELD_NAMES_CONVERTER);
                withFieldNames(V35, FIELD_NAMES_CONVERTER);
                withQuotedFields(QUOTED_FIELDS);
            }

            @Override
            public Class<PsseMultiTerminalDcConverter> psseTypeClass() {
                return PsseMultiTerminalDcConverter.class;
            }
        }

        private static class MultiTerminalDcBusData extends AbstractRecordGroup<PsseMultiTerminalDcBus> {
            MultiTerminalDcBusData() {
                super(INTERNAL_MULTI_TERMINAL_DC_BUS);
                withFieldNames(V33, FIELD_NAMES_BUS);
                withFieldNames(V35, FIELD_NAMES_BUS);
                withQuotedFields(QUOTED_FIELDS);
            }

            @Override
            public Class<PsseMultiTerminalDcBus> psseTypeClass() {
                return PsseMultiTerminalDcBus.class;
            }
        }

        private static class MultiTerminalDcLinkData extends AbstractRecordGroup<PsseMultiTerminalDcLink> {
            MultiTerminalDcLinkData() {
                super(INTERNAL_MULTI_TERMINAL_DC_LINK);
                withFieldNames(V33, FIELD_NAMES_LINK);
                withFieldNames(V35, FIELD_NAMES_LINK);
                withQuotedFields(QUOTED_FIELDS);
            }

            @Override
            public Class<PsseMultiTerminalDcLink> psseTypeClass() {
                return PsseMultiTerminalDcLink.class;
            }
        }
    }

    private static class IOJson extends RecordGroupIOJson<PsseMultiTerminalDcTransmissionLine> {
        IOJson(AbstractRecordGroup<PsseMultiTerminalDcTransmissionLine> recordGroup) {
            super(recordGroup);
        }

        @Override
        public List<PsseMultiTerminalDcTransmissionLine> read(BufferedReader reader, Context context) throws IOException {
            if (reader != null) {
                throw new PsseException("Unexpected reader. Should be null");
            }
            List<PsseMultiTerminalDcMain> mainList = new MultiTerminalDcMainData().read(null, context);
            List<PsseMultiTerminalDcConverterx> converterxList = new MultiTerminalDcConverterxData().read(null, context);
            List<PsseMultiTerminalDcBusx> busxList = new MultiTerminalDcBusxData().read(null, context);
            List<PsseMultiTerminalDcLinkx> linkxList = new MultiTerminalDcLinkxData().read(null, context);
            return convertToMultiTerminalList(mainList, converterxList, busxList, linkxList);
        }

        @Override
        public void write(List<PsseMultiTerminalDcTransmissionLine> multiTerminalList, Context context, OutputStream outputStream) {
            if (outputStream != null) {
                throw new PsseException("Unexpected outputStream. Should be null");
            }
            List<PsseMultiTerminalDcMain> mainList = multiTerminalList.stream().map(PsseMultiTerminalDcTransmissionLine::getMain).collect(Collectors.toList());
            new MultiTerminalDcMainData().write(mainList, context, null);

            List<PsseMultiTerminalDcConverterx> converterxList = new ArrayList<>();
            multiTerminalList.forEach(multiTerminalDcTransmissionLine ->
                multiTerminalDcTransmissionLine.getDcConverters().forEach(converter -> converterxList.add(
                    new PsseMultiTerminalDcConverterx(multiTerminalDcTransmissionLine.getMain().getName(), converter))));
            new MultiTerminalDcConverterxData().write(converterxList, context, null);

            List<PsseMultiTerminalDcBusx> busxList = new ArrayList<>();
            multiTerminalList.forEach(multiTerminalDcTransmissionLine ->
                multiTerminalDcTransmissionLine.getDcBuses().forEach(bus -> busxList.add(
                    new PsseMultiTerminalDcBusx(multiTerminalDcTransmissionLine.getMain().getName(), bus))));
            new MultiTerminalDcBusxData().write(busxList, context, null);

            List<PsseMultiTerminalDcLinkx> linkxList = new ArrayList<>();
            multiTerminalList.forEach(multiTerminalDcTransmissionLine ->
                multiTerminalDcTransmissionLine.getDcLinks().forEach(link -> linkxList.add(
                    new PsseMultiTerminalDcLinkx(multiTerminalDcTransmissionLine.getMain().getName(), link))));
            new MultiTerminalDcLinkxData().write(linkxList, context, null);
        }

        private static List<PsseMultiTerminalDcTransmissionLine> convertToMultiTerminalList(List<PsseMultiTerminalDcMain> mainList,
            List<PsseMultiTerminalDcConverterx> converterxList, List<PsseMultiTerminalDcBusx> busxList,
            List<PsseMultiTerminalDcLinkx> linkxList) {

            List<PsseMultiTerminalDcTransmissionLine> multiTerminalDcTransmissionLineList = new ArrayList<>();
            for (PsseMultiTerminalDcMain main : mainList) {
                List<PsseMultiTerminalDcConverter> converterList = converterxList.stream().filter(c -> c.getName().equals(main.getName())).map(PsseMultiTerminalDcConverterx::getConverter).collect(Collectors.toList());
                List<PsseMultiTerminalDcBus> busList = busxList.stream().filter(c -> c.getName().equals(main.getName())).map(PsseMultiTerminalDcBusx::getBus).collect(Collectors.toList());
                List<PsseMultiTerminalDcLink> linkList = linkxList.stream().filter(c -> c.getName().equals(main.getName())).map(PsseMultiTerminalDcLinkx::getLink).collect(Collectors.toList());

                multiTerminalDcTransmissionLineList.add(new PsseMultiTerminalDcTransmissionLine(main, converterList, busList, linkList));
            }
            return multiTerminalDcTransmissionLineList;
        }

        private static class MultiTerminalDcConverterxData extends AbstractRecordGroup<PsseMultiTerminalDcConverterx> {
            MultiTerminalDcConverterxData() {
                super(INTERNAL_MULTI_TERMINAL_DC_CONVERTER);
                withQuotedFields(QUOTED_FIELDS);
            }

            @Override
            public Class<PsseMultiTerminalDcConverterx> psseTypeClass() {
                return PsseMultiTerminalDcConverterx.class;
            }
        }

        private static class MultiTerminalDcBusxData extends AbstractRecordGroup<PsseMultiTerminalDcBusx> {
            MultiTerminalDcBusxData() {
                super(INTERNAL_MULTI_TERMINAL_DC_BUS);
                withQuotedFields(QUOTED_FIELDS);
            }

            @Override
            public Class<PsseMultiTerminalDcBusx> psseTypeClass() {
                return PsseMultiTerminalDcBusx.class;
            }
        }

        private static class MultiTerminalDcLinkxData extends AbstractRecordGroup<PsseMultiTerminalDcLinkx> {
            MultiTerminalDcLinkxData() {
                super(INTERNAL_MULTI_TERMINAL_DC_LINK);
                withQuotedFields(QUOTED_FIELDS);
            }

            @Override
            public Class<PsseMultiTerminalDcLinkx> psseTypeClass() {
                return PsseMultiTerminalDcLinkx.class;
            }
        }
    }

    private static class MultiTerminalDcMainData extends AbstractRecordGroup<PsseMultiTerminalDcMain> {
        MultiTerminalDcMainData() {
            super(MULTI_TERMINAL_DC_TRANSMISSION_LINE);
            withFieldNames(V33, FIELD_NAMES_MAIN);
            withFieldNames(V35, FIELD_NAMES_MAIN);
            withQuotedFields(QUOTED_FIELDS);
        }

        @Override
        public Class<PsseMultiTerminalDcMain> psseTypeClass() {
            return PsseMultiTerminalDcMain.class;
        }
    }

    private static final String[] FIELD_NAMES_MAIN = {"name", "nconv", "ndcbs", "ndcln", "mdc", "vconv", "vcmod", "vconvn"};
    private static final String[] FIELD_NAMES_CONVERTER = {"ib", "n", "angmx", "angmn", "rc", "xc", "ebas", "tr", "tap", "tpmx", "tpmn", "tstp", "setvl", "dcpf", "marg", "cnvcod"};
    private static final String[] FIELD_NAMES_BUS = {"idc", "ib", "area", "zone", "dcname", "idc2", "rgrnd", "owner"};
    private static final String[] FIELD_NAMES_LINK = {"idc", "jdc", "dcckt", "met", "rdc", "ldc"};
    private static final String[] QUOTED_FIELDS = {"name", "dcname", "dcckt"};

}
