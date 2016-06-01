%
% Copyright (c) 2016, Ioannis Konstantelos <i.konstantelos@imperial.ac.uk>
% and Mingyang Sun <mingyang.sun11@imperial.ac.uk> – Imperial College London
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

 function [RHOHAT,P_MAX,ntype,P] = copulachoose(U)

%%Input the copula and chose which one has the best Gof and output it
P=[];
%%Assume U Gaussian copula
RHO=copulafit('Gaussian',U);               % U
P(1,1)=NEW_PIT(U,RHO(1,2),1);             % NEW_PIT of U

%%Assume U  Student's t copula
[RHO_t,ll] =copulafit('t',U);              % U
P(2,1)=NEW_PIT(U,[ll,RHO_t(1,2)],2);      %% NEW_PIT of U

%%Assume U Clayton copula
RHO_c = copulafit('clayton',U);            % U
P(3,1)=NEW_PIT(U,RHO_c,3);                %% NEW_PIT of U

%% Rotate 1--90
U1=[(1-U(:,1)) U(:,2)];
RHO_c1 = copulafit('clayton',U1);             % U
P(4,1)=NEW_PIT(U,RHO_c1,3);               %% NEW_PIT of U

%% Rotate 2--270
U2=[U(:,1) (1-U(:,2))];
RHO_c2 = copulafit('clayton',U2);             % U
P(5,1)=NEW_PIT(U,RHO_c2,3);               %% NEW_PIT of U

%% Rotate 3--180
U3=[(1-U(:,1)) (1-U(:,2))];
RHO_c3 = copulafit('clayton',U3);             % U
P(6,1)=NEW_PIT(U,RHO_c3,3);               %% NEW_PIT of U

%%Assume U Frank copula
RHO_fr = copulafit('frank',U);             % U
P(7,1)=NEW_PIT(U,RHO_fr,4);               %% NEW_PIT of U

%%Assume U Gumbel copula
RHO_gu = copulafit('gumbel',U);            % U
P(8,1)=NEW_PIT(U,RHO_gu,5);               %% NEW_PIT of U

%% Rotate 1--90
RHO_gu1 = copulafit('gumbel',U1);            % U
P(9,1)=NEW_PIT(U,RHO_gu1,5);               %% NEW_PIT of U

%% Rotate 2--270
RHO_gu2 = copulafit('gumbel',U2);            % U
P(10,1)=NEW_PIT(U,RHO_gu2,5);               %% NEW_PIT of U

%% Rotate 3--180
RHO_gu3 = copulafit('gumbel',U3);            % U
P(11,1)=NEW_PIT(U,RHO_gu3,5);               %% NEW_PIT of U

R=[RHO(1,2) 0;ll,RHO_t(1,2);RHO_c 0;RHO_c1 0;RHO_c2 0;RHO_c3 0;RHO_fr 0;RHO_gu 0;RHO_gu1 0;RHO_gu2 0;RHO_gu3 0];
[P_MAX,index]=max(P);
           
RHOHAT=R(index,:); % Chose the best fit type of copula
       
if index==1

        RHOHAT=RHOHAT(1,1);
        ntype=1;
        
                elseif index==2
                    
                  RHOHAT=RHOHAT;
                  ntype=2;
                  
                         elseif index==3

                         RHOHAT=RHOHAT(1,1);
                         ntype=3;
                         
                                elseif index==4

                                RHOHAT=RHOHAT(1,1);
                                ntype=4;
                                      elseif index==5

                                        RHOHAT=RHOHAT(1,1);
                                        ntype=5;
                                        
                                        elseif index==6

                                        RHOHAT=RHOHAT(1,1);
                                        ntype=6;
                                        
                                            elseif index==7
                                            RHOHAT=RHOHAT(1,1);
                                            ntype=7;
                                            
                                                elseif index==8
                                                 RHOHAT=RHOHAT(1,1);   
                                                 ntype=8;   
                                                 
                                                    elseif index==9
                                                    RHOHAT=RHOHAT(1,1); 
                                                    ntype=9;    
                                                    
                                                        elseif index==10
                                                        RHOHAT=RHOHAT(1,1); 
                                                        ntype=10;   
                                                            
                                                            elseif index==11
                                                            RHOHAT=RHOHAT(1,1); 
                                                            ntype=11; 
end

end

