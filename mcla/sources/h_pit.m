%
% Copyright (c) 2016, Ioannis Konstantelos <i.konstantelos@imperial.ac.uk>
% and Mingyang Sun <mingyang.sun11@imperial.ac.uk> – Imperial College London
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

function H = h_pit(U1,U2,theta,n)


switch n 

case 1 % Gaussian
    
    r=theta;
    b1=norminv(U1,0,1);% inverse standard normal distribution    
    b2=norminv(U2,0,1);% inverse standard normal distribution 
    AA = (b1-r*b2)/(1-r^2)^0.5;
    H = normcdf(AA,0,1); 
    
    case 2 % Student's t
        
        r1=theta(1);r2=theta(2);
        b1=tinv(U1,r1);% inverse standard student's t distribution  
        b2=tinv(U2,r1);% inverse standard student's t distribution 
        AA = (b1-r2*b2)./((1-r2^2).*(r1+b2.^2)./(r1+1)).^0.5;
        H = tcdf(AA,(r1+1));
        
        case 3 % Clayton
            r=theta;
            a1=-r-1;
            a2=-1-(1/r);
            H = U2.^a1.*((U1.^-r)+(U2.^-r)-1).^a2;
           

                          case 4  %Frank
                          r=theta;
                          a1=exp(-r*U2);
                          a2=1-exp(-r*U1);
                          a3=1-exp(-r);
                          H=a1./(a1-1+(a3./a2));
              
                                case 5 % Gumbel
                                r=theta;
                                b1=(-log(U1));
                                b2=(-log(U2));
                                C12=exp(-(b1.^r+b2.^r).^(1/r));
                                K=(1./U2);
                                p = ((b1).^r+(b2).^r).^(1/r-1);
                                H=C12.*K.*((b2).^(r-1)).*p;

                          

                                                                        
 
            
            
end

end

