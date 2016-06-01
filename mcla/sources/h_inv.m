%
% Copyright (c) 2016, Ioannis Konstantelos <i.konstantelos@imperial.ac.uk>
% and Mingyang Sun <mingyang.sun11@imperial.ac.uk> – Imperial College London
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

function H_inv = h_inv(U1,U2,theta,n)


switch n 

case 1 % Gaussian
    
    r=theta;
    b1=norminv(U1,0,1);% inverse standard normal distribution    
    b2=norminv(U2,0,1);% inverse standard normal distribution 
    B = b1*(1-r^2)^0.5+r*b2;
    H_inv = normcdf(B,0,1);  % h^-1 function
    
    case 2 % Student's t
        
        r1=theta(1);r2=theta(2);
        b1=tinv(U1,(r1+1));% inverse standard student's t distribution  
        b2=tinv(U2,r1);% inverse standard student's t distribution 
        AA = (b1.*((1-r2^2).*(r1+b2.^2)./(r1+1)).^0.5)+r2.*b2;
        H_inv = tcdf(AA,r1);
        
        case 3 % Clayton
            r=theta;
            a=U2.^(r+1); b=-r/(r+1);c=(U1.*a).^b+1-U2.^(-r);
            H_inv = c.^(-1/r);   
            
            case 4 %Frank
                r=theta;
                a=(1-exp(-r)); b=(U1.^-1)-1; c=exp(-r*U2);
                H_inv=(-log(1-a./(b.*c+1)))./r;
            
            case 5 % Gumbel
            [qq,tt]=size(U1);  
            for i=1:qq
            U11=U1(i); U22=U2(i);
            H_inv(i,1)  = bisection(theta,U11,U22);
            end
                      
end

end
