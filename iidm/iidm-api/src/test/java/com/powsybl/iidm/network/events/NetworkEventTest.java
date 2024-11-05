package com.powsybl.iidm.network.events;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NetworkEventTest {

    @Test
    void test() {
        assertEquals(List.of(new RemovalNetworkEvent("a", true)), List.of(new RemovalNetworkEvent("a", true)));
        assertEquals(List.of(new UpdateNetworkEvent("a", "dd", null, "before", "after")), List.of(new UpdateNetworkEvent("a", "dd", null, "before", "after")));
    }
}
