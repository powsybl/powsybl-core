/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.immutable;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;

import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
class ImmutableCacheIndex {

    private final Map<Identifiable, AbstractImmutableIdentifiable> identifiableCacheMap = new WeakHashMap<>();

    private final Map<Component, ImmutableComponent> componentCacheMap = new WeakHashMap<>();
    private final Map<Terminal, ImmutableTerminal> terminalCacheMap = new WeakHashMap<>();

    // Although currentlimit is not shared, it could be cached in its holder
    // But for reduce complexity(two sides), it is cached here.
    private final Map<CurrentLimits, ImmutableCurrentLimits> limitsCacheMap = new WeakHashMap<>();

    private final Map<PhaseTapChanger, ImmutablePhaseTapChanger> ptcCacheMap = new WeakHashMap<>();
    private final Map<RatioTapChanger, ImmutableRatioTapChanger> rtcCacheMap = new WeakHashMap<>();

    private final Network network;

    private VariantManager cachedVariantManager;

    ImmutableCacheIndex(Network network) {
        this.network = Objects.requireNonNull(network);
    }

    Network getNetwork() {
        return network;
    }

    Substation getSubstation(Substation substation) {
        return substation == null ? null : (ImmutableSubstation) identifiableCacheMap.computeIfAbsent(substation, key -> new ImmutableSubstation((Substation) key, this));
    }

    VoltageLevel getVoltageLevel(VoltageLevel vl) {
        return vl == null ? null : (VoltageLevel) identifiableCacheMap.computeIfAbsent(vl, k -> new ImmutableVoltageLevel((VoltageLevel) k, this));
    }

    TwoWindingsTransformer getTwoWindingsTransformer(TwoWindingsTransformer twt) {
        return twt == null ? null : (TwoWindingsTransformer) identifiableCacheMap.computeIfAbsent(twt, k -> new ImmutableTwoWindingsTransformer((TwoWindingsTransformer) k, this));
    }

    ThreeWindingsTransformer getThreeWindingsTransformer(ThreeWindingsTransformer twt) {
        return twt == null ? null : (ThreeWindingsTransformer) identifiableCacheMap.computeIfAbsent(twt, k -> new ImmutableThreeWindingsTransformer((ThreeWindingsTransformer) k, this));
    }

    Bus getBus(Bus bus) {
        return bus == null ? null : (Bus) identifiableCacheMap.computeIfAbsent(bus, k -> new ImmutableBus((Bus) k, this));
    }

    Generator getGenerator(Generator generator) {
        return generator == null ? null : (Generator) identifiableCacheMap.computeIfAbsent(generator, k -> new ImmutableGenerator((Generator) k, this));
    }

    Load getLoad(Load load) {
        return load == null ? null : (Load) identifiableCacheMap.computeIfAbsent(load, k -> new ImmutableLoad((Load) k, this));
    }

    HvdcLine getHvdcLine(HvdcLine hvdcLine) {
        return hvdcLine == null ? null : (HvdcLine) identifiableCacheMap.computeIfAbsent(hvdcLine, k -> new ImmutableHvdcLine((HvdcLine) k, this));
    }

    Switch getSwitch(Switch sw) {
        return sw == null ? null : (Switch) identifiableCacheMap.computeIfAbsent(sw, k -> new ImmutableSwitch((Switch) k, this));
    }

    Component getComponent(Component component) {
        return component == null ? null : componentCacheMap.computeIfAbsent(component, k -> new ImmutableComponent(component, this));
    }

    Terminal getTerminal(Terminal terminal) {
        return terminal == null ? null : terminalCacheMap.computeIfAbsent(terminal, k -> new ImmutableTerminal(terminal, this));
    }

    CurrentLimits getCurrentLimits(CurrentLimits limits) {
        return limits == null ? null : limitsCacheMap.computeIfAbsent(limits, k -> new ImmutableCurrentLimits(limits));
    }

    PhaseTapChanger getPhaseTapChanger(PhaseTapChanger ptc) {
        return ptc == null ? null : ptcCacheMap.computeIfAbsent(ptc, k -> new ImmutablePhaseTapChanger(ptc, this));
    }

    RatioTapChanger getRatioTapChanger(RatioTapChanger rtc) {
        return rtc == null ? null : rtcCacheMap.computeIfAbsent(rtc, k -> new ImmutableRatioTapChanger(rtc, this));
    }

    Line getLine(Line line) {
        return line == null ? null : (Line) identifiableCacheMap.computeIfAbsent(line, k -> {
            if (line.isTieLine()) {
                return new ImmutableTieLine((TieLine) line, this);
            } else {
                return new ImmutableLine((Line) line, this);
            }
        });
    }

    HvdcConverterStation getHvdcConverterStation(HvdcConverterStation cs) {
        if (cs == null) {
            return null;
        }
        if (cs instanceof LccConverterStation) {
            return getLccConverterStation((LccConverterStation) cs);
        } else if (cs instanceof VscConverterStation) {
            return getVscConverterStation((VscConverterStation) cs);
        } else {
            throw new PowsyblException("Invalid type " + cs.getClass() + " to be immutablized");
        }
    }

    VscConverterStation getVscConverterStation(VscConverterStation vsc) {
        return vsc == null ? null :  (VscConverterStation) identifiableCacheMap.computeIfAbsent(vsc, k -> new ImmutableVscConverterStation(vsc, this));
    }

    LccConverterStation getLccConverterStation(LccConverterStation lcc) {
        return lcc == null ? null :  (LccConverterStation) identifiableCacheMap.computeIfAbsent(lcc, k -> new ImmutableLccConverterStation(lcc, this));
    }

    ShuntCompensator getShuntCompensator(ShuntCompensator shuntCompensator) {
        return shuntCompensator == null ? null : (ShuntCompensator) identifiableCacheMap.computeIfAbsent(shuntCompensator, k -> new ImmutableShuntCompensator(shuntCompensator, this));
    }

    StaticVarCompensator getStaticVarCompensator(StaticVarCompensator svc) {
        return svc == null ? null : (StaticVarCompensator) identifiableCacheMap.computeIfAbsent(svc, k -> new ImmutableStaticVarCompensator(svc, this));
    }

    DanglingLine getDanglingLine(DanglingLine dll) {
        return dll == null ? null : (DanglingLine) identifiableCacheMap.computeIfAbsent(dll, k -> new ImmutableDanglingLine(dll, this));
    }

    VariantManager getVariantManager(VariantManager variantManager) {
        if (cachedVariantManager == null) {
            cachedVariantManager = new ImmutableVariantManager(variantManager);
        }
        return cachedVariantManager;
    }

    Connectable getConnectable(Connectable connectable) {
        if (connectable == null) {
            return null;
        }
        switch (connectable.getType()) {
            case BUSBAR_SECTION:
                return connectable;
            case LINE:
            case TWO_WINDINGS_TRANSFORMER:
                return getBranch((Branch) connectable);
            case THREE_WINDINGS_TRANSFORMER:
                return getThreeWindingsTransformer((ThreeWindingsTransformer) connectable);
            case GENERATOR:
                return getGenerator((Generator) connectable);
            case LOAD:
                return getLoad((Load) connectable);
            case SHUNT_COMPENSATOR:
                return getShuntCompensator((ShuntCompensator) connectable);
            case DANGLING_LINE:
                return getDanglingLine((DanglingLine) connectable);
            case STATIC_VAR_COMPENSATOR:
                return getStaticVarCompensator((StaticVarCompensator) connectable);
            case HVDC_CONVERTER_STATION:
                return getHvdcConverterStation((HvdcConverterStation) connectable);
            default:
                throw new AssertionError(connectable.getType().name() + " is not valid to be immutablized to connectable");
        }
    }

    Branch getBranch(Branch b) {
        if (b == null) {
            return null;
        }
        switch (b.getType()) {
            case LINE:
                return getLine((Line) b);
            case TWO_WINDINGS_TRANSFORMER:
                return getTwoWindingsTransformer((TwoWindingsTransformer) b);
            default:
                throw new AssertionError(b.getType().name() + " is not valid to be immutablized to branch");
        }
    }

}
