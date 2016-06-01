%
% Copyright (c) 2016, Quinary <itesla@quinary.com>
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

function exitcode = MODULE3PRE_HELPERS(opath1, ofile, K_s, NSam_s, s_rng_seed_s)
disp('wp4.1 - pre - module3 - v6.4');
disp(sprintf(' opath1: %s', opath1));
disp(sprintf(' ofile: %s', ofile));
disp(sprintf(' K: %s', K_s));
disp(sprintf(' NSam: %s', NSam_s));
disp(sprintf(' s_rng_seed: %s', s_rng_seed_s));


  K=str2double(K_s);
  NSam=str2double(NSam_s);
  s_rng_seed=str2double(s_rng_seed_s);
  
close all;

moutput.errmsg = 'Ok';       
try  
  %% Define how many samples to be generated from each cluster-model according to cluster-weights w
fprintf('\n[SCRIPT] Defining sampling pattern ..')
tStart = tic;
% Read in cluster weights from the statvar file (stored by module1)
load(opath1);

% cls defines which cluster-model will generate each sample
cls = rand_gen(1:K,w,NSam);
% The vector NSam_c defines how many samples each cluster-model will generate
NSam_c = zeros(1,K);
for k = 1:K 
   NSam_c(k) = numel(cls(cls == k));  
end
fprintf(' %.2f seconds\n',toc(tStart));
moutput.NSam_c=NSam_c;

save(ofile, '-struct', 'moutput');
exitcode=0;

catch err
 moutput.errmsg    = err.message;
 disp(getReport(err,'extended'));
 exitcode = -1;
end

%exit(exitcode);

end
