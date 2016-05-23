%
% Copyright (c) 2016, Quinary <itesla@quinary.com>
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

function exitcode = MODULE2_HELPERS(m1file, ofile, k_s, IR_s, Tflag_s)

disp('wp4.1 - module2 - v6.7');
disp(sprintf(' m1file: %s', m1file));
disp(sprintf(' ofile: %s', ofile));
disp(sprintf(' k: %s', k_s));
disp(sprintf(' IR: %s', IR_s));
disp(sprintf(' Tflag: %s', Tflag_s));


%k=str2double(k_s);
k=str2double(k_s)+1;
IR=str2double(IR_s);
Tflag=str2double(Tflag_s);
tStart=tic;
exitcode=MODULE2_HELPER(m1file,ofile,k,IR,Tflag);
fprintf('\nTotal [MODULE2 | cluster %d] runtime : %.2f seconds\n',k,toc(tStart));
exit(exitcode);
end
