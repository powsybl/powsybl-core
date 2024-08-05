/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.modification.AbstractSingleNetworkModification;
import com.powsybl.iidm.modification.util.ModificationLogs;
import com.powsybl.iidm.network.*;
import org.slf4j.Logger;

import java.util.Objects;

import static com.powsybl.iidm.modification.util.ModificationReports.undefinedPercent;
import static com.powsybl.iidm.modification.util.ModificationReports.unexpectedIdentifiableType;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
abstract class AbstractLineConnectionModification<M extends AbstractLineConnectionModification<M>> extends AbstractSingleNetworkModification {
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

    private static boolean checkPositionPercent(double positionPercent, boolean throwException, ReportNode reportNode, Logger logger) {
        if (Double.isNaN(positionPercent)) {
            logger.error("Percent should not be undefined");
            undefinedPercent(reportNode);
            if (throwException) {
                throw new PowsyblException("Percent should not be undefined");
            }
            return false;
        }
        return true;
    }

    protected boolean failChecks(Network network, boolean throwException, ReportNode reportNode, Logger logger) {
        Identifiable<?> identifiable = network.getIdentifiable(bbsOrBusId);
        if (identifiable == null) {
            ModificationLogs.busOrBbsDoesNotExist(bbsOrBusId, reportNode, throwException);
            return true;
        }
        if (!checkPositionPercent(positionPercent, throwException, reportNode, logger)) {
            return true;
        }
        voltageLevel = getVoltageLevel(identifiable, throwException, reportNode, logger);
        return voltageLevel == null;
    }

    private static VoltageLevel getVoltageLevel(Identifiable<?> identifiable, boolean throwException, ReportNode reportNode, Logger logger) {
        if (identifiable instanceof Bus bus) {
            return bus.getVoltageLevel();
        } else if (identifiable instanceof BusbarSection bbs) {
            return bbs.getTerminal().getVoltageLevel();
        } else {
            logger.error("Unexpected type of identifiable {}: {}", identifiable.getId(), identifiable.getType());
            unexpectedIdentifiableType(reportNode, identifiable);
            if (throwException) {
                throw new PowsyblException("Unexpected type of identifiable " + identifiable.getId() + ": " + identifiable.getType());
            }
            return null;
        }
    }
}
