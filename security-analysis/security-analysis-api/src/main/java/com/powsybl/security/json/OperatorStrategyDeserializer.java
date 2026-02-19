/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.base.Suppliers;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.commons.extensions.ExtensionProviders;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.ContingencyContext;
import com.powsybl.contingency.ContingencyContextType;
import com.powsybl.security.condition.Condition;
import com.powsybl.security.strategy.OperatorStrategy;
import com.powsybl.security.strategy.ConditionalActions;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static com.powsybl.security.json.SecurityAnalysisResultDeserializer.SOURCE_VERSION_ATTRIBUTE;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class OperatorStrategyDeserializer extends StdDeserializer<OperatorStrategy> {

    private static final String CONTEXT_NAME = "OperatorStrategy";
    private static final String TAG_CONTINGENCY_STATUS = "Tag: contingencyStatus";

    public OperatorStrategyDeserializer() {
        super(OperatorStrategy.class);
    }

    private static final Supplier<ExtensionProviders<ExtensionJsonSerializer>> SUPPLIER =
            Suppliers.memoize(() -> ExtensionProviders.createProvider(ExtensionJsonSerializer.class, "security-analysis"));

    private static final class ParsingContext {
        String version;
        String id;
        ContingencyContextType contingencyContextType;
        String contingencyId;
        List<ConditionalActions> stages;
        Condition condition;
        List<String> actionIds;
        List<Extension<OperatorStrategy>> extensions = Collections.emptyList();
    }

    @Override
    public OperatorStrategy deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        ParsingContext context = new ParsingContext();
        context.version = JsonUtil.getSourceVersion(deserializationContext, SOURCE_VERSION_ATTRIBUTE);
        if (context.version == null) {  // assuming current version...
            context.version = SecurityAnalysisResultSerializer.VERSION;
        }
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
                case "conditionalActions":
                    parser.nextToken();
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, TAG_CONTINGENCY_STATUS,
                            context.version, "1.5");
                    context.stages = JsonUtil.readList(deserializationContext, parser, ConditionalActions.class);
                    return true;
                case "condition":
                    parser.nextToken();
                    JsonUtil.assertLessThanOrEqualToReferenceVersion(CONTEXT_NAME, TAG_CONTINGENCY_STATUS,
                            context.version, "1.4");
                    context.condition = JsonUtil.readValue(deserializationContext, parser, Condition.class);
                    return true;
                case "actionIds":
                    parser.nextToken();
                    JsonUtil.assertLessThanOrEqualToReferenceVersion(CONTEXT_NAME, TAG_CONTINGENCY_STATUS,
                            context.version, "1.4");
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
        OperatorStrategy strategy;
        if (context.version.compareTo("1.5") < 0) {
            strategy = new OperatorStrategy(context.id, contingencyContext, context.condition, context.actionIds);
        } else {
            strategy = new OperatorStrategy(context.id, contingencyContext, context.stages);
        }

        SUPPLIER.get().addExtensions(strategy, context.extensions);
        return strategy;
    }
}
