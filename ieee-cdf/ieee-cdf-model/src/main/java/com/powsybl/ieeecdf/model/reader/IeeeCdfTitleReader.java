/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.model.reader;

import com.powsybl.commons.PowsyblException;
import com.powsybl.ieeecdf.model.IeeeCdfTitle;
import com.powsybl.ieeecdf.model.conversion.LocalDateConversion;
import com.powsybl.ieeecdf.model.conversion.SeasonConversion;
import com.powsybl.ieeecdf.model.schema.IeeeCdfTitleSchema;
import org.jsapar.parse.text.TextParseTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public final class IeeeCdfTitleReader extends AbstractIeeeCdfReader {

    private IeeeCdfTitleReader() {
        // private constructor to prevent instantiation
        super();
    }

    public static List<IeeeCdfTitle> parseTitle(BufferedReader reader) throws IOException {
        String titleLine = reader.readLine();
        if (titleLine == null) {
            return new ArrayList<>();
        }
        TextParseTask task = new TextParseTask(IeeeCdfTitleSchema.build(), new StringReader(titleLine));

        List<IeeeCdfTitle> titleList = new ArrayList<>();
        task.setLineConsumer(line -> {
            IeeeCdfTitle title = new IeeeCdfTitle();
            readString(line, "date", date -> title.setDate(LocalDateConversion.fromString(date)));
            readString(line, "originatorName", title::setOriginatorName);
            readDouble(line, "mvaBase", title::setMvaBase);
            readInteger(line, "year", title::setYear);
            readString(line, "season", season -> title.setSeason(SeasonConversion.fromString(season)));
            readString(line, "caseIdentification", title::setCaseIdentification);
            titleList.add(title);
        });

        // Manage the exceptions
        task.setErrorConsumer(error -> {
            throw new PowsyblException("Failed to parse the file during IeeeCdfBranch part", error);
        });

        // Execute the parsing task
        task.execute();
        return titleList;
    }
}
