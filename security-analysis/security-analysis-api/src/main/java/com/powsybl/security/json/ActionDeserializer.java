/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.security.action.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class ActionDeserializer extends StdDeserializer<Action> {

    public ActionDeserializer() {
        super(Action.class);
    }

    private static class ParsingContext {
        String type;
        String id;
        String switchId;
        String lineId;
        String transformerId;
        String generatorId;
        Boolean open;
        Boolean openSide1;
        Boolean openSide2;
        Boolean relativeValue;
        int value;
        ThreeWindingsTransformer.Side side = null;
        Boolean activePowerRelativeValue;
        Double activePowerValue;
        Boolean voltageRegulatorOn;
        Double targetV;
        Double targetQ;
        List<Action> actions;
    }

    @Override
    public Action deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        ParsingContext context = new ParsingContext();
        JsonUtil.parseObject(parser, fieldName -> {
            switch (fieldName) {
                case "type":
                    context.type = parser.nextTextValue();
                    return true;
                case "id":
                    context.id = parser.nextTextValue();
                    return true;
                case "switchId":
                    context.switchId = parser.nextTextValue();
                    return true;
                case "lineId":
                    context.lineId = parser.nextTextValue();
                    return true;
                case "transformerId":
                    context.transformerId = parser.nextTextValue();
                    return true;
                case "generatorId":
                    context.generatorId = parser.nextTextValue();
                    return true;
                case "open":
                    parser.nextToken();
                    context.open = parser.getBooleanValue();
                    return true;
                case "openSide1":
                    parser.nextToken();
                    context.openSide1 = parser.getBooleanValue();
                    return true;
                case "openSide2":
                    parser.nextToken();
                    context.openSide2 = parser.getBooleanValue();
                    return true;
                case "relativeValue":
                    parser.nextToken();
                    context.relativeValue = parser.getBooleanValue();
                    return true;
                case "value":
                    parser.nextToken();
                    context.value = parser.getIntValue();
                    return true;
                case "side":
                    String sideStr = parser.nextTextValue();
                    context.side = ThreeWindingsTransformer.Side.valueOf(sideStr);
                    return true;
                case "activePowerRelativeValue":
                    parser.nextToken();
                    context.activePowerRelativeValue = parser.getBooleanValue();
                    return true;
                case "activePowerValue":
                    parser.nextToken();
                    context.activePowerValue = parser.getDoubleValue();
                    return true;
                case "voltageRegulatorOn":
                    parser.nextToken();
                    context.voltageRegulatorOn = parser.getBooleanValue();
                    return true;
                case "targetV":
                    parser.nextToken();
                    context.targetV = parser.getDoubleValue();
                    return true;
                case "targetQ":
                    parser.nextToken();
                    context.targetQ = parser.getDoubleValue();
                    return true;
                case "actions":
                    parser.nextToken();
                    context.actions = parser.readValueAs(new TypeReference<ArrayList<Action>>() {
                    });
                    return true;
                default:
                    return false;
            }
        });
        if (context.type == null) {
            throw JsonMappingException.from(parser, "action type can not be null");
        }
        switch (context.type) {
            case SwitchAction.NAME:
                if (context.open == null) {
                    throw JsonMappingException.from(parser, "for switch action, open field can't be null");
                }
                return new SwitchAction(context.id, context.switchId, context.open);
            case LineConnectionAction.NAME:
                if (context.openSide1 == null || context.openSide2 == null) {
                    throw JsonMappingException.from(parser, "for line action openSide1 and openSide2 fields can't be null");
                }
                return new LineConnectionAction(context.id, context.lineId, context.openSide1, context.openSide2);
            case PhaseTapChangerTapPositionAction.NAME:
                if (context.relativeValue == null) {
                    throw JsonMappingException.from(parser, "for phase tap changer tap position action relative value field can't be null");
                }
                if (context.value == 0) {
                    throw JsonMappingException.from(parser, "for phase tap changer tap position action value field can't equal zero");
                }
                if (context.side != null) {
                    return new PhaseTapChangerTapPositionAction(context.id, context.transformerId, context.relativeValue, context.value, context.side);
                } else {
                    return new PhaseTapChangerTapPositionAction(context.id, context.transformerId, context.relativeValue, context.value);
                }
            case GeneratorAction.NAME:
                GeneratorActionBuilder generatorActionBuilder = new GeneratorActionBuilder();
                generatorActionBuilder
                        .withId(context.id)
                        .withGeneratorId(context.generatorId);
                if (context.activePowerRelativeValue != null) {
                    generatorActionBuilder.withActivePowerRelativeValue(context.activePowerRelativeValue);
                }
                if (context.activePowerValue != null) {
                    generatorActionBuilder.withActivePowerValue(context.activePowerValue);
                }
                if (context.voltageRegulatorOn != null) {
                    generatorActionBuilder.withVoltageRegulatorOn(context.voltageRegulatorOn);
                }
                if (context.targetV != null) {
                    generatorActionBuilder.withTargetV(context.targetV);
                }
                if (context.targetQ != null) {
                    generatorActionBuilder.withTargetQ(context.targetQ);
                }
                return generatorActionBuilder.build();
            case MultipleActionsAction.NAME:
                return new MultipleActionsAction(context.id, context.actions);
            default:
                throw JsonMappingException.from(parser, "Unknown action type: " + context.type);
        }
    }
}
