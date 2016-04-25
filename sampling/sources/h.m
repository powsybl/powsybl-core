%
% Copyright (c) 2016, Ioannis Konstantelos <i.konstantelos@imperial.ac.uk>
% and Mingyang Sun <mingyang.sun11@imperial.ac.uk> – Imperial College London
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

function H = h(U1,U2,theta,n)

U1(find(U1<10^-6))=10^-6; 
U1(find(U1>1-10^-6))=1-10^-6;
U2(find(U2<10^-6))=10^-6; 
U2(find(U2>1-10^-6))=1-10^-6;

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
            
            case 4 % Clayton 90
            r=theta;
            a1=-r-1;
            a2=-1-(1/r);
            U1=1-U1;
            H = U2.^a1.*((U1.^-r)+(U2.^-r)-1).^a2; 
            H=1-H;    
                
                case 5 % Clayton 270
                r=theta;
                a1=-r-1;
                a2=-1-(1/r);
                U2=1-U2;
                H = U2.^a1.*((U1.^-r)+(U2.^-r)-1).^a2;         
                           
                    case 6 % Clayton 180
                    r=theta;
                    a1=-r-1;
                    a2=-1-(1/r);
                    U2=1-U2;U1=1-U1;
                    H = U2.^a1.*((U1.^-r)+(U2.^-r)-1).^a2;  
                    H=1-H;

                          case 7  %Frank
                          r=theta;
                          a1=exp(-r*U2);
                          a2=1-exp(-r*U1);
                          a3=1-exp(-r);
                          H=a1./(a1-1+(a3./a2));
              
                                case 8 % Gumbel
                                r=theta;
                                b1=(-log(U1));
                                b2=(-log(U2));
                                C12=exp(-(b1.^r+b2.^r).^(1/r));
                                K=(1./U2);
                                p = ((b1).^r+(b2).^r).^(1/r-1);
                                H=C12.*K.*((b2).^(r-1)).*p;

                                    case 9 % Gumbel 90
                                    U1=1-U1;
                                    r=theta;
                                    b1=(-log(U1));
                                    b2=(-log(U2));
                                    C12=exp(-(b1.^r+b2.^r).^(1/r));
                                    K=(1./U2);
                                    p = ((b1).^r+(b2).^r).^(1/r-1);
                                    H=C12.*K.*((b2).^(r-1)).*p;
                                    H=1-H;
                                            case 10 % Gumbel 270
                                            U2=1-U2;
                                            r=theta;
                                            b1=(-log(U1));
                                            b2=(-log(U2));
                                            C12=exp(-(b1.^r+b2.^r).^(1/r));
                                            K=(1./U2);
                                            p = ((b1).^r+(b2).^r).^(1/r-1);
                                            H=C12.*K.*((b2).^(r-1)).*p;

                                                    case 11 % Gumbel 180
                                                    U2=1-U2;U1=1-U1;
                                                    r=theta;
                                                    b1=(-log(U1));
                                                    b2=(-log(U2));
                                                    C12=exp(-(b1.^r+b2.^r).^(1/r));
                                                    K=(1./U2);
                                                    p = ((b1).^r+(b2).^r).^(1/r-1);
                                                    H=C12.*K.*((b2).^(r-1)).*p;
                                                    H=1-H;
%                                                               case 12 % BB1
%                                                               th=theta(1); de=theta(2);
%                                                               t1 = U1.^(-th);      %%pow(u[i],-th);
%                                                               t2 = t1-1.;
%                                                               t3 =  t2.^de;        %%pow(t2,de);
%                                                               t16 = 1./U1;
%                                                               t17 = 1./t2;
%                                                               t4 =  U2.^(-th);     %%pow(v[i],-th);
%                                                               t5 = t4-1.;
%                                                               t6 = t5.^(de);       %%pow(t5,de);
%                                                               t7 = t3+t6;
%                                                               t9 = t7.^(1/de);     %%pow(t7,1/de);
%                                                               t10 = 1+t9;
%                                                               t12 = t10.^(-1/th);  %%pow(t10,-1/th);
%                                                               t13 = t12*t9;
%                                                               t20 = 1./t10;
%                                                               H= t13.*t3.*t1.*t16.*t17./t7.*t20;             
%  
% case 13 %BB6
% th=theta(1); de=theta(2);
% t1 = 1-U1;
% t2 = t1.^(th);    %%pow(t1,th);
% t3 = 1-t2;
% t4 = log(t3);
% t5 = (-t4).^(de); %%pow(-t4,de);
% t12 = 1/de;
% t16 = 1/th;
% t6 = 1-U2;
% t7 = t6.^(th);     %%pow(t6,th);
% t8 = 1-t7;
% t9 = log(t8);
% t10 = (-t9).^(de); %%pow(-t9,de);
% t11 = t5+t10;
% t13 = t11.^(t12);  %%pow(t11,t12);
% t14 = exp(-t13);
% t15 = 1-t14;
% t17 = t15.^(t16);   %% pow(t15,t16);
% H = -t17.*t13.*t5.*t2./t1./t3./t4./t11.*t14./t15;    
%     
%         case 14 %BB7
%         th=theta(1); de=theta(2);           
%         t1 = 1-U1;
%         t2 = t1.^(th);      %%pow(t1,1.0*th);
%         t3 = 1-t2;
%         t4 = t3.^(-de);     %%pow(t3,-1.0*de);
%         t6 = (1-U2).^(th);  %%pow(1.0-v[i],1.0*th);
%         t8 = (1-t6).^(-de); %%pow(1.0-t6,-1.0*de);
%         t9 = t4+t8-1;
%         t11 = t9.^(-1/de);  %%pow(t9,-1.0/de);
%         t12 = 1-t11;
%         t14 = t12.^(1/th);  %%pow(t12,1.0/th);
%         H = t14.*t11.*t4.*t2./t1./t3./t9./t12;    
% 
%                     case  15 %BB8   
%                     th=theta(1); de=theta(2);
%                     t2 = 1-de.*U1;
%                     t3 = t2.^(th);  %%pow(t2,th);
%                     t10 = 1-de;
%                     t11 = t10.^(th); %%pow(t10,th);
%                     t12 = 1-t11;
%                     t13 = 1./t12;
%                     t16 = 1/th;
%                     t6 = 1-de.*U2;
%                     t7 = t6.^(th);     %%pow(t6,th);
%                     t8 = 1.0-t7;
%                     t15 = 1-(1-t3).*t8.*t13;
%                     t17 = t15.^(t16);  %%pow(t15,t16);
%                     H = t17.*t3./t2.*t8.*t13./t15;

            
end

end

