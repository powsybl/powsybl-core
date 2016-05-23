%
% Copyright (c) 2016, Ioannis Konstantelos <i.konstantelos@imperial.ac.uk>
% and Mingyang Sun <mingyang.sun11@imperial.ac.uk> – Imperial College London
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

function exitcode = AGGREGATOR(opath3, statvarfile, K, ofile)

try   
%% Aggregate all cluster-model .mat outputs to a single .mat file
    fprintf('\n[AGGREGATOR] Aggregating all %d cluster-model outputs ..\n',K)
    Y = [];
    for cls = 1:K
        %Q to make integration easier, clusters file names are zero-based, thus cls-1 instead of cls 
        filename  = sprintf('%sMOD3_%d.mat', opath3, cls-1);
        fprintf('           |- loading cluster %s data from file %s ..', num2str(cls), filename);
        tStart = tic;
        load(filename);
        temp = dir(filename);
        fprintf(' (%.2f MB) .. %.2f seconds\n',temp.bytes/1048576,toc(tStart));
        Y = [Y ; Y_c];    
    end
    
 %% Add back all statVars columns
    
    % Load file
    fprintf('           |- loading statvarfile file %s ..', statvarfile);   
    tStart = tic;
    load(statvarfile);
    temp = dir(statvarfile);
    fprintf(' (%.2f MB) .. %.2f seconds\n',temp.bytes/1048576,toc(tStart));
    
    % append statvars columns to Y 
    fprintf('[AGGREGATOR] Appending stationary variables ..'); 
    tStart = tic;
    [NObs, ~] = size(Y);
    for i = 1:size(statVars,1)
        Y = insert_ind(Y,statVars(i,1)-1,ones(NObs,1)*statVars(i,2),2);
    end
    fprintf(' %.2f seconds\n',toc(tStart));
    
 %% Display size of sampled dataset
    [NObs,NVar] = size(Y);
    fprintf('[AGGREGATOR] Size of sampled dataset : %d observations of %d variables \n',NObs,NVar)       
 
 %% Store the combined output matrix
    moutput.Y       = Y;
    moutput.errmsg  = 'Ok';
    exitcode        = 0;

catch err   
    moutput.errmsg  = err.message;
    disp(getReport(err,'extended'));
    exitcode = -1;
end

%% Save final output file
fprintf('[AGGREGATOR] Saving final output to %s ...',ofile)
tStart = tic;
%Q for integration's sake, save as version v7
save(ofile, '-struct', 'moutput', '-v7');
temp = dir(ofile);
fprintf(' (%.2f MB) .. %.2f seconds\n',temp.bytes/1048576,toc(tStart));

% Exit MATLAB when finished
% exit(exitcode);
end
