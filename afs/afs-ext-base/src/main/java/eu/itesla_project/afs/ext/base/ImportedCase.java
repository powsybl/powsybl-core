/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.afs.ext.base;

import eu.itesla_project.afs.core.AfsException;
import eu.itesla_project.afs.core.AppFileSystem;
import eu.itesla_project.afs.core.FileIcon;
import eu.itesla_project.afs.storage.AppFileSystemStorage;
import eu.itesla_project.afs.storage.NodeId;
import eu.itesla_project.commons.datasource.ReadOnlyDataSource;
import eu.itesla_project.iidm.import_.Importer;
import eu.itesla_project.iidm.import_.ImportersLoader;
import eu.itesla_project.iidm.network.Network;

import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.Properties;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ImportedCase extends ProjectCase {

    public static final String PSEUDO_CLASS = "importedCase";

    static final String FORMAT = "format";
    static final String DATA_SOURCE = "dataSource";
    static final String PARAMETERS = "parameters";

    public ImportedCase(NodeId id, AppFileSystemStorage storage, NodeId projectId, AppFileSystem fileSystem) {
        super(id, storage, projectId, fileSystem);
    }

    @Override
    public FileIcon getIcon() {
        return CaseIconCache.INSTANCE.get(
                getProject().getFileSystem().getData().getComponentDefaultConfig().newFactoryImpl(ImportersLoader.class),
                getProject().getFileSystem().getData().getComputationManager(),
                getImporter().getFormat());
    }

    public ReadOnlyDataSource getDataSource() {
        return storage.getDataSourceAttribute(id, DATA_SOURCE);
    }

    public Properties getParameters() {
        Properties parameters = new Properties();
        try (StringReader reader = new StringReader(storage.getStringAttribute(id, PARAMETERS))) {
            parameters.load(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return parameters;
    }

    public Importer getImporter() {
        String format = storage.getStringAttribute(id, FORMAT);
        return getProject().getFileSystem().getData().getComponentDefaultConfig().newFactoryImpl(ImportersLoader.class).loadImporters()
                .stream()
                .filter(importer -> importer.getFormat().equals(format))
                .findFirst()
                .orElseThrow(() -> new AfsException("Importer not found for format " + format));
    }

    @Override
    public Network loadNetwork() {
        Importer importer = getImporter();
        ReadOnlyDataSource dataSource = getDataSource();
        Properties parameters = getParameters();
        return importer.import_(dataSource, parameters);
    }
}
