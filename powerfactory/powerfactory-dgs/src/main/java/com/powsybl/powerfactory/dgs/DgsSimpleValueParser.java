/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.dgs;

import com.powsybl.powerfactory.model.DataAttributeType;
import com.powsybl.powerfactory.model.PowerFactoryException;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class DgsSimpleValueParser extends AbstractDgsValueParser {

    DgsSimpleValueParser(String attributeName, DataAttributeType attributeType, int indexField) {
        super(attributeName, attributeType, indexField);
    }

    @Override
    public void parse(String[] fields, DgsHandler handler, DgsParsingContext context) {
        switch (attributeType) {
            case STRING:
                read(fields, Function.identity(), handler::onStringValue);
                break;
            case INTEGER:
                read(fields, Integer::parseInt, handler::onIntegerValue);
                break;
            case FLOAT:
                read(fields, context::parseFloat, handler::onRealValue);
                break;
            case OBJECT:
                read(fields, Long::parseLong, handler::onObjectValue);
                break;
            default:
                throw new PowerFactoryException("Unexpected attribute type:" + attributeType);
        }
    }

    private <T> void read(String[] fields, Function<String, T> parser, BiConsumer<String, T> onValue) {
        if (isValidField(fields, indexField)) {
            String value = fields[indexField];
            onValue.accept(attributeName, parser.apply(value));
        }
    }
}
