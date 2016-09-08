package eu.itesla_project.histodb.server.rest;

import be.pepite.dataserver.api.ColumnDescriptor;
import be.pepite.dataserver.api.Dataset;
import be.pepite.dataserver.rest.resource.AbstractDataserverResource;
import com.mongodb.util.JSON;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;

import java.io.IOException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: pduchesne
 * Date: 4/10/12
 * Time: 16:19
 * To change this template use File | Settings | File Templates.
 */
public class ITeslaRules
    extends AbstractDataserverResource
{

    String algoType;
    String contingencyId;
    String indexType;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);

        algoType = (String)getRequest().getAttributes().get("algoType");
        contingencyId = (String)getRequest().getAttributes().get("contingencyId");
        indexType = (String)getRequest().getAttributes().get("indexType");
    }

    @Get("json")
    public Object getRepresentation() throws Exception{

        Form queryForm = getRequest().getOriginalRef().getQueryAsForm();
        String workflowId = queryForm.getFirstValue("workflowId", true, null);
        String startStr = queryForm.getFirstValue("start", "0");
        String countStr = queryForm.getFirstValue("count", "50");

        Map<String, Object> query = new HashMap();
        if (algoType != null) query.put("algoType", algoType);
        if (contingencyId != null) query.put("contingencyId", contingencyId);
        if (indexType != null) query.put("indexType", indexType);
        if (workflowId != null) query.put("workflowId", workflowId);

        Dataset set = datasource.getData(query,
                Integer.parseInt(startStr),
                Integer.parseInt(countStr),
                ColumnDescriptor.getDescriptorsForNames("algoType", "contingencyId", "indexType", "workflowId", "quality", "treeSize", "criticality", "tree"));
        Iterator<Collection> it = set.getRowIterator();

        JSONArray result = new JSONArray();

        while (it.hasNext()) {
            Object[] values = it.next().toArray();
            JSONObject ruleObj = new JSONObject();
            ruleObj.put("algoType", values[0]);
            ruleObj.put("contingencyId", values[1]);
            ruleObj.put("indexType", values[2]);
            ruleObj.put("workflowId", values[3]);
            ruleObj.put("quality", values[4]);
            ruleObj.put("treeSize", values[5]);
            ruleObj.put("criticality", values[6]);
            ruleObj.put("tree", new JSONObject((String)values[7]));
            result.put(ruleObj);
        }

        return result;
    }

    @Get("cond")
    public String getTreeConditions() throws Exception{

        Form queryForm = getRequest().getOriginalRef().getQueryAsForm();
        String workflowId = queryForm.getFirstValue("workflowId", true, null);

        Map<String, Object> query = new HashMap();
        if (algoType != null) query.put("algoType", algoType);
        if (contingencyId != null) query.put("contingencyId", contingencyId);
        if (indexType != null) query.put("indexType", indexType);
        if (workflowId != null) query.put("workflowId", workflowId);

        Dataset set = datasource.getData(query,
                0,
                -1,
                ColumnDescriptor.getDescriptorsForNames("tree"));
        Iterator<Collection> it = set.getRowIterator();

        StringBuffer conditions = new StringBuffer();

        while (it.hasNext()) {
            Object[] values = it.next().toArray();
            JSONObject jsonTreeDescr = new JSONObject((String)values[0]);

            processJsonTree(jsonTreeDescr, conditions);
        }

        return conditions.toString();
    }

    public static void processJsonTree(JSONObject jsonTreeDescr, StringBuffer conditions) throws JSONException {

        JSONArray inputs = jsonTreeDescr.getJSONArray("attributes");
        JSONObject tree = jsonTreeDescr.getJSONObject("tree");
        JSONObject stats = jsonTreeDescr.getJSONObject("stats");

        List<String> trueConditions = new ArrayList<String>();

        int trueIdx = Integer.MIN_VALUE;
        JSONArray symbols = tree.getJSONArray("symbols");
        for (int i=0;i<symbols.length();i++) if ("true".equals(symbols.get(i))) trueIdx = i;

        processTreeNode(tree.getJSONObject("root"), inputs, null, trueConditions, stats, trueIdx);

        for (String trueCondition: trueConditions) {
            if (conditions.length() > 0) conditions.append(" or ");
            conditions.append("(").append(trueCondition).append(")");
        }
        // if no 'true' condition were found, return 'false' constant
        if (conditions.length()==0) conditions.append("(false)");
    }

    public static void processTreeNode(JSONObject node, JSONArray inputs, String currentCondition, List<String> trueConditions, JSONObject stats, int trueIdx) {
        if ("thresholdTest".equals(node.optString("type"))) {
            int inputIdx = node.optInt("inputIndex");
            // conditional node
            String trueCondition = "("+inputs.opt(inputIdx)+" < "+node.optDouble("threshold")+")";
            String falseCondition = "("+inputs.opt(inputIdx)+" >= "+node.optDouble("threshold")+")";
            processTreeNode(node.optJSONObject("trueChild"), inputs, (currentCondition==null||currentCondition.length()==0)?trueCondition:(currentCondition+" and "+trueCondition), trueConditions, stats, trueIdx);
            processTreeNode(node.optJSONObject("falseChild"), inputs, (currentCondition==null||currentCondition.length()==0)?falseCondition:(currentCondition+" and "+falseCondition), trueConditions, stats, trueIdx);
        } else {
            String nodeIdx = node.optString("id");
            JSONArray nodeValues = stats.optJSONObject(nodeIdx).optJSONArray("counts");
            double purity = nodeValues.optInt(trueIdx) / (double)stats.optJSONObject(nodeIdx).optInt("count");
            if (purity > 0.95 && node.optBoolean("value")) {
                trueConditions.add(currentCondition == null ? "true" : currentCondition);
            }
        }
    }

    @Post("json")
    public void putRule(JSONObject rule) throws Exception {

        String workflowId = rule.getString("workflowId");

        if (datasource == null) {
            //create datasource if needed
            datasource = getApplication().getDataService().getDataStore(storeId).createDataSource(dataId);
        }

        if (algoType == null || contingencyId == null || workflowId == null || indexType == null) {
            throw new IllegalArgumentException("Cannot create/update rule ; incomplete identifier (algo="+algoType+" ; cont="+contingencyId+" ; workflow="+workflowId+" ; indexType="+indexType+" )");
        }

        Map<String, Object> query = new HashMap();
        query.put("algoType", algoType);
        query.put("contingencyId", contingencyId);
        query.put("indexType", indexType);

        String[] headers = new String[] {"algoType", "contingencyId", "indexType", "workflowId", "quality", "treeSize", "criticality", "tree"};
        Object[] values = new Object [] {
                algoType,
                contingencyId,
                indexType,
                workflowId,
                rule.getDouble("quality"),
                rule.getInt("treeSize"),
                rule.getDouble("criticality"),
                rule.getJSONObject("tree").toString()
        };

        datasource.updateData(query, headers, values);

        datasource.persistState();

    }
}
