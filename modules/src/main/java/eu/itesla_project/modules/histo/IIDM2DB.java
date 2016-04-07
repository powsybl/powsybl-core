/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.histo;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import eu.itesla_project.iidm.network.*;
import eu.itesla_project.iidm.network.util.SV;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 *
 * @author Philippe Duchesne <ph.duchesne@pepite.be>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class IIDM2DB {

    private static Logger log = LoggerFactory.getLogger(IIDM2DB.class);

    public static class HorizonKey {
        public final int forecastDistance;
        public final Horizon horizon;

        public HorizonKey(int forecastDistance, Horizon horizon) {
            this.forecastDistance = forecastDistance;
            this.horizon = horizon;
        }

        @Override
        public int hashCode() {
            return toString().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null && obj instanceof HorizonKey && obj.toString().equals(this.toString());
        }

        @Override
        public String toString() {
            return (horizon.toString()+forecastDistance);
        }
    }

    public static class CimValuesMap extends HashMap<HorizonKey, LinkedHashMap<HistoDbAttributeId, Object>> {

        Map<String, Map<String, JSONArray>> toposPerSubstation = new HashMap();

        LinkedHashMap<HistoDbAttributeId,Object> getValueMap(HorizonKey key) {

            LinkedHashMap<HistoDbAttributeId,Object> valueMap = this.get(key);

            if (valueMap == null) this.put(key, valueMap = new LinkedHashMap<HistoDbAttributeId, Object>());

            return valueMap;
        }

        public boolean hasForecastValues() {
            return keySet().size()>1 || (keySet().size()==1 && keySet().iterator().next().forecastDistance>0);
        }

        public LinkedHashMap<HistoDbAttributeId,Object> getSingleValueMap() {
            if (this.size() == 0) throw new IllegalStateException("This CimValuesMap has no horizons");
            if (this.size() > 1) throw new IllegalStateException("This CimValuesMap has several horizons " + this.keySet());

            return this.values().iterator().next();
        }

        void addTopology(String substationId, String topoHash, JSONArray topo) {
            Map<String, JSONArray> topos = toposPerSubstation.get(substationId);

            if (topos == null) toposPerSubstation.put(substationId, topos = new HashMap());

            topos.put(topoHash, topo);
        }

        public Map<String, Map<String, JSONArray>> getToposPerSubstation() {
            return toposPerSubstation;
        }
    }

    public static JSONArray toTopoSet(VoltageLevel vl) throws JSONException {
        SortedMap<String, JSONArray> topoList = new TreeMap<>();

        Multimap<String, String> topoTmp = HashMultimap.create();

        vl.visitEquipments(new TerminalTopologyVisitor() {
            @Override
            public void visitTerminal(Terminal t) {
                Connectable c = t.getConnectable();
                Bus b = t.getBusView().getBus();
                if (b == null) {
                    if (c.getType() == ConnectableType.LOAD
                            || c.getType() == ConnectableType.GENERATOR
                            || c.getType() == ConnectableType.SHUNT_COMPENSATOR) {
                        // add the injection in the topo set even if not connected but just connectable to this bus
                        // see WP4.2 data mining topology spec for more detailed information
                        b = t.getBusView().getConnectableBus();
                    }
                }
                if (b != null) {
                    topoTmp.put(b.getId(), c.getId());
                } else {
                    // connect the equipment to its own bus
                    topoTmp.put(c.getId() + "FICTIVE_BUS", c.getId());
                }
            }
        });

        for (Map.Entry<String, Collection<String>> entry : topoTmp.asMap().entrySet()) {
            SortedSet<String> topoContent = new TreeSet<>(entry.getValue());
            JSONArray topoArray = new JSONArray(topoContent.toArray(new String[]{}));
            topoList.put(topoArray.toString(), topoArray);
        }

        return new JSONArray(topoList.values().toArray(new JSONArray[]{}));
    }

    public static class Config {
        private final String cimName;
        private final boolean extractTemporalFields;
        private final boolean replaceMissingValues;
        private final Set<Country> countryFilter;
        private final int minBaseVoltageFilter;

        public Config(String cimName, boolean extractTemporalFields) {
            this(cimName, extractTemporalFields, false);
        }

        public Config(String cimName, boolean extractTemporalFields, boolean replaceMissingValues) {
            this(cimName, extractTemporalFields, replaceMissingValues, null);
        }

        public Config(String cimName, boolean extractTemporalFields, boolean replaceMissingValues, Set<Country> countryFilter) {
            this(cimName, extractTemporalFields, replaceMissingValues, countryFilter, 0);
        }

        public Config(String cimName, boolean extractTemporalFields, boolean replaceMissingValues, Set<Country> countryFilter, int minBaseVoltageFilter) {
            this.cimName = cimName;
            this.extractTemporalFields = extractTemporalFields;
            this.replaceMissingValues = replaceMissingValues;
            this.countryFilter = countryFilter;
            this.minBaseVoltageFilter = minBaseVoltageFilter;
        }

        public String getCimName() {
            return cimName;
        }

        public boolean isExtractTemporalFields() {
            return extractTemporalFields;
        }

        public boolean isReplaceMissingValues() {
            return replaceMissingValues;
        }

        public Set<Country> getCountryFilter() {
            return countryFilter;
        }

        public int getMinBaseVoltageFilter() {
            return minBaseVoltageFilter;
        }
    }

    private static class TerminalContext {
        float p = Float.NaN;
        float q = Float.NaN;
        float v = Float.NaN;
        float i = Float.NaN;

        private void update(Terminal t) {
            if (t.getBusView().getBus() != null) {
                p = t.getP();
                q = t.getQ();
                v = t.getBusView().getBus().getV();
                i = t.getI();
            }
        }

        private static TerminalContext create(Terminal t) {
            TerminalContext context = new TerminalContext();
            context.update(t);
            return context;
        }
    }

    public static CimValuesMap extractCimValues(Network n, Config config) {

        CimValuesMap valuesMap = new CimValuesMap();

        for (Substation ss : n.getSubstations()) {

            if (config.getCountryFilter() != null && !config.getCountryFilter().contains(ss.getCountry())) {
                continue;
            }

            for (VoltageLevel vl : ss.getVoltageLevels()) {

                if (vl.getNominalV() < config.getMinBaseVoltageFilter()) {
                    continue;
                }

                final LinkedHashMap<HistoDbAttributeId, Object> valueMap = valuesMap.getValueMap(new HorizonKey(vl.getForecastDistance(), vl.getHorizon()));

                if (config.getCimName() != null && !valueMap.containsKey(HistoDbMetaAttributeId.cimName)) valueMap.put(HistoDbMetaAttributeId.cimName, config.getCimName());

                if (config.isExtractTemporalFields()) {
                    if (!valueMap.containsKey(HistoDbMetaAttributeId.datetime)) valueMap.put(HistoDbMetaAttributeId.datetime, n.getDate().toDate());
                    if (!valueMap.containsKey(HistoDbMetaAttributeId.daytime)) valueMap.put(HistoDbMetaAttributeId.daytime, n.getDate().getMillisOfDay());
                    if (!valueMap.containsKey(HistoDbMetaAttributeId.month)) valueMap.put(HistoDbMetaAttributeId.month, n.getDate().getMonthOfYear());
                    if (!valueMap.containsKey(HistoDbMetaAttributeId.forecastTime)) valueMap.put(HistoDbMetaAttributeId.forecastTime, vl.getForecastDistance());
                    if (!valueMap.containsKey(HistoDbMetaAttributeId.horizon)) valueMap.put(HistoDbMetaAttributeId.horizon, vl.getHorizon().toString());
                }

                vl.visitEquipments(new AbstractTopologyVisitor() {

                    private void visitInjection(SingleTerminalConnectable inj) {
                        visitInjection(inj, new TerminalContext());
                    }

                    private void visitInjection(SingleTerminalConnectable inj, TerminalContext context) {
                        Terminal t = inj.getTerminal();
                        context.update(t);

                        if (config.isReplaceMissingValues()) {
                            if (Float.isNaN(context.p)) {
                                context.p = 0f;
                            }
                            if (Float.isNaN(context.q)) {
                                context.q = 0f;
                            }
                            if (Float.isNaN(context.v)) {
                                // use connectable bus voltage, better than nothing...
                                context.v = t.getBusBreakerView().getConnectableBus().getV();
                            }
                            if (Float.isNaN(context.v)) {
                                context.v = 0f; // TODO is there a better value?
                            }
                            if (Float.isNaN(context.i)) {
                                context.i = 0f;
                            }
                        }
                        valueMap.put(new HistoDbNetworkAttributeId(inj.getId(), HistoDbAttr.P), context.p);
                        valueMap.put(new HistoDbNetworkAttributeId(inj.getId(), HistoDbAttr.Q), context.q);
                        valueMap.put(new HistoDbNetworkAttributeId(inj.getId(), HistoDbAttr.V), context.v);
                        valueMap.put(new HistoDbNetworkAttributeId(inj.getId(), HistoDbAttr.I), context.i);
                    }

                    private void visitBranch(TwoTerminalsConnectable branch, TwoTerminalsConnectable.Side side, float r, float x, float g1, float b1, float g2, float b2, float ratio) {
                        Terminal t = side == TwoTerminalsConnectable.Side.ONE ? branch.getTerminal1() : branch.getTerminal2();

                        TerminalContext context = TerminalContext.create(t);

                        if (config.isReplaceMissingValues()) {
                            if (Float.isNaN(context.p)) {
                                context.p = 0f;
                            }
                            if (Float.isNaN(context.q)) {
                                context.q = 0f;
                            }
                            if (Float.isNaN(context.v)) {
                                Terminal otherT = t == branch.getTerminal1() ? branch.getTerminal2() : branch.getTerminal1();
                                Bus otherBus = otherT.getBusView().getBus();
                                if (otherBus != null && !Float.isNaN(otherBus.getV())) {
                                    // compute the voltage from the other side physical values
                                    // TODO approx we do not consider voltage drop due to branch impedance
                                    if (t == branch.getTerminal1()) {
                                        // we are on side 1 disconnected and side 2 is connected
                                        context.v = otherBus.getV() / ratio;
                                    } else if (t == branch.getTerminal2()) {
                                        // we are on side 2 disconnected and side 1 is connected
                                        context.v = otherBus.getV() * ratio;
                                    } else {
                                        throw new AssertionError();
                                    }
                                } else {
                                    // use connectable bus voltage, better than nothing...
                                    context.v = t.getBusBreakerView().getConnectableBus().getV();
                                }
                            }
                            if (Float.isNaN(context.v)) {
                                context.v = 0;  // TODO is there a better value?
                            }
                            if (Float.isNaN(context.i)) {
                                context.i = 0;
                            }
                        }
                        valueMap.put(new HistoDbNetworkAttributeId(branch.getId(), t.getVoltageLevel().getId(), HistoDbAttr.P), context.p);
                        valueMap.put(new HistoDbNetworkAttributeId(branch.getId(), t.getVoltageLevel().getId(), HistoDbAttr.Q), context.q);
                        valueMap.put(new HistoDbNetworkAttributeId(branch.getId(), t.getVoltageLevel().getId(), HistoDbAttr.V), context.v);
                        valueMap.put(new HistoDbNetworkAttributeId(branch.getId(), t.getVoltageLevel().getId(), HistoDbAttr.I), context.i);
                    }

                    @Override
                    public void visitGenerator(Generator g) {
                        TerminalContext context = new TerminalContext();
                        visitInjection(g, context);
                        // reactive limit
                        float qmax = g.getReactiveLimits().getMaxQ(context.p);
                        valueMap.put(new HistoDbNetworkAttributeId(g.getId(), HistoDbAttr.QR), Math.abs(qmax - context.q));
                    }

                    @Override
                    public void visitLoad(Load l) {
                        if (l.getLoadType() != LoadType.FICTITIOUS) {
                            visitInjection(l);
                        }
                    }

                    @Override
                    public void visitShuntCompensator(ShuntCompensator sc) {
                        visitInjection(sc);
                    }

                    @Override
                    public void visitDanglingLine(DanglingLine dl) {
                        visitInjection(dl);
                        valueMap.put(new HistoDbNetworkAttributeId(dl.getId(), HistoDbAttr.P0), dl.getP0());
                        valueMap.put(new HistoDbNetworkAttributeId(dl.getId(), HistoDbAttr.Q0), dl.getQ0());
                    }

                    @Override
                    public void visitLine(Line l, Line.Side side) {
                        visitBranch(l, side, l.getR(), l.getX(), l.getG1(), l.getB1(), l.getG2(), l.getB2(), 1);
                    }

                    @Override
                    public void visitTwoWindingsTransformer(TwoWindingsTransformer twt, TwoWindingsTransformer.Side side) {
                        visitBranch(twt, side, twt.getR(), twt.getX(), twt.getG(), twt.getB(), 0, 0, SV.getRatio(twt));
                    }

                    @Override
                    public void visitThreeWindingsTransformer(ThreeWindingsTransformer twt, ThreeWindingsTransformer.Side side) {
                        Terminal t;
                        switch (side) {
                            case ONE:
                                t = twt.getLeg1().getTerminal();
                                break;
                            case TWO:
                                t = twt.getLeg2().getTerminal();
                                break;
                            case THREE:
                                t = twt.getLeg3().getTerminal();
                                break;
                            default:
                                throw new AssertionError();
                        }
                        TerminalContext context = TerminalContext.create(t);

                        if (config.isReplaceMissingValues()) {
                            if (Float.isNaN(context.p)) {
                                context.p = 0f;
                            }
                            if (Float.isNaN(context.q)) {
                                context.q = 0f;
                            }
                            if (Float.isNaN(context.v)) {
                                context.v = 0; // TODO is possible to find a better replacement value?
                            }
                            if (Float.isNaN(context.i)) {
                                context.i = 0f;
                            }
                        }
                        valueMap.put(new HistoDbNetworkAttributeId(twt.getId(), t.getVoltageLevel().getId(), HistoDbAttr.V), context.v);
                        valueMap.put(new HistoDbNetworkAttributeId(twt.getId(), t.getVoltageLevel().getId(), HistoDbAttr.I), context.i);
                        valueMap.put(new HistoDbNetworkAttributeId(twt.getId(), t.getVoltageLevel().getId(), HistoDbAttr.P), context.p);
                        valueMap.put(new HistoDbNetworkAttributeId(twt.getId(), t.getVoltageLevel().getId(), HistoDbAttr.Q), context.q);

                    }
                });

                // taps
                for (TwoWindingsTransformer twt : ss.getTwoWindingsTransformers()) {
                    if (twt.getPhaseTapChanger() != null) {
                        valueMap.put(new HistoDbNetworkAttributeId(twt.getId(), HistoDbAttr.PTC), twt.getPhaseTapChanger().getCurrentStepPosition());
                    }
                    if (twt.getRatioTapChanger() != null) {
                        valueMap.put(new HistoDbNetworkAttributeId(twt.getId(), HistoDbAttr.RTC), twt.getRatioTapChanger().getCurrentStepPosition());
                    }
                }
                for (ThreeWindingsTransformer twt : ss.getThreeWindingsTransformers()) {
                    valueMap.put(new HistoDbNetworkAttributeId(twt.getId(), twt.getLeg2().getTerminal().getVoltageLevel().getId(), HistoDbAttr.RTC), twt.getLeg2().getRatioTapChanger().getCurrentStepPosition());
                    valueMap.put(new HistoDbNetworkAttributeId(twt.getId(), twt.getLeg3().getTerminal().getVoltageLevel().getId(), HistoDbAttr.RTC), twt.getLeg3().getRatioTapChanger().getCurrentStepPosition());
                }

                /**
                 * Extract topologies and mean tension
                 */
                try {
                    JSONArray toposArray = toTopoSet(vl);
                    String jsonRep = toposArray.toString();

                    valueMap.put(new HistoDbNetworkAttributeId(vl.getId(), HistoDbAttr.TOPO), jsonRep);

                    String base64hash = computeTopoHash(jsonRep);
                    valuesMap.addTopology(vl.getId(), base64hash, toposArray);

                    valueMap.put(new HistoDbNetworkAttributeId(vl.getId(), HistoDbAttr.TOPOHASH), base64hash);
                } catch (JSONException e) {
                    throw new RuntimeException("Failed to gather topologies", e);
                }

                float pgen = 0;
                float qgen = 0;
                float pload = 0;
                float qload = 0;
                float qshunt = 0;

                for (Generator g: vl.getGenerators()) {
                    Terminal t = g.getTerminal();
                    if (t.getBusView().getBus() != null) {
                        if (!Float.isNaN(t.getP())) {
                            pgen += t.getP();
                        }
                        if (!Float.isNaN(t.getQ())) {
                            qgen += t.getQ();
                        }
                    }
                }
                for (Load l: vl.getLoads()) {
                    Terminal t = l.getTerminal();
                    if (t.getBusView().getBus() != null) {
                        if (!Float.isNaN(t.getP())) {
                            pload += t.getP();
                        }
                        if (!Float.isNaN(t.getQ())) {
                            qload += t.getQ();
                        }
                    }
                }
                for (ShuntCompensator s: vl.getShunts()) {
                    Terminal t = s.getTerminal();
                    if (t.getBusView().getBus() != null) {
                        if (!Float.isNaN(t.getQ())) {
                            qshunt += t.getQ();
                        }
                    }
                }

                float vSum = 0;
                int validBusCount = 0;
                int busCount = 0;
                float vMin = Float.NaN;
                float vMax = Float.NaN;
                for (Bus b : vl.getBusView().getBuses()) {
                    if (!Float.isNaN(b.getV())) {
                        vSum += b.getV();
                        validBusCount++;
                        vMin = Float.isNaN(vMin) ? b.getV() : Math.min(vMin, b.getV());
                        vMax = Float.isNaN(vMax) ? b.getV() : Math.max(vMax, b.getV());
                    }
                    busCount++;
                }
                float meanV = Float.NaN;
                if (validBusCount > 0) {
                    meanV = vSum / validBusCount;
                }
                if (config.isReplaceMissingValues()) {
                    if (Float.isNaN(meanV)) {
                        meanV = 0; // TODO is there a better value?
                    }
                    if (Float.isNaN(vMin)) {
                        vMin = 0; // TODO is there a better value?
                    }
                    if (Float.isNaN(vMax)) {
                        vMax = 0; // TODO is there a better value?
                    }
                }

                valueMap.put(new HistoDbNetworkAttributeId(vl.getId(), HistoDbAttr.PGEN), pgen);
                valueMap.put(new HistoDbNetworkAttributeId(vl.getId(), HistoDbAttr.QGEN), qgen);
                valueMap.put(new HistoDbNetworkAttributeId(vl.getId(), HistoDbAttr.PLOAD), pload);
                valueMap.put(new HistoDbNetworkAttributeId(vl.getId(), HistoDbAttr.QLOAD), qload);
                valueMap.put(new HistoDbNetworkAttributeId(vl.getId(), HistoDbAttr.QSHUNT), qshunt);
                valueMap.put(new HistoDbNetworkAttributeId(vl.getId(), HistoDbAttr.V), meanV);
                valueMap.put(new HistoDbNetworkAttributeId(vl.getId(), HistoDbAttr.VMIN), vMin);
                valueMap.put(new HistoDbNetworkAttributeId(vl.getId(), HistoDbAttr.VMAX), vMax);
                valueMap.put(new HistoDbNetworkAttributeId(vl.getId(), HistoDbAttr.BC), busCount);
            }
        }

        return valuesMap;

    }

    public static String computeTopoHash(String topoRep) {

        String base64hash;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-1").digest(topoRep.getBytes("UTF-8"));
            // create a hash from the SHA-1 first 6 bytes
            digest = Arrays.copyOfRange(digest, 0, 6);
            base64hash = Base64.encodeBase64String(digest).trim();
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to compute topology hash from"+topoRep, e);
        }

        return base64hash;
    }

}
