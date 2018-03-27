/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.converter;

import com.powsybl.security.SecurityAnalysisResult;

import java.io.Writer;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public interface SecurityAnalysisResultExporter {

    /**
     * Get the format of this exporter
     *
     * @return the format name of this exporter
     */
    String getFormat();

    /**
     * Get a brief description of this exporter
     *
     * @return a brief description of this exporter
     */
    String getComment();

    /**
     * Export a result of a security analysis
     *
     * @param result The result of the security analysis
     * @param writer The writer used for the export
     */
    void export(SecurityAnalysisResult result, Writer writer);

}
