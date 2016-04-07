/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.topo;

import com.google.common.base.Strings;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TopologyChoice {

    @XmlAttribute(name="num")
    private Integer num;

    /* Cluster ID is kept for further reference by DataMiningPlatformClient to update probabilities */
    @XmlAttribute(name="clusterId")
    private String clusterId;

    // for JAXB
    public TopologyChoice() {
    }

    public TopologyChoice(String clusterId) {
        this.clusterId = clusterId;
    }

    public TopologyChoice(Integer num, String clusterId) {
        this.num = num;
        this.clusterId = clusterId;
    }

    @XmlElement(name="possibleTopology")
    private final List<PossibleTopology> possibleTopologies = new ArrayList<>();

    @XmlTransient
    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public List<PossibleTopology> getPossibleTopologies() {
        return possibleTopologies;
    }

    public void number(NumberingContext context) {
        num = context.topologyChoiceNum++;
        for (PossibleTopology possibleTopology : possibleTopologies) {
            possibleTopology.number(context);
        }
    }

    @XmlTransient
    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public void print(PrintStream out, int indent) {
        out.print(Strings.repeat(" ", indent) + "topoChoice");
        if (num != null) {
            out.print(" num=" + num);
        }
        if (clusterId != null) {
            out.print(" clusterId=" + clusterId);
        }
        out.println(" count=" + possibleTopologies.size());
        for (PossibleTopology possibleTopology : possibleTopologies) {
            possibleTopology.print(out, 8);
        }
    }

    @Override
    protected TopologyChoice clone() {
        TopologyChoice clone = new TopologyChoice(num, clusterId);
        for (PossibleTopology possibleTopology : possibleTopologies) {
            clone.getPossibleTopologies().add(possibleTopology.clone());
        }
        return clone;
    }

}
