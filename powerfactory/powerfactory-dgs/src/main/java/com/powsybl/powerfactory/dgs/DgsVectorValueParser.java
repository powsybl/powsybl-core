/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.dgs;

import com.powsybl.powerfactory.model.DataAttributeType;
import com.powsybl.powerfactory.model.PowerFactoryException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class DgsVectorValueParser extends AbstractDgsValueParser {

    private final int length;

    DgsVectorValueParser(String attributeName, DataAttributeType attributeType, int indexField, int length) {
        super(attributeName, attributeType, indexField);
        this.length = length;
    }

    @Override
    public void parse(String[] fields, DgsHandler handler, DgsParsingContext context) {
        switch (attributeType) {
            case STRING_VECTOR:
                readVector(fields, Function.identity(), handler::onStringVectorValue);
                break;
            case INTEGER_VECTOR:
                readVector(fields, Integer::parseInt, handler::onIntVectorValue);
                break;
            case DOUBLE_VECTOR:
                readVector(fields, context::parseDouble, handler::onDoubleVectorValue);
                break;
            case OBJECT_VECTOR:
                // Read object numbers as long integers
                readVector(fields, Long::parseLong, handler::onObjectVectorValue);
                break;
            default:
                throw new PowerFactoryException("Unexpected vector attribute type:" + attributeType);
        }
    }

    private <T> void readVector(String[] fields, Function<String, T> parserFunction, BiConsumer<String, List<T>> onValue) {
        read(fields, parserFunction).ifPresent(v -> onValue.accept(attributeName, v));
    }

    private <T> Optional<List<T>> read(String[] fields, Function<String, T> parserFunction) {
        Objects.requireNonNull(fields);
        List<T> values = new ArrayList<>();
        int actualLength = Integer.parseInt(fields[indexField]);
        if (actualLength > this.length) {
            throw new PowerFactoryException("VectorArray: Unexpected length: '" + attributeName +
                    "' length: " + actualLength + ", expected length: " + this.length);
        }
        if (actualLength == 0) {
            return Optional.empty();
        }
        for (int i = 0; i < actualLength; i++) {
            if (isValidField(fields, indexField + 1 + i)) {
                values.add(parserFunction.apply(fields[indexField + 1 + i]));
            } else {
                return Optional.empty();
            }
        }
        return Optional.of(values);
    }
}
