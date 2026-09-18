/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nc.converter;

import com.powsybl.action.Action;
import com.powsybl.action.ActionList;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.list.DefaultContingencyList;
import com.powsybl.contingency.strategy.OperatorStrategy;
import com.powsybl.contingency.strategy.OperatorStrategyList;
import com.powsybl.iidm.network.Network;
import com.powsybl.nc.model.NcModel;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Converts the neutral NC model into inputs understood by PowSyBl security analysis.
 *
 * @author Thomas Bouquet {@literal <thomas.bouquet at rte-france.com>}
 * @author Roman Vykuka {@literal <vykuka at gmail.com>}
 */
public final class NcConverter {
    private NcConverter() {
    }

    public static NcConversionResult convert(NcModel model, Network network) {
        return convert(model, network, ReportNode.NO_OP);
    }

    public static NcConversionResult convert(NcModel model, Network network, ReportNode reportNode) {
        Objects.requireNonNull(model);
        Objects.requireNonNull(network);
        Objects.requireNonNull(reportNode);

        ReportNode conversionReportNode = NcConversionReports.convertingNcModelReport(reportNode);

        Map<String, Contingency> contingencies = new ContingencyConverter(model, network,
            NcConversionReports.convertingContingenciesReport(conversionReportNode)).convert();
        AssessedElementConverter.Result assessedElements = new AssessedElementConverter(model, network,
            contingencies.keySet(), NcConversionReports.convertingAssessedElementsReport(conversionReportNode)).convert();
        Map<String, Action> actions = new ActionConverter(model, network,
            NcConversionReports.convertingRemedialActionsReport(conversionReportNode)).convert();

        List<OperatorStrategy> strategies = new OperatorStrategyConverter(model, network, contingencies.keySet(),
            assessedElements.contexts(), actions,
            NcConversionReports.convertingOperatorStrategiesReport(conversionReportNode)).convert();

        return new NcConversionResult(
            new DefaultContingencyList("NC Contingencies", List.copyOf(contingencies.values())),
            assessedElements.monitors(),
            new ActionList(List.copyOf(actions.values())),
            new OperatorStrategyList(strategies));
    }

}
