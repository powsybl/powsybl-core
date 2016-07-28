/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.online;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import eu.itesla_project.commons.io.ModuleConfig;
import eu.itesla_project.commons.io.PlatformConfig;
import eu.itesla_project.iidm.network.Country;
import eu.itesla_project.cases.CaseType;
import eu.itesla_project.modules.securityindexes.SecurityIndexType;

import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class OnlineWorkflowParameters implements Serializable {

    /*
     * example of online-default-parameters.properties file.
     *

	# basecase to be analyzed
	baseCaseDate=2013-01-15T18:30:00+01:00
	# number of states
	states=10
	# interval of historical data to be used for WCA
	histoInterval=2013-01-01T00:00:00+01:00/2013-01-31T23:59:00+01:00
	# offline workflow id that produced the security rules
	offlineWorkflowId=1358271900000-1356994800000-1359673140000
	# time horizon for the online analysis
	timeHorizon=DACF 
	# id of the forecast error analysis
    feAnalysisId = France_flagPQ0_ir06
    # purity threshold for the rules
    rulesPurityThreshold=0.95
    # flag for the storage of sampled states
    storeStates=true
    # flag for analyzing the basecase with the sampled states
    analyseBasecase=true
    # flag for performing an online workflow aimed at the validation
    validation=false
    # list of security indexes to be used by the workflow, leave empty to use all the available ones
    securityIndexes=SMALLSIGNAL,TSO_SYNCHROLOSS,TSO_GENERATOR_VOLTAGE_AUTOMATON

     */

    private static final long serialVersionUID = 1L;
    public static final CaseType DEFAULT_CASE_TYPE = CaseType.FO;
    public static final List<Country> DEFAULT_COUNTRIES = Arrays.asList(Country.FR);
    public static final boolean DAFAULT_MERGE_OPTIMIZED = false;
    public static final float DEFAULT_LIMIT_REDUCTION = 1f;
    public static final boolean DAFAULT_HANDLE_VIOLATIONS_IN_N = false;
    public static final float DEFAULT_CONSTRAINT_MARGIN = 0f;

    private DateTime baseCaseDate;
    private int states;
    private Interval histoInterval;
    private String offlineWorkflowId;
    private TimeHorizon timeHorizon;
    private String feAnalysisId;
    private double rulesPurityThreshold;
    private boolean storeStates;
    private boolean analyseBasecase;
    private boolean validation;
    private Set<SecurityIndexType> securityIndexes;
    private CaseType caseType;
    private Set<Country> countries;
    private boolean mergeOptimized;
    private float limitReduction;
    private boolean handleViolationsInN;
    private float constraintMargin;

    public static OnlineWorkflowParameters loadDefault() {
        ModuleConfig config = PlatformConfig.defaultConfig().getModuleConfig("online-default-parameters");
        DateTime baseCaseDate = DateTime.parse(config.getStringProperty("baseCaseDate"));
        int states = config.getIntProperty("states");
        String offlineWorkflowId = config.getStringProperty("offlineWorkflowId", null);
        TimeHorizon timeHorizon = TimeHorizon.fromName(config.getStringProperty("timeHorizon").trim());
        Interval histoInterval = Interval.parse(config.getStringProperty("histoInterval"));
        String feAnalysisId = config.getStringProperty("feAnalysisId");
        double rulesPurityThreshold = Double.parseDouble(config.getStringProperty("rulesPurityThreshold"));
        boolean storeStates = config.getBooleanProperty("storeStates", false);
        boolean analyseBasecase = config.getBooleanProperty("analyseBasecase", true);
        boolean validation = config.getBooleanProperty("validation", false);
        Set<SecurityIndexType> securityIndexes = null;
        String securityIndexesAsString = config.getStringProperty("securityIndexes", "");
        if ( !"".equals(securityIndexesAsString) ) {
            securityIndexes = Arrays.stream(securityIndexesAsString.split(","))
                    .map(SecurityIndexType::valueOf)
                    .collect(Collectors.toSet());
        }
        CaseType caseType = config.getEnumProperty("caseType", CaseType.class, DEFAULT_CASE_TYPE);
        Set<Country> countries = new HashSet<>(config.getEnumListProperty("countries", Country.class, DEFAULT_COUNTRIES));
        boolean mergeOptimized = config.getBooleanProperty("mergeOptimized", DAFAULT_MERGE_OPTIMIZED);
        float limitReduction = config.getFloatProperty("limitReduction", DEFAULT_LIMIT_REDUCTION);
        boolean handleViolationsInN = config.getBooleanProperty("handleViolationsInN", DAFAULT_HANDLE_VIOLATIONS_IN_N);
        float constraintMargin = config.getFloatProperty("constraintMargin", DEFAULT_CONSTRAINT_MARGIN);

        return new OnlineWorkflowParameters(baseCaseDate, 
                states, 
                histoInterval, 
                offlineWorkflowId, 
                timeHorizon, 
                feAnalysisId, 
                rulesPurityThreshold, 
                storeStates, 
                analyseBasecase, 
                validation,
                securityIndexes,
                caseType,
                countries,
                mergeOptimized,
                limitReduction,
                handleViolationsInN,
                constraintMargin
                );
    }

    public OnlineWorkflowParameters(DateTime baseCaseDate, int states, Interval histoInterval, String offlineWorkflowId, TimeHorizon timeHorizon, 
            String feAnalysisId, double rulesPurityThreshold, boolean storeStates, boolean analyseBasecase, boolean validation, 
            Set<SecurityIndexType> securityIndexes, CaseType caseType, Set<Country> countries, boolean mergeOptimized, 
            float limitReduction, boolean handleViolationsInN, float constraintMargin) {
        Objects.requireNonNull(baseCaseDate);
        Objects.requireNonNull(histoInterval);
        this.baseCaseDate = baseCaseDate;
        this.states=states;
        this.histoInterval = histoInterval;
        this.offlineWorkflowId = offlineWorkflowId;
        this.timeHorizon = timeHorizon;
        this.feAnalysisId = feAnalysisId;
        this.rulesPurityThreshold = rulesPurityThreshold;
        this.storeStates = storeStates;
        this.analyseBasecase = analyseBasecase;
        this.validation = validation;
        this.securityIndexes = securityIndexes;
        this.caseType = caseType;
        this.countries = countries;
        this.mergeOptimized = mergeOptimized;
        this.limitReduction = limitReduction;
        this.handleViolationsInN = handleViolationsInN;
        this.constraintMargin = constraintMargin;
    }

    public DateTime getBaseCaseDate() {
        return baseCaseDate;
    }

    public Interval getHistoInterval() {
        return histoInterval;
    }

    public int getStates() { 
        return states; 
    }

    public String getOfflineWorkflowId() { 
        return offlineWorkflowId; 
    }

    public TimeHorizon getTimeHorizon() { 
        return timeHorizon; 
    }

    public String getFeAnalysisId() { 
        return feAnalysisId; 
    }

    public double getRulesPurityThreshold() {
        return rulesPurityThreshold;
    }

    public boolean storeStates() {
        return storeStates;
    }

    public boolean analyseBasecase() {
        return analyseBasecase;
    }

    public boolean validation() {
        return validation;
    }

    public Set<SecurityIndexType> getSecurityIndexes() {
        return securityIndexes;
    }

    public CaseType getCaseType() {
        return caseType;
    }

    public Set<Country> getCountries() {
        return countries;
    }

    public boolean isMergeOptimized() {
        return mergeOptimized;
    }

    public float getLimitReduction() {
        return limitReduction;
    }

    public boolean isHandleViolationsInN() {
        return handleViolationsInN;
    }

    public float getConstraintMargin() {
        return constraintMargin;
    }

    @Override
    public String toString() {
        return "{baseCaseDate=" + baseCaseDate
                + ", states=" + states
                + ", histoInterval=" + histoInterval
                + ", offlineWorkflowId=" + offlineWorkflowId
                + ", timeHorizon=" + timeHorizon
                + ", feAnalysisId=" + feAnalysisId
                + ", rulesPurityThreshold=" + rulesPurityThreshold
                + ", storeStates=" + storeStates
                + ", analyseBasecase=" + analyseBasecase
                + ", validation=" + validation
                + ", securityIndexes=" + securityIndexes                
                + ", caseType=" + caseType
                + ", countries=" + countries
                + ", mergeOptimized=" + mergeOptimized
                + ", limitReduction=" + limitReduction
                + ", handleViolationsInN=" + handleViolationsInN
                + ", constraintMargin=" + constraintMargin
                + "}";
    }

    public void setStates(int states) {
        this.states = states;
    }

    public void setBaseCaseDate(DateTime baseCaseDate) {
        this.baseCaseDate = baseCaseDate;
    }

    public void setHistoInterval(Interval histoInterval) {
        this.histoInterval = histoInterval;
    }

    public void setOfflineWorkflowId(String offlineWorkflowId) {
        this.offlineWorkflowId = offlineWorkflowId;
    }

    public void setTimeHorizon(TimeHorizon timeHorizon) {
        this.timeHorizon = timeHorizon;
    }

    public void setFeAnalysisId(String feAnalysisId) {
        this.feAnalysisId = feAnalysisId;
    }

    public void setRulesPurityThreshold(double rulesPurityThreshold) {
        this.rulesPurityThreshold = rulesPurityThreshold;
    }

    public void setStoreStates(boolean storeStates) {
        this.storeStates = storeStates;
    }

    public void setAnalyseBasecase(boolean analyseBasecase) {
        this.analyseBasecase = analyseBasecase;
    }

    public void setValidation(boolean validation) {
        this.validation = validation;
    }

    public void setSecurityIndexes(Set<SecurityIndexType> securityIndexes) {
        this.securityIndexes = securityIndexes;
    }

    public void setCaseType(CaseType caseType) {
        this.caseType = caseType;
    }

    public void setCountries(Set<Country> countries) {
        this.countries = countries;
    }

    public void setMergeOptimized(boolean mergeOptimized) {
        this.mergeOptimized = mergeOptimized;
    }

    public void setLimitReduction(float limitReduction) {
        this.limitReduction = limitReduction;
    }

    public void setHandleViolationsInN(boolean handleViolationsInN) {
        this.handleViolationsInN = handleViolationsInN;
    }

    public void setConstraintMargin(float constraintMargin) {
        this.constraintMargin = constraintMargin;
    }

}
