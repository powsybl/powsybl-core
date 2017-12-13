/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Branch;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationType;

import java.io.IOException;

import static com.powsybl.iidm.network.TwoTerminalsConnectable.*;

/**
 * @author Olivier Bretteville <olivier.bretteville@rte-france.com>
 */
public class ApogeeLimitViolationSerializer extends StdSerializer<LimitViolation> {

    private final Network network;

    public ApogeeLimitViolationSerializer(Network network) {
        super(LimitViolation.class);

        this.network = network;
    }

    @Override
    public void serialize(LimitViolation limitViolation, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("subjectId", limitViolation.getSubjectId());
        if (Float.isNaN(limitViolation.getValueBefore())) {
            jsonGenerator.writeBooleanField("isPostContingency", false);
        } else {
            jsonGenerator.writeBooleanField("isPostContingency", true);
        }
        jsonGenerator.writeStringField("limitType", limitViolation.getLimitType().name());
        JsonUtil.writeOptionalStringField(jsonGenerator, "limitName", limitViolation.getLimitName());
        jsonGenerator.writeNumberField("limit", limitViolation.getLimit());
        jsonGenerator.writeNumberField("limitReduction", limitViolation.getLimitReduction());
        JsonUtil.writeOptionalFloatField(jsonGenerator, "valueBefore", limitViolation.getValueBefore());
        JsonUtil.writeOptionalFloatField(jsonGenerator, "valueBeforeMW", limitViolation.getValueBeforeMW());
        jsonGenerator.writeNumberField("value", limitViolation.getValue());
        JsonUtil.writeOptionalFloatField(jsonGenerator, "valueMW", limitViolation.getValueMW());
        JsonUtil.writeOptionalIntegerField(jsonGenerator, "limitDuration", limitViolation.getAcceptableDuration());

        if (limitViolation.getLimitType() == LimitViolationType.CURRENT) {
            JsonUtil.writeOptionalStringField(jsonGenerator, "countryOr", LimitViolation.getCountry(limitViolation, network, Branch.Side.ONE).getName());
            JsonUtil.writeOptionalStringField(jsonGenerator, "countryEx", LimitViolation.getCountry(limitViolation, network, Branch.Side.TWO).getName());
            JsonUtil.writeOptionalStringField(jsonGenerator, "regionOr", LimitViolation.getRegion(limitViolation, network, Branch.Side.ONE));
            JsonUtil.writeOptionalStringField(jsonGenerator, "regionEx", LimitViolation.getRegion(limitViolation, network, Branch.Side.TWO));
            JsonUtil.writeOptionalStringField(jsonGenerator, "substationOr", LimitViolation.getSubstation(limitViolation, network, Branch.Side.ONE));
            JsonUtil.writeOptionalStringField(jsonGenerator, "substationEx", LimitViolation.getSubstation(limitViolation, network, Branch.Side.TWO));
            JsonUtil.writeOptionalFloatField(jsonGenerator, "baseVoltageOr", LimitViolation.getNominalVoltage(limitViolation, network, Branch.Side.ONE));
            JsonUtil.writeOptionalFloatField(jsonGenerator, "baseVoltageEx", LimitViolation.getNominalVoltage(limitViolation, network, Branch.Side.TWO));
            if (limitViolation.getSide() == Side.ONE) {
                jsonGenerator.writeBooleanField("isOrigin", true);
            } else {
                jsonGenerator.writeBooleanField("isOrigin", false);
            }
        } else {
            JsonUtil.writeOptionalStringField(jsonGenerator, "country", LimitViolation.getCountry(limitViolation, network).getName());
            JsonUtil.writeOptionalStringField(jsonGenerator, "region", LimitViolation.getRegion(limitViolation, network));
            JsonUtil.writeOptionalStringField(jsonGenerator, "substation", LimitViolation.getSubstation(limitViolation, network));
            JsonUtil.writeOptionalFloatField(jsonGenerator, "baseVoltage", LimitViolation.getNominalVoltage(limitViolation, network));
        }

        jsonGenerator.writeEndObject();
    }
}
