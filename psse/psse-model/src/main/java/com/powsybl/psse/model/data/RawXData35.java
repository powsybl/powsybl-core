/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.psse.model.PsseCaseIdentification;
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseRawModel;
import com.powsybl.psse.model.PsseVersion;
import com.powsybl.psse.model.data.JsonModel.ArrayData;
import com.powsybl.psse.model.data.JsonModel.JsonNetwork;
import com.powsybl.psse.model.data.JsonModel.TableData;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

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

    @Override
    public void write(PsseRawModel model, Context context, DataSource dataSource) throws IOException {
        Objects.requireNonNull(model);
        Objects.requireNonNull(context);
        Objects.requireNonNull(dataSource);
        if (context.getVersion() != PsseVersion.VERSION_35) {
            throw new PsseException("Unexpected version " + context.getVersion());
        }

        try (BufferedOutputStream outputStream = new BufferedOutputStream(dataSource.newOutputStream(null, "rawx", false));) {
            write(model, context, outputStream);
        }
    }

    private void write(PsseRawModel model, Context context, BufferedOutputStream outputStream) throws IOException {

        JsonNetwork network = new JsonNetwork();

        ArrayData arrayData = new CaseIdentificationData().write1(model, context);
        if (!arrayDataIsEmpty(arrayData)) {
            network.setCaseid(arrayData);
        }
        TableData tableData = new BusData().write(model, context);
        if (!tableDataIsEmpty(tableData)) {
            network.setBus(tableData);
        }
        tableData = new LoadData().write(model, context);
        if (!tableDataIsEmpty(tableData)) {
            network.setLoad(tableData);
        }
        tableData = new FixedBusShuntData().write(model, context);
        if (!tableDataIsEmpty(tableData)) {
            network.setFixshunt(tableData);
        }
        tableData = new GeneratorData().write(model, context);
        if (!tableDataIsEmpty(tableData)) {
            network.setGenerator(tableData);
        }
        tableData = new NonTransformerBranchData().write(model, context);
        if (!tableDataIsEmpty(tableData)) {
            network.setAcline(tableData);
        }
        tableData = new TransformerData().write(model, context);
        if (!tableDataIsEmpty(tableData)) {
            network.setTransformer(tableData);
        }

        tableData = new AreaInterchangeData().write(model, context);
        if (!tableDataIsEmpty(tableData)) {
            network.setArea(tableData);
        }
        tableData = new ZoneData().write(model, context);
        if (!tableDataIsEmpty(tableData)) {
            network.setZone(tableData);
        }
        tableData = new OwnerData().write(model, context);
        if (!tableDataIsEmpty(tableData)) {
            network.setOwner(tableData);
        }

        JsonModel jsonModel = new JsonModel(network);
        String json = Util.writeJsonModel(jsonModel);
        String adjustedJson = StringUtils.replaceEach(json, new String[] {"\"[", "]\"", "\\\""}, new String[] {"[", "]", "\""});
        outputStream.write(adjustedJson.getBytes(StandardCharsets.UTF_8));

        outputStream.close();
    }

    private boolean arrayDataIsEmpty(ArrayData arrayData) {
        return arrayData.getQuotedFields() == null || arrayData.getData() == null
            || arrayData.getQuotedFields().isEmpty() || arrayData.getData().isEmpty();
    }

    private boolean tableDataIsEmpty(TableData tableData) {
        return tableData.getQuotedFields() == null || tableData.getData() == null
            || tableData.getQuotedFields().isEmpty() || tableData.getData().isEmpty();
    }
}
