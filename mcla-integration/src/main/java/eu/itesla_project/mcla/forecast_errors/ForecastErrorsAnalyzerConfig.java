/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.mcla.forecast_errors;

import eu.itesla_project.commons.config.ModuleConfig;
import eu.itesla_project.commons.config.PlatformConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Objects;
/*
   MCLA code 1.8
*/
/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class ForecastErrorsAnalyzerConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(ForecastErrorsAnalyzerConfig.class);

	private Path binariesDir;
	private Path runtimeHomeDir;
	private String histoDBUser;
	private String histoDBPassword;
	private String histoDBServiceUrl;
	private final boolean debug;
    private final Integer rngSeed;
    private final Integer checkModule0;
    private final double percpuGaussLoad;
    private final double percpuGaussRes;
    private final double correlationGauss;
    private final double tolVar;
    private final double nMinObsFract;
    private final Integer nMinObsInterv;
    private final Integer imputationMeth;
    private final Integer nGaussians;
    private final Integer kOutlier;
    private final double tolerance;
    private final Integer iterations;
    private final double epsilo;
    private final Integer conditionalSampling;
    private final Integer tFlags;
    private final double histo_estremeQ;
    private final double thresGUI;
    private final String nats;

    public ForecastErrorsAnalyzerConfig(
			Path binariesDir,
			Path runtimeHomeDir,
			String histoDBServiceUrl,
			String histoDBUser,
			String histoDBPassword,
            Integer checkModule0,
            double percpuGaussLoad,
            double percpuGaussRes,
            double correlationGauss,
            double tolVar,
            double nMinObsFract,
            Integer nMinObsInterv,
            Integer imputationMeth,
            Integer nGaussians,
            Integer kOutlier,
            double tolerance,
            Integer iterations,
            double epsilo,
            Integer conditionalSampling,
            Integer tFlags,
            double histo_estremeQ,
            double thresGUI,
            String nats,
			Integer rngSeed,
			boolean debug
			) {
		Objects.requireNonNull(binariesDir,"sampler compiled binaries directory is null");
		Objects.requireNonNull(runtimeHomeDir,"matlab runtime directory is null");
		Objects.requireNonNull(histoDBServiceUrl, "histodb service url is null");
		Objects.requireNonNull(histoDBUser, "histodb user is null");
		Objects.requireNonNull(histoDBPassword, "histodb password is null");

		this.binariesDir=binariesDir;
		this.runtimeHomeDir = runtimeHomeDir;
		this.histoDBServiceUrl = histoDBServiceUrl;
		this.histoDBUser = histoDBUser;
		this.histoDBPassword = histoDBPassword;
        this.rngSeed = rngSeed;
        this.checkModule0=checkModule0;
        this.percpuGaussLoad=percpuGaussLoad;
        this.percpuGaussRes=percpuGaussRes;
        this.correlationGauss=correlationGauss;
        this.tolVar=tolVar;
        this.nMinObsFract=nMinObsFract;
        this.nMinObsInterv=nMinObsInterv;
        this.imputationMeth=imputationMeth;
        this.nGaussians=nGaussians;
        this.kOutlier = kOutlier;
        this.tolerance = tolerance;
        this.iterations = iterations;
        this.epsilo = epsilo;
        this.conditionalSampling = conditionalSampling;
        this.tFlags = tFlags;
        this.histo_estremeQ = histo_estremeQ;
        this.thresGUI = thresGUI;
        this.nats = nats;
		this.debug=debug;
	}

	public static ForecastErrorsAnalyzerConfig load() {
        ModuleConfig config = PlatformConfig.defaultConfig().getModuleConfig("forecastErrorsAnalyzer");

        Path binariesDir = config.getPathProperty("binariesDir");
        Path runtimeHomeDir = config.getPathProperty("runtimeHomeDir");
        String histoDBServiceUrl = config.getStringProperty("histoDBServiceUrl");
        String histoDBUser = config.getStringProperty("histoDBUser");
        String histoDBPassword = config.getStringProperty("histoDBPassword");
        boolean debug = config.getBooleanProperty("debug", false);
        Integer checkModule0 = config.getOptionalIntProperty("checkModule0");
        double percpuGaussLoad = config.getDoubleProperty("percpuGaussLoad");
        double percpuGaussRes = config.getDoubleProperty("percpuGaussRes");
        double correlationGauss = config.getDoubleProperty("correlationGauss");
        double tolVar= config.getDoubleProperty("tolvar");
        double nMinObsFract= config.getDoubleProperty("Nmin_obs_fract");
        Integer nMinObsInterv = config.getOptionalIntProperty("Nmin_obs_interv");
        Integer imputationMeth = config.getOptionalIntProperty("imputation_meth");
        Integer nGaussians = config.getOptionalIntProperty("Ngaussians");
        Integer rngSeed = config.getOptionalIntProperty("rngSeed");
        Integer kOutlier = config.getOptionalIntProperty("koutlier");
        double tolerance = config.getDoubleProperty("tolerance");
        Integer iterations = config.getOptionalIntProperty("iterations");
        double epsilo = config.getDoubleProperty("epsilo");
        Integer conditionalSampling = config.getOptionalIntProperty("conditionalSampling");
        Integer tFlags = config.getOptionalIntProperty("tFlags");
        double histo_estremeQ = config.getDoubleProperty("histo_estremeQ");
        double thresGUI = config.getDoubleProperty("thresGUI");
        String nats = config.getStringProperty("nats","All");

        return new ForecastErrorsAnalyzerConfig(binariesDir, runtimeHomeDir, histoDBServiceUrl, histoDBUser, histoDBPassword, checkModule0, percpuGaussLoad, percpuGaussRes, correlationGauss, tolVar, nMinObsFract, nMinObsInterv, imputationMeth, nGaussians, kOutlier, tolerance, iterations, epsilo, conditionalSampling, tFlags, histo_estremeQ, thresGUI, nats, rngSeed, debug);
	}

	public Path getBinariesDir() {
		return binariesDir;
	}

	public Path getRuntimeHomeDir() {
		return runtimeHomeDir;
	}

	public String getHistoDBUser() {
		return histoDBUser;
	}

	public String getHistoDBPassword() {
		return histoDBPassword;
	}

	public String getHistoDBServiceUrl() {
		return histoDBServiceUrl;
	}

    public Integer getCheckModule0() {
    	return checkModule0; 
    }

    public double getPercpuGaussLoad() {
        return percpuGaussLoad;
    }

    public double getPercpuGaussRes() {
        return percpuGaussRes;
    }

    public double getCorrelationGauss() {
        return correlationGauss;
    }

    public double getTolVar() {
        return tolVar;
    }

    public double getnMinObsFract() {
        return nMinObsFract;
    }

    public Integer getnMinObsInterv() {
        return nMinObsInterv;
    }

    public Integer getImputationMeth() {
        return imputationMeth;
    }

    public Integer getnGaussians() {
        return nGaussians;
    }

    public Integer getkOutlier() { return kOutlier; }

    public double getTolerance() { return tolerance; }

    public Integer getIterations() { return iterations;}

    public double getEpsilo() { return epsilo; }

    public Integer getConditionalSampling() { return conditionalSampling; }

    public Integer gettFlags() { return tFlags; }

    public double getHisto_estremeQ() { return histo_estremeQ; }

    public double getThresGUI() { return thresGUI; }

    public String getNats() { return nats; }

    public Integer getRngSeed() {
		return rngSeed;
	}

	public static Logger getLogger() {
		return LOGGER;
	}

	public boolean isDebug() {
	        return debug;
	}

	@Override
	public String toString() {
		return "ForecastErrorsAnalyzerConfig [binariesDir=" + binariesDir + ", runtimeHomeDir=" + runtimeHomeDir
				+ ", histoDBUser=" + histoDBUser + ", histoDBPassword=*" + ", histoDBServiceUrl=" + histoDBServiceUrl
                + ", check module0=" + checkModule0
                + ", per cpu gauss load=" + percpuGaussLoad
                + ", per cpu gauss res=" + percpuGaussRes
                + ", correlation gauss=" + correlationGauss
                + ", tolvar=" + tolVar
                + ", Nmin_obs_fract=" + nMinObsFract
                + ", Nmin_obs_interv=" + nMinObsInterv
                + ", imputation_meth=" + imputationMeth
                + ", Ngaussians=" + nGaussians
                + ", koutlier=" + kOutlier
                + ", tolerance=" + tolerance
                + ", iterations=" + iterations
                + ", epsilo=" + epsilo
                + ", conditionalSampling=" + conditionalSampling
                + ", tFlags=" + tFlags
                + ", histo_estremeQ=" + histo_estremeQ
                + ", thresGUI=" + thresGUI
                + ", nats=" + nats
				+ ", debug=" + debug + "]";
	}

}
