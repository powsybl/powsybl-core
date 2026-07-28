/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.nc;

import com.powsybl.triplestore.api.PropertyBags;
import com.powsybl.triplestore.api.QueryCatalog;
import com.powsybl.triplestore.api.TripleStore;
import com.powsybl.triplestore.api.TripleStoreFactory;
import org.apache.commons.lang3.SystemUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.powsybl.nc.NcConverter.LOGGER;

/**
 * @author Jean-Pierre Arnould {@literal <jean-pierre.arnould at rte-france.com>}
 * @author Thomas Bouquet {@literal <thomas.bouquet at rte-france.com>}
 */
public class QueryManager {
    private static final String TRIPLESTORE_IMPLEMENTATION = "rdf4j";
    private static final String SPARQL_GRAPH = "nc-2.2.sparql";
    private static final String NAMESPACE = "http://entsoe.eu";
    private static final Pattern KEYWORD_PATTERN = Pattern.compile("<dcat:keyword>(?<keyword>[A-Z]{2,3})</dcat:keyword>");
    private static final int MAXIMUM_ENTRIES = 200;
    private static final int MAXIMUM_ENTRY_SIZE = 1_000_000_000;

    private final TripleStore tripleStore;
    private final QueryCatalog queryCatalog;
    private final Map<NcProfile, Set<String>> ncProfileContexts; // gather NC profiles per type to reduce the number of files queried
    private final Set<String> otherContexts;

    public QueryManager() {
        this.tripleStore = TripleStoreFactory.create(TRIPLESTORE_IMPLEMENTATION);
        this.queryCatalog = new QueryCatalog(SPARQL_GRAPH);
        this.ncProfileContexts = new HashMap<>();
        this.otherContexts = new HashSet<>();
    }

    // TODO: add report nodes

    /**
     * Populate the triplestore with the content of NC profiles stored in a ZIP archive.
     *
     * @param path : path of the zip archive that contains the NC profiles
     */
    public void read(String path) {
        try {
            read(new ZipInputStream(new FileInputStream(path)));
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while opening the archive: " + e.getMessage(), e);
        }
    }

    /**
     * Populate the triplestore with the content of NC profiles stored in a ZIP archive.
     *
     * @param zipInputStream : {@link ZipInputStream} of the zip archive that contains the NC profiles
     */
    public void read(ZipInputStream zipInputStream) {
        ZipEntry zipEntry;
        try (zipInputStream) {
            // use a maximal number of entries and entry size to avoid DDOS attack with malicious zip file
            int entriesCount = 0;
            while ((zipEntry = zipInputStream.getNextEntry()) != null && entriesCount < MAXIMUM_ENTRIES) { // NOSONAR
                entriesCount++;
                if (!zipEntry.isDirectory()) {
                    importZipEntry(zipEntry, zipInputStream);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while reading the archive: " + e.getMessage());
        }
    }

    private void importZipEntry(ZipEntry zipEntry, ZipInputStream zipInputStream) throws IOException {
        LOGGER.info("Processing entry {}.", zipEntry.getName());
        Optional<File> temporaryFile = createTemporaryFile();
        if (temporaryFile.isPresent()) {
            int currentSizeEntry = 0;
            InputStream in = new BufferedInputStream(zipInputStream);
            try (OutputStream out = new BufferedOutputStream(new FileOutputStream(temporaryFile.get()))) {
                int nBytes = -1;
                byte[] buffer = new byte[2048];

                while ((nBytes = in.read(buffer)) > 0 && currentSizeEntry < MAXIMUM_ENTRY_SIZE) {
                    out.write(buffer, 0, nBytes);
                    currentSizeEntry += nBytes;
                    String stringBuffer = new String(buffer, StandardCharsets.UTF_8);
                    Matcher matcher = KEYWORD_PATTERN.matcher(stringBuffer);
                    if (matcher.find()) {
                        String contextName = "contexts:" + zipEntry.getName();
                        Optional<NcProfile> profile = NcProfile.fromKeyword(matcher.group("keyword"));
                        if (profile.isPresent()) {
                            ncProfileContexts.computeIfAbsent(profile.get(), k -> new HashSet<>()).add(contextName);
                        } else {
                            otherContexts.add(contextName);
                        }
                    }
                }
            }

            try (FileInputStream fileInputStream = new FileInputStream(temporaryFile.get())) {
                tripleStore.read(fileInputStream, NAMESPACE, zipEntry.getName());
            }
            postProcessTemporaryFile(temporaryFile.get());
        } else {
            LOGGER.warn("Could not create a temporary file to process entry {}.", zipEntry.getName());
        }
    }

    private static Optional<File> createTemporaryFile() throws IOException {
        if (SystemUtils.IS_OS_UNIX) {
            FileAttribute<Set<PosixFilePermission>> attribute = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------"));
            return Optional.of(Files.createTempFile("powsybl-nc", ".tmp", attribute).toFile()); // Compliant
        } else {
            File temporaryFile = Files.createTempFile("powsybl-nc", ".tmp").toFile();  // NOSONAR
            // sonar wants us to set readable and writable right after creating file,
            // but it counts it as a bug if you don't use the return variable
            // and doesn't see the calls if you use the return variable...
            boolean fileIsOkay = temporaryFile.setReadable(true, true) &&
                temporaryFile.setWritable(true, true) &&
                temporaryFile.setExecutable(true, true);
            return fileIsOkay ? Optional.of(temporaryFile) : Optional.empty();
        }
    }

    private static void postProcessTemporaryFile(File temporaryFile) {
        try {
            Files.delete(temporaryFile.toPath());
        } catch (IOException ioException) {
            LOGGER.info("Temporary file {} cannot be deleted.", temporaryFile.getName());
            temporaryFile.deleteOnExit();
        }
    }

    /**
     * Run a SPARQL query on the triplestore.
     *
     * @param queryName the name of the query in the catalog
     * @return the result of the query
     */
    public PropertyBags query(String queryName) {
        return query(queryName, null);
    }

    /**
     * Run a SPARQL query on the triplestore, restricted to a specific NC profile type.
     *
     * @param queryName       the name of the query in the catalog
     * @param profilesToQuery the NC profile type to query
     * @return the result of the query
     */
    public PropertyBags query(String queryName, NcProfile profilesToQuery) {
        String query = queryCatalog.get(queryName);
        if (query == null) {
            LOGGER.warn("Query '{}' not found in catalog.", queryName);
            return new PropertyBags();
        }

        Set<String> contextsToQuery = new HashSet<>();
        if (profilesToQuery != null) {
            contextsToQuery.addAll(ncProfileContexts.getOrDefault(profilesToQuery, Set.of()));
        }
        if (contextsToQuery.isEmpty()) {
            contextsToQuery.addAll(tripleStore.contextNames());
        }

        PropertyBags multiContextsPropertyBags = new PropertyBags();
        for (String context : contextsToQuery) {
            String contextQuery = String.format(query, context);
            multiContextsPropertyBags.addAll(tripleStore.query(contextQuery));
        }
        return multiContextsPropertyBags;
    }
}
