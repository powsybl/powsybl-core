%
% Copyright (c) 2016, Ioannis Konstantelos <i.konstantelos@imperial.ac.uk>
% and Mingyang Sun <mingyang.sun11@imperial.ac.uk> – Imperial College London
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

clear all;clc;
tStart_global = tic;

%% Define input parameters

% does the model exist?
MODEL_EXIST = 0;

% file address for input historical data file
ifile  = 'C:\ioannis_itesla\input\francedata_100cols.mat';

% path for internal-output module folders
mpath  = 'C:\ioannis_itesla\March_test1\';

% check to see if target folder exists, if not create all subfolders
if (MODEL_EXIST == 1) && (exist(mpath,'dir') == 0)
    fprintf('[SCRIPT] Model %s does not exist --- Terminating now\n',mpath)
    exit
else
    if (MODEL_EXIST == 0) && (exist(mpath,'dir') == 0)
        fprintf('[SCRIPT] Target folder %s does not exist and will be created now.\n',mpath)
        mkdir(fullfile(mpath,'mod1_output\'));
        mkdir(fullfile(mpath,'mod2_output\'));
        mkdir(fullfile(mpath,'mod3_output\'));
    else 
        if (MODEL_EXIST == 0) && (exist(mpath,'dir') ~= 0)
            fprintf('[SCRIPT] Target folder %s exists and will be overwritten.\n',mpath)
        else
            if (MODEL_EXIST == 1) && (exist(mpath,'dir') ~= 0)
                fprintf('[SCRIPT] Model already exists in %s.\n',mpath)
            else
                if (MODEL_EXIST == 1) && (exist(mpath,'dir') == 0)
                    error('[SCRIPT] Target folder %s does not exist.\n',mpath)     
                end
            end
        end
    end
end

% save console output to a logfile
diary(fullfile(mpath,'log.txt'));
fprintf('%s\n', datestr(now)); % current date and time
    
% file address for final .mat output file
ofile   = fullfile(mpath,'mod3_output\Y.mat');
     
% file address for .mat file containing indices&values of stationary variables
statvarfile = fullfile(mpath,'mod1_output\statvars.mat');
% path to save the output files from module 1
opath1  = fullfile(mpath,'mod1_output\');
% path to save the output files from module 2
opath2  = fullfile(mpath,'mod2_output\');
% path to save the output files from  module 3
opath3  = fullfile(mpath,'mod3_output\');

% Information Retainment Criterion
IR = 0.5;
% Number of clusters (rule of thumb = sqrt(NObs/2))
K = 5;
% Number of samples
NSam = 10000;
% Truncated model flag (set to 0 for a simpler model that does not store large correlation matrices in module2 .mat files)
Tflag = 1;
% Random number seed as string e.g. '123'(0 for random seed)
seed = 0;%'1';

%% Check if model is provided
if MODEL_EXIST == 1
   fprintf('[SCRIPT] Model already exists in %s\n',mpath); 
else
%% Run module 1 
    tStart = tic;
    exitcode1 = MODULE1_HELPER(ifile, opath1, statvarfile, K, seed);
    fprintf('\nTotal [MODULE1] runtime : %.2f seconds\n',toc(tStart));

%% Run module 2 in sequence for each cluster
    for k = 1:K 
        tStart = tic;
        % Define name of cluster-specific input file for module 2 (as defined in MODULE1_HELPER)
        m1file    = sprintf('%sMOD1_%d.mat', opath1,k); 
        % Define name of cluster-specific output file for module 2 
        m2file    = sprintf('%sMOD2_%d.mat', opath2,k);
        % Run module 2
        exitcode2 = MODULE2_HELPER(m1file,m2file,k,IR,Tflag);
        fprintf('\nTotal [MODULE2 | cluster %d] runtime : %.2f seconds\n',k,toc(tStart));
    end
end
%% Print Truncated C-Vine model summary
fprintf('*********************************************************');
modelSummary(mpath);
fprintf('*********************************************************');

%% Define how many samples to be generated from each cluster-model according to cluster-weights w
fprintf('\n[SCRIPT] Defining sampling pattern ..')
tStart = tic;
% Read in cluster weights from the different module1 files
w = zeros(K,1);
for k = 1:K 
    m1file = sprintf('%sMOD1_%d.mat', opath1,k); 
    load(m1file);
    w(k) = module1.w;
end
% cls defines which cluster-model will generate each sample
cls = rand_gen(1:K,w,NSam);
% The vector NSam_c defines how many samples each cluster-model will generate
NSam_c = zeros(1,K);
for k = 1:K 
   NSam_c(k) = numel(cls(cls == k));  
end
fprintf(' %.2f seconds\n',toc(tStart));

%% Run module 3 in sequence for each cluster
for k = 1:K 
    tStart = tic;
    % Define name of cluster-specific module 1 file
    m1file    = sprintf('%sMOD1_%d.mat', opath1,k); 
    % Define name of cluster-specific module 2 file
    m2file    = sprintf('%sMOD2_%d.mat', opath2,k);
    % Define name of cluster-specific output file for module 3 
    m3file  = sprintf('%sMOD3_%d.mat',opath3,k);
    % Run module 3
    exitcode3   = MODULE3_HELPER(m1file,m2file,m3file,k,NSam_c(k),seed);
    fprintf('\nTotal [MODULE3 | cluster %d] runtime : %.2f seconds\n',k,toc(tStart));
end

%% Aggregate all cluster-specific files in opath3 to a single .mat file (ofile)
exitcode4 =  AGGREGATOR(opath3,statvarfile,K,ofile);

%% Test historical vs. sampled marginals with Kolmogorov-Smirnoff Test
%testAllMarginals(ifile,ofile);

%% Test historical vs. sampled pairwise plots
%testAllBi(ifile,ofile)

%%
fprintf('\nTotal workflow runtime : %.2f seconds\n',toc(tStart_global));

%% suspend diary
diary off
