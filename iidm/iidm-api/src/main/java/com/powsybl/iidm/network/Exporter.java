/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.parameters.Parameter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This is the base class for all IIDM exporters.
 *
 * <p><code>Exporter</code> lookup is based on the <code>ServiceLoader</code>
 * architecture so do not forget to create a
 * <code>META-INF/services/com.powsybl.iidm.network.Exporter</code> file
 * with the fully qualified name of your <code>Exporter</code> implementation.
 *
 * @see java.util.ServiceLoader
 * @see Exporters
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface Exporter {

    /**
     * Get all supported export formats.
     */
    static Collection<String> getFormats(ExportersLoader loader) {
        Objects.requireNonNull(loader);
        return loader.loadExporters().stream().map(Exporter::getFormat).collect(Collectors.toSet());
    }

    static Collection<String> getFormats() {
        return getFormats(new ExportersServiceLoader());
    }

    /**
     * Find an exporter.
     *
     * @param format the export format
     * @return the exporter if one exists for the given format or
     * <code>null</code> otherwise
     */
    static Exporter find(ExportersLoader loader, String format) {
        Objects.requireNonNull(format);
        Objects.requireNonNull(loader);
        for (Exporter e : loader.loadExporters()) {
            if (format.equals(e.getFormat())) {
                return e;
            }
        }
        return null;
    }

    static Exporter find(String format) {
        return find(new ExportersServiceLoader(), format);
    }

    /**
     * Get a unique identifier of the format.
     */
    String getFormat();

    /**
     * Get some information about this exporter.
     */
    String getComment();

    /**
     * Get a description of export parameters
     * @return
     */
    default List<Parameter> getParameters() {
        return Collections.emptyList();
    }

    /**
     * Export a model.
     *
     * @param network the model
     * @param parameters some properties to configure the export
     * @param dataSource data source
     */
    default void export(Network network, Properties parameters, DataSource dataSource) {
        export(network, parameters, dataSource, ReportNode.NO_OP);
    }

    /**
     * Export a model.
     *
     * @param network the model
     * @param parameters some properties to configure the export
     * @param dataSource data source
     * @param reportNode the reportNode used for functional logs
     */
    default void export(Network network, Properties parameters, DataSource dataSource, ReportNode reportNode) {
        export(network, parameters, dataSource);
    }

}
