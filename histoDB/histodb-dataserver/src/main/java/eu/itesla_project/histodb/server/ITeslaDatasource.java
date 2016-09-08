/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.histodb.server;

import be.pepite.dataserver.api.*;
import be.pepite.dataserver.datastores.mongodb.MongoDataSource;
import eu.itesla_project.histodb.server.attributes.CurrentPowerRatio;
import eu.itesla_project.histodb.server.attributes.StrictlyNegative;
import eu.itesla_project.histodb.server.attributes.StrictlyPositive;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import eu.itesla_project.iidm.network.*;
import eu.itesla_project.modules.histo.HistoDbAttributeId;
import eu.itesla_project.modules.histo.IIDM2DB;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created with IntelliJ IDEA.
 * User: pduchesne
 * Date: 4/06/13
 * Time: 16:18
 * To change this template use File | Settings | File Templates.
 */
public class ITeslaDatasource
    extends MongoDataSource
{
    static Logger log = LoggerFactory.getLogger(ITeslaDatasource.class);

    private final AtomicReference<Network> latestNetwork = new AtomicReference<>();

    public ITeslaDatasource(DBCollection db) {
        super(db);
    }

    @Override
    public void ensureIndexes() {
        log.info("Ensuring mongo indexes are up to date...");
        mongoDb.ensureIndex("datetime");
        mongoDb.ensureIndex("daytime");
        mongoDb.ensureIndex("cimName");
        mongoDb.ensureIndex(new BasicDBObject().append("horizon", 1).append("datetime", 1));
        mongoDb.ensureIndex(new BasicDBObject().append("forecastTime", 1).append("datetime", 1));
        mongoDb.ensureIndex(new BasicDBObject().append("horizon", 1).append("forecastTime", 1).append("datetime", 1));
        log.info("... indexing done.");
    }

    @Override
    public void initInternal() {
        super.initInternal();

        if (getConfig().getConfigValues().has("latestCIM")) {
            try {
                String cimPath = getConfig().getConfigValues().getString("latestCIM");
                setLatestNetwork(CimHistoImporter.readCim(new File(cimPath)));
                log.info("Latest CIM read from config: "+cimPath);
            } catch (Exception e) {
                log.warn("Failed to read cim path from JSON config", e);
            }

        }
    }

    @Override
    public void fillMetadata(Metadata md) {
        super.fillMetadata(md);

        if (latestNetwork.get() != null) {
            for (Substation ss: latestNetwork.get().getSubstations()) {
                getMetadata().getRegions().addAll(ss.getGeographicalTags());
                getMetadata().getCountries().add(ss.getCountry().toString());
            }

            String[] topoCols = new String[latestNetwork.get().getVoltageLevelCount()];
            String[] vlIds = new String[latestNetwork.get().getVoltageLevelCount()];
            int i = 0;

            for (VoltageLevel vl: latestNetwork.get().getVoltageLevels()) {
                vlIds[i] = vl.getId();
                topoCols[i++] = vl.getId()+"_TOPO";
            }

            Iterator<Collection> toposIt = getData(null, 0, -1,ColumnDescriptor.getDescriptorsForNames(topoCols)).getRowIterator();
            while (toposIt.hasNext()) {
                String[] topoValues = (String[])toposIt.next().toArray(new String[] {});
                for (int n=0;n<topoValues.length;n++)
                    try {
                        // TODO should check that returned hash matches the one from DB ?
                        JSONArray topoArray = new JSONArray(topoValues[n]);
                        String topoHash = IIDM2DB.computeTopoHash(topoArray.toString());
                        getMetadata().addTopology(vlIds[n], topoHash, topoArray);
                    } catch (Exception e) {
                        log.warn("Failed to read topology from DB ("+topoCols[n]+":"+n+") : "+topoValues[n]);
                    }
            }
        }
    }

    @Override
    protected DBObject findRepresentativeRecord() {
        // in parent the metadata are filled with a random row
        // but on the other hand the iTesla latestNetwork is used to retrieve potential attributes
        // --> possible mismatch ; must use latestNetwork row here if available

        DBObject result = null;
        if (latestNetwork.get() != null) {
            LinkedHashMap<HistoDbAttributeId,Object> values = IIDM2DB.extractCimValues(latestNetwork.get(), new IIDM2DB.Config(null, true)).getSingleValueMap();
            LinkedHashMap<String,Object> values2 = new LinkedHashMap<String,Object>(values.size());
            for (Map.Entry<HistoDbAttributeId,Object> entry : values.entrySet()) {
                values2.put(escapeColName(entry.getKey().toString()), entry.getValue());
            }
            result = new BasicDBObject(values2);
        }
        else result = super.findRepresentativeRecord();

        return result;

    }

    public void setLatestCim(String path) {
        getConfig().setConfigValue("latestCIM",path);
    }

    public String getLatestCim() {
        return getConfig().getConfigValues().optString("latestCIM", null);
    }

    @Override
    protected Metadata createMetadataInstance() {
        return new ITeslaMetadata();
    }

    @Override
    public ITeslaMetadata getMetadata() {
        return (ITeslaMetadata)super.getMetadata();
    }

    public Network getLatestNetwork() {
        return latestNetwork.get();
    }

    public void setLatestNetwork(Network latestNetwork) {
        this.latestNetwork.set(latestNetwork);

        if (getMetadata() != null && getMetadata().getRegions().size() == 0) {
            for (Substation ss: latestNetwork.getSubstations()) {
                getMetadata().getRegions().addAll(ss.getGeographicalTags());
                getMetadata().getCountries().add(ss.getCountry().toString());
            }
        }
    }

    @Override
    protected DBCursor getDataCursor(Map<String, ?> ranges, int start, int count, ColumnDescriptor[] columnDescriptors, Map<String, INDEXTYPE> indexTypes) {
        DBCursor cursor = super.getDataCursor(ranges, start, count, columnDescriptors, indexTypes);

        //TODO use indexes on filters ; check http://emptysqua.re/blog/optimizing-mongodb-compound-indexes/
        // example : {horizon:1,datetime:1,CHOO17GROUP_1_NGU_SM_P:1}

        return cursor.sort(new BasicDBObject("datetime", 1));
    }

    //TODO : use this method to retrieve equipments first, then feed into findAttributes
    public String[] findEquipments(Collection<String> eqTypes, Collection<String> regions, Collection<String> countries) {

        //TODO querying would be more efficient using direct SPARQL queries on the RDF graph

        List<String> equips = new ArrayList<String>();

        latestNetwork.get().getLine("test");

        if (eqTypes == null || eqTypes.contains("gen")) {
            for (Generator g: latestNetwork.get().getGenerators()) {
                if (regions != null && Collections.disjoint(regions, g.getTerminal().getVoltageLevel().getSubstation().getGeographicalTags())) continue;
                if (countries != null && !countries.contains(g.getTerminal().getVoltageLevel().getSubstation().getCountry().toString())) continue;
                equips.add(g.getId());
            }
        }

        if (eqTypes == null || eqTypes.contains("loads")) {
            for (Load l: latestNetwork.get().getLoads()) {
                if (regions != null && Collections.disjoint(regions, l.getTerminal().getVoltageLevel().getSubstation().getGeographicalTags())) continue;
                if (countries != null && !countries.contains(l.getTerminal().getVoltageLevel().getSubstation().getCountry().toString())) continue;
                equips.add(l.getId());
            }
        }

        if (eqTypes == null || eqTypes.contains("shunts")) {
            for (ShuntCompensator sc: latestNetwork.get().getShunts()) {
                if (regions != null && Collections.disjoint(regions, sc.getTerminal().getVoltageLevel().getSubstation().getGeographicalTags())) continue;
                if (countries != null && !countries.contains(sc.getTerminal().getVoltageLevel().getSubstation().getCountry().toString())) continue;
                equips.add(sc.getId());
            }
        }

        if (eqTypes == null || eqTypes.contains("2wt")) {
            for (TwoWindingsTransformer wt2: latestNetwork.get().getTwoWindingsTransformers()) {
                if (regions != null &&
                        Collections.disjoint(regions, wt2.getTerminal1().getVoltageLevel().getSubstation().getGeographicalTags()) &&
                        Collections.disjoint(regions, wt2.getTerminal2().getVoltageLevel().getSubstation().getGeographicalTags())) continue;
                if (countries != null &&
                        !countries.contains(wt2.getTerminal1().getVoltageLevel().getSubstation().getCountry().toString()) &&
                        !countries.contains(wt2.getTerminal2().getVoltageLevel().getSubstation().getCountry().toString())) continue;
                equips.add(wt2.getId());
            }
        }

        //TODO ThreeWindingsTransformers

        if (eqTypes == null || eqTypes.contains("lines")) {
            for (Line l: latestNetwork.get().getLines()) {
                if (l.getTerminal1().getBusView().getBus() != null) {
                    if (    ((regions == null || !Collections.disjoint(regions, l.getTerminal1().getVoltageLevel().getSubstation().getGeographicalTags()))
                            && (countries == null || countries.contains(l.getTerminal1().getVoltageLevel().getSubstation().getCountry().toString())))
                         || (regions == null || !Collections.disjoint(regions, l.getTerminal2().getVoltageLevel().getSubstation().getGeographicalTags()))
                            && (countries == null || countries.contains(l.getTerminal2().getVoltageLevel().getSubstation().getCountry().toString()))) {
                        equips.add(l.getId());
                    }
                }
            }
        }

        if (eqTypes == null || eqTypes.contains("station")) {
            for (VoltageLevel vl: latestNetwork.get().getVoltageLevels()) {
                if (regions != null && Collections.disjoint(regions, vl.getSubstation().getGeographicalTags())) continue;
                if (countries != null && !countries.contains(vl.getSubstation().getCountry().toString())) continue;
                equips.add(vl.getId());
            }
        }

        return equips.toArray(new String[] {});
    }

    public ColumnDescriptor[] findAttributes(Collection<String> eqTypes, Collection<String> powerTypes, Collection<String> measureTypes, Collection<String> regions, Collection<String> countries, Collection<String> equipIds) {
        //TODO querying would be more efficient using direct SPARQL queries on the RDF graph

        ColumnDescriptorSet attributes = new ColumnDescriptorSet();


        if (eqTypes == null || eqTypes.contains("dangling")) {
            for (DanglingLine dl: latestNetwork.get().getDanglingLines()) {
                if (equipIds != null && !equipIds.contains(dl.getId())) continue;
                if (regions != null && Collections.disjoint(regions, dl.getTerminal().getVoltageLevel().getSubstation().getGeographicalTags())) continue;
                if (countries != null && !countries.contains(dl.getTerminal().getVoltageLevel().getSubstation().getCountry().toString())) continue;
                if (measureTypes == null || measureTypes.contains("P")) attributes.addNamedDescriptor(dl.getId() + "_P");
                if (measureTypes == null || measureTypes.contains("Q")) attributes.addNamedDescriptor(dl.getId() + "_Q");
                if (measureTypes == null || measureTypes.contains("I")) attributes.addNamedDescriptor(dl.getId() + "_I");
                if (measureTypes == null || measureTypes.contains("V")) attributes.addNamedDescriptor(dl.getId() + "_V");
                if (measureTypes == null || measureTypes.contains("P0")) attributes.addNamedDescriptor(dl.getId() + "_P0");
                if (measureTypes == null || measureTypes.contains("Q0")) attributes.addNamedDescriptor(dl.getId() + "_Q0");
                if (measureTypes == null || measureTypes.contains("STATUS")) attributes.addNamedDescriptor(dl.getId() + "_STATUS");
            }
        }

        if (eqTypes == null || eqTypes.contains("gen")) {
            for (Generator g: latestNetwork.get().getGenerators()) {
                if (equipIds != null && !equipIds.contains(g.getId())) continue;
                if (regions != null && Collections.disjoint(regions, g.getTerminal().getVoltageLevel().getSubstation().getGeographicalTags())) continue;
                if (countries != null && !countries.contains(g.getTerminal().getVoltageLevel().getSubstation().getCountry().toString())) continue;
                if (powerTypes != null && !powerTypes.contains(g.getEnergySource().name())) continue;
                if (measureTypes == null || measureTypes.contains("P")) attributes.addNamedDescriptor(g.getId() + "_P");
                if (measureTypes == null || measureTypes.contains("Q")) attributes.addNamedDescriptor(g.getId() + "_Q");
                if (measureTypes == null || measureTypes.contains("V")) attributes.addNamedDescriptor(g.getId() + "_V");
                if (measureTypes == null || measureTypes.contains("I")) attributes.addNamedDescriptor(g.getId() + "_I");
                if (measureTypes == null || measureTypes.contains("STATUS")) attributes.addNamedDescriptor(g.getId() + "_STATUS");
                if (measureTypes == null || measureTypes.contains("PP")) attributes.add(new StrictlyPositive(g.getId()+ "_P"));
                if (measureTypes == null || measureTypes.contains("PN")) attributes.add(new StrictlyNegative(g.getId()+ "_P"));
                if (measureTypes == null || measureTypes.contains("QP")) attributes.add(new StrictlyPositive(g.getId()+ "_Q"));
                if (measureTypes == null || measureTypes.contains("QN")) attributes.add(new StrictlyNegative(g.getId()+ "_Q"));
            }
        }

        if (eqTypes == null || eqTypes.contains("loads")) {
            for (Load l: latestNetwork.get().getLoads()) {
                if (equipIds != null && !equipIds.contains(l.getId())) continue;
                if (regions != null && Collections.disjoint(regions, l.getTerminal().getVoltageLevel().getSubstation().getGeographicalTags())) continue;
                if (countries != null && !countries.contains(l.getTerminal().getVoltageLevel().getSubstation().getCountry().toString())) continue;
                if (measureTypes == null || measureTypes.contains("P")) attributes.addNamedDescriptor(l.getId() + "_P");
                if (measureTypes == null || measureTypes.contains("Q")) attributes.addNamedDescriptor(l.getId() + "_Q");
                if (measureTypes == null || measureTypes.contains("V")) attributes.addNamedDescriptor(l.getId() + "_V");
                if (measureTypes == null || measureTypes.contains("I")) attributes.addNamedDescriptor(l.getId() + "_I");
                if (measureTypes == null || measureTypes.contains("STATUS")) attributes.addNamedDescriptor(l.getId() + "_STATUS");
            }
        }

        if (eqTypes == null || eqTypes.contains("shunts")) {
            for (ShuntCompensator sc: latestNetwork.get().getShunts()) {
                if (equipIds != null && !equipIds.contains(sc.getId())) continue;
                if (regions != null && Collections.disjoint(regions, sc.getTerminal().getVoltageLevel().getSubstation().getGeographicalTags())) continue;
                if (countries != null && !countries.contains(sc.getTerminal().getVoltageLevel().getSubstation().getCountry().toString())) continue;
                if (measureTypes == null || measureTypes.contains("Q")) attributes.addNamedDescriptor(sc.getId() + "_Q");
                if (measureTypes == null || measureTypes.contains("V")) attributes.addNamedDescriptor(sc.getId() + "_V");
                if (measureTypes == null || measureTypes.contains("STATUS")) attributes.addNamedDescriptor(sc.getId() + "_STATUS");
            }
        }

        if (eqTypes == null || eqTypes.contains("2wt")) {
            for (TwoWindingsTransformer wt2: latestNetwork.get().getTwoWindingsTransformers()) {
                if (equipIds != null && !equipIds.contains(wt2.getId())) continue;
                if (regions != null &&
                    Collections.disjoint(regions, wt2.getTerminal1().getVoltageLevel().getSubstation().getGeographicalTags()) &&
                    Collections.disjoint(regions, wt2.getTerminal2().getVoltageLevel().getSubstation().getGeographicalTags())) continue;
                if (countries != null &&
                    !countries.contains(wt2.getTerminal1().getVoltageLevel().getSubstation().getCountry().toString()) &&
                    !countries.contains(wt2.getTerminal2().getVoltageLevel().getSubstation().getCountry().toString())) continue;

                String leg1Id = wt2.getId()+"__TO__"+ wt2.getTerminal1().getVoltageLevel().getId();
                String leg2Id = wt2.getId()+"__TO__"+ wt2.getTerminal2().getVoltageLevel().getId();

                if (measureTypes == null || measureTypes.contains("V")) {
                    attributes.addNamedDescriptor(leg1Id + "_V");
                    attributes.addNamedDescriptor(leg1Id + "_V");
                }
                if (measureTypes == null || measureTypes.contains("I")) {
                    attributes.addNamedDescriptor(leg1Id + "_I");
                    attributes.addNamedDescriptor(leg2Id + "_I");
                }
                if (measureTypes == null || measureTypes.contains("Q")) {
                    attributes.addNamedDescriptor(leg1Id + "_Q");
                    attributes.addNamedDescriptor(leg2Id + "_Q");
                }
                if (measureTypes == null || measureTypes.contains("P")) {
                    attributes.addNamedDescriptor(leg1Id + "_P");
                    attributes.addNamedDescriptor(leg2Id + "_P");
                }
                if (measureTypes == null || measureTypes.contains("STATUS")) {
                    attributes.addNamedDescriptor(leg1Id + "_STATUS");
                    attributes.addNamedDescriptor(leg2Id + "_STATUS");
                }
                if (measureTypes == null || measureTypes.contains("RTC")) attributes.addNamedDescriptor(wt2.getId() + "_RTC");
                if (measureTypes == null || measureTypes.contains("PTC")) attributes.addNamedDescriptor(wt2.getId() + "_PTC");
            }
        }

        if (eqTypes == null || eqTypes.contains("3wt")) {
            for (ThreeWindingsTransformer wt3: latestNetwork.get().getThreeWindingsTransformers()) {
                if (equipIds != null && !equipIds.contains(wt3.getId())) continue;
                if (regions != null &&
                        Collections.disjoint(regions, wt3.getLeg1().getTerminal().getVoltageLevel().getSubstation().getGeographicalTags()) &&
                        Collections.disjoint(regions, wt3.getLeg2().getTerminal().getVoltageLevel().getSubstation().getGeographicalTags()) &&
                        Collections.disjoint(regions, wt3.getLeg3().getTerminal().getVoltageLevel().getSubstation().getGeographicalTags())) continue;
                if (countries != null &&
                        !countries.contains(wt3.getLeg1().getTerminal().getVoltageLevel().getSubstation().getCountry().toString()) &&
                        !countries.contains(wt3.getLeg2().getTerminal().getVoltageLevel().getSubstation().getCountry().toString()) &&
                        !countries.contains(wt3.getLeg3().getTerminal().getVoltageLevel().getSubstation().getCountry().toString())) continue;

                String leg1Id = wt3.getId()+"__TO__"+ wt3.getLeg1().getTerminal().getVoltageLevel().getId();
                String leg2Id = wt3.getId()+"__TO__"+ wt3.getLeg2().getTerminal().getVoltageLevel().getId();
                String leg3Id = wt3.getId()+"__TO__"+ wt3.getLeg3().getTerminal().getVoltageLevel().getId();

                if (measureTypes == null || measureTypes.contains("V")) {
                    attributes.addNamedDescriptor(leg1Id + "_V");
                    attributes.addNamedDescriptor(leg2Id + "_V");
                    attributes.addNamedDescriptor(leg3Id + "_V");
                }
                if (measureTypes == null || measureTypes.contains("I")) {
                    attributes.addNamedDescriptor(leg1Id + "_I");
                    attributes.addNamedDescriptor(leg2Id + "_I");
                    attributes.addNamedDescriptor(leg3Id + "_I");
                }
                if (measureTypes == null || measureTypes.contains("P")) {
                    attributes.addNamedDescriptor(leg1Id + "_P");
                    attributes.addNamedDescriptor(leg2Id + "_P");
                    attributes.addNamedDescriptor(leg3Id + "_P");
                }
                if (measureTypes == null || measureTypes.contains("Q")) {
                    attributes.addNamedDescriptor(leg1Id + "_Q");
                    attributes.addNamedDescriptor(leg2Id + "_Q");
                    attributes.addNamedDescriptor(leg3Id + "_Q");
                }
                if (measureTypes == null || measureTypes.contains("STATUS")) {
                    attributes.addNamedDescriptor(leg1Id + "_STATUS");
                    attributes.addNamedDescriptor(leg2Id + "_STATUS");
                    attributes.addNamedDescriptor(leg3Id + "_STATUS");
                }
                if (measureTypes == null || measureTypes.contains("RTC")) {
                    attributes.addNamedDescriptor(leg2Id + "_RTC");
                    attributes.addNamedDescriptor(leg3Id + "_RTC");
                }
            }
        }

        if (eqTypes == null || eqTypes.contains("lines")) {
            for (Line l: latestNetwork.get().getLines()) {
                if (equipIds != null && !equipIds.contains(l.getId())) continue;
                if (l.getTerminal1().getBusView().getBus() != null) {
                    if (    (regions == null || !Collections.disjoint(regions, l.getTerminal1().getVoltageLevel().getSubstation().getGeographicalTags()))
                         && (countries == null || countries.contains(l.getTerminal1().getVoltageLevel().getSubstation().getCountry().toString()))) {
                        String terminalId = l.getId()+"__TO__"+l.getTerminal1().getVoltageLevel().getId();
                        if (measureTypes == null || measureTypes.contains("P")) attributes.addNamedDescriptor(terminalId + "_P");
                        if (measureTypes == null || measureTypes.contains("Q")) attributes.addNamedDescriptor(terminalId + "_Q");
                        if (measureTypes == null || measureTypes.contains("V")) attributes.addNamedDescriptor(terminalId + "_V");
                        if (measureTypes == null || measureTypes.contains("I")) attributes.addNamedDescriptor(terminalId + "_I");
                        if (measureTypes == null || measureTypes.contains("STATUS")) attributes.addNamedDescriptor(terminalId + "_STATUS");
                        if (measureTypes == null || measureTypes.contains("IP")) attributes.add(new CurrentPowerRatio(terminalId));
                    }
                }

                if (l.getTerminal2().getBusView().getBus() != null) {
                    if (   (regions == null || !Collections.disjoint(regions, l.getTerminal2().getVoltageLevel().getSubstation().getGeographicalTags()))
                        && (countries == null || countries.contains(l.getTerminal2().getVoltageLevel().getSubstation().getCountry().toString()))) {
                        String terminalId = l.getId()+"__TO__"+l.getTerminal2().getVoltageLevel().getId();
                        if (measureTypes == null || measureTypes.contains("P")) attributes.addNamedDescriptor(terminalId + "_P");
                        if (measureTypes == null || measureTypes.contains("Q")) attributes.addNamedDescriptor(terminalId + "_Q");
                        if (measureTypes == null || measureTypes.contains("V")) attributes.addNamedDescriptor(terminalId + "_V");
                        if (measureTypes == null || measureTypes.contains("I")) attributes.addNamedDescriptor(terminalId + "_I");
                        if (measureTypes == null || measureTypes.contains("STATUS")) attributes.addNamedDescriptor(terminalId + "_STATUS");
                        if (measureTypes == null || measureTypes.contains("IP")) attributes.add(new CurrentPowerRatio(terminalId));
                    }
                }
            }
        }

        if (eqTypes == null || eqTypes.contains("stations")) {
            for (VoltageLevel vl: latestNetwork.get().getVoltageLevels()) {
                if (equipIds != null && !equipIds.contains(vl.getId())) continue;
                if (regions != null && Collections.disjoint(regions, vl.getSubstation().getGeographicalTags())) continue;
                if (countries != null && !countries.contains(vl.getSubstation().getCountry().toString())) continue;
                if (measureTypes == null || measureTypes.contains("TOPO")) attributes.addNamedDescriptor(vl.getId() + "_TOPO");
                if (measureTypes == null || measureTypes.contains("T")) attributes.addNamedDescriptor(vl.getId() + "_TOPOHASH");
                if (measureTypes == null || measureTypes.contains("V")) attributes.addNamedDescriptor(vl.getId() + "_V");
                if (measureTypes == null || measureTypes.contains("PGEN")) attributes.addNamedDescriptor(vl.getId() + "_PGEN");
                if (measureTypes == null || measureTypes.contains("QGEN")) attributes.addNamedDescriptor(vl.getId() + "_QGEN");
                if (measureTypes == null || measureTypes.contains("PLOAD")) attributes.addNamedDescriptor(vl.getId() + "_PLOAD");
                if (measureTypes == null || measureTypes.contains("QLOAD")) attributes.addNamedDescriptor(vl.getId() + "_QLOAD");
                if (measureTypes == null || measureTypes.contains("QSHUNT")) attributes.addNamedDescriptor(vl.getId() + "_QSHUNT");
            }
        }

        if (eqTypes == null || eqTypes.contains("sim")) {
            for (String colName: getMetadata().getColumnNames()) {
                if (colName.startsWith("SIM_")) attributes.addNamedDescriptor(colName);
                if (colName.startsWith("TASK_")) attributes.addNamedDescriptor(colName);
            }
        }


        List<ColumnDescriptor> orderedAttrs = new ArrayList();
        orderedAttrs.add(new ColumnDescriptor("datetime"));
        orderedAttrs.add(new ColumnDescriptor("forecastTime"));
        orderedAttrs.add(new ColumnDescriptor("horizon"));
        orderedAttrs.addAll(attributes);

        return orderedAttrs.toArray(new ColumnDescriptor[] {});
    }
}
