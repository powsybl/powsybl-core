/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ext.base;

import com.powsybl.afs.AfsException;
import com.powsybl.afs.ProjectFile;
import com.powsybl.afs.ProjectFileCreationContext;
import com.powsybl.afs.storage.AppStorageDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.import_.Importer;
import com.powsybl.iidm.import_.ImportersLoader;
import com.powsybl.iidm.network.Network;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;

/**
 *  A type of {@code ProjectFile} which represents a {@link Network} imported to the project,
 *  and provides methods to get the {@code Network} object or query it with a script.
 *
 *  @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ImportedCase extends ProjectFile implements ProjectCase {

    public static final String PSEUDO_CLASS = "importedCase";
    public static final int VERSION = 0;

    static final String FORMAT = "format";
    static final String PARAMETERS = "parameters";

    private final ImportersLoader importersLoader;

    public ImportedCase(ProjectFileCreationContext context, ImportersLoader importersLoader) {
        super(context, VERSION);
        this.importersLoader = Objects.requireNonNull(importersLoader);
    }

    public ReadOnlyDataSource getDataSource() {
        return new AppStorageDataSource(storage, info.getId(), info.getName());
    }

    public Properties getParameters() {
        Properties parameters = new Properties();
        try (Reader reader = new InputStreamReader(storage.readBinaryData(info.getId(), PARAMETERS).orElseThrow(AssertionError::new), StandardCharsets.UTF_8)) {
            parameters.load(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return parameters;
    }

    public Importer getImporter() {
        String format = info.getGenericMetadata().getString(FORMAT);
        return importersLoader.loadImporters()
                .stream()
                .filter(importer -> importer.getFormat().equals(format))
                .findFirst()
                .orElseThrow(() -> new AfsException("Importer not found for format " + format));
    }

    @Override
    public String queryNetwork(ScriptType scriptType, String scriptContent) {
        Objects.requireNonNull(scriptType);
        Objects.requireNonNull(scriptContent);
        return findService(NetworkCacheService.class).queryNetwork(this, scriptType, scriptContent);
    }

    @Override
    public Network getNetwork() {
        return findService(NetworkCacheService.class).getNetwork(this);
    }

    @Override
    public void invalidateNetworkCache() {
        findService(NetworkCacheService.class).invalidateCache(this);
    }

    @Override
    public void delete() {
        super.delete();

        // also clean cache
        invalidateNetworkCache();
    }

    @Override
    public void addListener(ProjectCaseListener l) {
        // nothing to do
    }

    @Override
    public void removeListener(ProjectCaseListener l) {
        // nothing to do
    }
}
