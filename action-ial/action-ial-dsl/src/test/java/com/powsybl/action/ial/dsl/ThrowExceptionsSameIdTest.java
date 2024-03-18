/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.ial.dsl;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import groovy.lang.GroovyCodeSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
class ThrowExceptionsSameIdTest {

    private Network network;

    @BeforeEach
    void setUp() {
        network = EurostagTutorialExample1Factory.create();
    }

    @Test
    void testRule() {
        ActionDslException e = assertThrows(ActionDslException.class, () -> new ActionDslLoader(new GroovyCodeSource(getClass().getResource("/exception-rule.groovy"))).load(network));
        assertTrue(e.getMessage().contains("Rule 'rule1' is defined several times"));
    }

    @Test
    void testAction() {
        ActionDslException e = assertThrows(ActionDslException.class, () -> new ActionDslLoader(new GroovyCodeSource(getClass().getResource("/exception-action.groovy"))).load(network));
        assertTrue(e.getMessage().contains("Action 'action1' is defined several times"));
    }

}
