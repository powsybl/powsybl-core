%
% Copyright (c) 2016, Ioannis Konstantelos <i.konstantelos@imperial.ac.uk>
% and Mingyang Sun <mingyang.sun11@imperial.ac.uk> – Imperial College London
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

function [U] = map_ecdf(X)
%----------------------------------------------------
% map_ecdf maps input data X to the uniform domain by transforming 
% according to the ecdf. 
% Note that duplicates are interpolated instead of resulting in the same probability.
%
% INPUTS:   -- X:         [NObs, NVar] input array
%
%----------------------------------------------------

%% Pre-allocate U 
[NObs,NVar]=size(X);
U = zeros(NObs,NVar);

%% Perform ecdf transform
if NObs > 1
    
    for i = 1:NVar     
        % Map each variable through ecdf 
         [~,idx] = sort(X(:,i));
         [~, new_idx] = sort(idx);
         U(:,i) = new_idx/NObs;
    end
else
    % If there is a single observation (this should not happen), ecdf is taken halfway to 0.5 
    U = 0.5*ones(NObs,NVar);
end
end



