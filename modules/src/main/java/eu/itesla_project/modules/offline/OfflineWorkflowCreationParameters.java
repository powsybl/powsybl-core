/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.offline;

import eu.itesla_project.commons.io.ModuleConfig;
import eu.itesla_project.commons.io.PlatformConfig;
import eu.itesla_project.iidm.network.Country;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class OfflineWorkflowCreationParameters implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final boolean DEFAULT_INIT_TOPO = false;

    public static final double DEFAULT_CORRELATION_THRESHOLD = 0.1;

    public static final double DEFAULT_PROBABILITY_THRESHOLD = 0.005;

    public static final boolean DEFAULT_LOAD_FLOW_TRANSFORMER_VOLTAGE_CONTROL_ON = false;

    public static final boolean DEFAULT_SIMPLIFIED_WORKFLOW = false;

    public static final boolean DEFAULT_MERGE_OPTIMIZED = false;

    public static final Set<Country> DEFAULT_ATTRIBUTES_COUNTRY_FILTER = null;

    public static final int DEFAULT_ATTRIBUTES_MIN_BASE_VOLTAGE_FILTER = 0;

    private final Set<Country> countries; 
    
    private final DateTime baseCaseDate;

    private final Interval histoInterval;

    private final boolean generationSampled;

    private final boolean boundariesSampled;

    private final boolean initTopo;

    private final double correlationThreshold;

    private final double probabilityThreshold;

    private final boolean loadFlowTransformerVoltageControlOn;

    private final boolean simplifiedWorkflow;

    private final boolean mergeOptimized;

    private final Set<Country> attributesCountryFilter;

    private final int attributesMinBaseVoltageFilter;

    public static OfflineWorkflowCreationParameters load() {
        ModuleConfig config = PlatformConfig.defaultConfig().getModuleConfig("offline-default-creation-parameters");
        Set<Country> countries = new HashSet<>(config.getEnumListProperty("countries", Country.class));
        DateTime baseCaseDate = config.getDateTimeProperty("baseCaseDate");
        Interval histoInterval = config.getIntervalProperty("histoInterval");
        boolean generationSampled = config.getBooleanProperty("generationSampled", false);
        boolean boundariesSampled = config.getBooleanProperty("boundariesSampled", false);
        boolean initTopo = config.getBooleanProperty("initTopo", DEFAULT_INIT_TOPO);
        double correlationThreshold = config.getDoubleProperty("correlationThreshold", DEFAULT_CORRELATION_THRESHOLD);
        double probabilityThreshold = config.getDoubleProperty("probabilityThreshold", DEFAULT_PROBABILITY_THRESHOLD);
        boolean loadFlowTransformerVoltageControlOn = config.getBooleanProperty("loadFlowTransformerVoltageControlOn", DEFAULT_LOAD_FLOW_TRANSFORMER_VOLTAGE_CONTROL_ON);
        boolean simplifiedWorkflow = config.getBooleanProperty("simplifiedWorkflow", DEFAULT_SIMPLIFIED_WORKFLOW);
        boolean mergeOptimized = config.getBooleanProperty("mergeOptimized", DEFAULT_MERGE_OPTIMIZED);
        List<Country> attributesCountryFilterList = config.getEnumListProperty("attributesCountryFilter", Country.class, (List<Country>) DEFAULT_ATTRIBUTES_COUNTRY_FILTER);
        Set<Country> attributesCountryFilter = attributesCountryFilterList != null ? new HashSet<>(attributesCountryFilterList) : null;
        int attributesMinBaseVoltageFilter = config.getIntProperty("attributesMinBaseVoltageFilter", DEFAULT_ATTRIBUTES_MIN_BASE_VOLTAGE_FILTER);
        return new OfflineWorkflowCreationParameters(countries, baseCaseDate, histoInterval, generationSampled, boundariesSampled,
                                                     initTopo, correlationThreshold, probabilityThreshold, loadFlowTransformerVoltageControlOn,
                                                     simplifiedWorkflow, mergeOptimized, attributesCountryFilter, attributesMinBaseVoltageFilter);
    }
    public OfflineWorkflowCreationParameters(Set<Country> countries, DateTime baseCaseDate, Interval histoInterval,
                                             boolean generationSampled, boolean boundariesSampled) {
        this(countries, baseCaseDate, histoInterval, generationSampled, boundariesSampled, DEFAULT_INIT_TOPO,
                DEFAULT_CORRELATION_THRESHOLD, DEFAULT_PROBABILITY_THRESHOLD, DEFAULT_LOAD_FLOW_TRANSFORMER_VOLTAGE_CONTROL_ON,
                DEFAULT_SIMPLIFIED_WORKFLOW, DEFAULT_MERGE_OPTIMIZED, DEFAULT_ATTRIBUTES_COUNTRY_FILTER, DEFAULT_ATTRIBUTES_MIN_BASE_VOLTAGE_FILTER);
    }

    public OfflineWorkflowCreationParameters(Set<Country> countries, DateTime baseCaseDate, Interval histoInterval,
                                             boolean generationSampled, boolean boundariesSampled,
                                             boolean initTopo, double correlationThreshold, double probabilityThreshold,
                                             boolean loadFlowTransformerVoltageControlOn, boolean simplifiedWorkflow,
                                             boolean mergeOptimized, Set<Country> attributesCountryFilter, int attributesMinBaseVoltageFilter) {
        if (countries.size() <= 0) {
            throw new IllegalArgumentException("One country is required at least");
        }
        this.countries = Objects.requireNonNull(countries);
        this.baseCaseDate = Objects.requireNonNull(baseCaseDate);
        this.histoInterval = Objects.requireNonNull(histoInterval);
        this.generationSampled = generationSampled;
        this.boundariesSampled = boundariesSampled;
        this.initTopo = initTopo;
        this.correlationThreshold = correlationThreshold;
        this.probabilityThreshold = probabilityThreshold;
        this.loadFlowTransformerVoltageControlOn = loadFlowTransformerVoltageControlOn;
        this.simplifiedWorkflow = simplifiedWorkflow;
        this.mergeOptimized = mergeOptimized;
        this.attributesCountryFilter = attributesCountryFilter;
        this.attributesMinBaseVoltageFilter = attributesMinBaseVoltageFilter;
    }

    public Set<Country> getCountries() {
        return countries;
    }

    public DateTime getBaseCaseDate() {
        return baseCaseDate;
    }

    public Interval getHistoInterval() {
        return histoInterval;
    }

    public boolean isGenerationSampled() {
        return generationSampled;
    }

    public boolean isBoundariesSampled() {
        return boundariesSampled;
    }

    public boolean isInitTopo() {
        return initTopo;
    }

    public double getCorrelationThreshold() {
        return correlationThreshold;
    }

    public double getProbabilityThreshold() {
        return probabilityThreshold;
    }

    public boolean isLoadFlowTransformerVoltageControlOn() {
        return loadFlowTransformerVoltageControlOn;
    }

    public boolean isSimplifiedWorkflow() {
        return simplifiedWorkflow;
    }

    public boolean isMergeOptimized() {
        return mergeOptimized;
    }

    public Set<Country> getAttributesCountryFilter() {
        return attributesCountryFilter;
    }

    public int getAttributesMinBaseVoltageFilter() {
        return attributesMinBaseVoltageFilter;
    }

    public void print(PrintStream out) {
        out.println("countries: " + countries);
        out.println("base case date: " + baseCaseDate);
        out.println("histo interval: " + histoInterval);
        out.println("generation sampled: " + generationSampled);
        out.println("boundaries sampled: " + boundariesSampled);
        out.println("topology initilization: " + initTopo);
        out.println("topology correlation threshold: " + correlationThreshold);
        out.println("topology probability threshold: " + probabilityThreshold);
        out.println("loadflow transformer voltage control on: " + loadFlowTransformerVoltageControlOn);
        out.println("simplified workflow: " + simplifiedWorkflow);
        out.println("merge optimized: " + mergeOptimized);
        out.println("attributes country filter: " + attributesCountryFilter);
        out.println("attributes min base voltage filter: " + attributesMinBaseVoltageFilter);
    }
    
    @Override
    public String toString() {
        return "{countries=" + countries +
                ", baseCaseDate=" + baseCaseDate
                + ", histoInterval=" + histoInterval
                + ", generationSampled=" + generationSampled
                + ", boundariesSampled=" + boundariesSampled
                + ", initTopo=" + initTopo
                + ", correlationThreshold=" + correlationThreshold
                + ", probabilityThreshold=" + probabilityThreshold
                + ", loadFlowTransformerVoltageControlOn=" + loadFlowTransformerVoltageControlOn
                + ", simplifiedWorkflow=" + simplifiedWorkflow
                + ", mergeOptimized=" + mergeOptimized
                + ", attributesCountryFilter=" + attributesCountryFilter
                + ", attributesMinBaseVoltageFilter=" + attributesMinBaseVoltageFilter
                + "}";
    }

}
