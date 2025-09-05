package com.powsybl.iidm.modification.tripping;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;

import java.util.Objects;
import java.util.Set;

public class ConverterTripping extends AbstractTripping {

    public ConverterTripping(String id) {
        super(id);
    }

    @Override
    public String getName() {
        return "ConverterTripping";
    }

    @Override
    public void traverse(Network network, Set<Switch> switchesToOpen, Set<Terminal> terminalsToDisconnect, Set<Terminal> traversedTerminals) {
        Objects.requireNonNull(network);

        AcDcConverter<VoltageSourceConverter> converter = network.getVoltageSourceConverter(id);
        if (converter == null) {
            throw new PowsyblException("Converter '" + id + "' not found");
        }

        for (Terminal t : converter.getTerminals()) {
            TrippingTopologyTraverser.traverse(t, switchesToOpen, terminalsToDisconnect, traversedTerminals);
        }
    }

    @Override
    public void traverseDc(Network network, Set<DcTerminal> terminalsToDisconnect, Set<DcTerminal> traversedDcTerminals) {
        Objects.requireNonNull(network);
        AcDcConverter<VoltageSourceConverter> converter = network.getVoltageSourceConverter(id);
        if (converter == null) {
            throw new PowsyblException("Converter '" + id + "' not found");
        }

        for (DcTerminal t : converter.getDcTerminals()) {
            TrippingTopologyTraverser.traverse(t, terminalsToDisconnect, traversedDcTerminals);
        }
    }
}
