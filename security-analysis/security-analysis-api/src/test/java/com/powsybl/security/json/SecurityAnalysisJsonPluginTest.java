/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.powsybl.action.AbstractAction;
import com.powsybl.action.Action;
import com.powsybl.action.ActionBuilder;
import com.powsybl.action.ActionList;
import com.powsybl.action.json.ActionJsonModule;
import com.powsybl.commons.json.JsonUtil;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class SecurityAnalysisJsonPluginTest {

    @JsonTypeName(DummyAction.NAME)
    static class DummyAction extends AbstractAction {

        static final String NAME = "dummy-action";

        @JsonCreator
        protected DummyAction(@JsonProperty("id") String id) {
            super(id);
        }

        @JsonProperty(value = "type", access = JsonProperty.Access.READ_ONLY)
        @Override
        public String getType() {
            return NAME;
        }
    }

    @JsonTypeName(DummyAction.NAME)
    static class DummyActionBuilder implements ActionBuilder<DummyActionBuilder> {

        String id;

        @Override
        @JsonProperty(value = "type", access = JsonProperty.Access.READ_ONLY)
        public String getType() {
            return DummyAction.NAME;
        }

        @Override
        public DummyActionBuilder withId(String id) {
            this.id = id;
            return this;
        }

        @Override
        public String getId() {
            return this.id;
        }

        @Override
        public DummyActionBuilder withNetworkElementId(String elementId) {
            return null;
        }

        @Override
        public Action build() {
            return new DummyAction(id);
        }
    }

    @Test
    void testJsonPlugins() {
        JacksonModule jsonModule = new SimpleModule()
                .registerSubtypes(DummyAction.class, DummyActionBuilder.class);
        SecurityAnalysisJsonPlugin plugin = () -> List.of(jsonModule);
        JsonMapper mapper = JsonUtil.createJsonMapperBuilder()
                .addModule(new SecurityAnalysisJsonModule(List.of(plugin)))
                .addModule(new ActionJsonModule())
            .build();

        DummyAction action = new DummyAction("hello");
        ActionList actions = new ActionList(List.of(action));
        String serialized = mapper.writeValueAsString(actions);
        ActionList parsed = mapper.readValue(serialized, ActionList.class);

        assertEquals(1, parsed.getActions().size());
        Action parsedAction = parsed.getActions().getFirst();
        assertInstanceOf(DummyAction.class, parsedAction);
        assertEquals("hello", parsedAction.getId());
    }
}
