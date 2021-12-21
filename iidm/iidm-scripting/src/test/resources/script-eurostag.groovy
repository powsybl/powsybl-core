network.getLineStream()
        .filter { l -> l.terminal1.voltageLevel.nominalV > 300 }
        .forEach { l -> l.terminal1.voltageLevel.nominalV = 280 }


network.getLoad("LOAD").newDetail()
        .withVariableActivePower(100)
        .withFixedActivePower(500)
        .add()

network.getGenerator("GEN").newEntsoeCategory()
        .withCode(4)
        .add()

for (b in network.getBusView().getBuses()) {
    if (b.isInMainConnectedComponent()) {
        b.angle = 0
    }
}

for (generator in network.generators) {
    if (!generator.terminal.isConnected()) {
        generator.terminal.connect()
    }
}