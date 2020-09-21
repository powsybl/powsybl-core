/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.converter;

import com.powsybl.sensitivity.SensitivityAnalysisResults;

import java.io.Writer;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public interface SensitivityAnalysisResultExporter {

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
     * Export a result of a sensitivity analysis
     *
     * @param result The result of the sensitivity analysis
     * @param writer The writer used for the export
     */
    void export(SensitivityAnalysisResults result, Writer writer);

}
