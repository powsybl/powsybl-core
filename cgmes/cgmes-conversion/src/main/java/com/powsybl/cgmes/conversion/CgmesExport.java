/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import java.util.Properties;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.google.auto.service.AutoService;
import com.powsybl.cgmes.conversion.export.CgmesExportContext;
import com.powsybl.cgmes.conversion.export.StateVariablesAdder;
import com.powsybl.cgmes.conversion.export.StateVariablesExport;
import com.powsybl.cgmes.conversion.export.SteadyStateHypothesisExport;
import com.powsybl.cgmes.conversion.update.CgmesUpdate;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.cgmes.model.CgmesModelFactory;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.export.Exporter;
import com.powsybl.iidm.network.Network;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
@AutoService(Exporter.class)
public class CgmesExport implements Exporter {

    @Override
    public void export(Network network, Properties params, DataSource ds) {
        Objects.requireNonNull(network);
        CgmesModelExtension ext = network.getExtension(CgmesModelExtension.class);
        if (params != null && Boolean.valueOf(params.getProperty("cgmes.export.usingOnlyNetwork"))) {
            if (ext != null) {
                CgmesModel cgmesSource = ext.getCgmesModel();
                if (cgmesSource != null) {
                    throw new CgmesModelException("CGMES model should not be available as Network extension");
                }
            }
            exportUsingOnlyNetwork(network, ds);
        } else {
            if (ext == null) {
                throw new CgmesModelException("CGMES model is required and not found in Network extension");
            }
            exportUsingOriginalCgmesModel(network, ds, ext);
        }
    }

    private void exportUsingOnlyNetwork(Network network, DataSource ds) {
        // At this point only SSH, SV can be exported when relying only in Network data
        // (minimum amount of CGMES references are expected as aliases/properties/extensions)
        String baseName = network.hasProperty("baseName") ? network.getProperty("baseName") : network.getNameOrId();
        String filenameSv = baseName + "_SV.xml";
        String filenameSsh = baseName + "_SSH.xml";
        CgmesExportContext context = new CgmesExportContext(network);
        try (OutputStream os = ds.newOutputStream(filenameSv, false)) {
            XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", os);
            StateVariablesExport.write(network, writer, context);
        } catch (IOException | XMLStreamException x) {
            throw new PowsyblException("Exporting to CGMES using only Network");
        }
        try (OutputStream os = ds.newOutputStream(filenameSsh, false)) {
            XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", os);
            SteadyStateHypothesisExport.write(network, writer, context);
        } catch (IOException | XMLStreamException x) {
            throw new PowsyblException("Exporting to CGMES using only Network");
        }
    }

    private void exportUsingOriginalCgmesModel(Network network, DataSource ds, CgmesModelExtension ext) {
        CgmesUpdate cgmesUpdate = ext.getCgmesUpdate();
        CgmesModel cgmesSource = ext.getCgmesModel();
        CgmesModel cgmes = CgmesModelFactory.copy(cgmesSource);
        String variantId = network.getVariantManager().getWorkingVariantId();
        cgmesUpdate.update(cgmes, variantId);
        // Fill the State Variables data with the Network current state values
        StateVariablesAdder adder = new StateVariablesAdder(cgmes, network);
        adder.addStateVariablesToCgmes();
        cgmes.write(ds);
    }

    @Override
    public String getComment() {
        return "ENTSO-E CGMES version 2.4.15";
    }

    @Override
    public String getFormat() {
        return "CGMES";
    }
}
