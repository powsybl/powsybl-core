/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.histodb.client.impl;

import eu.itesla_project.modules.histo.HistoDbAttributeId;
import eu.itesla_project.modules.histo.HistoDbAttributeIdParser;
import eu.itesla_project.modules.rules.*;
import eu.itesla_project.modules.rules.expr.*;
import eu.itesla_project.simulation.securityindexes.SecurityIndexId;
import eu.itesla_project.simulation.securityindexes.SecurityIndexType;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class JsonSecurityRule implements SecurityRule {

    private final RuleId id;
    private final String workflowId;
    private final float quality;
    private final int treeSize;
    private final float criticality;
    private final JSONObject jsonTree;

    public JsonSecurityRule(RuleId id, String workflowId, float quality, int treeSize, float criticality, JSONObject jsonTree) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(workflowId);
        Objects.requireNonNull(jsonTree);
        this.id = id;
        this.workflowId = workflowId;
        this.quality = quality;
        this.treeSize = treeSize;
        this.criticality = criticality;
        this.jsonTree = jsonTree;
    }

    @Override
    public RuleId getId() {
        return id;
    }

    @Override
    public String getWorkflowId() {
        return workflowId;
    }

    public float getQuality() {
        return quality;
    }

    public int getTreeSize() {
        return treeSize;
    }

    public float getCriticality() {
        return criticality;
    }

    public JSONObject getJsonTree() {
        return jsonTree;
    }

    public static SecurityRule fromJSON(JSONObject json) {
        return new JsonSecurityRule(
                new RuleId(RuleAttributeSet.valueOf(json.getString("algoType")),
                           new SecurityIndexId(json.getString("contingencyId"),
                                               SecurityIndexType.fromLabel(json.getString("indexType")))),
                json.getString("workflowId"),
                (float) json.getDouble("quality"),
                json.getInt("treeSize"),
                (float) json.getDouble("criticality"),
                json.getJSONObject("tree")
        );
    }

    public JSONObject toJSON() {
        JSONObject jsonRule = new JSONObject();
        jsonRule.put("contingencyId", id.getSecurityIndexId().getContingencyId());
        jsonRule.put("indexType", id.getSecurityIndexId().getSecurityIndexType().getLabel());
        jsonRule.put("algoType", id.getAttributeSet());
        jsonRule.put("workflowId", workflowId);
        jsonRule.put("quality", quality);
        jsonRule.put("treeSize", treeSize);
        jsonRule.put("criticality", criticality);
        jsonRule.put("tree", jsonTree);
        return jsonRule;
    }

    private static boolean isTrueCondition(JSONObject stats, JSONObject node, double purityThreshold, int trueIdx) {
        String nodeIdx = node.optString("id");
        JSONArray nodeValues = stats.optJSONObject(nodeIdx).optJSONArray("counts");
        double purity = ((double) nodeValues.optInt(trueIdx)) / stats.optJSONObject(nodeIdx).optInt("count");
        return purity >= purityThreshold && node.optBoolean("value");
    }

    private static void processTreeNode(JSONObject node, JSONArray inputs, double purityThreshold, SecondLevelNode currentCondition, List<SecondLevelNode> trueConditions, JSONObject stats, int trueIdx) {
        if ("thresholdTest".equals(node.optString("type"))) {
            // conditional node
            int inputIdx = node.optInt("inputIndex");
            HistoDbAttributeId attrId = HistoDbAttributeIdParser.parse(inputs.opt(inputIdx).toString());
            double threshold = node.optDouble("threshold");
            ComparisonOperator trueCondition = new ComparisonOperator(new Attribute(attrId), new Litteral(threshold), ComparisonOperator.Type.LESS);
            ComparisonOperator falseCondition = new ComparisonOperator(new Attribute(attrId), new Litteral(threshold), ComparisonOperator.Type.GREATER_EQUAL);
            processTreeNode(node.optJSONObject("trueChild"), inputs, purityThreshold,
                            currentCondition == null ? trueCondition : new AndOperator(currentCondition.clone(), trueCondition),
                            trueConditions, stats, trueIdx);
            processTreeNode(node.optJSONObject("falseChild"), inputs, purityThreshold,
                            currentCondition == null ? falseCondition : new AndOperator(currentCondition.clone(), falseCondition),
                            trueConditions, stats, trueIdx);
        } else {
            if (currentCondition != null && isTrueCondition(stats, node, purityThreshold, trueIdx)) {
                trueConditions.add(currentCondition);
            }
        }
    }

    @Override
    public SecurityRuleExpression toExpression() {
        return toExpression(1f);
    }

    @Override
    public SecurityRuleExpression toExpression(double purityThreshold) {
        JSONArray inputs = jsonTree.getJSONArray("attributes");
        JSONObject tree = jsonTree.getJSONObject("tree");
        JSONObject stats = jsonTree.getJSONObject("stats");

        int trueIdx = Integer.MIN_VALUE;
        JSONArray symbols = tree.getJSONArray("symbols");
        for (int i = 0; i < symbols.size(); i++) {
            if ("true".equals(symbols.get(i))) {
                trueIdx = i;
            }
        }

        JSONObject root = tree.getJSONObject("root");
        if (treeSize == 1) {
            return new SecurityRuleExpression(id, isTrueCondition(stats, root, purityThreshold, trueIdx) ? SecurityRuleStatus.ALWAYS_SECURE : SecurityRuleStatus.ALWAYS_UNSECURE, null);
        } else {
            List<SecondLevelNode> trueConditions = new ArrayList<>(1);
            processTreeNode(root, inputs, purityThreshold, null, trueConditions, stats, trueIdx);

            if (trueConditions.isEmpty()) {
                return new SecurityRuleExpression(id, SecurityRuleStatus.ALWAYS_UNSECURE, null);
            } else {
                SecondLevelNode n = null;
                for (SecondLevelNode trueCondition: trueConditions) {
                    if (n != null) {
                        n = new OrOperator(n, trueCondition);
                    } else {
                        n = trueCondition;
                    }
                }
                return new SecurityRuleExpression(id, SecurityRuleStatus.SECURE_IF, n);
            }
        }
    }

    @Override
    public String toString() {
        return "RuleDescriptionImpl{" + "id=" + id + ", workflowId=" + workflowId + ", quality=" + quality + ", treeSize=" + treeSize + ", criticality=" + criticality + ", jsonTree=" + jsonTree + '}';
    }
}
