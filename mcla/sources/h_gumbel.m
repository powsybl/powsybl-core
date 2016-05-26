%
% Copyright (c) 2016, Ioannis Konstantelos <i.konstantelos@imperial.ac.uk>
% and Mingyang Sun <mingyang.sun11@imperial.ac.uk> – Imperial College London
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

function y = h_gumbel( x,U1,U2,r )

y=exp(-((-log(x)).^r+(-log(U2)).^r).^(1/r)).*(1./U2).*((-log(U2)).^(r-1)).*((-log(x)).^r+(-log(U2)).^r).^(1/r-1)-U1;

end

