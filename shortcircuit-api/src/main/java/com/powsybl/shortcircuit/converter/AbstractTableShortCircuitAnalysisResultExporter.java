/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit.converter;

import com.powsybl.commons.io.table.Column;
import com.powsybl.commons.io.table.TableFormatter;
import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.commons.io.table.TableFormatterFactory;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationHelper;
import com.powsybl.shortcircuit.FaultResult;
import com.powsybl.shortcircuit.FortescueFaultResult;
import com.powsybl.shortcircuit.MagnitudeFaultResult;
import com.powsybl.shortcircuit.ShortCircuitAnalysisResult;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Objects;

/**
 * @author Boubakeur Brahimi
 */
public abstract class AbstractTableShortCircuitAnalysisResultExporter implements ShortCircuitAnalysisResultExporter {

    protected abstract TableFormatterFactory getTableFormatterFactory();

    protected TableFormatterConfig getTableFormatterConfig() {
        return TableFormatterConfig.load();
    }

    @Override
    public void export(ShortCircuitAnalysisResult result, Writer writer, Network network) {
        Objects.requireNonNull(result);
        Objects.requireNonNull(writer);
        TableFormatterFactory tableFormatterFactory = getTableFormatterFactory();
        TableFormatterConfig tableFormatterConfig = getTableFormatterConfig();
        printShortCircuitResults(result, writer, tableFormatterFactory, tableFormatterConfig);
        printLimitViolationResults(result, writer, tableFormatterFactory, tableFormatterConfig, network);
    }

    private static void printShortCircuitResults(ShortCircuitAnalysisResult result, Writer writer,
                                                 TableFormatterFactory formatterFactory, TableFormatterConfig formatterConfig) {
        Objects.requireNonNull(result);
        Objects.requireNonNull(writer);
        Objects.requireNonNull(formatterFactory);
        Objects.requireNonNull(formatterConfig);
        try (TableFormatter formatter = formatterFactory.create(writer, "Short circuit analysis", formatterConfig,
                new Column("ID"), new Column("Three Phase Fault Current"))) {
            for (FaultResult action : result.getFaultResults()) {
                if (action instanceof FortescueFaultResult fortescueFaultResult) {
                    formatter.writeCell(action.getFault().getElementId())
                            .writeCell(fortescueFaultResult.getCurrent().getPositiveMagnitude());
                } else {
                    formatter.writeCell(action.getFault().getElementId())
                            .writeCell(((MagnitudeFaultResult) action).getCurrent());
                }

            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void printLimitViolationResults(ShortCircuitAnalysisResult result, Writer writer,
                                                   TableFormatterFactory formatterFactory, TableFormatterConfig formatterConfig, Network network) {
        Objects.requireNonNull(result);
        Objects.requireNonNull(writer);
        Objects.requireNonNull(formatterFactory);
        Objects.requireNonNull(formatterConfig);
        try (TableFormatter formatter = formatterFactory.create(writer, "Limit violations", formatterConfig,
                new Column("ID"), new Column("Voltage level"), new Column("Country"), new Column("Base voltage"),
                new Column("Limit type"), new Column("Limit"), new Column("Value"))) {

            for (FaultResult faultResult : result.getFaultResults()) {
                for (LimitViolation limitViolation : faultResult.getLimitViolations()) {
                    formatter.writeCell(faultResult.getFault().getId())
                            .writeCell(limitViolation.getSubjectId())
                            .writeCell(LimitViolationHelper.getCountry(limitViolation, network).map(Enum::name).orElse(""))
                            .writeCell(LimitViolationHelper.getNominalVoltage(limitViolation, network))
                            .writeCell(limitViolation.getLimitType().name())
                            .writeCell(limitViolation.getLimit())
                            .writeCell(limitViolation.getValue());
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
