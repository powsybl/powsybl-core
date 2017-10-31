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

/**
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 */
public class CaseExporter extends DefaultLoadFlowActionSimulatorObserver {

    private final Path outputCaseFolder;
    private final String outputCaseFormat;

    public CaseExporter(Path outputCaseFolder, String outputCaseFormat) {
        this.outputCaseFolder = Objects.requireNonNull(outputCaseFolder);
        this.outputCaseFormat = Objects.requireNonNull(outputCaseFormat);
    }

    @Override
    public void loadFlowDiverged(Network network, Contingency contingency) {
        System.out.println(" loadFlowDiverged ");
        exportNetwork(network);
    }

    @Override
    public void loadFlowConverged(Network network, Contingency contingency, List<LimitViolation> violations) {
        System.out.println(" loadFlowConverged ");
        exportNetwork(network);
    }

    private void exportNetwork(Network network) {
        printSystemOut("exportNetwork", network, outputCaseFolder.resolve(network.getId() + "." + network.getCaseDate() + "." + outputCaseFormat + ".gz"), outputCaseFormat);
        Exporters.export(outputCaseFormat, network, new Properties(), outputCaseFolder.resolve(network.getId() + "." + network.getCaseDate().toString() + "." + outputCaseFormat + ".gz"));
    }

    void printSystemOut(String where, Network network, Path outputCaseFolder, String outputCaseFormat) {
        System.out.println(" outputCaseFolder : " + outputCaseFolder.toString());
        System.out.println(" outputCaseFormat : " + outputCaseFormat);

        System.out.println(" where : " + where);
        System.out.println(" Network : " + " network.getId() " + network.getId());
        System.out.println("           " + " network.getCaseDate() " + network.getCaseDate());
    }
}
