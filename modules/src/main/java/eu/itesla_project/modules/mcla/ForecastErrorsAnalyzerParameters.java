/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.mcla;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Properties;

import org.joda.time.Interval;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class ForecastErrorsAnalyzerParameters {
	
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


	public ForecastErrorsAnalyzerParameters(Interval histoInterval, String feAnalysisId, double ir, Integer flagPQ, 
											Integer method, Integer nClusters, double percentileHistorical, 
			 								Integer modalityGaussian, Integer outliers, Integer conditionalSampling, Integer nSamples) {
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

	public Integer getnClusters() { 
		return nClusters; 
	}

	public Integer getOutliers() { 
		return outliers; 
	}

	public double getPercentileHistorical() { 
		return percentileHistorical; 
	}

	public Integer getModalityGaussian() { 
		return modalityGaussian; 
	}

	public Integer getConditionalSampling() { 
		return conditionalSampling; 
	}

	public Integer getnSamples() { return nSamples; }

	public void toFile(Path file) throws FileNotFoundException, IOException {
		Properties properties = new Properties();
		try(OutputStream output = new FileOutputStream(file.toFile())) {
			properties.setProperty("histoInterval", histoInterval.toString());
			properties.setProperty("feAnalysisId", feAnalysisId);
			properties.setProperty("ir", Double.toString(ir));
			properties.setProperty("flagPQ", Integer.toString(flagPQ));
			properties.setProperty("method", Integer.toString(method));
			properties.setProperty("nClusters", Integer.toString(nClusters));
			properties.setProperty("percentileHistorical", Double.toString(percentileHistorical));
			properties.setProperty("modalityGaussian", Integer.toString(modalityGaussian));
			properties.setProperty("outliers", Integer.toString(outliers));
			properties.setProperty("conditionalSampling", Integer.toString(conditionalSampling));
			properties.setProperty("nSamples", Integer.toString(nSamples));
			properties.store(output, null);
		}
	}
	
	public static ForecastErrorsAnalyzerParameters fromFile(Path file) throws FileNotFoundException, IOException {
		Properties properties = new Properties();
		try(InputStream input =  new FileInputStream(file.toFile())) {
			properties.load(input);
			return new ForecastErrorsAnalyzerParameters(
					Interval.parse(properties.getProperty("histoInterval")),
					properties.getProperty("feAnalysisId"),
					Double.parseDouble(properties.getProperty("ir")),
					Integer.parseInt(properties.getProperty("flagPQ")),
					Integer.parseInt(properties.getProperty("method")),
					Integer.parseInt(properties.getProperty("nClusters")),
					Double.parseDouble(properties.getProperty("percentileHistorical")),
					Integer.parseInt(properties.getProperty("modalityGaussian")),
					Integer.parseInt(properties.getProperty("outliers")),
					Integer.parseInt(properties.getProperty("conditionalSampling")),
					Integer.parseInt( (properties.getProperty("nSamples") != null) ? properties.getProperty("nSamples") : "-1")
			);
		}
	}

	@Override
	public String toString() {
		return "ForecastErrorsAnalyzerParameters["
				+ "histoInterval=" + histoInterval + ","
				+ "feAnalysisId=" + feAnalysisId + ","
				+ "ir=" + ir + ","
				+ "flagPQ=" + flagPQ + ","
				+ "method=" + method + ","
				+ "nClusters=" + nClusters + ","
				+ "percentileHistorical=" + percentileHistorical + ","
				+ "modalityGaussian=" + modalityGaussian + ","
				+ "outliers=" + outliers + ","
				+ "conditionalSampling=" + conditionalSampling + ","
				+ "nSamples=" + nSamples
				+ "]";
	}
	
}
