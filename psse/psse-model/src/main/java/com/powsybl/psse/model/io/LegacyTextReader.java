/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.io;

import com.powsybl.psse.model.PsseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;

import static com.powsybl.psse.model.io.FileFormat.LEGACY_TEXT;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class LegacyTextReader {
    private static final Logger LOG = LoggerFactory.getLogger(LegacyTextReader.class);
    private final BufferedReader reader;
    private boolean qRecordFound;

    public LegacyTextReader(BufferedReader reader) {
        this.reader = reader;
    }

    public BufferedReader getBufferedReader() {
        return reader;
    }

    public boolean isQRecordFound() {
        return qRecordFound;
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
        if (!isQRecordFound()) {
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
        if (line.trim().equals("Q")) {
            qRecordFound = true;
            return true;
        }
        return line.trim().equals("0");
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
        while (emptyLine(line)) {
            line = readRecordLine();
        }
        return line;
    }

    // Read a line that contains a record
    // Removes comments and normalizes spaces in unquoted areas
    public String readRecordLine() throws IOException {
        String line = reader.readLine();
        if (line == null) {
            throw new PsseException("PSSE. Unexpected end of file");
        }
        if (isRecordLineDefiningTheAttributeFields(line)) {
            return ""; // an empty line must be returned
        }
        StringBuilder newLine = new StringBuilder();
        Matcher m = FileFormat.LEGACY_TEXT_QUOTED_OR_WHITESPACE.matcher(removeComment(line));
        while (m.find()) {
            // If current group is quoted, keep it as it is
            if (m.group().indexOf(LEGACY_TEXT.getQuote()) >= 0) {
                m.appendReplacement(newLine, replaceSpecialCharacters(m.group()));
            } else {
                // current group is whitespace, keep a single whitespace
                m.appendReplacement(newLine, " ");
            }
        }
        m.appendTail(newLine);
        return newLine.toString().trim();
    }

    // all the lines beginning with "@!" are record lines defining the attribute fields
    private static boolean isRecordLineDefiningTheAttributeFields(String line) {
        return line.length() >= 2 && line.substring(0, 2).equals("@!");
    }

    private static String removeComment(String line) {
        // Only outside quotes
        return line.replaceAll("('[^']*')|(^/[^/]*)|(/[^/]*)", "$1$2");
    }

    private static String replaceSpecialCharacters(String line) {
        return line.replace("\\", "\\\\").replace("$", "\\$");
    }
}
