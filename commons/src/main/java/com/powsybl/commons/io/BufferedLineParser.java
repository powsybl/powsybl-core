/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class BufferedLineParser {

    private final BufferedReader reader;

    private int bufferSize;

    public BufferedLineParser(BufferedReader reader, int bufferSize) {
        this.reader = reader;
        this.bufferSize = bufferSize;
    }

    public void parse(Consumer<List<String>> consumer) throws IOException {
        List<String> lineBuffer = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            lineBuffer.add(0, line);
            if (lineBuffer.size() > bufferSize) {
                lineBuffer.remove(lineBuffer.size() - 1);
            }
            consumer.accept(lineBuffer);
        }
    }

}
