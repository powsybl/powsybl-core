/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.local.storage;

import com.powsybl.afs.ext.base.Case;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.iidm.import_.Importer;
import com.powsybl.iidm.import_.Importers;

import java.nio.file.Path;
import java.util.*;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LocalCase implements LocalFile {

    private final Path file;

    private final Importer importer;

    public LocalCase(Path file, Importer importer) {
        this.file = Objects.requireNonNull(file);
        this.importer = Objects.requireNonNull(importer);
    }

    @Override
    public String getName() {
        return file.getFileName().toString();
    }

    @Override
    public Path getParentPath() {
        return file.getParent();
    }

    @Override
    public String getPseudoClass() {
        return Case.PSEUDO_CLASS;
    }

    @Override
    public String getStringAttribute(String name) {
        switch (name) {
            case "format":
                return importer.getFormat();

            case "description":
                return importer.getComment();

            default:
                throw new AssertionError(name);
        }
    }

    @Override
    public OptionalInt getIntAttribute(String name) {
        throw new AssertionError(name);
    }

    @Override
    public OptionalDouble getDoubleAttribute(String name) {
        throw new AssertionError();
    }

    @Override
    public Optional<Boolean> getBooleanAttribute(String name) {
        throw new AssertionError();
    }

    @Override
    public DataSource getDataSourceAttribute(String name) {
        switch (name) {
            case "dataSource":
                return Importers.createDataSource(file);

            default:
                throw new AssertionError();
        }
    }
}
