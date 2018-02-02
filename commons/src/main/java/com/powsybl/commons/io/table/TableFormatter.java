/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.io.table;

import java.io.IOException;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface TableFormatter extends AutoCloseable {

    TableFormatter writeComment(String comment) throws IOException;

    TableFormatter writeCell(String s) throws IOException;

    TableFormatter writeEmptyCell() throws IOException;

    TableFormatter writeEmptyCells(int count) throws IOException;

    TableFormatter writeEmptyLine() throws IOException;

    TableFormatter writeEmptyLines(int count) throws IOException;

    TableFormatter writeCell(char c) throws IOException;

    TableFormatter writeCell(int i) throws IOException;

    TableFormatter writeCell(float f) throws IOException;

    TableFormatter writeCell(double d) throws IOException;

    TableFormatter writeCell(boolean b) throws IOException;

    @Override void close() throws IOException;
}
