/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.comparator;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.commons.io.table.Column;
import com.powsybl.commons.io.table.CsvTableFormatterFactory;
import com.powsybl.commons.io.table.TableFormatter;
import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.iidm.network.Branch;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationType;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class SecurityAnalysisResultComparisonWriter implements AutoCloseable {

    private static final String RESULT = "Result";
    private static final String EQUIVALENT = "equivalent";
    private static final String DIFFERENT = "different";
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityAnalysisResultComparisonWriter.class);

    private TableFormatter formatter;
    private String contingency;

    public SecurityAnalysisResultComparisonWriter(Writer writer) {
        Objects.requireNonNull(writer);
        formatter = new CsvTableFormatterFactory().create(writer, "Security Analysis Results Comparison", TableFormatterConfig.load(), getColumns());
    }

    private Column[] getColumns() {
        return new Column[] {
            new Column("Contingency"),
            new Column("Status" + RESULT + "1"),
            new Column("Status" + RESULT + "2"),
            new Column("Equipment"),
            new Column("End"),
            new Column("ViolationType"),
            new Column("ViolationName" + RESULT + "1"),
            new Column("Value" + RESULT + "1"),
            new Column("Limit" + RESULT + "1"),
            new Column("ViolationName" + RESULT + "2"),
            new Column("Value" + RESULT + "2"),
            new Column("Limit" + RESULT + "2"),
            new Column("Actions" + RESULT + "1"),
            new Column("Actions" + RESULT + "2"),
            new Column("Comparison")
        };
    }

    public void setContingency(String contingency) {
        this.contingency = contingency;
    }

    private <T> void checkInput(T value1, T value2) {
        if (value1 == null && value2 == null) {
            throw new IllegalArgumentException("At least one of the input values must be not null");
        }
    }

    public SecurityAnalysisResultComparisonWriter write(Boolean computationOk1, Boolean computationOk2, boolean equivalent) {
        checkInput(computationOk1, computationOk2);
        try {
            formatter = contingency == null ? formatter.writeEmptyCell() : formatter.writeCell(contingency);
            formatter = computationOk1 == null ? formatter.writeEmptyCells(1) : formatter.writeCell(getStatus(computationOk1));
            formatter = computationOk2 == null ? formatter.writeEmptyCells(1) : formatter.writeCell(getStatus(computationOk2));
            formatter.writeEmptyCells(11);
            formatter.writeCell(getComparison(equivalent));
        } catch (IOException e) {
            LOGGER.error("Error writing security analysis results computation status comparison: {}", e.getMessage());
        }
        return this;
    }

    private String getStatus(boolean computationOk) {
        return computationOk ? "converge" : "diverge";
    }

    public SecurityAnalysisResultComparisonWriter write(LimitViolation violation1, LimitViolation violation2, boolean equivalent) {
        checkInput(violation1, violation2);
        try {
            formatter = contingency == null ? formatter.writeEmptyCell() : formatter.writeCell(contingency);
            formatter.writeEmptyCells(2);
            formatter.writeCell(getEquipment(violation1, violation2));
            formatter = getEnd(violation1, violation2) == null ? formatter.writeEmptyCell() : formatter.writeCell(getEnd(violation1, violation2).name());
            formatter.writeCell(getViolationType(violation1, violation2).name());
            writeViolation(violation1);
            writeViolation(violation2);
            formatter.writeEmptyCells(2);
            formatter.writeCell(getComparison(equivalent));
        } catch (IOException e) {
            LOGGER.error("Error writing security analysis results violations comparison: {}", e.getMessage());
        }
        return this;
    }

    private String getEquipment(LimitViolation violation1, LimitViolation violation2) {
        return violation1 == null ? violation2.getSubjectId() : violation1.getSubjectId();
    }

    private Branch.Side getEnd(LimitViolation violation1, LimitViolation violation2) {
        return violation1 == null ? violation2.getSide() : violation1.getSide();
    }

    private LimitViolationType getViolationType(LimitViolation violation1, LimitViolation violation2) {
        return violation1 == null ? violation2.getLimitType() : violation1.getLimitType();
    }

    private void writeViolation(LimitViolation violation) throws IOException {
        if (violation == null) {
            formatter.writeEmptyCells(3);
        } else {
            formatter = violation.getLimitName() == null ? formatter.writeEmptyCell() : formatter.writeCell(violation.getLimitName());
            formatter.writeCell(violation.getValue())
                     .writeCell(getViolationLimit(violation));
        }
    }

    private double getViolationLimit(LimitViolation violation) {
        return violation.getLimit() * violation.getLimitReduction();
    }

    public SecurityAnalysisResultComparisonWriter write(List<String> actions1, List<String> actions2, boolean equivalent) {
        checkInput(actions1, actions2);
        if (noActions(actions1, actions2)) {
            return this; // skip actions line in cvs file if there are no actions
        }
        try {
            formatter = contingency == null ? formatter.writeEmptyCell() : formatter.writeCell(contingency);
            formatter.writeEmptyCells(11);
            formatter = actions1 == null ? formatter.writeEmptyCell() : formatter.writeCell(actions1.toString());
            formatter = actions2 == null ? formatter.writeEmptyCell() : formatter.writeCell(actions2.toString());
            formatter.writeCell(getComparison(equivalent));
        } catch (IOException e) {
            LOGGER.error("Error writing security analysis results actions comparison: {}", e.getMessage());
        }
        return this;
    }

    private boolean noActions(List<String> actions1, List<String> actions2) {
        return (actions1 == null || actions1.isEmpty())
               && (actions2 == null || actions2.isEmpty());
    }

    private String getComparison(boolean equivalent) {
        return equivalent ? EQUIVALENT : DIFFERENT;
    }

    @Override
    public void close() throws IOException {
        formatter.close();
    }

}
