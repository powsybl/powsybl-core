/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.afs.local.storage;

import eu.itesla_project.afs.ext.base.Case;
import eu.itesla_project.commons.datasource.DataSource;
import eu.itesla_project.iidm.import_.Importer;
import eu.itesla_project.iidm.import_.Importers;

import java.nio.file.Path;
import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LocalCaseStorage implements LocalFileStorage {

    private final Path file;

    private final Importer importer;

    public LocalCaseStorage(Path file, Importer importer) {
        this.file = Objects.requireNonNull(file);
        this.importer = Objects.requireNonNull(importer);
    }

    @Override
    public String getName() {
        return file.getFileName().toString();
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
    public DataSource getDataSourceAttribute(String name) {
        switch (name) {
            case "dataSource":
                return Importers.createDataSource(file);

            default:
                throw new AssertionError();
        }
    }
}
