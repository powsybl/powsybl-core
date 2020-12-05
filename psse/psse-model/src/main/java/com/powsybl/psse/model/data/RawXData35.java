/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.psse.model.PsseCaseIdentification;
import com.powsybl.psse.model.PsseRawModel;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class RawXData35 extends RawXDataCommon {

    @Override
    public PsseRawModel read(ReadOnlyDataSource dataSource, String ext, Context context) throws IOException {
        try (InputStream is = dataSource.newInputStream(null, ext)) {
            return read(is, context);
        }
    }

    private PsseRawModel read(InputStream stream, Context context) throws IOException {
        JsonNode networkNode = networkNode(stream);
        PsseCaseIdentification caseIdentification = new CaseIdentificationData().read1(networkNode, context);
        caseIdentification.validate();

        PsseRawModel model = new PsseRawModel(caseIdentification);

        model.addBuses(new BusData().read(networkNode, context));
        model.addLoads(new LoadData().read(networkNode, context));
        model.addFixedShunts(new FixedBusShuntData().read(networkNode, context));
        model.addGenerators(new GeneratorData().read(networkNode, context));
        model.addNonTransformerBranches(new NonTransformerBranchData().read(networkNode, context));
        model.addTransformers(new TransformerData().read(networkNode, context));

        model.addAreas(new AreaInterchangeData().read(networkNode, context));
        model.addZones(new ZoneData().read(networkNode, context));
        model.addOwners(new OwnerData().read(networkNode, context));

        model.addSwitchedShunts(new SwitchedShuntData().read(networkNode, context));

        return model;
    }
}
