%
% Copyright (c) 2016, Ioannis Konstantelos <i.konstantelos@imperial.ac.uk>
% and Mingyang Sun <mingyang.sun11@imperial.ac.uk> – Imperial College London
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

function exitcode = MODULE2_HELPER(m1file, ofile, k, IR, Tflag)
%--------------------------------------------------------------
% MODULE2_HELPER loads input file, run MODULE2 and saves to specified output file 
% 
% INPUTS:   -- m1file:       filename of module1 input file
%           -- ofile:       output file for module2
%           -- k:           cluster index
%           -- IR:          Information Retainment criterion 
%           -- Tflag:       0 -> no truncation with multivariate Gaussian copula
%                           1 -> truncation with multivariate Gaussian copula
%
% OUTPUTS:  -- exitcode:    0  if executed correctly, -1 if error is encountered
%            
%--------------------------------------------------------------
try    
% Load input file
    tStart = tic;
    fprintf('\n[MODULE2_HELPER | cluster %d] Loading module1 file %s ..',k,m1file)
    load(m1file);
    temp = dir(m1file);
    fprintf(' (%.2f MB) .. %.2f seconds\n',temp.bytes/1048576,toc(tStart));
    
% Run module 2 
    [MOD2] = MODULE2(module1,IR,Tflag);
    moutput.module2   = MOD2;
    moutput.errmsg    = 'Ok';   
    exitcode = 0;
catch err
   moutput.errmsg=err.message;
   disp(getReport(err,'extended'));
   exitcode = -1;
end

%% Save output file to ofile
fprintf('[MODULE2_HELPER | cluster %d] Saving model parameters from workspace to %s ..',k,ofile)
tStart = tic;
save(ofile, '-struct', 'moutput','-v7.3');
temp = dir(ofile);
fprintf(' (%.2f MB) .. %.2f seconds\n',temp.bytes/1048576,toc(tStart));

% Exit MATLAB when finished
% exit(exitcode);
end
