%
% Copyright (c) 2016, Ricerca sul Sistema Energetico – RSE S.p.A. <itesla@rse-web.it>
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

%%% demo: calls FEA and MCLA related functions

%%%%%% FEA ANALYSIS

%% module1

m1_ifile='feaiteslaFRBE.mat';%''feaiteslaFRBE_tutti.mat';%'fea_input_7busItesla.mat';%'feaiteslaFRBE_tutti.mat';%'feaiteslaFRBE_tutti.mat';%'feaiteslaFRBE.mat';%'fea_input_7busItesla.mat';%'feanalyzerinput.mat';%'fea_input_7busItesla.mat';%'feanalyzerinput.mat';%'fea_input_7busItesla.mat';%'feaanalyzeinput_400.mat';%'fea_input_7busItesla.mat';%'feaanalyzeinput_400.mat';%'fea_input_7busItesla.mat';%'feanalyzerinput.mat';%'fea_input_7busItesla.mat';%'feanalyzerinput.mat';%% name of the file with forecast data fea_input_7busItesla = 7 bus grid; feanalyzerinput = French grid (latest version from Quinary)
m1_ofile='fea_module1.mat';   %% module1 output file name
ofile_forFPF = 'fea_stats_for_FPF';
ofileGUI = 'fea_data_uncertainty_GUI'; %% output mat file which stores the data for the GUI representation of forecast errors
IRs='0.8'; %% IR: fraction of explained variance for PCA
Ks='3'; %% number of clusters for PCA
s_flagPQ='1'; %%  flagPQ for all injections: if 1, P and Q vars are separately sampled, if 0, Q is sampled with the same initial pf starting from P vars.
s_flagPQ_RES='0'; %%  flagPQ for RES: if 1, P and Q injected by RES are separately sampled, if 0, Q is sampled with the same initial pf starting from P injections. up to now it is not used.
percentile_historical = '0.05'; % quantile of the distribution of historical data related to Q vars, to set realistic limits of Q samples in case of using a constant power factor to produce Q samples starting from P samples
percpu_fict_gauss_load = '0.05'; % percentage of current load forecast, to be used for std dev of fictitious gaussians
percpu_fict_gauss_RES = '0.15'; % percentage of current RES forecast, to be used for std dev of fictitious gaussians
correlation_fict_gauss = '0'; % Pearson correlation among RES and load variables: "0" all variables RES and load are uncorrelated; "1" = all variables RES and load are completely correlated; an intermediate value means that RES and LOAD are intermediately correlated, but inside each category (RES and Load) all loads (all RES) are completely correlated.
histo_estremeQ='5'; % multiple of ("percpu_fict_gauss_load" x current_forecast_value) used to estimate the "percentile_historical" and "1-percentile_historical" quantiles of fictitious gaussian distributions in case of lack of historical data
option_sign = '0'; % if 1, when value of extracted sample has a different sign wrt forecast the sampled value is put to 0. if 0, the sampled value is assumed as valid sample.option applied only to RES injections
% options to deal with raw data and fix possible gaps
s_method='4';  %% method for missing data imputation: if 1, method = proposed method by RSE; if 2, method = gaussian conditional sampling; if 3, method = gaussian mixture imputation; if 4, interpolation based method.
imputation_meth='2'; % type of inputation techniques for gaussian mixture (s_method = 3): 2= random draw, 1 = conditional mean of gaussian components
check_module0 = '0'; % evaluates the goodness of the fit by the imputation method (check preservation of correlation among variables)
tolvar = '1e-3';% minimum variance of imput variables, in MW
Nmin_obs_fract = '0.7';% minim fraction of valid samples for each stochastic variables
Nmin_obs_interv = '150';% min nr of samples in common for each pair of variables
outliers = '1';% 0 = outliers are included as valid samples, 1 = outliers are excluded
koutlier='5'; % n for the n-sigma rule to filter out the outliers
Ngaussians='5'; % nr of gaussians of the mixture
Tflags='0'; % truncate gaussian for module 3 backprojection: 0 = no, 1 = yes
%%% options for conditional sampling calculation
conditional_sampling = '1'; % 1 = activated conditional sampling, 0 = not active
iterations = '100'; % nr of iterations for calculation of inverse of correlation matrix with given accuracy
tolerance = '1e-8'; % accuracy in percentage for the calculation of inverse of correlation matrix R
epsilo = '1e-5'; % quantity to be added to diagonal to better approximate the inverse of the correlation matrix (better not exceed 5e-3/iterations)
centering = '1'; % centering the conditioned snapshots onto the specific basecase (DACF) to improve uncertainty model
thresGUI='0.95'; % threshold of vlaue of negative correlation below which the correlations are not reported in the GUI
nations = 'FRBE'; % parameter indicates which countries undergo a FEA using histo data. the others are treated as indipendent areas with Gaussian forecast error models. if string = 'all' all the coutnries with available histo data are treated with a unique normal FEA

% FEA_NATIONS_SPLIT_HELPER(m1_ifile,ofilenations,Nmin_obs_fract)

FEA_MODULE1_HELPER(m1_ifile, m1_ofile,nations,ofile_forFPF,ofileGUI,IRs, Ks, s_flagPQ,s_method,tolvar,Nmin_obs_fract,Nmin_obs_interv,outliers,koutlier,imputation_meth,Ngaussians,percentile_historical,check_module0,tolerance,iterations,epsilo,conditional_sampling,histo_estremeQ,thresGUI);


%% module2
m2_partk_prefix='fea_m2_';  %% module 2 file name prefix for partial cluster results
% partial module2 computation (one per cluster)

for i=1:(str2double(Ks))
    cls = i-1;
    FEA_MODULE2_HELPER(m1_ofile, sprintf('%s%u.mat',m2_partk_prefix, cls), int2str(cls),IRs,Tflags);
end

% aggregate module1 output and partial module2 results in one file
m2_ofile='fea_output.mat';  %% module2 output file name
m2_current_folder='.';
FEA_MODULE2_REDUCE_HELPER(m1_ofile, m2_current_folder, m2_partk_prefix, Ks, m2_ofile,percpu_fict_gauss_load,percpu_fict_gauss_RES,correlation_fict_gauss);

% module 3 output
m3_ofile='uncond_sampling_output_mixed2.mat';%'uncond_sampling_output_old.mat';  %'mcsamplerinput_forecast_offline_samples_DACF',%'uncond_sampling_output.mat';  %% module3 output file name
m3_current_folder='.';
uncond_nsamples_s='500';
SAMPLING_MODULE3_HELPER(m2_ofile, m3_ofile,uncond_nsamples_s, '1');

%%%%%% MCLA SAMPLER
mcla_ifile='mcsamplerinput_7busItesla';%'mcsamplerinput_20130227_0230_FO3_FR0';%'mcsamplerinput_7busItesla';%'mcsamplerinput_20130227_0230_FO3_FR0';%'mcsamplerinput_7busItesla';%'mcsamplerinput_20130227_0230_FO3_FR0';%'mcsamplerinput_7busItesla';%'mcsamplerinput_20130227_0230_FO3_FR0';%'mcsamplerinput_20130225_0830_FO1_FR0_DACF';%'mcsamplerinput_20130115_1830_FO2_FR0';%'mcsamplerinput_20130227_0730_FO3_FR0.mat';%'mcsamplerinput_20130115_1830_FO2_FR0';%'mcsamplerinput_20130225_0830_FO1_FR0_DACF';%'mcsamplerinput_20130227_0230_FO3_FR0';%'mcsamplerinput_20130225_0830_FO1_FR0_DACF_4986881446631756559';%'mcsamplerinput_20130227_0230_FO3_FR0';%'mcsamplerinput_20130225_0830_FO1_FR0_DACF_4986881446631756559';%'mcsamplerinput_20130115_1830_FO2_FR0';%'mcsamplerinput_20130225_0830_FO1_FR0_DACF_4986881446631756559';%'mcsamplerinput_20130115_1830_FO2_FR0';%'mcsamplerinput_7busItesla';%'mcsamplerinput_20130115_1830_FO2_FR0';%'mcsamplerinput_7busItesla';%'mcsamplerinput_20130115_1830_FO2_FR0';%'mcsamplerinput_7busItesla';%'mcsamplerinput_20130115_1830_FO2_FR0';%'mcsamplerinput_7busItesla';%'mcsamplerinput_20130115_1830_FO2_FR0';%'mcsamplerinput_20130115_1830_FO2_FR0';%'mcsamplerinput_7busItesla';%
mcla_ofile='mcsampleroutput.mat';
nsamples_s='100';
MCLA_HELPER(mcla_ifile,m3_ofile, mcla_ofile,nsamples_s,option_sign,centering);

%%%%% FPF HELPER
fpfc_ofile='fea_stats_cond_for_FPF';
FPF_HELPER(mcla_ifile,m2_ofile, fpfc_ofile, '1');
