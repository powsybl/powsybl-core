/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.io;

import com.google.re2j.Matcher;
import com.google.re2j.Pattern;
import com.powsybl.psse.model.PsseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
            if (qRecordFound) {
                throw new PsseException("PSSE. Unexpected end of file");
            }
            return "Q";
        }
        if (isRecordLineDefiningTheAttributeFields(line)) {
            return ""; // an empty line must be returned
        }
        StringBuilder newLine = new StringBuilder();
        Matcher m = FileFormat.LEGACY_TEXT_QUOTED_OR_WHITESPACE.matcher(processText(removeComment(line)));
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

    private static String removeComment(String line) {
        return line.replaceAll(FileFormat.REMOVE_COMMENT_REGEX, "$1$2");
    }

    // Compact spaces, remove spaces before the comma, and replace space with comma outside quoted text
    protected static String processText(String line) {
        if (line == null || line.isEmpty()) {
            return line;
        }

        StringBuilder result = new StringBuilder();
        Pattern pattern = FileFormat.LEGACY_QUOTED_OR_NON_QUOTED_TEXT;
        Matcher matcher = pattern.matcher(line.trim());

        while (matcher.find()) {
            String part = matcher.group();
            if (part.startsWith("'") && part.endsWith("'")) {
                result.append(part);
            } else {
                // Outside quotes: process the txt
                // On the next line: "(?<=\S|^)" = backtracking protection. The previous char should be a non-white char or a line start.
                result.append(part.replaceAll("(?<=\\S|^)\\s+,", ",") // see comment above
                        .replaceAll(",\\s+", ",")
                        .replaceAll("\\s+", ","));
            }
        }

        return result.toString();
    }

    // all the lines beginning with "@!" are record lines defining the attribute fields
    private static boolean isRecordLineDefiningTheAttributeFields(String line) {
        return line.startsWith("@!");
    }

    private static String replaceSpecialCharacters(String line) {
        return line.replace("\\", "\\\\").replace("$", "\\$");
    }
}
