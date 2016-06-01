%
% Copyright (c) 2016, Ricerca sul Sistema Energetico – RSE S.p.A. <itesla@rse-web.it>
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

function [X_NEW1 ] = run_module3(MOD1,MOD2,scenarios)

K = length(MOD1.w);

cls = rand_gen(1:K,MOD1.w,scenarios);
% The vector NSam_c defines how many samples each cluster-model will generate
NSam_c = zeros(1,K);
for k = 1:K 
   NSam_c(k) = numel(cls(cls == k));  
end
X_NEW1=[];modu=struct;

% matlabpool commented:  on linux, throws a 
%      Warning: MATLAB was unable to open a pseudo-tty: Unknown error [0,1].
%matlabpool open
parfor i = 1:K
    
    [ Y_c  ] = MODULE3(MOD1,MOD2.allparas{i},NSam_c(i),i);
    
    X_NEW10{i} = Y_c;
end
%matlabpool close
X_NEW1=[];
for k = 1:K 
   X_NEW1 = [ X_NEW1; X_NEW10{k}];  
end
