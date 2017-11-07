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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;

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
        exportNetwork(network, "Diverged");
    }

    @Override
    public void loadFlowConverged(Network network, Contingency contingency, List<LimitViolation> violations) {
        if (StringUtils.isNotEmpty(contingency.getId())) {
            exportNetwork(network, contingency.getId());
        } else {
            exportNetwork(network, "Converged");
        }
    }

    private void exportNetwork(Network network, String aString) {
        Exporters.export(outputCaseFormat, network, new Properties(), outputCaseFolder.resolve(aString  + "_" + network.getId() + "_" + dateForName() + "." + outputCaseFormat + ".gz"));
    }

    private String dateForName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(new Date());
    }
}
