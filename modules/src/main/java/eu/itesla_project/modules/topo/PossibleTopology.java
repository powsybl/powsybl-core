/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.topo;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import eu.itesla_project.iidm.network.ConnectableType;
import eu.itesla_project.iidm.network.util.ShortIdDictionary;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;
import java.io.PrintStream;
import java.util.*;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PossibleTopology {

    public static final Comparator<PossibleTopology> COMPARATOR = (c1, c2) -> {
        if (c1.getProbability() == c2.getProbability()) {
            return 0;
        } else {
            return c1.getProbability() < c2.getProbability() ? -1 : 1;
        }
    };

    public static class Substation {

        @XmlAttribute(required=true)
        private final String id;

        @XmlTransient
        private final boolean fictive;

        @XmlElement(name="bus")
        private final List<Bus> buses = new ArrayList<>();

        @XmlTransient
        private final Map<String, Boolean> switches;

        public Substation(String id) {
            this(id, false);
        }

        public Substation(String id, boolean fictive) {
            this(id, fictive, new HashMap<>());
        }

        public Substation(String id, boolean fictive, Map<String, Boolean> switches) {
            this.id = id;
            this.fictive = fictive;
            this.switches = switches;
        }

        // for JAXB
        private Substation() {
            this(null);
        }

        public String getId() {
            return id;
        }

        public boolean isFictive() {
            return fictive;
        }

        public Bus findEquipmentBus(String eqId) {
            for (PossibleTopology.Bus bus : buses) {
                for (PossibleTopology.Equipment eq : bus.getEquipments()) {
                    if (eq.getId().equals(eqId)) {
                        return bus;
                    }
                }
            }
            return null;
        }

        public List<Bus> getBuses() {
            return buses;
        }

        public Map<String, Boolean> getSwitches() {
            return switches;
        }

        public void number(NumberingContext context) {
            for (Bus bus : buses) {
                bus.number(context);
            }
        }

        public void print(PrintStream out, int indent) {
            print(out, indent, null);
        }

        public void print(PrintStream out, int indent, ShortIdDictionary dict) {
            out.println(Strings.repeat(" ", indent) + "substation " + id);
            for (Bus bus : buses) {
                bus.print(out, indent + 4, dict);
            }
            for (Map.Entry<String, Boolean> entry : switches.entrySet()) {
                out.println(Strings.repeat(" ", indent + 4) + "switch " + entry.getKey() + " " + (entry.getValue() ? "open" : "closed"));
            }
        }

        public boolean containsEquipment(Equipment eq) {
            for (Bus bus : buses) {
                if (bus.getEquipments().contains(eq)) {
                    return true;
                }
            }
            return false;
        }

        public int getEquipmentCount() {
            int count = 0;
            for (Bus bus : buses) {
                count += bus.getEquipments().size();
            }
            return count;
        }

        public int getBranchCount() {
            int count = 0;
            for (Bus bus : buses) {
                count += bus.getBranchCount();
            }
            return count;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Substation)) {
                return false;
            }
            Substation other = (Substation) obj;
            return id.equals(other.id) && other.buses.size() == buses.size() && other.buses.containsAll(buses);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, buses);
        }

        @Override
        protected Substation clone() {
            Substation clone = new Substation(id, fictive, new HashMap<>(switches));
            for (Bus bus : buses) {
                clone.getBuses().add(bus.clone());
            }
            return clone;
        }
    }

    public static class Bus {

        @XmlAttribute(name="num")
        private Integer num;

        @XmlElement(name="equipment")
        private final List<Equipment> equipments;

        public Bus() {
            this((Integer) null);
        }

        public Bus(Integer num, List<Equipment> equipments) {
            this.num = num;
            this.equipments = equipments;
        }

        public Bus(List<Equipment> equipments) {
            this(null, equipments);
        }

        public Bus(Integer num) {
            this(num, new ArrayList<>());
        }

        public Bus(Equipment... equipments) {
            this(null, Lists.newArrayList(equipments));
        }

        @XmlTransient
        public Integer getNum() {
            return num;
        }

        public void setNum(Integer num) {
            this.num = num;
        }

        public List<Equipment> getEquipments() {
            return equipments;
        }

        public void number(NumberingContext context) {
            num = context.busNum++;
        }

        public int getBranchCount() {
            int count = 0;
            for (Equipment eq : equipments) {
                if (eq.isBranch()) {
                    count++;
                }
            }
            return count;
        }

        public void print(PrintStream out, int indent) {
            print(out, indent, null);
        }

        public void print(PrintStream out, int indent, ShortIdDictionary dict) {
            out.print(Strings.repeat(" ", indent) + "bus");
            if (num != null) {
                out.print(" num=" + num);
            }
            out.println(" " + toString(dict));
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Bus)) {
                return false;
            }
            Bus other = (Bus) obj;
            return other.equipments.size() == equipments.size() && other.equipments.containsAll(equipments);
        }

        @Override
        public int hashCode() {
            return equipments.hashCode();
        }

        @Override
        public String toString() {
            return toString(null);
        }

        public String toString(ShortIdDictionary dict) {
            StringBuilder builder = new StringBuilder("[");
            Iterator<Equipment> it = equipments.iterator();
            while (it.hasNext()) {
                Equipment eq = it.next();
                builder.append(eq.toString(dict));
                if (it.hasNext()) {
                    builder.append(", ");
                }
            }
            builder.append("]");
            return builder.toString();
        }

        @Override
        public Bus clone() {
            Bus clone = new Bus(num);
            for (Equipment equipment : equipments) {
                clone.getEquipments().add(equipment.clone());
            }
            return clone;
        }

    }

    public static class Equipment {

        private String id;

        @XmlAttribute(name="duplicateIndex")
        private final int duplicateIndex;

        @XmlAttribute(name="type")
        private ConnectableType type;

        public Equipment(String id, ConnectableType type) {
            this(id, 0, type);
        }

        public Equipment(String id, int duplicateIndex, ConnectableType type) {
            this.id = id;
            this.duplicateIndex = duplicateIndex;
            this.type = type;
        }

        public Equipment(String id, int duplicateIndex) {
            this(id, duplicateIndex, null);
        }

        public Equipment(String id) {
            this(id, null);
        }

        // for JAXB
        private Equipment() {
            this(null);
        }

        @XmlValue
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getDuplicateIndex() {
            return duplicateIndex;
        }

        public boolean hasType() {
            return type != null;
        }

        @XmlTransient
        public ConnectableType getType() {
            if (type == null) {
                throw new IllegalStateException("Type is not set");
            }
            return type;
        }

        public void setType(ConnectableType type) {
            this.type = type;
        }

        public boolean isBranch() {
            return isBranch(true);
        }

        public boolean isInjection() {
            switch (getType()) {
                case LOAD:
                case GENERATOR:
                case SHUNT_COMPENSATOR:
                    return true;

                default:
                    return false;
            }
        }

        public boolean isBranch(boolean includingDanglingLines) {
            switch (getType()) {
                case LINE:
                case TWO_WINDINGS_TRANSFORMER:
                    return true;

                case DANGLING_LINE:
                    return includingDanglingLines;

                case THREE_WINDINGS_TRANSFORMER:
                    throw new InternalError("TODO");

                default:
                    return false;
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Equipment)) {
                return false;
            }
            Equipment other = (Equipment) obj;
            return other.id.equals(id) && duplicateIndex == other.duplicateIndex;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, duplicateIndex);
        }

        @Override
        public String toString() {
            return toString(null);
        }

        public String toString(ShortIdDictionary dict) {
            return (dict != null ? dict.getShortId(id) : id) + ":" + duplicateIndex;
        }

        @Override
        protected Equipment clone() {
            return new Equipment(id, duplicateIndex, type);
        }
    }

    public static class MetaSubstation {

        @XmlElement(name="substation")
        private final List<Substation> substations = new ArrayList<>();

        public MetaSubstation() {
        }

        public List<Substation> getSubstations() {
            return substations;
        }

        public void addSubstation(Substation substation) {
            substations.add(substation);
        }

        public Substation getSubstation(String substationId) {
            for (Substation s : substations) {
                if (s.getId().equals(substationId)) {
                    return s;
                }
            }
            return null;
        }

        public boolean containsSubstation(String substationId) {
            return getSubstation(substationId) != null;
        }

        public void number(NumberingContext context) {
            for (Substation topology : substations) {
                topology.number(context);
            }
        }

        public void print(PrintStream out, int indent) {
            for (Substation topology : substations) {
                topology.print(out, indent);
            }
        }

        public String getId() {
            StringBuilder builder = new StringBuilder();
            Set<String> substationIds = new TreeSet<>();
            for (Substation substation : substations) {
                substationIds.add(substation.getId());
            }
            for (Iterator<String> it = substationIds.iterator(); it.hasNext();) {
                String id = it.next();
                builder.append(id);
                if (it.hasNext()) {
                    builder.append("+");
                }
            }
            return builder.toString();
        }

        public int getBranchCount() {
            int count = 0;
            for (Substation substation : substations) {
                count += substation.getBranchCount();
            }
            return count;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof MetaSubstation)) {
                return false;
            }
            MetaSubstation other = (MetaSubstation) obj;
            return other.substations.size() == substations.size() && other.substations.containsAll(substations);
        }

        @Override
        public int hashCode() {
            return substations.hashCode();
        }

        @Override
        public MetaSubstation clone() {
            MetaSubstation clone = new MetaSubstation();
            for (Substation substation : substations) {
                clone.getSubstations().add(substation.clone());
            }
            return clone;
        }

    }

    @XmlAttribute(name="num")
    private Integer num;

    @XmlElement(required=true)
    private final MetaSubstation metaSubstation;

    @XmlAttribute(required=true)
    private float probability;

    /* Topology hash is kept for further reference by DataMiningPlatformClient to update probabilities */
    @XmlAttribute(name="topoHash")
    private String topoHash;

    public PossibleTopology(MetaSubstation metaSubstation, float probability, String topoHash) {
        this(null, metaSubstation, probability, topoHash);
    }

    public PossibleTopology(Integer num, MetaSubstation metaSubstation, float probability, String topoHash) {
        this.num = num;
        this.metaSubstation = metaSubstation;
        this.probability = probability;
        this.topoHash = topoHash;
    }

    public PossibleTopology(float probability, String topoHash) {
        this(new MetaSubstation(), probability, topoHash);
    }

    // for JAXB
    private PossibleTopology() {
        this(Float.NaN, null);
    }

    @XmlTransient
    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    @XmlTransient
    public float getProbability() {
        return probability;
    }

    public void setProbability(float probability) {
        this.probability = probability;
    }

    @XmlTransient
    public String getTopoHash() {
        return topoHash;
    }

    public void setTopoHash(String topoHash) {
        this.topoHash = topoHash;
    }

    public MetaSubstation getMetaSubstation() {
        return metaSubstation;
    }

    public void number(NumberingContext context) {
        num = context.topologyNum++;
        metaSubstation.number(context);
    }

    public void print(PrintStream out, int indent) {
        out.print(Strings.repeat(" ", indent) + "possibleTopo id=" + metaSubstation.getId());
        if (num != null) {
            out.print(" num=" + num);
        }
        if (topoHash != null) {
            out.print(" topoHash=" + topoHash);
        }
        out.println(" proba=" + probability);
        metaSubstation.print(out, indent + 4);
    }

    @Override
    public PossibleTopology clone() {
        return new PossibleTopology(num, metaSubstation.clone(), probability, topoHash);
    }

}