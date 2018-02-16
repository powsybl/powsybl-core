/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.validation.io;

import java.io.Writer;

import com.powsybl.commons.io.table.TableFormatterFactory;
import com.powsybl.loadflow.validation.ValidationType;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.it>
 */
public class ValidationFormatterCsvMultilineWriterFactory implements ValidationWriterFactory {

    @Override
    public ValidationWriter create(String id, Class<? extends TableFormatterFactory> formatterFactoryClass, Writer writer, boolean verbose, ValidationType validationType, boolean compareResults) {
        return new ValidationFormatterCsvMultilineWriter(id, formatterFactoryClass, writer, verbose, validationType, compareResults);
    }

}
