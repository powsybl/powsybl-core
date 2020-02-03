/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.xml;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;

import java.util.concurrent.atomic.AtomicReference;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public final class TerminalRefXml {

    public static void writeTerminalRef(Terminal t, NetworkXmlWriterContext context, String elementName) throws XMLStreamException {
        writeTerminalRef(t, context, context.getVersion().getNamespaceURI(), elementName);
    }

    public static void writeTerminalRef(Terminal t, NetworkXmlWriterContext context, String namespace, String elementName) throws XMLStreamException {
        writeTerminalRef(t, context, namespace, elementName, context.getWriter());
    }

    public static void writeTerminalRef(Terminal t, NetworkXmlWriterContext context, String namespace, String elementName, XMLStreamWriter writer) throws XMLStreamException {
        Connectable c = t.getConnectable();
        if (!context.getFilter().test(c)) {
            throw new PowsyblException("Oups, terminal ref point to a filtered equipment " + c.getId());
        }
        writer.writeEmptyElement(namespace, elementName);
        String terminalRefId = c.getId();
        if (c instanceof BusbarSection) {
            // Found remote terminal that refers to a busbar section
            // Choose a bus for the terminal ref depending on export topology level
            if (context.getOptions().getTopologyLevel() == TopologyLevel.BUS_BREAKER) {
                terminalRefId = ((BusbarSection) c).getTerminal().getBusBreakerView().getConnectableBus().getId();
            } else if (context.getOptions().getTopologyLevel() == TopologyLevel.BUS_BRANCH) {
                terminalRefId = ((BusbarSection) c).getTerminal().getBusView().getConnectableBus().getId();
            }
        }
        writer.writeAttribute("id", context.getAnonymizer().anonymizeString(terminalRefId));
        if (c.getTerminals().size() > 1) {
            if (c instanceof Injection) {
                // nothing to do
            } else if (c instanceof Branch) {
                Branch branch = (Branch) c;
                writer.writeAttribute("side", branch.getSide(t).name());
            } else if (c instanceof ThreeWindingsTransformer) {
                ThreeWindingsTransformer twt = (ThreeWindingsTransformer) c;
                writer.writeAttribute("side", twt.getSide(t).name());
            } else {
                throw new AssertionError("Unexpected Connectable instance: " + c.getClass());
            }
        }
    }

    public static Terminal readTerminalRef(Network network, String id, String side) {
        Identifiable identifiable = network.getIdentifiable(id);
        if (identifiable instanceof Injection) {
            return ((Injection) identifiable).getTerminal();
        } else if (identifiable instanceof Branch) {
            return side.equals(Branch.Side.ONE.name()) ? ((Branch) identifiable).getTerminal1()
                    : ((Branch) identifiable).getTerminal2();
        } else if (identifiable instanceof ThreeWindingsTransformer) {
            ThreeWindingsTransformer twt = (ThreeWindingsTransformer) identifiable;
            return twt.getTerminal(ThreeWindingsTransformer.Side.valueOf(side));
        } else if (identifiable instanceof Bus) {
            Terminal t = findConnectedTerminal((Bus) identifiable);
            if (t != null) {
                return t;
            } else {
                throw new AssertionError("Could not find connected terminal for bus from terminalRef : " + identifiable.getId());
            }
        } else {
            throw new AssertionError("Unexpected Identifiable instance: " + identifiable.getClass());
        }
    }

    private static Terminal findConnectedTerminal(Bus  bus) {
        // Network API does not provide a direct way of obtaining connected terminals
        int numTerminals = bus.getConnectedTerminalCount();
        if (numTerminals <= 0) {
            return null;
        }
        final AtomicReference<Terminal> terminalRef = new AtomicReference<>();
        bus.visitConnectedEquipments(new TopologyVisitor() {
            public void visitBusbarSection(BusbarSection section) {
                terminalRef.compareAndSet(null, section.getTerminal());
            }

            public void visitLine(Line line, Branch.Side side) {
                terminalRef.compareAndSet(null, line.getTerminal(side));
            }

            public void visitTwoWindingsTransformer(TwoWindingsTransformer transformer, Branch.Side side) {
                terminalRef.compareAndSet(null, transformer.getTerminal(side));
            }

            public void visitThreeWindingsTransformer(ThreeWindingsTransformer transformer, ThreeWindingsTransformer.Side side) {
                terminalRef.compareAndSet(null, transformer.getTerminal(side));
            }

            public void visitGenerator(Generator generator) {
                terminalRef.compareAndSet(null, generator.getTerminal());
            }

            @Override
            public void visitBattery(Battery battery) {
                terminalRef.compareAndSet(null, battery.getTerminal());
            }

            public void visitLoad(Load load) {
                terminalRef.compareAndSet(null, load.getTerminal());
            }

            public void visitShuntCompensator(ShuntCompensator sc) {
                terminalRef.compareAndSet(null, sc.getTerminal());
            }

            public void visitDanglingLine(DanglingLine danglingLine) {
                terminalRef.compareAndSet(null, danglingLine.getTerminal());
            }

            public void visitStaticVarCompensator(StaticVarCompensator staticVarCompensator) {
                terminalRef.compareAndSet(null, staticVarCompensator.getTerminal());
            }

            @Override
            public void visitHvdcConverterStation(HvdcConverterStation<?> converterStation) {
                terminalRef.compareAndSet(null, converterStation.getTerminal());
            }
        });
        return terminalRef.get();
    }

    private TerminalRefXml() {
    }

}
