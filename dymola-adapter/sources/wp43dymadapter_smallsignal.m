%
% Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

% Authors: Quinary <itesla@quinary.com>

function exitcode=wp43dymadapter_smallsignal(ifile, ofile, furtherparsfile)

close all; %% delete all figures

disp('dymola output adapter - Dynamic SmallSignal');
disp(sprintf(' ifile: %s', ifile));
disp(sprintf(' ofile: %s', ofile));

exitcode=0;

d=dymload(ifile);
bsim_t=dymget(d,'Time');

bNames=cellstr(d.name(:,:));

%PsRegexp='^((line\_)|(pwLine)).*\.Ps';
PsRegexp='^line\_.*\.Ps';
%PsRegexp='^((line\_)|(pwLine)).*\.Pr';
PrRegexp='^line\_.*\.Pr';

blinesAttNamesPs=bNames(find(not(cellfun('isempty', regexp(bNames, PsRegexp)))));
blinesAttNamesPr=bNames(find(not(cellfun('isempty', regexp(bNames, PrRegexp)))));

if (length(blinesAttNamesPs) ~= length(blinesAttNamesPr))
   error('Error. \n must have the same number of attributes')
end

disp(sprintf(' found %i lines.', length(blinesAttNamesPs) ));

for i = 1:length(blinesAttNamesPs)
    bPsMatrix(i,:) = dymget(d,blinesAttNamesPs{i});
    bPrMatrix(i,:) = dymget(d,blinesAttNamesPr{i});
end
bPsMatrix=transpose(bPsMatrix);
bPrMatrix=transpose(bPrMatrix);

moutput.t=bsim_t;
moutput.LP1=max(bPrMatrix,bPsMatrix);


%moutput.step_min = 0.03;  % Minimum step size (full details in future documentation)
%moutput.var_min = 0.3;    % [0-1] Filter signals with varianza smaller than "var_min" 
%moutput.f = [0.1,10];    % [fmin,fmax] Range of  frequencies of interest in Hz
%moutput.d =[0,3,6];      % Damping values in percent (%) where the index distances will be calculated
%moutput.Nm  =10;          % Number of modes used in Prony to reconstruct the signals

if (nargin > 2)
        disp(sprintf(' parameters file: %s', furtherparsfile));
        load(furtherparsfile);
        moutput.step_min = step_min;  % Minimum step size (full details in future documentation)
        moutput.var_min = var_min;    % [0-1] Filter signals with varianza smaller than "var_min" 
        moutput.f = f;    % [fmin,fmax] Range of  frequencies of interest in Hz
        moutput.d = d;      % Damping values in percent (%) where the index distances will be calculated
        moutput.Nm  = Nm;          % Number of modes used in Prony to reconstruct the signals
        moutput.f_instant = f_instant;
        moutput.f_duration = f_duration;
end 

save(ofile , '-struct', 'moutput','-v7.3'); 

end
