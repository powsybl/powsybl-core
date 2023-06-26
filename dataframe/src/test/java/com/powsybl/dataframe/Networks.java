package com.powsybl.dataframe;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.ActivePowerControlAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;

public final class Networks {

    public static Network fix(Network network) {
        Generator gen = network.getGenerator("GEN");
        if (gen != null) {
            gen.setMaxP(4999);
        }
        Generator gen2 = network.getGenerator("GEN2");
        if (gen2 != null) {
            gen2.setMaxP(4999);
        }
        return network;
    }

    public static Network createEurostagTutorialExample1WithFixedCurrentLimits() {
        Network network = EurostagTutorialExample1Factory.createWithFixedCurrentLimits();
        return fix(network);
    }

    public static Network createEurostagTutorialExample1WithApcExtension() {
        Network network = createEurostagTutorialExample1();
        network.getGenerator("GEN")
            .newExtension(ActivePowerControlAdder.class)
            .withParticipate(true)
            .withDroop(1.1f)
            .add();
        return network;
    }

    public static Network createEurostagTutorialExample1() {
        Network network = EurostagTutorialExample1Factory.create();
        return fix(network);
    }

    private Networks() {
    }
}
