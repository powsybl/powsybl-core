/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.ArrayUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.psse.model.PsseCaseIdentification;
import com.powsybl.psse.model.PsseConstants;
import com.powsybl.psse.model.PsseConstants.PsseFileFormat;
import com.powsybl.psse.model.PsseConstants.PsseVersion;
import com.powsybl.psse.model.PsseContext;
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseRawModel;
import com.powsybl.psse.model.PsseRawModel35;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class PsseData {

    public void checkCase(BufferedReader reader) throws IOException {

     // CaseIdentification does not change, so it is read using version 33
        PsseVersion version = PsseVersion.VERSION_33;

        // just check the first record if this file is in PSS/E format
        PsseCaseIdentification caseIdentification;
        try {
            caseIdentification = new CaseIdentificationData(version).read(reader);
        } catch (PsseException e) {
            throw new PsseException("Invalid PSS/E RAW content");
        }
        checkCaseIdentification(caseIdentification);
    }

    public void checkCasex(String jsonFile) throws IOException {
        PsseVersion version = PsseVersion.VERSION_35;
        PsseFileFormat format = PsseFileFormat.FORMAT_RAWX;

        PsseCaseIdentification caseIdentification;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonFile);
            JsonNode networkNode = rootNode.get("network");

            caseIdentification = new CaseIdentificationData(version, format).readx(networkNode);
        } catch (IOException e) {
            throw new PsseException("Invalid PSS/E RAWX content");
        }
        checkCaseIdentification(caseIdentification);
    }

    private void checkCaseIdentification(PsseCaseIdentification caseIdentification) {
        int ic = caseIdentification.getIc();
        double sbase = caseIdentification.getSbase();
        int rev = caseIdentification.getRev();
        double basfrq = caseIdentification.getBasfrq();

        if (ic == 1) {
            throw new PsseException("Incremental load of PSS/E data option (IC = 1) not supported");
        }
        if (!ArrayUtils.contains(PsseConstants.SUPPORTED_VERSIONS, rev)) {
            String supportedVersions = IntStream.of(PsseConstants.SUPPORTED_VERSIONS).mapToObj(String::valueOf).collect(Collectors.joining(", "));
            throw new PsseException("PSS/E version " + rev + " not supported. Supported Versions are: " + supportedVersions + ".");
        }
        if (sbase <= 0.) {
            throw new PsseException("PSS/E Unexpected System MVA base " + sbase);
        }
        if (basfrq <= 0.) {
            throw new PsseException("PSS/E Unexpected System base frequency " + basfrq);
        }
    }

    public PsseRawModel read(BufferedReader reader, PsseContext context) throws IOException {

        // CaseIdentification does not change, so it is read using version 33
        PsseVersion version = PsseVersion.VERSION_33;
        PsseCaseIdentification caseIdentification = new CaseIdentificationData(version).read(reader, context);

        if (caseIdentification.getRev() == 33) {
            return read33(reader, caseIdentification, context);
        } else if (caseIdentification.getRev() == 35) {
            return read35(reader, caseIdentification, context);
        } else {
            throw new PsseException("Psse: unexpected version: " + caseIdentification.getRev());
        }
    }

    public PsseRawModel read33(BufferedReader reader,  PsseCaseIdentification caseIdentification, PsseContext context) throws IOException {

        PsseVersion version = PsseVersion.VERSION_33;
        PsseRawModel model = new PsseRawModel(caseIdentification);

        readBlocksA(reader, model, version, context);
        readBlocksB(reader, model, version, context);

        // q record (nothing to do)
        AbstractBlockData.readDiscardedQBlock(reader);

        return model;
    }

    public PsseRawModel read35(BufferedReader reader, PsseCaseIdentification caseIdentification, PsseContext context) throws IOException {
        PsseVersion version = PsseVersion.VERSION_35;
        PsseRawModel model = new PsseRawModel(caseIdentification);

        // System-Wide data
        AbstractBlockData.readDiscardedRecordBlock(reader); // TODO

        readBlocksA(reader, model, version, context);

        // System Switching device data
        AbstractBlockData.readDiscardedRecordBlock(reader); // TODO

        readBlocksB(reader, model, version, context);

        // Substation data
        AbstractBlockData.readDiscardedRecordBlock(reader); // TODO

        // q record (nothing to do)
        AbstractBlockData.readDiscardedQBlock(reader);

        return model;
    }

    private void readBlocksA(BufferedReader reader, PsseRawModel model, PsseVersion version, PsseContext context)
        throws IOException {

        model.addBuses(new BusData(version).read(reader, context));
        model.addLoads(new LoadData(version).read(reader, context));
        model.addFixedShunts(new FixedBusShuntData(version).read(reader, context));
        model.addGenerators(new GeneratorData(version).read(reader, context));
        model.addNonTransformerBranches(new NonTransformerBranchData(version).read(reader, context));
    }

    private void readBlocksB(BufferedReader reader, PsseRawModel model, PsseVersion version, PsseContext context)
        throws IOException {

        model.addTransformers(new TransformerData(version).read(reader, context));
        model.addAreas(new AreaInterchangeData(version).read(reader, context));

        // 2-terminal DC data
        AbstractBlockData.readDiscardedRecordBlock(reader); // TODO

        // voltage source converter data
        AbstractBlockData.readDiscardedRecordBlock(reader); // TODO

        // impedance correction data
        AbstractBlockData.readDiscardedRecordBlock(reader); // TODO

        // multi-terminal DC data
        AbstractBlockData.readDiscardedRecordBlock(reader); // TODO

        // multi-section line data
        AbstractBlockData.readDiscardedRecordBlock(reader); // TODO

        model.addZones(new ZoneData(version).read(reader, context));

        // inter-area transfer data
        AbstractBlockData.readDiscardedRecordBlock(reader); // TODO

        model.addOwners(new OwnerData(version).read(reader, context));

        // facts control device data
        AbstractBlockData.readDiscardedRecordBlock(reader); // TODO

        model.addSwitchedShunts(new SwitchedShuntData(version).read(reader, context));

        // gne device data
        AbstractBlockData.readDiscardedRecordBlock(reader); // TODO

        // Induction Machine data
        AbstractBlockData.readDiscardedRecordBlock(reader); // TODO
    }

    public PsseRawModel readx(String jsonFile, PsseContext context) throws IOException {

        PsseVersion version = PsseVersion.VERSION_35;
        PsseFileFormat format = PsseFileFormat.FORMAT_RAWX;

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonFile);
        JsonNode networkNode = rootNode.get("network");

        PsseCaseIdentification caseIdentification = new CaseIdentificationData(version, format).readx(networkNode, context);
        PsseRawModel model = new PsseRawModel35(caseIdentification);

        model.addBuses(new BusData(version, format).readx(networkNode, context));
        model.addLoads(new LoadData(version, format).readx(networkNode, context));
        model.addFixedShunts(new FixedBusShuntData(version, format).readx(networkNode, context));
        model.addGenerators(new GeneratorData(version, format).readx(networkNode, context));
        model.addNonTransformerBranches(new NonTransformerBranchData(version, format).readx(networkNode, context));
        model.addTransformers(new TransformerData(version, format).readx(networkNode, context));

        model.addAreas(new AreaInterchangeData(version, format).readx(networkNode, context));
        model.addZones(new ZoneData(version, format).readx(networkNode, context));
        model.addOwners(new OwnerData(version, format).readx(networkNode, context));

        model.addSwitchedShunts(new SwitchedShuntData(version, format).readx(networkNode, context));

        return model;
    }
}
