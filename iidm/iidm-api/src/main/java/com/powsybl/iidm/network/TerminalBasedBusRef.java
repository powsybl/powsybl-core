/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.powsybl.commons.PowsyblException;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * A bus reference anchored to an eligible connected terminal.
 *
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 * @since 7.5.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeName(".TerminalBasedBusRef")
public class TerminalBasedBusRef extends AbstractBusRef {

    private final String id;
    private final ThreeSides side;
    private final TerminalNumber terminalNumber;

    /**
     * Create a reference to a single-terminal injection.
     */
    public TerminalBasedBusRef(String id) {
        this(id, null, null);
    }

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public TerminalBasedBusRef(@JsonProperty("id") String id,
                               @JsonProperty("side") ThreeSides side,
                               @JsonProperty("terminalNumber") TerminalNumber terminalNumber) {
        this.id = Objects.requireNonNull(id);
        if (side != null && terminalNumber != null) {
            throw new PowsyblException("A terminal reference cannot specify both a side and a terminal number.");
        }
        this.side = side;
        this.terminalNumber = terminalNumber;
    }

    /**
     * Select the lexicographically smallest terminal of a bus, excluding fictitious connectables and busbar sections.
     *
     * @param bus a bus-view bus exposing its connected terminals
     * @return an empty optional if the bus has no eligible connected terminal
     */
    public static Optional<TerminalBasedBusRef> of(Bus bus) {
        return of(bus, connectable -> !connectable.isFictitious() && !(connectable instanceof BusbarSection));
    }

    /**
     * Select the lexicographically smallest eligible terminal of a bus. Connectable IDs use their natural string
     * ordering; terminals of the same connectable are ordered by the declaration order of {@link ThreeSides} and then
     * {@link TerminalNumber}.
     *
     * @param bus a bus-view bus exposing its connected terminals
     * @param eligible predicate defining which connectables can be used as anchors
     * @return an empty optional if the bus has no eligible connected terminal
     */
    public static Optional<TerminalBasedBusRef> of(Bus bus, Predicate<Connectable<?>> eligible) {
        Objects.requireNonNull(bus);
        Objects.requireNonNull(eligible);
        return bus.getConnectedTerminalStream()
                .filter(terminal -> eligible.test(terminal.getConnectable()))
                .map(TerminalBasedBusRef::candidateFrom)
                .min(Candidate::compareTo)
                .map(candidate -> new TerminalBasedBusRef(candidate.id(), candidate.side(), candidate.terminalNumber()));
    }

    private static Candidate candidateFrom(Terminal terminal) {
        Connectable<?> connectable = terminal.getConnectable();
        ThreeSides side = Terminal.getConnectableSide(terminal).orElse(null);
        TerminalNumber terminalNumber = Terminal.getConnectableTerminalNumber(terminal).orElse(null);
        if (side == null && terminalNumber == null && !(connectable instanceof Injection<?>)) {
            throw new PowsyblException("Unsupported terminal reference connectable: " + connectable.getClass());
        }
        return new Candidate(connectable.getId(), side, terminalNumber);
    }

    @Override
    protected Optional<Bus> resolveByLevel(Network network, TopologyLevel level) {
        Identifiable<?> identifiable = network.getIdentifiable(id);
        if (identifiable == null) {
            return Optional.empty();
        }

        Terminal terminal;
        if (side != null) {
            if (!(identifiable instanceof Branch<?> || identifiable instanceof ThreeWindingsTransformer)) {
                throw new PowsyblException(id + " is not a branch or a three-windings transformer.");
            }
            if (identifiable instanceof Branch<?> && side == ThreeSides.THREE) {
                throw new PowsyblException("A branch terminal reference cannot use side THREE: " + id);
            }
            terminal = Terminal.getTerminal(identifiable, side);
        } else if (terminalNumber != null) {
            if (!(identifiable instanceof AcDcConverter<?>)) {
                throw new PowsyblException(id + " is not an AC/DC converter.");
            }
            terminal = Terminal.getTerminal(identifiable, terminalNumber);
        } else {
            if (!(identifiable instanceof Injection<?>)) {
                throw new PowsyblException(id + " is not an injection.");
            }
            terminal = Terminal.getTerminal(identifiable, ThreeSides.ONE);
        }
        return chooseBusByLevel(terminal, level);
    }

    public String getId() {
        return id;
    }

    public ThreeSides getSide() {
        return side;
    }

    public TerminalNumber getTerminalNumber() {
        return terminalNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TerminalBasedBusRef other)) {
            return false;
        }
        return id.equals(other.id) && side == other.side && terminalNumber == other.terminalNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, side, terminalNumber);
    }

    private record Candidate(String id, ThreeSides side, TerminalNumber terminalNumber) {

        private Candidate {
            Objects.requireNonNull(id);
            if (side != null && terminalNumber != null) {
                throw new PowsyblException("A terminal reference cannot specify both a side and a terminal number.");
            }
        }

        private int discriminatorOrder() {
            if (side != null) {
                return side.ordinal() + 1;
            }
            if (terminalNumber != null) {
                return ThreeSides.values().length + terminalNumber.ordinal() + 1;
            }
            return 0;
        }

        private int compareTo(Candidate other) {
            int idComparison = id.compareTo(other.id);
            return idComparison != 0 ? idComparison : Integer.compare(discriminatorOrder(), other.discriminatorOrder());
        }
    }
}
