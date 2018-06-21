/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.validation;

import java.io.Writer;
import java.util.Objects;

import com.powsybl.commons.config.ConfigurationException;
import com.powsybl.loadflow.validation.io.ValidationWriter;
import com.powsybl.loadflow.validation.io.ValidationWriterFactory;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public final class ValidationUtils {

    public static final String VALIDATION_ERROR = "validation error";
    public static final String VALIDATION_WARNING = "validation warning";

    private ValidationUtils() {
    }

    public static ValidationWriter createValidationWriter(String id, ValidationConfig config, Writer writer, ValidationType validationType) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);
        Objects.requireNonNull(validationType);
        try {
            ValidationWriterFactory factory = config.getValidationOutputWriter().getValidationWriterFactory().newInstance();
            return factory.create(id, config.getTableFormatterFactory(), writer, config.isVerbose(), validationType, config.isCompareResults());
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ConfigurationException(e);
        }
    }

    public static boolean areNaN(ValidationConfig config, float... values) {
        Objects.requireNonNull(config);
        if (config.areOkMissingValues()) {
            return false;
        }
        boolean areNaN = false;
        for (float value : values) {
            if (Float.isNaN(value)) {
                areNaN = true;
                break;
            }
        }
        return areNaN;
    }

    public static boolean areNaN(ValidationConfig config, double... values) {
        Objects.requireNonNull(config);
        if (config.areOkMissingValues()) {
            return false;
        }
        boolean areNaN = false;
        for (double value : values) {
            if (Double.isNaN(value)) {
                areNaN = true;
                break;
            }
        }
        return areNaN;
    }

    public static boolean boundedWithin(double lowerBound, double upperBound, double value, double margin) {
        if (Double.isNaN(value) || (Double.isNaN(lowerBound) && Double.isNaN(upperBound))) {
            return false;
        }
        if (Double.isNaN(lowerBound)) {
            return value - margin <= upperBound;
        }
        if (Double.isNaN(upperBound)) {
            return value + margin >= lowerBound;
        }
        return value + margin >= lowerBound && value - margin <= upperBound;
    }

    public static boolean isMainComponent(ValidationConfig config, boolean mainComponent) {
        Objects.requireNonNull(config);
        return config.isCheckMainComponentOnly() ? mainComponent : true;
    }

}
