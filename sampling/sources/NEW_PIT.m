%
% Copyright (c) 2016, Ioannis Konstantelos <i.konstantelos@imperial.ac.uk>
% and Mingyang Sun <mingyang.sun11@imperial.ac.uk> – Imperial College London
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

function P = NEW_PIT(X,r,fam)

% This script realize the PIT and the Kolmogrove test, the input is the bivariate 
% variables, estimated parameters and the chosen familiy
Z(:,1) = X(:,2); % T1
Z(:,2) = X(:,1);                  
Z(:,2) = h_pit(Z(:,2),Z(:,1),r,fam); % T2
S=(norminv(Z(:,1),0,1).^2)+(norminv(Z(:,2),0,1).^2);  % S
S = mod_data(S);   % Limit the value of S 
SS=sort(S);
test_cdf = [SS,cdf('chi2',SS,2)];
[III,~]=find(diff(test_cdf(:,2))<0);
test_cdf(III+1,2)=test_cdf(III+1,2)+0.00001;
[~,P] = kstest(SS,'CDF',test_cdf);   % kolmogrove test    
          
end
