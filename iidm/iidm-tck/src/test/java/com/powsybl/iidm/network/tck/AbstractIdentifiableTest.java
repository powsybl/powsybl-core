package com.powsybl.iidm.network.tck;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public abstract class AbstractIdentifiableTest {

    @Test
    public void test() {
        Network network = EurostagTutorialExample1Factory.create();
        Load load = network.getLoad("LOAD");
        assertEquals("LOAD", load.getId());
        load.setId("NEW_LOAD_ID");
        assertEquals(load.getId(), network.getIdentifiable("NEW_LOAD_ID").getId());
        assertEquals("NEW_LOAD_ID", load.getId());

        Generator gen = network.getGenerator("GEN");
        assertThrows(PowsyblException.class, () -> gen.setId("NEW_LOAD_ID"), "Object with id (NEW_LOAD_ID) already exists");
    }
}
