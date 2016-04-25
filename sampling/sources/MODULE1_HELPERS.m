%
% Copyright (c) 2016, Quinary <itesla@quinary.com>
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

function exitcode = MODULE1_HELPERS(ifile, opath, K_s, s_rng_seed_s)

disp('wp4.1 - module1 - v6.7');
disp(sprintf(' ifile:  %s',ifile));
disp(sprintf(' opath:  %s',opath));
disp(sprintf(' K:  %s',K_s));
disp(sprintf(' s_rng_seed:  %s', s_rng_seed_s));

K=str2double(K_s);
s_rng_seed=str2double(s_rng_seed_s);

tStart = tic;
statvarfile = sprintf('%sMOD1_statvars.mat', opath); 
exitcode=MODULE1_HELPER(ifile, opath, statvarfile, K, s_rng_seed);
fprintf('\nTotal [MODULE1] runtime : %.2f seconds\n',toc(tStart))
exit(exitcode);
end
