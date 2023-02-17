/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.commons.extensions.ExtensionProviders;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.ContingencyContext;
import com.powsybl.contingency.ContingencyContextType;
import com.powsybl.security.condition.Condition;
import com.powsybl.security.strategy.OperatorStrategy;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class OperatorStrategyDeserializer extends StdDeserializer<OperatorStrategy> {

    public OperatorStrategyDeserializer() {
        super(OperatorStrategy.class);
    }

    private static final Supplier<ExtensionProviders<ExtensionJsonSerializer>> SUPPLIER =
            Suppliers.memoize(() -> ExtensionProviders.createProvider(ExtensionJsonSerializer.class, "security-analysis"));

    private static class ParsingContext {
        String id;
        ContingencyContextType contingencyContextType;
        String contingencyId;
        Condition condition;
        List<String> actionIds;
        List<Extension<OperatorStrategy>> extensions = Collections.emptyList();
    }

    @Override
    public OperatorStrategy deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        ParsingContext context = new ParsingContext();
        JsonUtil.parseObject(parser, fieldName -> {
            switch (fieldName) {
                case "id":
                    parser.nextToken();
                    context.id = parser.getValueAsString();
                    return true;
                case "contingencyContextType":
                    context.contingencyContextType = ContingencyContextType.valueOf(parser.nextTextValue());
                    return true;
                case "contingencyId":
                    parser.nextToken();
                    context.contingencyId = parser.getValueAsString();
                    return true;
                case "condition":
                    parser.nextToken();
                    context.condition = JsonUtil.readValue(deserializationContext, parser, Condition.class);
                    return true;
                case "actionIds":
                    parser.nextToken();
                    context.actionIds = JsonUtil.readList(deserializationContext, parser, String.class);
                    return true;
                case "extensions":
                    parser.nextToken();
                    context.extensions = JsonUtil.readExtensions(parser, deserializationContext, SUPPLIER.get());
                    return true;
                default:
                    return false;
            }
        });
        // First version of the operator strategy only allows association to one contingency. Next versions use contingency
        // context. So, for backward compatibility purposes, we consider that if contingencyContextType is null, we have
        // a specific contingency context type.
        ContingencyContext contingencyContext = new ContingencyContext(context.contingencyId,
                context.contingencyContextType != null ? context.contingencyContextType : ContingencyContextType.SPECIFIC);
        OperatorStrategy operatorStrategy = new OperatorStrategy(context.id, contingencyContext, context.condition, context.actionIds);
        SUPPLIER.get().addExtensions(operatorStrategy, context.extensions);
        return operatorStrategy;
    }
}
