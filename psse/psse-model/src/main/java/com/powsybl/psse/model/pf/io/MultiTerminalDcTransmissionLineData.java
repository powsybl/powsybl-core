/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf.io;

import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.io.*;
import com.powsybl.psse.model.pf.PsseMultiTerminalDcTransmissionLine;
import com.powsybl.psse.model.pf.PsseMultiTerminalDcTransmissionLine.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.powsybl.psse.model.pf.io.PowerFlowRecordGroup.*;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class MultiTerminalDcTransmissionLineData extends AbstractRecordGroup<PsseMultiTerminalDcTransmissionLine> {

    MultiTerminalDcTransmissionLineData() {
        super(MULTI_TERMINAL_DC_TRANSMISSION_LINE);
        withIO(FileFormat.LEGACY_TEXT, new IOLegacyText(this));
        withIO(FileFormat.JSON, new IOJson(this));
    }

    @Override
    protected Class<PsseMultiTerminalDcTransmissionLine> psseTypeClass() {
        return PsseMultiTerminalDcTransmissionLine.class;
    }

    private static class IOLegacyText extends RecordGroupIOLegacyText<PsseMultiTerminalDcTransmissionLine> {

        IOLegacyText(AbstractRecordGroup<PsseMultiTerminalDcTransmissionLine> recordGroup) {
            super(recordGroup);
        }

        @Override
        public List<PsseMultiTerminalDcTransmissionLine> read(LegacyTextReader reader, Context context) throws IOException {

            MultiTerminalDcMainData mainData = new MultiTerminalDcMainData();
            List<PsseMultiTerminalDcTransmissionLine> multiTerminalList = new ArrayList<>();

            List<String> converterRecords = new ArrayList<>();
            List<String> busRecords = new ArrayList<>();
            List<String> linkRecords = new ArrayList<>();

            if (!reader.isQRecordFound()) {
                String line = reader.readRecordLine();
                while (!reader.endOfBlock(line)) {
                    PsseMultiTerminalDcMain main = mainData.readFromStrings(Collections.singletonList(line), context).get(0);

                    addNextNrecords(reader, converterRecords, main.getNconv());
                    addNextNrecords(reader, busRecords, main.getNdcbs());
                    addNextNrecords(reader, linkRecords, main.getNdcln());
                    line = reader.readRecordLine();

                    PsseMultiTerminalDcTransmissionLine multiTerminal = new PsseMultiTerminalDcTransmissionLine(main);
                    multiTerminalList.add(multiTerminal);
                }
            }

            List<PsseMultiTerminalDcConverter> converterList = new MultiTerminalDcConverterData().readFromStrings(converterRecords, context);
            List<PsseMultiTerminalDcBus> busList = new MultiTerminalDcBusData().readFromStrings(busRecords, context);
            List<PsseMultiTerminalDcLink> linkList = new MultiTerminalDcLinkData().readFromStrings(linkRecords, context);

            int indexConverter = 0;
            int indexBus = 0;
            int indexLink = 0;
            for (PsseMultiTerminalDcTransmissionLine multiTerminal : multiTerminalList) {
                indexConverter = addNextN(multiTerminal.getDcConverters(), converterList, multiTerminal.getNconv(), indexConverter);
                indexBus = addNextN(multiTerminal.getDcBuses(), busList, multiTerminal.getNdcbs(), indexBus);
                indexLink = addNextN(multiTerminal.getDcLinks(), linkList, multiTerminal.getNdcln(), indexLink);
            }

            return multiTerminalList;
        }

        private static void addNextNrecords(LegacyTextReader reader, List<String> records, int nRecords) throws IOException {
            int n = 0;
            while (n < nRecords) {
                records.add(reader.readRecordLine());
                n++;
            }
        }

        private static <T> int addNextN(List<T> multiTerminalItems, List<T> items, int nItems, int initialIndex) {
            int index = initialIndex;
            int n = 0;
            while (n < nItems) {
                multiTerminalItems.add(items.get(index));
                index++;
                n++;
            }
            return index;
        }

        @Override
        public void write(List<PsseMultiTerminalDcTransmissionLine> multiTerminalList, Context context, OutputStream outputStream) {
            List<PsseMultiTerminalDcMain> mainList = new ArrayList<>();
            List<PsseMultiTerminalDcConverter> converterList = new ArrayList<>();
            List<PsseMultiTerminalDcBus> busList = new ArrayList<>();
            List<PsseMultiTerminalDcLink> linkList = new ArrayList<>();

            multiTerminalList.forEach(multiTerminal -> {
                mainList.add(multiTerminal.getMain());
                converterList.addAll(multiTerminal.getDcConverters());
                busList.addAll(multiTerminal.getDcBuses());
                linkList.addAll(multiTerminal.getDcLinks());
            });

            MultiTerminalDcMainData mainData = new MultiTerminalDcMainData();
            List<String> mainStrings = mainData.buildRecords(mainList, context.getFieldNames(MULTI_TERMINAL_DC_TRANSMISSION_LINE), mainData.quotedFields(), context);

            MultiTerminalDcConverterData converterData = new MultiTerminalDcConverterData();
            List<String> converterStrings = converterData.buildRecords(converterList, context.getFieldNames(INTERNAL_MULTI_TERMINAL_DC_CONVERTER), converterData.quotedFields(), context);

            MultiTerminalDcBusData busData = new MultiTerminalDcBusData();
            List<String> busStrings = busData.buildRecords(busList, context.getFieldNames(INTERNAL_MULTI_TERMINAL_DC_BUS), busData.quotedFields(), context);

            MultiTerminalDcLinkData linkData = new MultiTerminalDcLinkData();
            List<String> linkStrings = linkData.buildRecords(linkList, context.getFieldNames(INTERNAL_MULTI_TERMINAL_DC_LINK), linkData.quotedFields(), context);

            int indexMain = 0;
            int indexConverter = 0;
            int indexBus = 0;
            int indexLink = 0;

            writeBegin(outputStream);
            for (PsseMultiTerminalDcTransmissionLine multiTerminal : multiTerminalList) {
                List<String> strings = new ArrayList<>();

                indexMain = addNextNstrings(mainStrings, strings, 1, indexMain);
                indexConverter = addNextNstrings(converterStrings, strings, multiTerminal.getNconv(), indexConverter);
                indexBus = addNextNstrings(busStrings, strings, multiTerminal.getNdcbs(), indexBus);
                indexLink = addNextNstrings(linkStrings, strings, multiTerminal.getNdcln(), indexLink);
                write(strings, outputStream);
            }
            writeEnd(outputStream);
        }

        private static int addNextNstrings(List<String> sourceStrings, List<String> strings, int nStrings,
            int initialIndex) {
            int index = initialIndex;
            int n = 0;
            while (n < nStrings) {
                strings.add(sourceStrings.get(index));
                index++;
                n++;

            }
            return index;
        }

        private static class MultiTerminalDcConverterData extends AbstractRecordGroup<PsseMultiTerminalDcConverter> {
            MultiTerminalDcConverterData() {
                super(INTERNAL_MULTI_TERMINAL_DC_CONVERTER, "ib", "n", "angmx", "angmn", "rc", "xc", "ebas", "tr", "tap", "tpmx", "tpmn", "tstp", "setvl", "dcpf", "marg", "cnvcod");
                withQuotedFields(QUOTED_FIELDS);
            }

            @Override
            public Class<PsseMultiTerminalDcConverter> psseTypeClass() {
                return PsseMultiTerminalDcConverter.class;
            }
        }

        private static class MultiTerminalDcBusData extends AbstractRecordGroup<PsseMultiTerminalDcBus> {
            MultiTerminalDcBusData() {
                super(INTERNAL_MULTI_TERMINAL_DC_BUS, "idc", "ib", "area", "zone", "dcname", "idc2", "rgrnd", "owner");
                withQuotedFields(QUOTED_FIELDS);
            }

            @Override
            public Class<PsseMultiTerminalDcBus> psseTypeClass() {
                return PsseMultiTerminalDcBus.class;
            }
        }

        private static class MultiTerminalDcLinkData extends AbstractRecordGroup<PsseMultiTerminalDcLink> {
            MultiTerminalDcLinkData() {
                super(INTERNAL_MULTI_TERMINAL_DC_LINK, "idc", "jdc", "dcckt", "met", "rdc", "ldc");
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
        public List<PsseMultiTerminalDcTransmissionLine> read(LegacyTextReader reader, Context context) throws IOException {
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
            super(MULTI_TERMINAL_DC_TRANSMISSION_LINE, "name", "nconv", "ndcbs", "ndcln", "mdc", "vconv", "vcmod", "vconvn");
            withQuotedFields(QUOTED_FIELDS);
        }

        @Override
        public Class<PsseMultiTerminalDcMain> psseTypeClass() {
            return PsseMultiTerminalDcMain.class;
        }
    }

    private static final String[] QUOTED_FIELDS = {"name", "dcname", "dcckt"};

}
