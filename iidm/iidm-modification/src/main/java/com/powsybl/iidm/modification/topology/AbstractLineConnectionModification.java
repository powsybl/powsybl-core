/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.modification.NetworkModificationImpact;
import com.powsybl.iidm.modification.util.ModificationLogs;
import com.powsybl.iidm.network.*;

import java.util.Objects;

import static com.powsybl.iidm.modification.util.ModificationLogs.logOrThrow;
import static com.powsybl.iidm.modification.util.ModificationReports.undefinedPercent;
import static com.powsybl.iidm.modification.util.ModificationReports.unexpectedIdentifiableType;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
abstract class AbstractLineConnectionModification<M extends AbstractLineConnectionModification<M>> extends AbstractNetworkModification {

    protected final String bbsOrBusId;

    protected final Line line;

    protected String line1Id;
    protected String line1Name;
    protected String line2Id;
    protected String line2Name;

    protected double positionPercent;

    protected VoltageLevel voltageLevel;

    protected AbstractLineConnectionModification(double positionPercent, String bbsOrBusId, String line1Id, String line1Name,
                                       String line2Id, String line2Name, Line line) {
        this.positionPercent = positionPercent;
        this.bbsOrBusId = Objects.requireNonNull(bbsOrBusId);
        this.line1Id = Objects.requireNonNull(line1Id);
        this.line1Name = line1Name;
        this.line2Id = Objects.requireNonNull(line2Id);
        this.line2Name = line2Name;
        this.line = Objects.requireNonNull(line);
    }

    public M setLine1Id(String line1Id) {
        this.line1Id = Objects.requireNonNull(line1Id);
        return (M) this;
    }

    public M setLine1Name(String line1Name) {
        this.line1Name = line1Name;
        return (M) this;
    }

    public M setLine2Id(String line2Id) {
        this.line2Id = Objects.requireNonNull(line2Id);
        return (M) this;
    }

    public M setLine2Name(String line2Name) {
        this.line2Name = line2Name;
        return (M) this;
    }

    public M setPositionPercent(double positionPercent) {
        this.positionPercent = positionPercent;
        return (M) this;
    }

    public double getPositionPercent() {
        return positionPercent;
    }

    public String getBbsOrBusId() {
        return bbsOrBusId;
    }

    public Line getLine() {
        return line;
    }

    public String getLine1Id() {
        return line1Id;
    }

    public String getLine1Name() {
        return line1Name;
    }

    public String getLine2Id() {
        return line2Id;
    }

    public String getLine2Name() {
        return line2Name;
    }

    private static boolean checkPositionPercent(double positionPercent, boolean throwException, ReportNode reportNode) {
        if (Double.isNaN(positionPercent)) {
            undefinedPercent(reportNode);
            logOrThrow(throwException, "Percent should not be undefined");
            return false;
        }
        return true;
    }

    protected boolean failChecks(Network network, boolean throwException, ReportNode reportNode) {
        Identifiable<?> identifiable = network.getIdentifiable(bbsOrBusId);
        if (identifiable == null) {
            ModificationLogs.busOrBbsDoesNotExist(bbsOrBusId, reportNode, throwException);
            return true;
        }
        if (!checkPositionPercent(positionPercent, throwException, reportNode)) {
            return true;
        }
        voltageLevel = getVoltageLevel(identifiable, throwException, reportNode);
        return voltageLevel == null;
    }

    @Override
    public NetworkModificationImpact hasImpactOnNetwork(Network network) {
        impact = DEFAULT_IMPACT;
        Identifiable<?> identifiable = network.getIdentifiable(bbsOrBusId);
        if (!checkVoltageLevel(identifiable) || Double.isNaN(positionPercent)) {
            impact = NetworkModificationImpact.CANNOT_BE_APPLIED;
        }
        return impact;
    }

    private static VoltageLevel getVoltageLevel(Identifiable<?> identifiable, boolean throwException, ReportNode reportNode) {
        if (identifiable instanceof Bus bus) {
            return bus.getVoltageLevel();
        } else if (identifiable instanceof BusbarSection bbs) {
            return bbs.getTerminal().getVoltageLevel();
        } else {
            unexpectedIdentifiableType(reportNode, identifiable);
            logOrThrow(throwException, String.format("Unexpected type of identifiable %s: %s", identifiable.getId(), identifiable.getType()));
            return null;
        }
    }
}
