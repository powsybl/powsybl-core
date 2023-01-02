/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.dgs;

import com.powsybl.powerfactory.model.DataAttributeType;
import com.powsybl.powerfactory.model.PowerFactoryException;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class DgsMatrixValueParser extends AbstractDgsValueParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(DgsMatrixValueParser.class);

    private final int rows;
    private final int cols;

    DgsMatrixValueParser(String attributeName, DataAttributeType attributeType, int indexField, int rows, int cols) {
        super(attributeName, attributeType, indexField);
        this.rows = rows;
        this.cols = cols;
    }

    @Override
    public void parse(String[] fields, DgsHandler handler, DgsParsingContext context) {
        read(fields, context).ifPresent(m -> handler.onDoubleMatrixValue(attributeName, m));
    }

    private Optional<RealMatrix> read(String[] fields, DgsParsingContext context) {
        int actualRows = Integer.parseInt(fields[indexField]);
        int actualCols = Integer.parseInt(fields[indexField + 1]);
        if (actualRows == 0 || actualCols == 0) {
            return Optional.empty();
        }
        if (actualRows != this.rows) {
            LOGGER.debug("RealMatrix: actual rows {} different than expected {}. All actual rows will be read.", actualRows, this.rows);
        }
        if (actualCols != this.cols) {
            throw new PowerFactoryException("RealMatrix: Unexpected number of cols: '"
                    + attributeName + "' rows: " + actualRows + " cols: " + actualCols + " expected cols: " + this.cols);
        }
        RealMatrix realMatrix = new BlockRealMatrix(actualRows, actualCols);
        for (int i = 0; i < actualRows; i++) {
            for (int j = 0; j < actualCols; j++) {
                if (isValidField(fields, indexField + 2 + i * actualCols + j)) {
                    double value = context.parseDouble(fields[indexField + 2 + i * actualCols + j]);
                    realMatrix.setEntry(i, j, value);
                } else {
                    return Optional.empty();
                }
            }
        }
        return Optional.of(realMatrix);
    }
}
