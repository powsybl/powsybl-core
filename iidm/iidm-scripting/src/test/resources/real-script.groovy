network.getLineStream()
        .filter { l -> l.terminal1.voltageLevel.nominalV > 300 }
        .forEach { l -> l.terminal1.voltageLevel.nominalV = 280 }
