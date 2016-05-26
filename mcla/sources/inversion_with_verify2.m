%
% Copyright (c) 2016, Ricerca sul Sistema Energetico – RSE S.p.A. <itesla@rse-web.it>
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

function [R A] = inversion_with_verify2(A,eps,itera,epsilo)
R = pinv(A);
I = eye(size(A,1)); As = A-I;
k=0;
b = norm(R*A-I,'fro');
while (b > eps/itera && k <= itera) 
    k = k+1;
%    Q = (abs(As)>thres);
    At = A+epsilo*eye(size(A));
    R = pinv(At);
    A = At;
    b = norm(R*A-I,'fro');
end
disp(['converging at iteration ' num2str(k)])
