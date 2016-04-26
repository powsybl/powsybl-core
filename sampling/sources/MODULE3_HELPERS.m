%
% Copyright (c) 2016, Quinary <itesla@quinary.com>
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

function exitcode = MODULE3_HELPERS(m1file, m2file, nsam_c_file ,ofile, k_s, s_rng_seed_s)
disp('wp4.1 - module3 - v6.7');
disp(sprintf(' m1file: %s', m1file));
disp(sprintf(' m2file: %s', m2file));
disp(sprintf(' nsam_c_file: %s', nsam_c_file));
disp(sprintf(' ofile: %s', ofile));
disp(sprintf(' k: %s', k_s));
disp(sprintf(' s_rng_seed: %s', s_rng_seed_s));

  k=str2double(k_s)+1;
  s_rng_seed=str2double(s_rng_seed_s);


try
  load(nsam_c_file);
  NSam_ck=NSam_c(k);
  
  tStart=tic;
  exitcode=MODULE3_HELPER(m1file, m2file, ofile, k, NSam_ck, s_rng_seed);
  fprintf('\nTotal [MODULE3 | cluster %d] runtime : %.2f seconds\n',k,toc(tStart));
  
catch err
   moutput.errmsg    = err.message;
   disp(getReport(err,'extended'));
   exitcode = -1;
end
  
exit(exitcode);

end
