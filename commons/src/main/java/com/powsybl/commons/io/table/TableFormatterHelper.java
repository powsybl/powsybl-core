/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.io.table;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class TableFormatterHelper {

    private final TableFormatter tableFormatter;
    private final List<Object> objectsToWrite = new LinkedList<>();

    public TableFormatterHelper(TableFormatter tableFormatter) {
        this.tableFormatter = tableFormatter;
    }

    public TableFormatterHelper addCell(Object object) {
        objectsToWrite.add(object);
        return this;
    }

    public TableFormatterHelper addCell(Object object, int position) {
        objectsToWrite.add(position, object);
        return this;
    }

    public TableFormatterHelper addEmptyCell() {
        objectsToWrite.add(new EmptyCells(1));
        return this;
    }

    public TableFormatterHelper addEmptyCell(int position) {
        objectsToWrite.add(position, new EmptyCells(1));
        return this;
    }

    public TableFormatterHelper addEmptyCells(int count) {
        objectsToWrite.add(new EmptyCells(count));
        return this;
    }

    public TableFormatterHelper addEmptyCells(int count, int position) {
        objectsToWrite.add(position, new EmptyCells(count));
        return this;
    }

    public TableFormatterHelper addEmptyLine() {
        objectsToWrite.add(new EmptyLines(1));
        return this;
    }

    public TableFormatterHelper addEmptyLine(int position) {
        objectsToWrite.add(position, new EmptyLines(1));
        return this;
    }

    public TableFormatterHelper addEmptyLines(int count) {
        objectsToWrite.add(new EmptyLines(count));
        return this;
    }

    public TableFormatterHelper addEmptyLines(int count, int position) {
        objectsToWrite.add(position, new EmptyLines(count));
        return this;
    }

    public TableFormatterHelper addComment(String comment) {
        objectsToWrite.add(new Comment(comment));
        return this;
    }

    public TableFormatterHelper addComment(String comment, int position) {
        objectsToWrite.add(position, new Comment(comment));
        return this;
    }

    public TableFormatter write() throws IOException {
        for (Object object : objectsToWrite) {
            if (object instanceof String s) {
                tableFormatter.writeCell(s);
            } else if (object instanceof Character c) {
                tableFormatter.writeCell(c);
            } else if (object instanceof Integer i) {
                tableFormatter.writeCell(i);
            } else if (object instanceof Float f) {
                tableFormatter.writeCell(f);
            } else if (object instanceof Double d) {
                tableFormatter.writeCell(d);
            } else if (object instanceof Boolean b) {
                tableFormatter.writeCell(b);
            } else if (object instanceof Comment comment) {
                tableFormatter.writeComment(comment.commentToWrite);
            } else if (object instanceof EmptyCells emptyCells) {
                tableFormatter.writeEmptyCells(emptyCells.numberOfCells);
            } else if (object instanceof EmptyLines emptyLines) {
                tableFormatter.writeEmptyLines(emptyLines.numberOfLines);
            }
        }
        return tableFormatter;
    }

    private static class Comment {
        String commentToWrite;

        Comment(String commentToWrite) {
            this.commentToWrite = commentToWrite;
        }
    }

    private static class EmptyCells {
        int numberOfCells;

        EmptyCells(int numberOfCells) {
            this.numberOfCells = numberOfCells;
        }
    }

    private static class EmptyLines {
        int numberOfLines;

        EmptyLines(int numberOfLines) {
            this.numberOfLines = numberOfLines;
        }
    }
}
