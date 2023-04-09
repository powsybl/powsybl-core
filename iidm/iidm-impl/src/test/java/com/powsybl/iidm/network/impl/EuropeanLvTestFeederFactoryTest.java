package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EuropeanLvTestFeederFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EuropeanLvTestFeederFactoryTest {

    @Test
    void test() {
        Network network = EuropeanLvTestFeederFactory.create();

    }
}