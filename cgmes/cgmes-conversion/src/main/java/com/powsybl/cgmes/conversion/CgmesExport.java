/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion;

import java.util.Objects;
import java.util.Properties;

import com.google.auto.service.AutoService;
import com.powsybl.cgmes.conversion.update.CgmesUpdate;
import com.powsybl.cgmes.conversion.update.StateVariablesAdder;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.cgmes.model.CgmesModelFactory;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.iidm.export.Exporter;
import com.powsybl.iidm.network.Network;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
@AutoService(Exporter.class)
public class CgmesExport implements Exporter {

    public CgmesExport() {
        this.profiling = new Profiling();
    }

    public CgmesExport(Profiling profiling) {
        Objects.requireNonNull(profiling);
        this.profiling = profiling;
    }

    @Override
    public void export(Network network, Properties params, DataSource ds) {

        // Right now the network must contain the original CgmesModel
        // In the future it should be possible to export to CGMES
        // directly from an IIDM Network,
        // without the need for the original CgmesModel
        CgmesModelExtension ext = network.getExtension(CgmesModelExtension.class);
        if (ext == null) {
            throw new CgmesModelException("No extension for CGMES model found in Network");
        }
        CgmesUpdate cgmesUpdate = ext.getCgmesUpdate();

        CgmesModel cgmesSource = ext.getCgmesModel();
        profiling.start();
        CgmesModel cgmes = CgmesModelFactory.copy(cgmesSource);
        profiling.end(Operations.TRIPLESTORE_COPY.name());

        String variantId = network.getVariantManager().getWorkingVariantId();

        profiling.startLoop();
        cgmesUpdate.update(cgmes, variantId, profiling);
        profiling.endLoop(Operations.TRIPLESTORE_UPDATE.name());

        profiling.start();
        // Clear the previous SV data in CGMES model
        // and fill it with the Network current state values
        cgmes.clear(CgmesSubset.STATE_VARIABLES);
        StateVariablesAdder.add(network, cgmes);
        profiling.end(Operations.ADD_STATE_VARIABLES.name());
        profiling.report();

        profiling.start();
        cgmes.write(ds);
        profiling.end(Operations.WRITE_UPDATED_CGMES.name());
    }

    @Override
    public String getComment() {
        return "ENTSO-E CGMES version 2.4.15";
    }

    @Override
    public String getFormat() {
        return "CGMES";
    }

    public enum Operations {
        IMPORT_CGMES, SCALING, LOAD_FLOW, TRIPLESTORE_COPY, CLONE_VARIANT, TRIPLESTORE_UPDATE,
        ADD_STATE_VARIABLES, WRITE_UPDATED_CGMES, CGMES_READ, CGMES_CONVERSION;
    }

    private final Profiling profiling;
}
