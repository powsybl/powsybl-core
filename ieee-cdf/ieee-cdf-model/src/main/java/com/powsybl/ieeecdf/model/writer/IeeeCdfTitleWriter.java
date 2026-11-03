/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.model.writer;

import com.powsybl.ieeecdf.model.conversion.LocalDateConversion;
import com.powsybl.ieeecdf.model.conversion.SeasonConversion;
import com.powsybl.ieeecdf.model.elements.IeeeCdfTitle;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public final class IeeeCdfTitleWriter extends AbstractIeeeCdfWriter {

    private IeeeCdfTitleWriter() {
        // private constructor to prevent instantiation
    }

    public static void writeTitle(BufferedWriter writer, IeeeCdfTitle title) throws IOException {
        writer.write(convertToLine(title));
        writer.newLine();
    }

    private static String convertToLine(IeeeCdfTitle title) {
        return FILLER +
            toString(LocalDateConversion.revert(title.getDate()), 2, 9, true) +
            FILLER +
            toString(title.getOriginatorName(), 11, 30, true) +
            FILLER +
            toString(title.getMvaBase(), 32, 37, true) +
            FILLER +
            toString(title.getYear(), 39, 42, true) +
            FILLER +
            toString(SeasonConversion.revert(title.getSeason()), 44) +
            FILLER +
            toString(title.getCaseIdentification(), 46, 73, true);
    }
}
