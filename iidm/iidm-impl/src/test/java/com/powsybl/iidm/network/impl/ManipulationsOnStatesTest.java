package com.powsybl.iidm.network.impl;

import com.google.common.collect.Sets;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StateManager;
import com.powsybl.iidm.network.StateManagerConstants;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ManipulationsOnStatesTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private StateManager stateManager;

    @Before
    public void setUp() {
        Network network = NoEquipmentNetworkFactory.create();
        stateManager = network.getStateManager();
    }

    @Test
    public void errorRemoveInitialState() {
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("Removing initial state is forbidden");
        stateManager.removeState(StateManagerConstants.INITIAL_STATE_ID);
    }

    @Test
    public void errorNotExistingState() {
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("not found");
        stateManager.removeState("not_exists");
    }

    @Test
    public void errorCloneToEmptyStates() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Empty target state id list");
        stateManager.cloneState(StateManagerConstants.INITIAL_STATE_ID, Collections.emptyList());
    }

    @Test
    public void errorCloneToExistingState() {
        stateManager.cloneState(StateManagerConstants.INITIAL_STATE_ID, "hello");
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("already exists");
        stateManager.cloneState(StateManagerConstants.INITIAL_STATE_ID, "hello");
    }

    @Test
    public void baseTests() {
        List<String> statesToAdd = Arrays.asList("s1", "s2", "s3", "s4");
        // extend
        stateManager.cloneState(StateManagerConstants.INITIAL_STATE_ID, statesToAdd);
        assertEquals(Sets.newHashSet(StateManagerConstants.INITIAL_STATE_ID, "s1", "s2", "s3", "s4"), stateManager.getStateIds());

        // delete
        stateManager.removeState("s2");
        assertEquals(Sets.newHashSet(StateManagerConstants.INITIAL_STATE_ID, "s1", "s3", "s4"), stateManager.getStateIds());

        // allocate
        stateManager.cloneState("s4", "s2b");
        assertEquals(Sets.newHashSet(StateManagerConstants.INITIAL_STATE_ID, "s1", "s2b", "s3", "s4"), stateManager.getStateIds());

        // reduce
        stateManager.setWorkingState("s4");
        stateManager.removeState("s4");
        assertEquals(Sets.newHashSet(StateManagerConstants.INITIAL_STATE_ID, "s2b", "s1", "s3"), stateManager.getStateIds());

        try {
            stateManager.getWorkingStateId();
            fail();
        } catch (Exception ignored) {
        }
    }
}
