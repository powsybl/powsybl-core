/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class GroovySetIdIssue {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void test() {
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("ID modification of 'GEN' is not allowed");
        Network network = EurostagTutorialExample1Factory.create();
        Binding binding = new Binding();
        binding.setProperty("network", network);
        GroovyShell shell = new GroovyShell(binding);
        shell.evaluate("network.getGenerator('GEN').id = 'FOO'");
    }
}
