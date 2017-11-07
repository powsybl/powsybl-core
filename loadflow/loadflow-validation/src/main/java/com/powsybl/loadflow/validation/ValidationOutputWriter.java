/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.validation;

import java.util.Objects;

import com.powsybl.loadflow.validation.io.ValidationFormatterCsvMultilineWriterFactory;
import com.powsybl.loadflow.validation.io.ValidationFormatterCsvWriterFactory;
import com.powsybl.loadflow.validation.io.ValidationWriterFactory;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.it>
 */
public enum ValidationOutputWriter {
    CSV(ValidationFormatterCsvWriterFactory.class),
    CSV_MULTILINE(ValidationFormatterCsvMultilineWriterFactory.class);

    private final Class<? extends ValidationWriterFactory> validationWriterFactory;

    ValidationOutputWriter(Class<? extends ValidationWriterFactory> validationWriterFactory) {
        this.validationWriterFactory = Objects.requireNonNull(validationWriterFactory);
    }

    public Class<? extends ValidationWriterFactory> getValidationWriterFactory() {
        return validationWriterFactory;
    }

}
