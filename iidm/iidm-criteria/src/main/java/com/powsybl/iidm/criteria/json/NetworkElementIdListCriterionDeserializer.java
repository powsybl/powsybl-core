/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.iidm.criteria.NetworkElementIdListCriterion;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>Deserializer for {@link NetworkElementIdListCriterion} objects.</p>
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class NetworkElementIdListCriterionDeserializer extends AbstractNetworkElementCriterionDeserializer<NetworkElementIdListCriterion> {
    public NetworkElementIdListCriterionDeserializer() {
        super(NetworkElementIdListCriterion.class);
    }

    @Override
    public NetworkElementIdListCriterion deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        ParsingContext parsingContext = new ParsingContext();
        JsonUtil.parsePolymorphicObject(parser, name -> deserialize(parser, parsingContext, name));

        return new NetworkElementIdListCriterion(parsingContext.name, parsingContext.ids);
    }

    private boolean deserialize(JsonParser parser, ParsingContext parsingContext, String name) throws IOException {
        if (name.equals("identifiers")) {
            parsingContext.ids = new HashSet<>(JsonUtil.parseStringArray(parser));
            return true;
        }
        return deserializeCommonAttributes(parser, parsingContext, name, NetworkElementIdListCriterion.TYPE);
    }

    protected static class ParsingContext extends AbstractNetworkElementCriterionDeserializer.ParsingContext {
        Set<String> ids = null;
    }
}
