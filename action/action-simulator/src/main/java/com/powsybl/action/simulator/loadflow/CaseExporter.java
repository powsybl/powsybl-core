/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.simulator.loadflow;

import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.export.Exporters;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.LimitViolation;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 * Allows to export to the "outputCaseFolder" folder in the "outputCaseFormat" format the case
 * for action-simulator. The call of this observer is optional.
 */
public class CaseExporter extends DefaultLoadFlowActionSimulatorObserver {

    private final Path outputCaseFolder;
    private final String outputCaseFormat;

    public CaseExporter(Path outputCaseFolder, String outputCaseFormat) {
        this.outputCaseFolder = Objects.requireNonNull(outputCaseFolder);
        this.outputCaseFormat = Objects.requireNonNull(outputCaseFormat);
    }

    @Override
    public void loadFlowDiverged(Contingency contingency, Network network, int round) {
        exportNetwork(contingency, network, round);
    }

    @Override
    public void loadFlowConverged(Contingency contingency, List<LimitViolation> violations, Network network, int round) {
        exportNetwork(contingency, network, round);
    }

    private void exportNetwork(Contingency contingency, Network network, int round) {
        if (null != contingency && StringUtils.isNotEmpty(contingency.getId())) {
            String contingencyIdAndRound = contingency.getId().replace(' ', '_').replace('.', '_') + String.valueOf("_Round_" + round);
            Exporters.export(outputCaseFormat, network, new Properties(), outputCaseFolder.resolve(contingencyIdAndRound + "_" + network.getId() + "_" + "." + outputCaseFormat + ".gz"));
        } else {
            String nSituationAndRound = "N_situation" + String.valueOf("_Round_" + round);
            Exporters.export(outputCaseFormat, network, new Properties(), outputCaseFolder.resolve(nSituationAndRound + "_" + network.getId() + "_" + "." + outputCaseFormat + ".gz"));
        }
    }
}
