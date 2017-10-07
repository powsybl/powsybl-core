/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ext.base;

import com.powsybl.afs.AfsException;
import com.powsybl.afs.AppFileSystem;
import com.powsybl.afs.File;
import com.powsybl.afs.storage.AppFileSystemStorage;
import com.powsybl.afs.storage.NodeId;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.import_.Importer;
import com.powsybl.iidm.import_.ImportersLoader;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class Case extends File {

    public static final String PSEUDO_CLASS = "case";

    static final String FORMAT = "format";
    static final String DATA_SOURCE = "dataSource";

    private final ImportersLoader importersLoader;

    public Case(NodeId id, AppFileSystemStorage storage, AppFileSystem fileSystem, ImportersLoader importersLoader) {
        super(id, storage, fileSystem, CaseIconCache.INSTANCE.get(importersLoader,
                                                                  fileSystem.getData().getComputationManager(),
                                                                  getFormat(storage, id)));
        this.importersLoader = Objects.requireNonNull(importersLoader);
    }

    private static String getFormat(AppFileSystemStorage storage, NodeId id) {
        return storage.getStringAttribute(id, FORMAT);
    }

    private String getFormat() {
        return getFormat(storage, id);
    }

    public ReadOnlyDataSource getDataSource() {
        return storage.getDataSourceAttribute(id, DATA_SOURCE);
    }

    public Importer getImporter() {
        String format = getFormat();
        return importersLoader.loadImporters()
                .stream()
                .filter(importer -> importer.getFormat().equals(format))
                .findFirst()
                .orElseThrow(() -> new AfsException("Importer not found for format " + format));
    }

}
