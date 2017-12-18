/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.simulator.loadflow;

import com.powsybl.commons.datasource.CompressionFormat;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.DataSourceUtil;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.export.Exporters;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.LimitViolation;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Properties;


/**
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 * Allows to export to the "outputCaseFolder" folder in the "outputCaseFormat" format the case
 * for action-simulator. The call of this observer is optional.
 */
public class CaseExporter extends DefaultLoadFlowActionSimulatorObserver {

    private final Path outputCaseFolder;

    private final String basename;

    private final String outputCaseFormat;

    private final CompressionFormat compressionFormat;

    public CaseExporter(Path outputCaseFolder, String basename, String outputCaseFormat, CompressionFormat compressionFormat) {
        this.outputCaseFolder = Objects.requireNonNull(outputCaseFolder);
        this.basename = Objects.requireNonNull(basename);
        this.outputCaseFormat = Objects.requireNonNull(outputCaseFormat);
        this.compressionFormat = compressionFormat;
    }

    @Override
    public void loadFlowDiverged(RunningContext runningContext) {
        exportNetwork(runningContext.getContingency(), runningContext.getNetwork(), runningContext.getRound());
    }

    @Override
    public void loadFlowConverged(RunningContext runningContext, List<LimitViolation> violations) {
        exportNetwork(runningContext.getContingency(), runningContext.getNetwork(), runningContext.getRound());
    }

    private void exportNetwork(Contingency contingency, Network network, int round) {
        DataSource dataSource = DataSourceUtil.createDataSource(outputCaseFolder, getBasename(contingency, round), compressionFormat, null);
        Exporters.export(outputCaseFormat, network, new Properties(), dataSource);
    }

    /**
     * Return the basename of the case file based on the initial basename, the contingency Id and the round
     */
    private final String getBasename(Contingency contingency, int round) {
        return new StringBuilder()
            .append(basename)
            .append("-")
            .append((contingency == null) ? "N" : contingency.getId())
            .append("-R")
            .append(round)
            .toString();
    }
}
