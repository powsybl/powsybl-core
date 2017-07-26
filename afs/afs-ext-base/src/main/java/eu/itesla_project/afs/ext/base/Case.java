/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.afs.ext.base;

import eu.itesla_project.afs.core.*;
import eu.itesla_project.afs.storage.AppFileSystemStorage;
import eu.itesla_project.afs.storage.NodeId;
import eu.itesla_project.commons.datasource.ReadOnlyDataSource;
import eu.itesla_project.iidm.import_.Importer;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class Case extends File {

    public static final String PSEUDO_CLASS = "case";

    static final String FORMAT = "format";
    static final String DATA_SOURCE = "dataSource";

    public Case(NodeId id, AppFileSystemStorage storage, AppFileSystem fileSystem) {
        super(id, storage, fileSystem);
    }

    private String getFormat() {
        return storage.getStringAttribute(id, FORMAT);
    }

    @Override
    public FileIcon getIcon() {
        return CaseIconCache.INSTANCE.get(fileSystem.getData().getImportersLoader(), fileSystem.getData().getComputationManager(), getFormat());
    }

    public ReadOnlyDataSource getDataSource() {
        return storage.getDataSourceAttribute(id, DATA_SOURCE);
    }

    public Importer getImporter() {
        String format = getFormat();
        return fileSystem.getData().getImportersLoader().loadImporters()
                .stream()
                .filter(importer -> importer.getFormat().equals(format))
                .findFirst()
                .orElseThrow(() -> new AfsException("Importer not found for format " + format));
    }

}
