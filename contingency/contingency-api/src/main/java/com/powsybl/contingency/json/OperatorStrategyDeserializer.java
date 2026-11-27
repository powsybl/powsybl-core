/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.json;

import com.google.common.base.Suppliers;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.commons.extensions.ExtensionProviders;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.ContingencyContext;
import com.powsybl.contingency.ContingencyContextType;
import com.powsybl.contingency.strategy.ConditionalActions;
import com.powsybl.contingency.strategy.OperatorStrategy;
import com.powsybl.contingency.strategy.condition.Condition;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class OperatorStrategyDeserializer extends StdDeserializer<OperatorStrategy> {

    private static final String CONTEXT_NAME = "OperatorStrategy";
    private static final String TAG_CONDITIONAL_ACTIONS = "Tag: conditionalActions";
    private static final String TAG_CONDITION = "Tag: condition";
    private static final String TAG_ACTION_IDS = "Tag: actionIds";
    public static final String SOURCE_VERSION_ATTRIBUTE = "sourceVersionAttribute";

    private final transient ValueDeserializer<Object> conditionalActionsDeserializer;
    private final transient ValueDeserializer<Object> conditionDeserializer;
    private final transient ValueDeserializer<Object> actionsIdsDeserializer;

    public OperatorStrategyDeserializer() {
        this(null, null, null);
    }

    public OperatorStrategyDeserializer(ValueDeserializer<Object> conditionalActionsDeserializer, ValueDeserializer<Object> conditionDeserializer, ValueDeserializer<Object> actionsIdsDeserializer) {
        super(OperatorStrategy.class);
        this.conditionalActionsDeserializer = conditionalActionsDeserializer;
        this.conditionDeserializer = conditionDeserializer;
        this.actionsIdsDeserializer = actionsIdsDeserializer;
    }

    @Override
    public ValueDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws DatabindException {
        return new OperatorStrategyDeserializer(
            JsonUtil.buildListDeserializer(ctxt, property, ConditionalActions.class),
            JsonUtil.buildValueDeserializer(ctxt, property, Condition.class),
            JsonUtil.buildListDeserializer(ctxt, property, String.class)
        );
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
    public OperatorStrategy deserialize(JsonParser parser, DeserializationContext deserializationContext) throws JacksonException {
        ParsingContext context = new ParsingContext();
        context.version = JsonUtil.getSourceVersion(deserializationContext, SOURCE_VERSION_ATTRIBUTE);
        if (context.version == null) {
            throw new PowsyblException("Missing version");
        }
        JsonUtil.parseObject(parser, fieldName -> parseChildNode(parser, deserializationContext, fieldName, context));
        // First version of the operator strategy only allows association to one contingency. Next versions use contingency
        // context. So, for backward compatibility purposes, we consider that if contingencyContextType is null, we have
        // a specific contingency context type.
        ContingencyContext contingencyContext = new ContingencyContext(context.contingencyId,
                context.contingencyContextType != null ? context.contingencyContextType : ContingencyContextType.SPECIFIC);
        OperatorStrategy strategy;
        if (JsonUtil.compareVersions(context.version, "1.1") < 0) {
            strategy = new OperatorStrategy(context.id, contingencyContext, context.condition, context.actionIds);
        } else {
            strategy = new OperatorStrategy(context.id, contingencyContext, context.stages);
        }

        SUPPLIER.get().addExtensions(strategy, context.extensions);
        return strategy;
    }

    private boolean parseChildNode(JsonParser parser, DeserializationContext deserializationContext, String fieldName,
                                   ParsingContext context) throws JacksonException {
        switch (fieldName) {
            case "id":
                parser.nextToken();
                context.id = parser.getValueAsString();
                return true;
            case "contingencyContextType":
                context.contingencyContextType = ContingencyContextType.valueOf(parser.nextStringValue());
                return true;
            case "contingencyId":
                parser.nextToken();
                context.contingencyId = parser.getValueAsString();
                return true;
            case "conditionalActions":
                parser.nextToken();
                JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, TAG_CONDITIONAL_ACTIONS,
                    context.version, "1.1");
                context.stages = JsonUtil.readList(conditionalActionsDeserializer, deserializationContext, parser, ConditionalActions.class);
                return true;
            case "condition":
                parser.nextToken();
                JsonUtil.assertLessThanOrEqualToReferenceVersion(CONTEXT_NAME, TAG_CONDITION,
                    context.version, "1.0");
                context.condition = JsonUtil.readValue(conditionDeserializer, deserializationContext, parser, Condition.class);
                return true;
            case "actionIds":
                parser.nextToken();
                JsonUtil.assertLessThanOrEqualToReferenceVersion(CONTEXT_NAME, TAG_ACTION_IDS,
                    context.version, "1.0");
                context.actionIds = JsonUtil.readList(actionsIdsDeserializer, deserializationContext, parser, String.class);
                return true;
            case "extensions":
                parser.nextToken();
                context.extensions = JsonUtil.readExtensions(parser, deserializationContext, SUPPLIER.get());
                return true;
            default:
                return false;
        }
    }
}
