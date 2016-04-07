/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline.forecast_errors;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import eu.itesla_project.commons.io.ModuleConfig;
import eu.itesla_project.commons.io.PlatformConfig;
import eu.itesla_project.iidm.network.Country;
import eu.itesla_project.modules.cases.CaseType;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class ForecastErrorsAnalysisParameters implements Serializable{
	
	/*
	 * example of fea-parameters.properties file.
	 *

    #this points to the network to be analyzed (ref. CaseRepository impl)
    baseCaseDate=2013-01-15T18:45:00+01:00

	# interval of data to be analysed
	histoInterval=2013-03-01T00:00:00+01:00/2013-12-31T00:00:00+01:00
	
	# id of the forecast errors analysis
    # it should encode meaningful information for identifying it: e.g. network id and some analysis parameters
    # it should not contain "strange" chars and spaces
    feAnalysisId = France_flagPQ0_ir06

    # fraction of explained variance for PCA
    ir=0.9

	# flagPQ for all injections: if 1, P and Q vars are separately sampled, if 0, Q is sampled with the same initial pf starting from P vars.
	flagPQ=0

    # method for missing data imputation: if 1, method = proposed method by RSE; if 2, method = gaussian conditional sampling; if 3, method = gaussian mixture imputation; if 4, interpolation based method
    method=3

    # number of clusters for PCA
	nClusters=3

	# quantile of the distribution of historical data related to Q vars, to set realistic limits of Q samples in case of using a constant power factor to produce Q samples starting from P samples
	percentileHistorical=0.05

	# 1 = fictitious gaussians simulate forecast errors, 0 = historical data with copula estimation are considered
	modalityGaussian=0

	# % 0 = outliers are included as valid samples, 1 = outliers are excluded
	outliers=1

	# 1 = activated conditional sampling, 0 = not active
	conditionalSampling=0

	# number of samples to generate -offline-
	nSamples=100


	*/

	public static final List<Country> DEFAULT_COUNTRIES = Arrays.asList(Country.FR);
	public static final CaseType DEFAULT_CASE_TYPE = CaseType.FO;
	
	private final DateTime baseCaseDate;
	private final Interval histoInterval;
	private final String feAnalysisId;
	private final double ir;
	private final Integer flagPQ;
	private final Integer method;
	private final Integer nClusters;
	private final double percentileHistorical;
	private final Integer modalityGaussian;
	private final Integer outliers;
	private final Integer conditionalSampling;
	private final Integer nSamples;
	private final Set<Country> countries;
	private final CaseType caseType;

	public ForecastErrorsAnalysisParameters(DateTime baseCaseDate, Interval histoInterval, String feAnalysisId, double ir, Integer flagPQ, 
											Integer method, Integer nClusters, double percentileHistorical, Integer modalityGaussian, Integer outliers, 
											Integer conditionalSampling, Integer nSamples, Set<Country> countries, CaseType caseType) {
		this.baseCaseDate = baseCaseDate;
		this.histoInterval = histoInterval;
		this.feAnalysisId = feAnalysisId;
		this.ir = ir;
		this.flagPQ = flagPQ;
		this.method = method;
		this.nClusters = nClusters;
		this.percentileHistorical = percentileHistorical;
		this.modalityGaussian = modalityGaussian;
		this.outliers = outliers;
		this.conditionalSampling = conditionalSampling;
		this.nSamples = nSamples;
		this.countries = countries;
		this.caseType = caseType;
	}
	
	public static ForecastErrorsAnalysisParameters load() {
        ModuleConfig config = PlatformConfig.defaultConfig().getModuleConfig("fea-parameters");

        DateTime baseCaseDate = DateTime.parse(config.getStringProperty("baseCaseDate"));
        Interval histoInterval = Interval.parse(config.getStringProperty("histoInterval"));
        String feAnalysisId = config.getStringProperty("feAnalysisId");
        double ir = config.getDoubleProperty("ir");
        Integer flagPQ = config.getIntProperty("flagPQ");
        Integer method = config.getIntProperty("method");
		Integer nClusters = config.getIntProperty("nClusters");
		double percentileHistorical = config.getDoubleProperty("percentileHistorical");
		Integer modalityGaussian = config.getOptionalIntProperty("modalityGaussian");
		Integer outliers = config.getOptionalIntProperty("outliers");
		Integer conditionalSampling = config.getOptionalIntProperty("conditionalSampling");
		Integer nSamples = config.getIntProperty("nSamples");
		Set<Country> countries = new HashSet<>(config.getEnumListProperty("countries", Country.class, DEFAULT_COUNTRIES));
		CaseType caseType = config.getEnumProperty("caseType", CaseType.class, DEFAULT_CASE_TYPE);

		return new ForecastErrorsAnalysisParameters(baseCaseDate, histoInterval, feAnalysisId, ir, flagPQ, method, nClusters, percentileHistorical, 
													modalityGaussian, outliers, conditionalSampling, nSamples, countries, caseType);
    }
	
    public DateTime getBaseCaseDate() {
        return baseCaseDate;
    }

	public Interval getHistoInterval() {
		return histoInterval;
	}

	public String getFeAnalysisId() {
		return feAnalysisId;
	}
	
	public double getIr() {
		return ir;
	}
	
	public Integer getFlagPQ() {
		return flagPQ;
	}
	
	public Integer getMethod() {
		return method;
	}

	public Integer getConditionalSampling() { return conditionalSampling; }

	public Integer getModalityGaussian() { return modalityGaussian; }

	public Integer getnClusters() { return nClusters; }

	public Integer getOutliers() { return outliers; }

	public double getPercentileHistorical() { return percentileHistorical; }

	public Integer getnSamples() { return nSamples; }
	
	public Set<Country> getCountries() {
		return countries;
	}
	
	public CaseType getCaseType() {
		return caseType;
	}

	@Override
	public String toString() {
		return "ForecastErrorsAnalsysParameters [baseCaseDate=" + baseCaseDate
				+ ", histoInterval=" + histoInterval
				+ ", feAnalysisId=" + feAnalysisId
				+ ", ir=" + ir
				+ ", flagPQ=" + flagPQ
				+ ", method=" + method
				+ ", nClusters=" + nClusters
				+ ", percentileHistorical=" + percentileHistorical
				+ ", modalityGaussian=" + modalityGaussian
				+ ", outliers=" + outliers
				+ ", conditionalSampling=" + conditionalSampling
				+ ", nSamples=" + nSamples
				+ ", countries=" + countries
				+ ", caseType=" + caseType
				+ "]";
	}

}
