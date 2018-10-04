/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.dsl;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import groovy.lang.GroovyCodeSource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class ThrowExceptionUndefinedActionTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private Network network;

    @Before
    public void setUp() {
        network = EurostagTutorialExample1Factory.create();
    }

    @Test
    public void test() {
        thrown.expect(ActionDslException.class);
        thrown.expectMessage("Actions [action, action2] not found");
        new ActionDslLoader(new GroovyCodeSource(getClass().getResource("/exception-undefined-action.groovy"))).load(network);
    }

}
