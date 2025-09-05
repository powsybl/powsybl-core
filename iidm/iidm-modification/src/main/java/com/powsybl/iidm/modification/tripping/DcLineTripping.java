package com.powsybl.iidm.modification.tripping;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;

import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;

public class DcLineTripping extends AbstractTripping {

    protected final String dcNodeId;

    private final BiFunction<Network, String, DcLine> supplier;

    public DcLineTripping(String dcLineId) {
        this(dcLineId, null);
    }

    public DcLineTripping(String dcLineId, String dcNodeId) {
        this(dcLineId, dcNodeId, Network::getDcLine);
    }

    protected DcLineTripping(String dcLineId, String dcNodeId, BiFunction<Network, String, DcLine> supplier) {
        super(dcLineId);
        this.dcNodeId = dcNodeId;
        this.supplier = supplier;
    }

    @Override
    public String getName() {
        return "DcLineTripping";
    }

    protected String getDcNodeId() {
        return dcNodeId;
    }

    @Override
    public void traverse(Network network, Set<Switch> switchesToOpen, Set<Terminal> terminalsToDisconnect, Set<Terminal> traversedTerminals) {
    }

    @Override
    public void traverseDc(Network network, Set<DcTerminal> terminalsToDisconnect, Set<DcTerminal> traversedTerminals) {
        Objects.requireNonNull(network);
        DcLine dcLine = supplier.apply(network, id);
        if (dcLine == null) {
            throw createNotFoundException();
        }
        traverseDoubleSidedEquipment(dcNodeId, dcLine.getDcTerminal1(), dcLine.getDcTerminal2(), terminalsToDisconnect, traversedTerminals, dcLine.getType().name());
    }

    protected PowsyblException createNotFoundException() {
        return new PowsyblException("DcLine '" + id + "' not found");
    }

    protected PowsyblException createNotConnectedException() {
        return new PowsyblException("DcNode '" + dcNodeId + "' not connected to dcLine '" + id + "'");
    }
}
