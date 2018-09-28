package com.powsybl.action.dsl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import groovy.lang.GroovyCodeSource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ThrowExceptionsSameIdTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private Network network;

    @Before
    public void setUp() {
        network = EurostagTutorialExample1Factory.create();
    }

    @Test
    public void testRule() {
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("Rule 'rule1' is defined several times");
        new ActionDslLoader(new GroovyCodeSource(getClass().getResource("/exception-rule.groovy"))).load(network);
    }

    @Test
    public void testAction() {
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("Action 'action1' is defined several times");
        new ActionDslLoader(new GroovyCodeSource(getClass().getResource("/exception-action.groovy"))).load(network);
    }

}
