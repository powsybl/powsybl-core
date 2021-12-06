import com.powsybl.iidm.network.extensions.LoadDetailAdder

network.getLineStream()
        .filter { l -> l.terminal1.voltageLevel.nominalV > 300 }
        .forEach { l -> l.terminal1.voltageLevel.nominalV = 280 }


network.getLoad("LOAD").newExtension(LoadDetailAdder.class)
        .withVariableActivePower(100)
        .withFixedActivePower(500)
        .add()