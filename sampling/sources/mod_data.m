%
% Copyright (c) 2016, Ioannis Konstantelos <i.konstantelos@imperial.ac.uk>
% and Mingyang Sun <mingyang.sun11@imperial.ac.uk> – Imperial College London
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

function UM = mod_data(U)
%UNTITLED3 Summary of this function goes here
%   Detailed explanation goes here
[q,t]=size(U);
a=mean(U(isfinite(U)));
for i=1:q
    for j=1:t
                       
        if U(i,j)==-inf
          U(i,j)=a;
        end
        if U(i,j)==inf
          U(i,j)=a;
        end
        
    end
end

for i=1:q
    for j=1:t
                       
        if U(i,j)-a>=5*a
             U(i,j)=a;
        end
        
    end
end

U(isnan(U))=0.5; 
UM=U;

end

