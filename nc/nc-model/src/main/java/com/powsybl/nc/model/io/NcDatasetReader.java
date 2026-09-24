/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nc.model.io;

import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ZipArchiveDataSource;
import com.powsybl.commons.parameters.Parameter;
import com.powsybl.commons.parameters.ParameterDefaultValueConfig;
import com.powsybl.commons.parameters.ParameterType;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.util.ServiceLoaderCache;
import com.powsybl.nc.model.NcDataset;
import com.powsybl.nc.model.NcException;
import com.powsybl.nc.model.NcKeyword;
import com.powsybl.nc.model.triplestore.NcDatasetTripleStore;
import com.powsybl.triplestore.api.TripleStore;
import com.powsybl.triplestore.api.TripleStoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

/**
 * Reads ENTSO-E Network Codes profiles into an {@link NcDataset}.
 * <p>
 * The reader mirrors the configuration surface of the other PowSyBl loaders: callers may supply
 * {@link Properties} resolved against {@link Parameter} definitions and platform configuration, and a
 * {@link ReportNode} collecting functional logs.
 *
 * @author Roman Vykuka {@literal <vykuka at gmail.com>}
 */
public final class NcDatasetReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(NcDatasetReader.class);

    public static final String FORMAT = "NC";

    public static final String TRIPLESTORE_IMPLEMENTATION = "nc.import.triplestore-implementation";

    private static final Parameter TRIPLESTORE_IMPLEMENTATION_PARAMETER = new Parameter(
            TRIPLESTORE_IMPLEMENTATION,
            ParameterType.STRING,
            "Triple store implementation used to load NC profiles",
            TripleStoreFactory.defaultImplementation(),
            List.copyOf(TripleStoreFactory.allImplementations()));

    private static final List<Parameter> PARAMETERS = List.of(TRIPLESTORE_IMPLEMENTATION_PARAMETER);

    private static final String XML_NAME_PATTERN = ".*\\.xml";

    private static final int MAX_PROFILES = 200;

    private NcDatasetReader() {
    }

    /**
     * Returns the parameters accepted by {@link #read(ReadOnlyDataSource, Properties, ReportNode)}.
     */
    public static List<Parameter> getParameters() {
        return PARAMETERS;
    }

    public static NcDataset read(ReadOnlyDataSource dataSource) {
        return read(dataSource, null, ReportNode.NO_OP);
    }

    public static NcDataset read(ReadOnlyDataSource dataSource, ReportNode reportNode) {
        return read(dataSource, null, reportNode);
    }

    public static NcDataset read(ReadOnlyDataSource dataSource, Properties parameters, ReportNode reportNode) {
        return read(dataSource, parameters, new ServiceLoaderCache<>(NcModelPostProcessor.class).getServices(), reportNode);
    }

    /**
     * Reads a ZIP archive of NC profiles. The archive is exposed through a {@link ZipArchiveDataSource}
     * so that archive handling stays consistent with the other PowSyBl loaders.
     */
    public static NcDataset read(Path archive, Properties parameters, ReportNode reportNode) {
        Objects.requireNonNull(archive, "archive");
        return read(new ZipArchiveDataSource(archive), parameters, reportNode);
    }

    /**
     * Reads NC profiles with an explicit list of post-processors, bypassing service discovery.
     */
    public static NcDataset read(ReadOnlyDataSource dataSource, Properties parameters,
                                 List<NcModelPostProcessor> postProcessors, ReportNode reportNode) {
        Objects.requireNonNull(dataSource, "dataSource");
        Objects.requireNonNull(postProcessors, "postProcessors");
        Objects.requireNonNull(reportNode, "reportNode");

        String implementation = Parameter.readString(FORMAT, parameters, TRIPLESTORE_IMPLEMENTATION_PARAMETER,
                ParameterDefaultValueConfig.INSTANCE);

        ReportNode readReportNode = NcModelReports.readingNcProfilesReport(reportNode, dataSource.getBaseName());
        TripleStore tripleStore = TripleStoreFactory.create(implementation);
        boolean successful = false;
        try {
            Map<String, Set<String>> contextsByKeyword = importProfiles(dataSource, tripleStore, readReportNode);
            NcDataset dataset = new NcDatasetTripleStore(tripleStore, contextsByKeyword, postProcessors, readReportNode);
            successful = true;
            return dataset;
        } finally {
            if (!successful) {
                tripleStore.close();
            }
        }
    }

    private static Map<String, Set<String>> importProfiles(ReadOnlyDataSource dataSource, TripleStore tripleStore,
                                                           ReportNode reportNode) {
        Map<String, Set<String>> contextsByKeyword = new HashMap<>();
        for (String profileName : listProfiles(dataSource)) {
            NcKeyword keyword = readKeyword(dataSource, profileName, reportNode);
            String context = loadProfile(dataSource, profileName, tripleStore);
            if (context == null) {
                NcModelReports.ncProfileWithoutData(reportNode, profileName);
                continue;
            }
            contextsByKeyword.computeIfAbsent(keyword.toString(), ignored -> new HashSet<>()).add(context);
            NcModelReports.ncProfileRead(reportNode, profileName, keyword.toString());
        }
        return contextsByKeyword;
    }

    private static List<String> listProfiles(ReadOnlyDataSource dataSource) {
        try {
            List<String> profileNames = new ArrayList<>(dataSource.listNames(XML_NAME_PATTERN));
            Collections.sort(profileNames);
            if (profileNames.size() > MAX_PROFILES) {
                throw new NcException("NC data source contains more than " + MAX_PROFILES + " profiles");
            }
            return profileNames;
        } catch (IOException e) {
            throw new NcException("Cannot list NC profiles of data source " + dataSource.getBaseName(), e);
        }
    }

    private static NcKeyword readKeyword(ReadOnlyDataSource dataSource, String profileName, ReportNode reportNode) {
        try (InputStream inputStream = dataSource.newInputStream(profileName)) {
            Optional<NcKeyword> keyword = NcProfileHeaderReader.readKeyword(inputStream, profileName);
            if (keyword.isEmpty()) {
                NcModelReports.ncProfileWithoutKeyword(reportNode, profileName);
                throw new NcException("Missing NC profile keyword in " + profileName);
            }
            return keyword.get();
        } catch (IOException e) {
            throw new NcException("Cannot read NC profile " + profileName, e);
        }
    }

    /**
     * Loads a profile into its own RDF context and returns the context name assigned by the triple store.
     * The name is discovered from the triple store instead of being reconstructed, because the naming of
     * contexts is an implementation detail of the triple store.
     */
    private static String loadProfile(ReadOnlyDataSource dataSource, String profileName, TripleStore tripleStore) {
        LOGGER.debug("Reading NC profile {}", profileName);
        Set<String> contextsBefore = Set.copyOf(tripleStore.contextNames());
        try (InputStream inputStream = dataSource.newInputStream(profileName)) {
            tripleStore.read(inputStream, NcConstants.RDF_BASE_URL, profileName);
        } catch (IOException e) {
            throw new NcException("Cannot read NC profile " + profileName, e);
        }
        return tripleStore.contextNames().stream()
                .filter(name -> !contextsBefore.contains(name))
                .findFirst()
                .orElse(null);
    }
}
