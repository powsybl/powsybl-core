/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.gl;

import com.powsybl.cgmes.model.CgmesNamespace;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.iidm.network.DanglingLineFilter;
import com.powsybl.iidm.network.Network;
import com.powsybl.triplestore.api.PrefixNamespace;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.TripleStore;
import com.powsybl.triplestore.api.TripleStoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
public class CgmesGLExporter {

    private static final Logger LOG = LoggerFactory.getLogger(CgmesGLExporter.class);
    public static final String MD_NAMESPACE = "http://iec.ch/TC57/61970-552/ModelDescription/1#";

    private Network network;
    private TripleStore tripleStore;

    private static final String MODEL_SCENARIO_TIME = "Model.scenarioTime";
    private static final String MODEL_CREATED = "Model.created";
    private static final String MODEL_DESCRIPTION = "Model.description";
    private static final String MODEL_VERSION = "Model.version";
    private static final String MODEL_PROFILE = "Model.profile";
    private static final String MODEL_DEPENDENT_ON = "Model.DependentOn";
    private static final String IDENTIFIED_OBJECT_NAME = "IdentifiedObject.name";

    public CgmesGLExporter(Network network, TripleStore tripleStore) {
        this.network = Objects.requireNonNull(network);
        this.tripleStore = Objects.requireNonNull(tripleStore);
    }

    public CgmesGLExporter(Network network) {
        this(network, TripleStoreFactory.create());
    }

    public void exportData(DataSource dataSource) {
        Objects.requireNonNull(dataSource);
        ExportContext context = new ExportContext();
        context.setBasename(dataSource.getBaseName());
        context.setGlContext(CgmesGLUtils.contextNameFor(CgmesSubset.GEOGRAPHICAL_LOCATION, tripleStore, dataSource.getBaseName()));
        addNamespaces(context);
        addModel(context);
        addCoordinateSystem(context);
        exportSubstationsPosition(context);
        exportLinesPosition(context);
        tripleStore.write(dataSource);
    }

    private void addNamespaces(ExportContext context) {
        if (isMissedNamespace("data")) {
            tripleStore.addNamespace("data", "http://" + context.getBasename().toLowerCase() + "/#");
        }
        if (isMissedNamespace("cim")) {
            tripleStore.addNamespace("cim", CgmesNamespace.CIM_16_NAMESPACE);
        }
        if (isMissedNamespace("md")) {
            tripleStore.addNamespace("md", MD_NAMESPACE);
        }
    }

    private boolean isMissedNamespace(String prefix) {
        return tripleStore.getNamespaces().stream().map(PrefixNamespace::getPrefix).noneMatch(prefix::equals);
    }

    private void addModel(ExportContext context) {
        PropertyBag modelProperties = new PropertyBag(Arrays.asList(MODEL_SCENARIO_TIME, MODEL_CREATED, MODEL_DESCRIPTION, MODEL_VERSION, MODEL_PROFILE, MODEL_DEPENDENT_ON), true);
        modelProperties.setResourceNames(Collections.singletonList(MODEL_DEPENDENT_ON));
        modelProperties.setClassPropertyNames(Arrays.asList(MODEL_SCENARIO_TIME, MODEL_CREATED, MODEL_DESCRIPTION, MODEL_VERSION, MODEL_PROFILE, MODEL_DEPENDENT_ON));
        modelProperties.put(MODEL_SCENARIO_TIME, network.getCaseDate().toString());
        modelProperties.put(MODEL_CREATED, new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").format(new Date()));
        modelProperties.put(MODEL_DESCRIPTION, network.getNameOrId());
        modelProperties.put(MODEL_VERSION, "4");
        modelProperties.put(MODEL_PROFILE, "http://entsoe.eu/CIM/GeographicalLocation/2/1");
        modelProperties.put(MODEL_DEPENDENT_ON, network.getId());
        tripleStore.add(context.getGlContext(), MD_NAMESPACE, "FullModel", modelProperties);
    }

    private void addCoordinateSystem(ExportContext context) {
        PropertyBag coordinateSystemProperties = new PropertyBag(Arrays.asList(IDENTIFIED_OBJECT_NAME, "crsUrn"), true);
        coordinateSystemProperties.setClassPropertyNames(Collections.singletonList(IDENTIFIED_OBJECT_NAME));
        coordinateSystemProperties.put(IDENTIFIED_OBJECT_NAME, CgmesGLUtils.COORDINATE_SYSTEM_NAME);
        coordinateSystemProperties.put("crsUrn", CgmesGLUtils.COORDINATE_SYSTEM_URN);
        context.setCoordinateSystemId(tripleStore.add(context.getGlContext(), CgmesNamespace.CIM_16_NAMESPACE, "CoordinateSystem", coordinateSystemProperties));
    }

    private void exportSubstationsPosition(ExportContext context) {
        SubstationPositionExporter positionExporter = new SubstationPositionExporter(tripleStore, context);
        LOG.info("Exporting Substations Position");
        network.getSubstationStream().forEach(positionExporter::exportPosition);
    }

    private void exportLinesPosition(ExportContext context) {
        LinePositionExporter positionExporter = new LinePositionExporter(tripleStore, context);
        LOG.info("Exporting Lines Position");
        network.getLineStream().forEach(positionExporter::exportPosition);
        LOG.info("Exporting Dangling Lines Position");
        network.getDanglingLineStream(DanglingLineFilter.UNPAIRED).forEach(positionExporter::exportPosition);
    }

}
