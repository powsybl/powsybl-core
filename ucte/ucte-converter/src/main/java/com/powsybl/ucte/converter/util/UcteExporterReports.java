/**
 * Copyright (c) 2026, TenneT (https://www.tennet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.converter.util;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.report.TypedValue;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Message-templated {@link ReportNode} entries reported by {@link com.powsybl.ucte.converter.UcteExporter}.
 *
 * @author Arthur Michaut {@literal <arthur.michaut at artelys.com>}
 */
public final class UcteExporterReports {

    private UcteExporterReports() {
    }

    public static ReportNode networkCreation(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withMessageTemplate("core.ucte.export.networkCreation")
                .add();
    }

    public static ReportNode busesAndSwitches(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withMessageTemplate("core.ucte.export.busesAndSwitches")
                .add();
    }

    public static ReportNode boundaryLines(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withMessageTemplate("core.ucte.export.boundaryLines")
                .add();
    }

    public static ReportNode lines(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withMessageTemplate("core.ucte.export.lines")
                .add();
    }

    public static ReportNode tieLines(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withMessageTemplate("core.ucte.export.tieLines")
                .add();
    }

    public static ReportNode transformers(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withMessageTemplate("core.ucte.export.transformers")
                .add();
    }

    public static void fileWritten(ReportNode reportNode, String fileName) {
        reportNode.newReportNode()
                .withMessageTemplate("core.ucte.export.fileWritten")
                .withUntypedValue("fileName", fileName)
                .add();
    }

    public static void switchCurrentLimitMissing(ReportNode reportNode, String switchId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.ucte.export.switchCurrentLimitMissing")
                .withUntypedValue("switchId", switchId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void nominalPowerMissing(ReportNode reportNode, String transformerId, double nominalPowerNovalue) {
        reportNode.newReportNode()
                .withMessageTemplate("core.ucte.export.nominalPowerMissing")
                .withUntypedValue("transformerId", transformerId)
                // nominal power is exported in a 5 chars column
                .withUntypedValue("nominalPowerNovalue", formatAsExported(nominalPowerNovalue, 5))
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    /**
     * Formats a double value the same way {@link com.powsybl.ucte.network.io.UcteWriter} exports it: as a decimal
     * string truncated to {@code fieldLength} characters.
     */
    private static String formatAsExported(double value, int fieldLength) {
        DecimalFormat numberFormatter = new DecimalFormat();
        numberFormatter.setGroupingUsed(false);
        numberFormatter.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.US));
        numberFormatter.setMinimumIntegerDigits(1);
        numberFormatter.setMinimumFractionDigits(1);
        numberFormatter.setMaximumFractionDigits(fieldLength);
        String formatted = numberFormatter.format(value);
        return formatted.length() > fieldLength ? formatted.substring(0, fieldLength) : formatted;
    }
}
