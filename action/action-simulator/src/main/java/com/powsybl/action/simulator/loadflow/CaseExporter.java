/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
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

    private final boolean exportEachRound;

    public CaseExporter(Path outputCaseFolder, String basename, String outputCaseFormat, CompressionFormat compressionFormat, boolean exportEachRound) {
        this.outputCaseFolder = Objects.requireNonNull(outputCaseFolder);
        this.basename = Objects.requireNonNull(basename);
        this.outputCaseFormat = Objects.requireNonNull(outputCaseFormat);
        this.compressionFormat = compressionFormat;
        this.exportEachRound = exportEachRound;
    }

    @Override
    public void loadFlowDiverged(RunningContext runningContext) {
        exportNetwork(runningContext);
    }

    @Override
    public void loadFlowConverged(RunningContext runningContext, List<LimitViolation> violations) {
        if (exportEachRound) {
            exportNetwork(runningContext);
        }
    }

    private void exportNetwork(RunningContext context) {
        DataSource dataSource = DataSourceUtil.createDataSource(outputCaseFolder, getBasename(context.getContingency(), context.getRound()), compressionFormat, null);
        Exporters.export(outputCaseFormat, context.getNetwork(), new Properties(), dataSource);
    }

    /**
     * Return the basename of the case file based on the initial basename, the contingency Id and the round
     */
    private String getBasename(Contingency contingency, int round) {
        String stateId = (contingency == null) ? "N" : contingency.getId();

        return basename + "-" + stateId + "-R" + round;
    }

    @Override
    public void noMoreViolations(RunningContext runningContext) {
        if (!exportEachRound) {
            exportNetwork(runningContext);
        }
    }

    @Override
    public void violationsAnymoreAndNoRulesMatch(RunningContext runningContext) {
        if (!exportEachRound) {
            exportNetwork(runningContext);
        }
    }

    @Override
    public void maxIterationsReached(RunningContext runningContext) {
        if (!exportEachRound) {
            exportNetwork(runningContext);
        }
    }

}
