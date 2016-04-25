%
% Copyright (c) 2016, Ioannis Konstantelos <i.konstantelos@imperial.ac.uk>
% and Mingyang Sun <mingyang.sun11@imperial.ac.uk> – Imperial College London
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

function exitcode = MODULE1_HELPER(ifile, opath, statvarfile, K, s_rng_seed)
%--------------------------------------------------------------
% MODULE1_HELPER loads the historical datafile ifile, runs MODULE1 and stores the output data in K cluster-specific .mat files. 
% 
% INPUTS:   -- ifile:       file address of .mat input file
%           -- opath:       path where to save
%           -- K:           number of clusters
%           -- s_rng_seed:  random number seed 
%
% OUTPUTS:  -- exitcode:    0  if executed correctly, -1 if error is encountered
%            
%--------------------------------------------------------------

close all;

try
    % Load input historical file as a cell array
    fprintf('[MODULE1_HELPER] Loading historical dataset from %s ..',ifile)
    tStart = tic;
    X = struct2cell(load(ifile));
    % Transform cell array to a data matrix
    X = X{1};
    temp = dir(ifile);
    fprintf(' (%.2f MB) .. %.2f seconds\n',temp.bytes/1048576,toc(tStart));
    % If seed is not specified, 'shuffle' on current platform time   
    if (nargin < 5) || (s_rng_seed == 0)
        rng('shuffle','twister');
        rng_data = rng;
        fprintf('[MODULE1_HELPER] Random seed not specified -> set to %d (%s)\n',rng_data.Seed,rng_data.Type)
    else 
        rng(s_rng_seed,'twister');
        rng_data = rng;
        fprintf('[MODULE1_HELPER] Random seed already specified  -> set to %d (%s)\n',rng_data.Seed,rng_data.Type')
    end
    
%% Run module 1 and save output to MOD1  
    [MOD1] = MODULE1(X, K);
    
%% Ensure that all clusters have at least >1 observations
    for i = 1:K
        if size(MOD1.Z_c{i,1},1) <= 1 
            fprintf('[MODULE1] WARNING: Cluster %d has only %d observation ! \n',i,size(MOD1.Z_c{i,1},1))
        end  
    end    
       
%% For each cluster-model, save corresponding data
fprintf('[MODULE1] Saving all %d cluster data indices .. \n',K)
    for i = 1:K 
        % Define filename for this cluster -> convention is the 'MOD1_' file prefix followed by cluster index
        %Q cluster numbers in file names are zero based; thus i-1 instead of i, to accomodate iTesla computation platform
        filename = sprintf('%sMOD1_%d.mat', opath, i-1 ); 
        fprintf('        |- saving cluster %d from workspace to %s ..',i,filename)
        tStart = tic;
        % Construct moutput structure containing module1 output
        moutput.module1.k      = i;
        moutput.module1.w      = MOD1.w(i);
        moutput.module1.Z_c    = MOD1.Z_c{i,1};
        moutput.rng_data       = rng_data;
        moutput.errmsg = 'Ok';       
        % Save moutput to the specified file
        save(filename , '-struct', 'moutput','-v7.3'); 
        temp = dir(filename);
        fprintf(' (%.2f MB) .. %.2f seconds\n',temp.bytes/1048576,toc(tStart));
    end
    exitcode = 0;

%% Save statvar file
    fprintf('[MODULE1_HELPER] Saving statVars file from workspace to %s ..',statvarfile)        
    tStart = tic;
    moutput1.statVars = MOD1.statVars;
    %Q to improve efficiency, save clusters weights at this point (instead
    % of reloading all module1 clusters mod1 files later at module3 computation time)
    moutput1.w = MOD1.w;
    save(statvarfile, '-struct', 'moutput1','-v7.3'); 
    temp = dir(statvarfile);
    fprintf(' (%.2f MB) .. %.2f seconds\n',temp.bytes/1048576,toc(tStart));    
    
catch err
    % Store and display error message
   moutput.errmsg = err.message;
   disp(getReport(err,'extended'));
   % Save error mesage to the specificied ouptput file
   filename = sprintf('%s_error.mat', opath); 
   save(filename , '-struct', 'moutput','-v7.3'); 
   exitcode = -1;
end


% Exit MATLAB when finished
% exit(exitcode);
end
