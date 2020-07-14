/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model;

import java.io.BufferedReader;
import java.io.IOException;

import java.util.Objects;
import com.powsybl.psse.model.data.PsseData;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class PsseRawReader {

    public boolean checkCaseIdentification(BufferedReader reader) throws IOException {
        Objects.requireNonNull(reader);

        return new PsseData().checkCase33(reader);
    }

    public PsseRawModel read(BufferedReader reader) throws IOException {
        Objects.requireNonNull(reader);
        PsseContext context = new PsseContext();

        return new PsseData().read33(reader, context);
    }

    public PsseRawModel readx(String jsonFile) throws IOException {
        Objects.requireNonNull(jsonFile);
        PsseContext context = new PsseContext();

        return new PsseData().readx35(jsonFile, context);
    }
}
