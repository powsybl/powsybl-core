%
% Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

% Authors: Quinary <itesla@quinary.com>

function exitcode=wp43dymadapter_underovervoltage(ifile, ofile, furtherparsfile)

close all; %% delete all figures

disp('dymola output adapter - Dynamic SmallSignal');
disp(sprintf(' ifile: %s', ifile));
disp(sprintf(' ofile: %s', ofile));

exitcode=0;


d=dymload(ifile);
bsim_t=dymget(d,'Time');

bNames=cellstr(d.name(:,:));


%% this worked with the old PowerSystem.mo lib
%bVMagnitudeRegexp='^bus_.*\.V$';

bVMagnitudeRegexp='^bus_.*\.V$';

bbusAttNamesVMagnitude=bNames(find(not(cellfun('isempty', regexp(bNames,bVMagnitudeRegexp)))));

disp(sprintf(' found %i buses.', length(bbusAttNamesVMagnitude) ));

for i = 1:length(bbusAttNamesVMagnitude)
    bbusVMagnitudeMatrix(i,:) = dymget(d,bbusAttNamesVMagnitude{i});
    
end
bbusVMagnitudeMatrix=transpose(bbusVMagnitudeMatrix);


moutput.t=bsim_t;
moutput.V=bbusVMagnitudeMatrix;

%moutput.p=3;
%moutput.d=1;

if (nargin > 2)
        disp(sprintf(' parameters file: %s', furtherparsfile));
        load(furtherparsfile);
        moutput.p = p;
        moutput.d = d;
end 

save(ofile , '-struct', 'moutput','-v7.3'); 

end
