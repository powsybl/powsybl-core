%
% Copyright (c) 2016, Ioannis Konstantelos <i.konstantelos@imperial.ac.uk>
% and Mingyang Sun <mingyang.sun11@imperial.ac.uk> – Imperial College London
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

function H_inv = h_inv_ex(U1,U2,theta,n)

% Keep only real part
U1 = real(U1);
U2 = real(U2);

switch n 

% Gaussian     
    case 1 
    r = theta;
    b1 = norminv(U1,0,1);% inverse standard normal distribution    
    b2 = norminv(U2,0,1);% inverse standard normal distribution 
    B = b1*(1-r^2)^0.5+r*b2;
    H_inv = normcdf(B,0,1);  % h^-1 function

% Student's t 
    case 2   
    r1 = theta(1);
    r2 = theta(2);
    b1 = tinv(U1,(r1+1));% inverse standard student's t distribution  
    b2 = tinv(U2,r1);% inverse standard student's t distribution 
    AA = (b1.*((1-r2^2).*(r1+b2.^2)./(r1+1)).^0.5)+r2.*b2;
    H_inv = tcdf(AA,r1);
    
% Clayton
    case 3 
    r = theta;
    a = U2.^(r+1); b=-r/(r+1);c=(U1.*a).^b+1-U2.^(-r);
    H_inv = c.^(-1/r); 
    
% Clayton 90    
    case 4 
    U1 = 1-U1;
    r = theta;
    a = U2.^(r+1); b=-r/(r+1);c=(U1.*a).^b+1-U2.^(-r);
    H_inv = c.^(-1/r);  
    H_inv = 1-H_inv;
    
% Clayton 270
    case 5 
    U2 = 1-U2;   
    r = theta;
    a = U2.^(r+1); b=-r/(r+1);c=(U1.*a).^b+1-U2.^(-r);
    H_inv = c.^(-1/r);  
    
% Clayton 180
    case 6 
    U1 = 1-U1;
    U2 = 1-U2;    
    r = theta;
    a = U2.^(r+1); b=-r/(r+1);c=(U1.*a).^b+1-U2.^(-r);
    H_inv = c.^(-1/r);  
    H_inv = 1-H_inv;
    
%Frank
    case 7 
    r = theta;
    a = (1-exp(-r)); b=(U1.^-1)-1; c=exp(-r*U2);
    H_inv = (-log(1-a./(b.*c+1)))./r;
    
% Gumbel
    case 8 
    [qq,~] = size(U1);  
    for i = 1:qq
        U11 = U1(i); 
        U22 = U2(i);
        H_inv(i,1)= bisection(theta,U11,U22);
    end
    
% Gumbel 90  
    case 9 
    U1 = 1-U1;
    [qq,~] = size(U1);  
    for i = 1:qq
        U11 = U1(i); 
        U22 = U2(i);
        H_inv(i,1) = bisection(theta,U11,U22);
    end
    H_inv = 1-H_inv;
    
% Gumbel 270
    case 10 
    U2=1-U2;
    [qq,~] = size(U1);  
    for i = 1:qq
        U11 = U1(i); 
        U22 = U2(i);
        H_inv(i,1)  = bisection(theta,U11,U22);
    end
    
% Gumbel 180
    case 11 
    U1 = 1-U1;
    U2 = 1-U2;
    [qq,~]=size(U1);  
    for i=1:qq
        U11=U1(i); U22=U2(i);
        H_inv(i,1)  = bisection(theta,U11,U22);
    end
    H_inv=1-H_inv;

end

end
