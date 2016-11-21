/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.online;

import eu.itesla_project.cases.CaseType;
import eu.itesla_project.commons.config.ModuleConfig;
import eu.itesla_project.commons.config.PlatformConfig;
import eu.itesla_project.iidm.network.Country;
import eu.itesla_project.simulation.securityindexes.SecurityIndexType;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

/**
 * @author Quinary <itesla@quinary.com>
 */
public class OnlineWorkflowParameters implements Serializable {

    private static final long serialVersionUID = 1L;
    public static final boolean DEFAULT_MERGE_OPTIMIZED = false;
    public static final float DEFAULT_LIMIT_REDUCTION = 1f;
    public static final boolean DEFAULT_HANDLE_VIOLATIONS_IN_N = false;
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
    private String caseFile;

    public static OnlineWorkflowParameters loadDefault() {
        ModuleConfig config = PlatformConfig.defaultConfig().getModuleConfig("online-default-parameters");

        int states = config.getIntProperty("states");
        String offlineWorkflowId = config.getStringProperty("offlineWorkflowId", null);
        TimeHorizon timeHorizon = TimeHorizon.fromName(config.getStringProperty("timeHorizon").trim());
        Interval histoInterval = Interval.parse(config.getStringProperty("histoInterval"));
        String feAnalysisId = config.getStringProperty("feAnalysisId");
        double rulesPurityThreshold = Double.parseDouble(config.getStringProperty("rulesPurityThreshold"));
        boolean storeStates = config.getBooleanProperty("storeStates", false);
        boolean analyseBasecase = config.getBooleanProperty("analyseBasecase", true);
        boolean validation = config.getBooleanProperty("validation", false);
        Set<SecurityIndexType> securityIndexes = config.getEnumSetProperty("securityIndexes", SecurityIndexType.class, null);
        boolean mergeOptimized = config.getBooleanProperty("mergeOptimized", DEFAULT_MERGE_OPTIMIZED);
        float limitReduction = config.getFloatProperty("limitReduction", DEFAULT_LIMIT_REDUCTION);
        boolean handleViolationsInN = config.getBooleanProperty("handleViolationsInN", DEFAULT_HANDLE_VIOLATIONS_IN_N);
        float constraintMargin = config.getFloatProperty("constraintMargin", DEFAULT_CONSTRAINT_MARGIN);

        String caseFile = config.getStringProperty("caseFile", null);
        if (caseFile != null) {
            if ((config.getStringProperty("baseCaseDate", null) != null)
                    || (config.getStringProperty("caseType", null) != null)
                    || (config.getStringProperty("countries", null) != null))
                throw new RuntimeException("caseFile and ( baseCaseDate, caseType, countries ) are mutually exclusive options");
            return new OnlineWorkflowParameters(states,
                    histoInterval,
                    offlineWorkflowId,
                    timeHorizon,
                    feAnalysisId,
                    rulesPurityThreshold,
                    storeStates,
                    analyseBasecase,
                    validation,
                    securityIndexes,
                    mergeOptimized,
                    limitReduction,
                    handleViolationsInN,
                    constraintMargin,
                    caseFile);
        }
        DateTime baseCaseDate = DateTime.parse(config.getStringProperty("baseCaseDate"));
        CaseType caseType = config.getEnumProperty("caseType", CaseType.class);
        Set<Country> countries = config.getEnumSetProperty("countries", Country.class);
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
                constraintMargin);
    }

    private OnlineWorkflowParameters(DateTime baseCaseDate, int states, Interval histoInterval, String offlineWorkflowId, TimeHorizon timeHorizon,
                                     String feAnalysisId, double rulesPurityThreshold, boolean storeStates, boolean analyseBasecase, boolean validation,
                                     Set<SecurityIndexType> securityIndexes, CaseType caseType, Set<Country> countries, boolean mergeOptimized,
                                     float limitReduction, boolean handleViolationsInN, float constraintMargin, String caseFile) {
        this.baseCaseDate = baseCaseDate;
        this.states = states;
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
        this.caseFile = caseFile;
    }


    public OnlineWorkflowParameters(DateTime baseCaseDate, int states, Interval histoInterval, String offlineWorkflowId, TimeHorizon timeHorizon,
                                    String feAnalysisId, double rulesPurityThreshold, boolean storeStates, boolean analyseBasecase, boolean validation,
                                    Set<SecurityIndexType> securityIndexes, CaseType caseType, Set<Country> countries, boolean mergeOptimized,
                                    float limitReduction, boolean handleViolationsInN, float constraintMargin) {
        this(baseCaseDate,
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
                constraintMargin,
                null);
        Objects.requireNonNull(this.baseCaseDate);
        Objects.requireNonNull(this.countries);
        Objects.requireNonNull(this.caseType);
        Objects.requireNonNull(this.histoInterval);
    }

    public OnlineWorkflowParameters(int states, Interval histoInterval, String offlineWorkflowId, TimeHorizon timeHorizon,
                                    String feAnalysisId, double rulesPurityThreshold, boolean storeStates, boolean analyseBasecase, boolean validation,
                                    Set<SecurityIndexType> securityIndexes, boolean mergeOptimized,
                                    float limitReduction, boolean handleViolationsInN, float constraintMargin, String caseFile) {
        this(null,
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
                null,
                null,
                mergeOptimized,
                limitReduction,
                handleViolationsInN,
                constraintMargin,
                caseFile);
        Objects.requireNonNull(this.caseFile);
        Objects.requireNonNull(this.histoInterval);
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

    public String getCaseFile() {
        return caseFile;
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
                + ", caseFile=" + caseFile
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

    public void setCaseFile(String caseFile) {
        this.caseFile = caseFile;
    }

}
