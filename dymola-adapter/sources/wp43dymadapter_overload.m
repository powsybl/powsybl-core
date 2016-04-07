%
% Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

% Authors: Quinary <itesla@quinary.com>

function exitcode=wp43dymadapter_overload(ifile, ofile, furtherparsfile)

close all; %% delete all figures

disp('dymola output adapter - Overload');
disp(sprintf(' ifile: %s', ifile));
disp(sprintf(' ofile: %s', ofile));

exitcode=0;

d=dymload(ifile);
bsim_t=dymget(d,'Time');
    
bNames=cellstr(d.name(:,:));

%SsRegexp='^((line\_)|(pwLine)).*\.Ss';
SsRegexp='^line\_.*\.Ss';
%SrRegexp='^((line\_)|(pwLine)).*\.Se';
SrRegexp='^line\_.*\.Sr';

blinesAttNamesSs=bNames(find(not(cellfun('isempty', regexp(bNames, SsRegexp)))));
blinesAttNamesSr=bNames(find(not(cellfun('isempty', regexp(bNames, SrRegexp)))));

if (length(blinesAttNamesSs) ~= length(blinesAttNamesSr))
   error('Error. \n must have the same number of attributes')
end

disp(sprintf(' found %i lines.', length(blinesAttNamesSs) ));

for i = 1:length(blinesAttNamesSs)
    bSsMatrix(i,:) = dymget(d,blinesAttNamesSs{i});
    bSrMatrix(i,:) = dymget(d,blinesAttNamesSr{i});
end
bSsMatrix=transpose(bSsMatrix);
bSrMatrix=transpose(bSrMatrix);

moutput.t=bsim_t;
moutput.S=max(bSsMatrix,bSrMatrix);

if (nargin > 2)
        disp(sprintf(' parameters file: %s', furtherparsfile));
        load(furtherparsfile);
        moutput.p = p;
        moutput.d = d;
end 

save(ofile , '-struct', 'moutput','-v7.3'); 

end
