/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.Report;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.commons.reporter.TypedValue;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.network.*;
import org.slf4j.Logger;

import java.util.Objects;

import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.*;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
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
        this.positionPercent = checkPositionPercent(positionPercent);
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
        this.positionPercent = checkPositionPercent(positionPercent);
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

    private static double checkPositionPercent(double positionPercent) {
        if (Double.isNaN(positionPercent)) {
            throw new PowsyblException("Percent should not be undefined");
        }
        return positionPercent;
    }

    protected boolean failChecks(Network network, boolean throwException, Reporter reporter, Logger logger) {
        Identifiable<?> identifiable = checkIdentifiable(bbsOrBusId, network, throwException, reporter, logger);
        if (identifiable == null) {
            return true;
        }
        voltageLevel = getVoltageLevel(identifiable, throwException, reporter, logger);
        return voltageLevel == null;
    }

    private static Identifiable<?> checkIdentifiable(String id, Network network, boolean throwException, Reporter reporter, Logger logger) {
        Identifiable<?> identifiable = network.getIdentifiable(id);
        if (identifiable == null) {
            logger.error("Identifiable {} not found", id);
            reporter.report(Report.builder()
                    .withKey("notFoundIdentifiable")
                    .withDefaultMessage("Identifiable ${identifiableId} not found")
                    .withValue("identifiableId", id)
                    .withSeverity(TypedValue.ERROR_SEVERITY)
                    .build());
            if (throwException) {
                throw new PowsyblException("Identifiable " + id + " not found");
            }
        }
        return identifiable;
    }

    private static VoltageLevel getVoltageLevel(Identifiable<?> identifiable, boolean throwException, Reporter reporter, Logger logger) {
        if (identifiable instanceof Bus) {
            Bus bus = (Bus) identifiable;
            return bus.getVoltageLevel();
        } else if (identifiable instanceof BusbarSection) {
            BusbarSection bbs = (BusbarSection) identifiable;
            return bbs.getTerminal().getVoltageLevel();
        } else {
            logger.error("Identifiable {} is not a bus or a busbar section", identifiable.getId());
            reporter.report(Report.builder()
                    .withKey("unexpectedIdentifiableType")
                    .withDefaultMessage("Identifiable ${identifiableId} is not a bus or a busbar section")
                    .withValue("identifiableId", identifiable.getId())
                    .withSeverity(TypedValue.ERROR_SEVERITY)
                    .build());
            if (throwException) {
                throw new PowsyblException("Identifiable " + identifiable.getId() + " is not a bus or a busbar section");
            }
            return null;
        }
    }
}
