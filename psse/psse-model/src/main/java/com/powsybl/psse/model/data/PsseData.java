/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.data;

import java.io.BufferedReader;
import java.io.IOException;

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

    public boolean checkCase(BufferedReader reader) throws IOException {

     // CaseIdentification does not change, so it is read using version 33
        PsseVersion version = PsseVersion.VERSION_33;

        // just check the first record if this file is in PSS/E format
        PsseCaseIdentification caseIdentification;
        try {
            caseIdentification = new CaseIdentificationData(version).read(reader);
        } catch (PsseException e) {
            return false; // invalid PSS/E content
        }
        return checkCaseIdentification(caseIdentification);
    }

    public boolean checkCasex(String jsonFile) throws IOException {
        PsseVersion version = PsseVersion.VERSION_35;
        PsseFileFormat format = PsseFileFormat.FORMAT_RAWX;

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonFile);
        JsonNode networkNode = rootNode.get("network");

        PsseCaseIdentification caseIdentification = new CaseIdentificationData(version, format).read(networkNode);
        return checkCaseIdentification(caseIdentification);
    }

    private boolean checkCaseIdentification(PsseCaseIdentification caseIdentification) {
        int ic = caseIdentification.getIc();
        double sbase = caseIdentification.getSbase();
        int rev = caseIdentification.getRev();
        double basfrq = caseIdentification.getBasfrq();

        if (ic == 0 && sbase > 0. && rev >= PsseConstants.MIN_SUPPORTED_VERSION
            && rev <= PsseConstants.MAX_SUPPORTED_VERSION && basfrq > 0.) {
            return true;
        }

        return false;
    }

    public PsseRawModel read(BufferedReader reader, PsseContext context) throws IOException {

        // CaseIdentification does not change, so it is read using version 33
        PsseVersion version = PsseVersion.VERSION_33;
        PsseCaseIdentification caseIdentification = new CaseIdentificationData(version).read(reader, context);

        if (caseIdentification.getRev() == 33) {
            return read33(reader, caseIdentification, context);
        } else {
            return read35(reader, caseIdentification, context);
        }
    }

    public PsseRawModel read33(BufferedReader reader,  PsseCaseIdentification caseIdentification, PsseContext context) throws IOException {

        PsseVersion version = PsseVersion.VERSION_33;
        PsseRawModel model = new PsseRawModel(caseIdentification);

        readBlocksA(reader, model, version, context);
        readBlocksB(reader, model, version, context);

        // q record (nothing to do)
        BlockData.readDiscardedRecordBlock(reader);

        return model;
    }

    public PsseRawModel read35(BufferedReader reader, PsseCaseIdentification caseIdentification, PsseContext context) throws IOException {
        PsseVersion version = PsseVersion.VERSION_35;
        PsseRawModel model = new PsseRawModel(caseIdentification);

        // System-Wide data
        BlockData.readDiscardedRecordBlock(reader); // TODO

        readBlocksA(reader, model, version, context);

        // System Switching device data
        BlockData.readDiscardedRecordBlock(reader); // TODO

        readBlocksB(reader, model, version, context);

        // Substation data
        BlockData.readDiscardedRecordBlock(reader); // TODO

        // q record (nothing to do)
        BlockData.readDiscardedRecordBlock(reader);

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
        BlockData.readDiscardedRecordBlock(reader); // TODO

        // voltage source converter data
        BlockData.readDiscardedRecordBlock(reader); // TODO

        // impedance correction data
        BlockData.readDiscardedRecordBlock(reader); // TODO

        // multi-terminal DC data
        BlockData.readDiscardedRecordBlock(reader); // TODO

        // multi-section line data
        BlockData.readDiscardedRecordBlock(reader); // TODO

        model.addZones(new ZoneData(version).read(reader, context));

        // inter-area transfer data
        BlockData.readDiscardedRecordBlock(reader); // TODO

        model.addOwners(new OwnerData(version).read(reader, context));

        // facts control device data
        BlockData.readDiscardedRecordBlock(reader); // TODO

        model.addSwitchedShunts(new SwitchedShuntData(version).read(reader, context));

        // gne device data
        BlockData.readDiscardedRecordBlock(reader); // TODO

        // Induction Machine data
        BlockData.readDiscardedRecordBlock(reader); // TODO
    }

    public PsseRawModel readx(String jsonFile, PsseContext context) throws IOException {

        PsseVersion version = PsseVersion.VERSION_35;
        PsseFileFormat format = PsseFileFormat.FORMAT_RAWX;

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonFile);
        JsonNode networkNode = rootNode.get("network");

        PsseCaseIdentification caseIdentification = new CaseIdentificationData(version, format).read(networkNode, context);
        PsseRawModel model = new PsseRawModel35(caseIdentification);

        model.addBuses(new BusData(version, format).read(networkNode, context));
        model.addLoads(new LoadData(version, format).read(networkNode, context));
        model.addFixedShunts(new FixedBusShuntData(version, format).read(networkNode, context));
        model.addGenerators(new GeneratorData(version, format).read(networkNode, context));
        model.addNonTransformerBranches(new NonTransformerBranchData(version, format).read(networkNode, context));
        model.addTransformers(new TransformerData(version, format).read(networkNode, context));

        model.addAreas(new AreaInterchangeData(version, format).read(networkNode, context));
        model.addZones(new ZoneData(version, format).read(networkNode, context));
        model.addOwners(new OwnerData(version, format).read(networkNode, context));

        model.addSwitchedShunts(new SwitchedShuntData(version, format).read(networkNode, context));

        return model;
    }
}
