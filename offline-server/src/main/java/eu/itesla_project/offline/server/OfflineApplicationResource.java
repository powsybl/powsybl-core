/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline.server;

import eu.itesla_project.iidm.network.Country;
import eu.itesla_project.modules.offline.OfflineWorkflowCreationParameters;
import eu.itesla_project.modules.offline.SecurityIndexSynthesis;
import eu.itesla_project.modules.rules.RuleAttributeSet;
import eu.itesla_project.modules.rules.RuleId;
import eu.itesla_project.modules.rules.SecurityRuleExpression;
import eu.itesla_project.modules.securityindexes.SecurityIndexId;
import eu.itesla_project.modules.securityindexes.SecurityIndexType;
import eu.itesla_project.offline.OfflineWorkflowStartParameters;
import eu.itesla_project.offline.server.message.CountriesMessage;
import eu.itesla_project.offline.server.message.SecurityIndexesSynthesisMessage;
import eu.itesla_project.offline.server.message.StabilityMessage;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Path("/offline")
public class OfflineApplicationResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(OfflineApplicationResource.class);
    
    @Inject
    private OfflineApplicationBean bean;

    public OfflineApplicationResource() {
    }

    @POST
    @Path("login")
    public void login() {
        LOGGER.debug("login()");
        // just to check the security interceptor
    }

    @POST
    @Path("workflow/create")
    @Produces(MediaType.TEXT_PLAIN)
    public Response createWorkflow(JsonObject paramsObj) {
        LOGGER.debug("createWorkflow()");
        String workflowId = paramsObj.getString("id", null);
        //DateTime parsing...
        DateTime baseCaseDate = DateTime.parse(paramsObj.getJsonString("baseCaseDate").getString());
        DateTime histoIntervalStart = DateTime.parse(paramsObj.getJsonObject("histoInterval").getJsonString("start").getString());
        DateTime histoIntervalEnd = DateTime.parse(paramsObj.getJsonObject("histoInterval").getJsonString("end").getString());
        List<JsonString> countryCodes = paramsObj.getJsonArray("countries").getValuesAs(JsonString.class);
        Set<Country> countries = new HashSet<>(countryCodes.size());
        for(JsonString countryCode : countryCodes) {
            countries.add(Country.valueOf(countryCode.getString()));
        }
        OfflineWorkflowCreationParameters parameters = new OfflineWorkflowCreationParameters(
                countries,
                baseCaseDate,
                new Interval(histoIntervalStart.getMillis(), histoIntervalEnd.getMillis()),
                false, // TODO
                false);
        try {
            return Response.status(200).entity(bean.getApplication().createWorkflow(workflowId, parameters)).build();
        } catch(Exception e) {
            LOGGER.error(e.toString(), e);
            return Response.status(500).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("workflow/{workflowId}/remove")
    public void removeWorkflow(@PathParam("workflowId") String workflowId) {
        LOGGER.debug("removeWorkflow({})", workflowId);
        bean.getApplication().removeWorkflow(workflowId);
    }

    @POST
    @Path("workflow/{workflowId}/start")
    public void startWorkflow(@PathParam("workflowId") String workflowId, JsonObject paramsObj) {
        OfflineWorkflowStartParameters parameters = new OfflineWorkflowStartParameters(
                paramsObj.getInt("sampleQueueSize"),
                paramsObj.getInt("samplingThreads"),
                paramsObj.getInt("samplesPerThread"),
                paramsObj.getInt("stateQueueSize"),
                paramsObj.getInt("duration"),
                -1);
        bean.getApplication().startWorkflow(workflowId, parameters);
    }

    @POST
    @Path("workflow/{workflowId}/stop")
    public void stopWorkflow(@PathParam("workflowId") String workflowId) {
        LOGGER.debug("stopWorkflow({})", workflowId);
        bean.getApplication().stopWorkflow(workflowId);
    }

    @POST
    @Path("workflow/{workflowId}/computesecurityrules")
    public void computeSecurityRules(@PathParam("workflowId") String workflowId) {
        LOGGER.debug("computeSecurityRules({})", workflowId);
        bean.getApplication().computeSecurityRules(workflowId);
    }

    @POST
    @Path("workflow/{workflowId}/getsecurityrules")
    public void getSecurityRules(@PathParam("workflowId") String workflowId) {
        LOGGER.debug("getSecurityRules({})", workflowId);
        bean.getApplication().getSecurityRules(workflowId);
    }

    @POST
    @Path("workflow/{workflowId}/getsecurityindexes")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSecurityIndexes(@PathParam("workflowId") String workflowId) {
        LOGGER.debug("getSecurityRules({})", workflowId);
        SecurityIndexSynthesis synthesis = bean.getApplication().getSecurityIndexesSynthesis(workflowId);
        SecurityIndexesSynthesisMessage message = new SecurityIndexesSynthesisMessage(synthesis, workflowId);
        return Response.ok().entity(message.toJson()).build();
    }

    @POST
    @Path("rule/{workflowId}/{attributeSet}/{securityIndexType}/{contingencyId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSecurityRule(
            @PathParam("workflowId") String workflowId,
            @PathParam("attributeSet") String attributeSet,
            @PathParam("securityIndexType") String securityIndexType,
            @PathParam("contingencyId") String contingencyId) {
        LOGGER.debug("getSecurityRule({}, {}, {}, {})", workflowId, attributeSet, securityIndexType, contingencyId);
        SecurityIndexId securityIndexId = new SecurityIndexId(contingencyId, SecurityIndexType.valueOf(securityIndexType));
        RuleId ruleId = new RuleId(RuleAttributeSet.valueOf(attributeSet), securityIndexId);
        SecurityRuleExpression securityRuleExpression = bean.getApplication().getSecurityRuleExpression(workflowId, ruleId);
        if (securityRuleExpression == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        StabilityMessage message = new StabilityMessage(securityRuleExpression);
        return Response.ok().entity(message.toJson()).build();
    }

    @POST
    @Path("countries")
    @Produces(MediaType.APPLICATION_JSON)
    public Response countries() {
        LOGGER.debug("countries()");
        CountriesMessage message = new CountriesMessage(Country.values());
        return Response.ok().entity(message.toJson()).build();
    }

}
