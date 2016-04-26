%
% Copyright (c) 2016, Ioannis Konstantelos <i.konstantelos@imperial.ac.uk>
% and Mingyang Sun <mingyang.sun11@imperial.ac.uk> – Imperial College London
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

function [X] = modU(X)
%--------------------------------------------------------------
% modU removes any NaN, absolute 0 and absolute 1 entries.
%--------------------------------------------------------------
X(isnan(X)) = 0;
X(X <= 0.000000001)  = 0.000000001;
X(X >= 0.999999999)  = 0.999999999;
 
end

