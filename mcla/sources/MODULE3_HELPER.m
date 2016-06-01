%
% Copyright (c) 2016, Ioannis Konstantelos <i.konstantelos@imperial.ac.uk>
% and Mingyang Sun <mingyang.sun11@imperial.ac.uk> – Imperial College London
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

function exitcode = MODULE3_HELPER(m1file, m2file, ofile, k, NSam_c, s_rng_seed)
%--------------------------------------------------------------
% MODULE3_HELPER loads input files of modules 1 and 2, runs MODULE3 and saves the cluster-specific output to ofile 
% 
% INPUTS:   -- m1file:      filename of module1 input file
%           -- m2file:      filename of module2 input file
%           -- ofile:       output file for module3 (cluster-specific)
%           -- k:           cluster index
%           -- NSam_c:      number of samples to be generated
%           -- Tflag:       0 -> no truncation with multivariate Gaussian copula
%                           1 -> truncation with multivariate Gaussian copula
%           -- s_rng_seed:  random number seed 
%
% OUTPUTS:  -- exitcode:    0  if executed correctly, -1 if error is encountered
%            
%--------------------------------------------------------------

try
    % Load module 1 file
    fprintf('\n[MODULE3_HELPER | cluster %d] Loading module 1 file %s ..',k,m1file)
    tStart = tic;
    load(m1file);
    temp = dir(m1file);
    fprintf(' (%.2f MB) .. %.2f seconds\n',temp.bytes/1048576,toc(tStart));
    
    % Load module 2 file
    fprintf('[MODULE3_HELPER | cluster %d] Loading module 2 file %s ..',k,m2file)
    tStart = tic;
    load(m2file);
    temp = dir(m2file);
    fprintf(' (%.2f MB) .. %.2f seconds\n',temp.bytes/1048576,toc(tStart));
    
    % if seed is not specified, 'shuffle'  on current platform time    
    if (nargin < 6) || (s_rng_seed == 0)
        rng('shuffle','twister');
        rng_data = rng;
        fprintf('[MODULE3_HELPER | cluster %d] Random seed not specified -> set to %d (%s)\n',k,rng_data.Seed,rng_data.Type)
    else 
        rng(str2double(s_rng_seed),'twister');
        rng_data = rng;
        fprintf('[MODULE3_HELPER | cluster %d] Random seed already specified -> set to %d (%s)\n',k,rng_data.Seed,rng_data.Type')
    end
    
    %% Run module 3  -- number of samples to be generated  is module1.NSam_c
    [Y_c]   = MODULE3(module1,module2,NSam_c); 
    moutput.Y_c      = Y_c;
    moutput.rng_data = rng_data;
    moutput.errmsg   = 'Ok';
    exitcode         = 0;
catch err
   moutput.errmsg    = err.message;
   disp(getReport(err,'extended'));
   exitcode = -1;
end

%% Save cluster-model output file
fprintf('[MODULE3_HELPER | cluster %d] Saving output file to %s ..',k,ofile)
tStart = tic;
save(ofile, '-struct', 'moutput');
temp = dir(ofile);
fprintf(' (%.2f MB) .. %.2f seconds\n',temp.bytes/1048576,toc(tStart));

% Exit MATLAB when finished
% exit(exitcode);

end
