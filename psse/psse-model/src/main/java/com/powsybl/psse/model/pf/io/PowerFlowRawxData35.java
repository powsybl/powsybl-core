/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.pf.io;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.psse.model.io.Context;
import com.powsybl.psse.model.pf.PsseCaseIdentification;
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.pf.PssePowerFlowModel;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static com.powsybl.psse.model.PsseVersion.Major.V35;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class PowerFlowRawxData35 extends PowerFlowRawxDataAllVersions {

    @Override
    public PssePowerFlowModel read(ReadOnlyDataSource dataSource, String ext, Context context) throws IOException {
        try (InputStream is = dataSource.newInputStream(null, ext)) {
            return read(is, context);
        }
    }

    private PssePowerFlowModel read(InputStream stream, Context context) throws IOException {
        JsonNode networkNode = networkNode(stream);
        PsseCaseIdentification caseIdentification = new CaseIdentificationData().read1x(networkNode, context);
        caseIdentification.validate();

        PssePowerFlowModel model = new PssePowerFlowModel(caseIdentification);

        model.addBuses(new BusData().readJson(networkNode, context));
        model.addLoads(new LoadData().readJson(networkNode, context));
        model.addFixedShunts(new FixedBusShuntData().readJson(networkNode, context));
        model.addGenerators(new GeneratorData().readJson(networkNode, context));
        model.addNonTransformerBranches(new NonTransformerBranchData().readJson(networkNode, context));
        model.addTransformers(new TransformerData().readJson(networkNode, context));

        model.addAreas(new AreaInterchangeData().readJson(networkNode, context));
        model.addZones(new ZoneData().readJson(networkNode, context));
        model.addOwners(new OwnerData().readJson(networkNode, context));

        model.addSwitchedShunts(new SwitchedShuntData().readJson(networkNode, context));

        return model;
    }

    @Override
    public void write(PssePowerFlowModel model, Context context, DataSource dataSource) throws IOException {
        Objects.requireNonNull(model);
        Objects.requireNonNull(context);
        Objects.requireNonNull(dataSource);
        if (context.getVersion().major() != V35) {
            throw new PsseException("Unexpected version " + context.getVersion().getMajorNumber());
        }
        try (BufferedOutputStream outputStream = new BufferedOutputStream(dataSource.newOutputStream(null, "rawx", false));) {
            write(model, context, outputStream);
        }
    }

    private void write(PssePowerFlowModel model, Context context, BufferedOutputStream outputStream) throws IOException {
        try (JsonGenerator generator = new JsonFactory().createGenerator(outputStream).setPrettyPrinter(new DefaultPrettyPrinter())) {
            generator.writeStartObject();
            generator.writeFieldName("network");
            generator.writeStartObject();
            generator.flush();

            new CaseIdentificationData().write1x(model, context, generator);
            new BusData().writeJson(model.getBuses(), context, generator);
            new LoadData().writeJson(model.getLoads(), context, generator);
            new FixedBusShuntData().writeJson(model.getFixedShunts(), context, generator);
            new GeneratorData().writeJson(model.getGenerators(), context, generator);
            new NonTransformerBranchData().writeJson(model.getNonTransformerBranches(), context, generator);
            new TransformerData().writeJson(model.getTransformers(), context, generator);
            new AreaInterchangeData().writeJson(model.getAreas(), context, generator);
            new ZoneData().writeJson(model.getZones(), context, generator);
            new OwnerData().writeJson(model.getOwners(), context, generator);

            generator.writeEndObject(); // network
            generator.writeEndObject(); // root
            generator.flush();
        }
    }
}
