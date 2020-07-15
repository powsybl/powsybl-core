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
import com.powsybl.psse.model.PsseContext;
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseLoad35;
import com.powsybl.psse.model.PsseRawModel;
import com.powsybl.psse.model.PsseRawModel35;
import com.powsybl.psse.model.data.BlockData.PsseFileFormat;
import com.powsybl.psse.model.data.BlockData.PsseVersion;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class PsseData {

    public PsseData() {
    }

    public boolean checkCase33(BufferedReader reader) throws IOException {

        PsseVersion version = PsseVersion.VERSION_33;

        // just check the first record if this file is in PSS/E format
        PsseCaseIdentification caseIdentification;
        try {
            caseIdentification = new CaseIdentificationData(version).read(reader);
        } catch (PsseException e) {
            return false; // invalid PSS/E content
        }

        int ic = caseIdentification.getIc();
        double sbase = caseIdentification.getSbase();
        int rev = caseIdentification.getRev();
        double basfrq = caseIdentification.getBasfrq();

        if (ic == 0 && sbase > 0. && rev <= PsseConstants.SUPPORTED_VERSION && basfrq > 0.) {
            return true;
        }

        return false;
    }

    public PsseRawModel read33(BufferedReader reader, PsseContext context) throws IOException {

        PsseVersion version = PsseVersion.VERSION_33;

        PsseCaseIdentification caseIdentification = new CaseIdentificationData(version).read(reader, context);
        PsseRawModel model = new PsseRawModel(caseIdentification);

        model.addBuses(new BusData(version).read(reader, context));
        model.addLoads(new LoadData(version).read(reader, context));
        model.addFixedShunts(new FixedBusShuntData(version).read(reader, context));
        model.addGenerators(new GeneratorData(version).read(reader, context));
        model.addNonTransformerBranches(new NonTransformerBranchData(version).read(reader, context));
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

        // q record (nothing to do)
        BlockData.readDiscardedRecordBlock(reader);

        System.err.printf("Loads %d %n", model.getLoads().size());
        System.err.printf("Generators %d %n", model.getGenerators().size());
        System.err.printf("NonTransformerBranches %d %n", model.getNonTransformerBranches().size());

        return model;
    }

    public PsseRawModel readx35(String jsonFile, PsseContext context) throws IOException {

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

        System.err.printf("Loads %d %n", model.getLoads().size());
        System.err.printf("Generators %d %n", model.getGenerators().size());
        System.err.printf("NonTransformerBranches %d %n", model.getNonTransformerBranches().size());
        System.err.printf("Transformers %d %n", model.getTransformers().size());
        System.err.printf("Zones %d %n", model.getZones().size());
        System.err.printf("Owners %d %n", model.getOwners().size());
        System.err.printf("SwitchedShunts %d %n", model.getSwitchedShunts().size());
        model.getLoads().forEach(load -> {
            PsseLoad35 load35 = (PsseLoad35) load;
            load35.print();
        });

        return model;
    }
}
