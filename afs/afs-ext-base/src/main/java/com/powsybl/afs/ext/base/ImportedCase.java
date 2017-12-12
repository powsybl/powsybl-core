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
import com.powsybl.afs.storage.AppStorage;
import com.powsybl.afs.storage.NodeId;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.import_.Importer;
import com.powsybl.iidm.import_.ImportersLoader;
import com.powsybl.iidm.network.Network;

import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.Properties;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ImportedCase extends ProjectFile implements ProjectCase {

    public static final String PSEUDO_CLASS = "importedCase";
    public static final int VERSION = 0;

    static final String FORMAT = "format";
    static final String DATA_SOURCE = "dataSource";
    static final String PARAMETERS = "parameters";

    private final ImportersLoader importersLoader;

    public ImportedCase(ProjectFileCreationContext context, ImportersLoader importersLoader) {
        super(context, VERSION, CaseIconCache.INSTANCE.get(
                importersLoader,
                context.getFileSystem().getData().getComputationManager(),
                getFormat(context.getStorage(), context.getInfo().getId())));
        this.importersLoader = Objects.requireNonNull(importersLoader);
    }

    private static String getFormat(AppStorage storage, NodeId id) {
        return storage.getStringAttribute(id, FORMAT);
    }

    public ReadOnlyDataSource getDataSource() {
        return storage.getDataSourceAttribute(info.getId(), DATA_SOURCE);
    }

    public Properties getParameters() {
        Properties parameters = new Properties();
        try (StringReader reader = new StringReader(storage.getStringAttribute(info.getId(), PARAMETERS))) {
            parameters.load(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return parameters;
    }

    public Importer getImporter() {
        String format = getFormat(storage, info.getId());
        return importersLoader.loadImporters()
                .stream()
                .filter(importer -> importer.getFormat().equals(format))
                .findFirst()
                .orElseThrow(() -> new AfsException("Importer not found for format " + format));
    }

    @Override
    public String queryNetwork(String groovyScript) {
        return fileSystem.findService(NetworkService.class).queryNetwork(this, groovyScript);
    }

    @Override
    public Network getNetwork() {
        return fileSystem.findService(NetworkService.class).getNetwork(this);
    }

    @Override
    public ScriptError getScriptError() {
        return null;
    }

    @Override
    public String getScriptOutput() {
        return "";
    }

    @Override
    public void delete() {
        super.delete();

        // also clean cache
        fileSystem.findService(NetworkService.class).invalidateCache(this);
    }
}
