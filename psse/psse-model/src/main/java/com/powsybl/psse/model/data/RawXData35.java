/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.psse.model.PsseCaseIdentification;
import com.powsybl.psse.model.PsseConstants.PsseVersion;
import com.powsybl.psse.model.PsseContext;
import com.powsybl.psse.model.PsseRawModel;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class RawXData35 implements RawData {

    @Override
    public boolean isValidFile(ReadOnlyDataSource dataSource, String ext) throws IOException {
        String jsonFile = new String(ByteStreams.toByteArray(dataSource.newInputStream(null, ext)), StandardCharsets.UTF_8);
        JsonNode networkNode = new ObjectMapper().readTree(jsonFile).get("network");
        PsseCaseIdentification caseIdentification = new CaseIdentificationData().read1(networkNode, new PsseContext());
        caseIdentification.validate();
        return true;
    }

    @Override
    public PsseVersion readVersion(ReadOnlyDataSource dataSource, String ext) throws IOException {
        String jsonFile = new String(ByteStreams.toByteArray(dataSource.newInputStream(null, ext)), StandardCharsets.UTF_8);
        JsonNode networkNode = new ObjectMapper().readTree(jsonFile).get("network");
        PsseCaseIdentification caseIdentification = new CaseIdentificationData().read1(networkNode, new PsseContext());
        return PsseVersion.fromNumber(caseIdentification.getRev());
    }

    @Override
    public PsseRawModel read(ReadOnlyDataSource dataSource, String ext, PsseContext context) throws IOException {
        String jsonFile = new String(ByteStreams.toByteArray(dataSource.newInputStream(null, ext)), StandardCharsets.UTF_8);
        return read(jsonFile, context);
    }

    private PsseRawModel read(String jsonFile, PsseContext context) throws IOException {
        JsonNode networkNode = new ObjectMapper().readTree(jsonFile).get("network");
        PsseCaseIdentification caseIdentification = new CaseIdentificationData().read1(networkNode, context);
        caseIdentification.validate();

        PsseRawModel model = new PsseRawModel(caseIdentification);

        model.addBuses(new BusData().readx(networkNode, context));
        model.addLoads(new LoadData().readx(networkNode, context));
        model.addFixedShunts(new FixedBusShuntData().readx(networkNode, context));
        model.addGenerators(new GeneratorData().readx(networkNode, context));
        model.addNonTransformerBranches(new NonTransformerBranchData().readx(networkNode, context));
        model.addTransformers(new TransformerData().readx(networkNode, context));

        model.addAreas(new AreaInterchangeData().readx(networkNode, context));
        model.addZones(new ZoneData().readx(networkNode, context));
        model.addOwners(new OwnerData().readx(networkNode, context));

        model.addSwitchedShunts(new SwitchedShuntData().readx(networkNode, context));

        return model;
    }
}
