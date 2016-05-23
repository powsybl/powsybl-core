%
% Copyright (c) 2016, Ioannis Konstantelos <i.konstantelos@imperial.ac.uk>
% and Mingyang Sun <mingyang.sun11@imperial.ac.uk> – Imperial College London
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

function R = modR(R)
%--------------------------------------------------------------
% modR replaces NaNs with zeros and ensures the diagonal of a matrix is 1
% This is done because most large datasets will have some variables that
% are zero throughout and this results in NaNs etc. This method allows us
% to pre-treat the correlation matrix and render it suitable for sampling purposes.
%            
%--------------------------------------------------------------
[~,NVar] = size(R);

R(isnan(R) == 1) = 0;

for i = 1:NVar
    for j = 1:NVar
        R(i,j) = 1;
    end
end

end
