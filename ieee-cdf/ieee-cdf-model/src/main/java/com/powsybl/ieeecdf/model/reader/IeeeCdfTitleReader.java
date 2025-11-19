/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.model.reader;

import com.powsybl.ieeecdf.model.conversion.LocalDateConversion;
import com.powsybl.ieeecdf.model.conversion.SeasonConversion;
import com.powsybl.ieeecdf.model.elements.IeeeCdfTitle;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public final class IeeeCdfTitleReader extends AbstractIeeeCdfReader {

    private IeeeCdfTitleReader() {
        // private constructor to prevent instantiation
        super();
    }

    public static IeeeCdfTitle parseTitle(BufferedReader reader) throws IOException {
        String titleLine = reader.readLine();
        return titleLine == null ? null : parseTitle(titleLine);
    }

    private static IeeeCdfTitle parseTitle(String line) {
        IeeeCdfTitle title = new IeeeCdfTitle();
        readString(line, 2, 9, date -> title.setDate(LocalDateConversion.fromString(date)));
        readString(line, 11, 30, title::setOriginatorName);
        readDouble(line, 32, 37, title::setMvaBase);
        readInteger(line, 39, 42, title::setYear);
        readString(line, 44, season -> title.setSeason(SeasonConversion.fromString(season)));
        readString(line, 46, 73, title::setCaseIdentification);
        return title;
    }
}
