%
% Copyright (c) 2016, Quinary <itesla@quinary.com>
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

function exitcode = AGGREGATOR(opath3, statvarfile, K_s, ofile)

disp('wp4.1 - aggregator - v6.7');
disp(sprintf(' opath3:  %s', opath3));
disp(sprintf(' statvarfile:  %s', statvarfile));
disp(sprintf(' K:  %s',K_s));
disp(sprintf(' ofile:  %s', ofile));

  
  K=str2double(K_s);
  
  exitcode=AGGREGATOR(opath3, statvarfile, K, ofile);
  
  exit(exitcode);
end
