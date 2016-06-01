%
% Copyright (c) 2016, Ioannis Konstantelos <i.konstantelos@imperial.ac.uk>
% and Mingyang Sun <mingyang.sun11@imperial.ac.uk> – Imperial College London
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

function outList = sQuantile(W, U)
%----------------------------------------------------
% sQuantile maps the sorted input data list in U from the uniform domain to the
% actual domain denoted by the sorted vector W
%
%----------------------------------------------------
idx = ceil(length(W)*U);
outList = W(idx + not(idx));
end
