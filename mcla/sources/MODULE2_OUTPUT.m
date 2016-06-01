%
% Copyright (c) 2016, Ricerca sul Sistema Energetico – RSE S.p.A. <itesla@rse-web.it>
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

function [ module2 ] = MODULE2_OUTPUT( module1,CoreNum )
% Output the vine copula models for all clusters
% Input: CoreNum: number of cores
if matlabpool('size')<=0
    matlabpool('open','local',CoreNum); 
else
    disp('Already initialized'); 
end
[m,k] = size(module1.clusters_p);
Y_c    = module1.clusters;
%% Parallel Parameterizition
parfor cluster=1:k
    
       Yi = Y_c{cluster,1};
       [para{cluster,1}] = MODULE2( module1,cluster);

end

for i=1:k
    
       PARA = para{i,1};
       pp(i) = PARA.pvalue_all*module1.clusters_p(i) ;

end
module2.allparas=para;
module2.pvalue= sum(pp);
end

