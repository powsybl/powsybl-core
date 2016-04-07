%
% Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

% Authors: Quinary <itesla@quinary.com>

function exitcode=wp43dymadapter_transient(ifile, ofile, furtherparsfile)

close all; %% delete all figures

disp('dymola output adapter - Dynamic SmallSignal');
disp(sprintf(' ifile: %s', ifile));
disp(sprintf(' ofile: %s', ofile));

exitcode=0;

d=dymload(ifile);
bsim_t=dymget(d,'Time');

bNames=cellstr(d.name(:,:));

%bThetaRegexp='^((pwGenerator)|(gen_))+.*\.((theta)|(ANGLE))$';
bThetaRegexp='^gen_.*\.ANGLE$';
%bInertiaRegexp='^((pwGenerator)|(gen_))+.*\.H$';
bInertiaRegexp='^gen_.*\.H$';

bgensAttNamesTheta=bNames(find(not(cellfun('isempty', regexp(bNames,bThetaRegexp)))));
bgensAttNamesInertia=bNames(find(not(cellfun('isempty', regexp(bNames,bInertiaRegexp)))));

if (length(bgensAttNamesTheta) ~= length(bgensAttNamesInertia))
   error('Error. \n must have the same number of attributes')
end


disp(sprintf(' found %i generators.', length(bgensAttNamesTheta) ));


for i = 1:length(bgensAttNamesTheta)
    bgensThetaMatrix(i,:) = dymget(d,bgensAttNamesTheta{i});
    bgensInertiaMatrix(i,:) = dymget(d,bgensAttNamesInertia{i});
end
bgensThetaMatrix=transpose(bgensThetaMatrix);
bgensInertiaMatrix=unique(transpose(bgensInertiaMatrix), 'rows');


moutput.t=bsim_t;
moutput.delta=bgensThetaMatrix;
moutput.M=2*bgensInertiaMatrix;
moutput.M1=bgensInertiaMatrix;


if (nargin > 2)
        disp(sprintf(' parameters file: %s', furtherparsfile));
        load(furtherparsfile);
        % put here params
        % e.g. moutput.param1=param1
end 


save(ofile , '-struct', 'moutput','-v7.3'); 

end
