/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.data;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.psse.model.PsseCaseIdentification;
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseRawModel;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static com.powsybl.psse.model.PsseVersion.Major.V35;

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
        PsseCaseIdentification caseIdentification = new CaseIdentificationData().read1x(networkNode, context);
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

    @Override
    public void write(PsseRawModel model, Context context, DataSource dataSource) throws IOException {
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

    private void write(PsseRawModel model, Context context, BufferedOutputStream outputStream) throws IOException {
        JsonGenerator generator = new JsonFactory().createGenerator(outputStream).setPrettyPrinter(new DefaultPrettyPrinter());
        generator.writeStartObject();
        generator.writeFieldName("network");
        generator.writeStartObject();
        generator.flush();

        new CaseIdentificationData().write1x(model, context, generator);
        new BusData().writex(model.getBuses(), context, generator);
        new LoadData().writex(model.getLoads(), context, generator);
        new FixedBusShuntData().writex(model.getFixedShunts(), context, generator);
        new GeneratorData().writex(model.getGenerators(), context, generator);
        new NonTransformerBranchData().writex(model.getNonTransformerBranches(), context, generator);
        new TransformerData().writex(model.getTransformers(), context, generator);
        new AreaInterchangeData().writex(model.getAreas(), context, generator);
        new ZoneData().writex(model.getZones(), context, generator);
        new OwnerData().writex(model.getOwners(), context, generator);

        generator.writeEndObject(); // network
        generator.writeEndObject(); // root
        generator.flush();
    }
}
