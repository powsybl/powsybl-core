/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.import_;

import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.parameters.Parameter;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * This is the base class for all IIDM importers.
 *
 * <p><code>Importer</code> lookup is based on the <code>ServiceLoader</code>
 * architecture so do not forget to create a
 * <code>META-INF/services/com.powsybl.iidm.importData.Importer</code> file
 * with the fully qualified name of your <code>Importer</code> implementation.
 *
 * @see java.util.ServiceLoader
 * @see Importers
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface Importer {

    /**
     * Get a unique identifier of the format.
     */
    String getFormat();

    /**
     * Get a description of import parameters
     * @return
     */
    default List<Parameter> getParameters() {
        return Collections.emptyList();
    }

    /**
     * Get some information about this importer.
     */
    String getComment();

    /**
     * Check if the data source is importable
     * @param dataSource the data source
     * @return true if the data source is importable, false otherwise
     */
    boolean exists(ReadOnlyDataSource dataSource);

    /**
     * Create a model.
     *
     * @param dataSource data source
     * @param parameters some properties to configure the import
     * @return the model
     */
    Network importData(ReadOnlyDataSource dataSource, Properties parameters);

    /**
     * Copy data from one data source to another.
     * @param fromDataSource from data source
     * @param toDataSource destination data source
     */
    default void copy(ReadOnlyDataSource fromDataSource, DataSource toDataSource) {
        throw new UnsupportedOperationException("Copy not implemented");
    }
}
