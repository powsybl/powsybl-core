/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class LegacyTextReader {
    private static final Logger LOG = LoggerFactory.getLogger(LegacyTextReader.class);
    private final BufferedReader reader;
    private boolean endOfFileFound;

    public LegacyTextReader(BufferedReader reader) {
        this.reader = reader;
    }

    public BufferedReader getBufferedReader() {
        return reader;
    }

    public boolean isEndOfFileFound() {
        return endOfFileFound;
    }

    // skip lines until a 0 is found.
    // this method supports null lines to avoid verifying if the Q record has been found
    public void skip(RecordGroupIdentification recordGroup) throws IOException {
        LOG.debug("read and ignore record group {}", recordGroup);
        int number = -1;
        String line;
        do {
            line = reader.readLine();
            if (line != null) {
                try (Scanner scanner = new Scanner(line)) {
                    if (scanner.hasNextInt()) {
                        number = scanner.nextInt();
                    }
                }
            }
        } while (line != null && number != 0);
    }

    public List<String> readRecords() throws IOException {
        List<String> records = new ArrayList<>();
        if (!isEndOfFileFound()) {
            String line = readRecordLine();
            while (!endOfBlock(line)) {
                if (!emptyLine(line)) {
                    records.add(line);
                }
                line = readRecordLine();
            }
        }
        return records;
    }

    public boolean endOfBlock(String line) {
        if (line == null) {
            endOfFileFound = true;
            return true;
        }

        var trimmedLine = line.trim();
        if (trimmedLine.trim().isEmpty()) {
            return false;
        }

        String lineWithoutComment = removeComment(trimmedLine);

        if (lineWithoutComment.equals("Q")) {
            endOfFileFound = true;
            return true;
        }
        return lineWithoutComment.equals("0");
    }

    private String removeComment(String line) {
        int commentIndex = line.indexOf('/');
        return commentIndex > 0 ? line.substring(0, commentIndex).trim() : line;
    }

    private boolean emptyLine(String line) {
        return line.trim().isEmpty();
    }

    // read a raw line
    public String readLine() throws IOException {
        return reader.readLine();
    }

    public String readUntilFindingARecordLineNotEmpty() throws IOException {
        String line = readRecordLine();
        while (emptyLine(line) || isRecordLineDefiningTheAttributeFields(line)) {
            line = readRecordLine();
        }
        return line;
    }

    // Read a line that contains a record
    // Removes comments and normalizes spaces in unquoted areas
    public String readRecordLine() throws IOException {
        String line = reader.readLine();
        if (line == null) {
            return line;
        }
        if (isRecordLineDefiningTheAttributeFields(line)) {
            return ""; // an empty line must be returned
        }
        return line.trim();
    }

    // all the lines beginning with "@!" are record lines defining the attribute fields
    private static boolean isRecordLineDefiningTheAttributeFields(String line) {
        return line.length() >= 2 && line.substring(0, 2).equals("@!");
    }
}
