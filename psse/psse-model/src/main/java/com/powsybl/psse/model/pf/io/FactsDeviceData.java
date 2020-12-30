/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.pf.io;

import static com.powsybl.psse.model.PsseVersion.Major.V33;
import static com.powsybl.psse.model.PsseVersion.Major.V35;
import static com.powsybl.psse.model.pf.io.PowerFlowRecordGroup.FACTS_CONTROL_DEVICE;

import com.powsybl.psse.model.io.AbstractRecordGroup;
import com.powsybl.psse.model.pf.PsseFacts;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class FactsDeviceData extends AbstractRecordGroup<PsseFacts> {

    FactsDeviceData() {
        super(FACTS_CONTROL_DEVICE);
        withFieldNames(V33, "name", "i", "j", "mode", "pdes", "qdes", "vset", "shmx", "trmx", "vtmn", "vtmx", "vxmx", "imx", "linx",
            "rmpct", "owner", "set1", "set2", "vsref", "remot", "mname");
        withFieldNames(V35, "name", "ibus", "jbus", "mode", "pdes", "qdes", "vset", "shmx", "trmx", "vtmn", "vtmx", "vxmx", "imx", "linx",
            "rmpct", "owner", "set1", "set2", "vsref", "fcreg", "nreg", "mname");
        withQuotedFields("name", "mname");
    }

    @Override
    public Class<PsseFacts> psseTypeClass() {
        return PsseFacts.class;
    }

    /***
    FactsDeviceData(PsseVersion psseVersion) {
        super(psseVersion);
    }

    FactsDeviceData(PsseVersion psseVersion, PsseFileFormat psseFileFormat) {
        super(psseVersion, psseFileFormat);
    }

    List<PsseFacts> read(BufferedReader reader, PsseContext context) throws IOException {
        assertMinimumExpectedVersion(PsseBlockData.FACTS_DEVICE_DATA, PsseVersion.VERSION_33);

        List<String> records = readRecordBlock(reader);
        String[] headers = factsDeviceDataHeaders(this.getPsseVersion());
        context.setFactsDeviceDataReadFields(readFields(records, headers, context.getDelimiter()));

        if (this.getPsseVersion() == PsseVersion.VERSION_35) {
            List<PsseFacts35> facts35List = parseRecordsHeader(records, PsseFacts35.class, headers);
            return new ArrayList<>(facts35List);
        } else { // version_33
            return parseRecordsHeader(records, PsseFacts.class, headers);
        }
    }

    List<PsseFacts> readx(JsonNode networkNode, PsseContext context) {
        assertMinimumExpectedVersion(PsseBlockData.FACTS_DEVICE_DATA, PsseVersion.VERSION_35, PsseFileFormat.FORMAT_RAWX);

        JsonNode factsNode = networkNode.get("facts");
        if (factsNode == null) {
            return new ArrayList<>();
        }

        String[] headers = nodeFields(factsNode);
        List<String> records = nodeRecords(factsNode);

        context.setFactsDeviceDataReadFields(headers);
        List<PsseFacts35> facts35List = parseRecordsHeader(records, PsseFacts35.class, headers);
        return new ArrayList<>(facts35List);
    }

    void write(PsseRawModel model, PsseContext context, OutputStream outputStream) {
        assertMinimumExpectedVersion(PsseBlockData.FACTS_DEVICE_DATA, PsseVersion.VERSION_33);

        String[] headers = context.getFactsDeviceDataReadFields();
        String[] quoteFields = BlockData.insideHeaders(factsDeviceDataQuoteFields(), headers);

        if (this.getPsseVersion() == PsseVersion.VERSION_35) {

            List<PsseFacts35> facts35List = model.getFacts().stream()
                .map(m -> (PsseFacts35) m).collect(Collectors.toList());

            BlockData.<PsseFacts35>writeBlock(PsseFacts35.class, facts35List,
                headers, quoteFields, context.getDelimiter().charAt(0), outputStream);
        } else {
            BlockData.<PsseFacts>writeBlock(PsseFacts.class, model.getFacts(),
                headers, quoteFields, context.getDelimiter().charAt(0), outputStream);
        }
        BlockData.writeEndOfBlockAndComment("END OF FACTS CONTROL DEVICE DATA, BEGIN SWITCHED SHUNT DATA", outputStream);
    }

    TableData writex(PsseRawModel model, PsseContext context) {
        assertMinimumExpectedVersion(PsseBlockData.FACTS_DEVICE_DATA, PsseVersion.VERSION_35,
            PsseFileFormat.FORMAT_RAWX);

        String[] headers = context.getFactsDeviceDataReadFields();
        List<PsseFacts35> facts35List = model.getFacts().stream()
            .map(m -> (PsseFacts35) m).collect(Collectors.toList());

        List<String> stringList = BlockData.<PsseFacts35>writexBlock(PsseFacts35.class,
            facts35List, headers,
            BlockData.insideHeaders(factsDeviceDataQuoteFields(), headers),
            context.getDelimiter().charAt(0));

        return new TableData(headers, stringList);
    }

    private static String[] factsDeviceDataHeaders(PsseVersion version) {
        if (version == PsseVersion.VERSION_35) {
            return new String[] {"name", "ibus", "jbus", "mode", "pdes", "qdes", "vset", "shmx", "trmx", "vtmn", "vtmx", "vxmx", "imx", "linx",
                "rmpct", "owner", "set1", "set2", "vsref", "fcreg", "nreg", "mname"};
        } else { // Version 33
            return new String[] {"name", "i", "j", "mode", "pdes", "qdes", "vset", "shmx", "trmx", "vtmn", "vtmx", "vxmx", "imx", "linx",
                "rmpct", "owner", "set1", "set2", "vsref", "remot", "mname"};
        }
    }

    private static String[] factsDeviceDataQuoteFields() {
        return new String[] {"name", "mname"};
    }
    ***/
}
