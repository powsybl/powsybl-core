/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ext.base;

import com.powsybl.afs.AfsException;
import com.powsybl.afs.File;
import com.powsybl.afs.FileCreationContext;
import com.powsybl.afs.storage.AppStorageDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.import_.Importer;
import com.powsybl.iidm.import_.ImportersLoader;

import java.util.Objects;

/**
 * A type of {@code File} which represents a {@link com.powsybl.iidm.network.Network}.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class Case extends File {

    public static final String PSEUDO_CLASS = "case";
    public static final int VERSION = 0;

    public static final String FORMAT = "format";

    private final ImportersLoader importersLoader;

    public Case(FileCreationContext context, ImportersLoader importersLoader) {
        super(context, VERSION);
        this.importersLoader = Objects.requireNonNull(importersLoader);
    }

    private String getFormat() {
        return info.getGenericMetadata().getString(FORMAT);
    }

    public ReadOnlyDataSource getDataSource() {
        return new AppStorageDataSource(storage, info.getId(), info.getName());
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
