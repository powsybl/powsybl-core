%
% Copyright (c) 2016, Ioannis Konstantelos <i.konstantelos@imperial.ac.uk>
% and Mingyang Sun <mingyang.sun11@imperial.ac.uk> – Imperial College London
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

function [] = modelSummary(mpath)

% path of output files from module 1
opath1  = fullfile(mpath,'mod1_output\');
% path of output files from module 2
opath2  = fullfile(mpath,'mod2_output\');

%% Compute number of cluster-models by counting number of files in dir
K = size(dir([opath2, '\*.mat']),1);
fprintf('\n%d data clusters identified\n\n',K);

%% Preallocate vectors
w       = cell(K,1);
tObs    = zeros(K,1);     % size of training set
tVar    = zeros(K,1); 
Nr      = zeros(K,1);
Nr_T    = zeros(K,1);
Nr_MAX  = zeros(K,1);
pvalue  = cell(K,1);

%% Save values from module files
for k = 1:K 
    % Define name of cluster-specific module 1 file
    m1file = sprintf('%sMOD1_%d.mat',opath1,k); 
    % Define name of cluster-specific module 2 file
    m2file = sprintf('%sMOD2_%d.mat',opath2,k);
    % Load module 1 and 2 files
    load(m1file);
    load(m2file);
    % Save values
    w{k}      = sprintf('%.4f',module1.w);
    tObs(k)   = size(module1.Z_c,1);
    tVar(k)   = size(module1.Z_c,2);
    Nr(k)     = module2.Nr;
    Nr_T(k)   = size(module2.R,2);
    Nr_MAX(k) = module2.Nr_MAX;
    pvalue{k} = sprintf('%.4f',module2.pvalue_c);
end

%% Create table
Cluster = (1:K)';
T = table(Cluster,w,tObs,tVar,Nr,Nr_T,Nr_MAX,pvalue);

% order clusters accroding to weights
%T = sortrows(T,'w','descend'); 

%% Output model summary
disp(T);

end

