%
% Copyright (c) 2016, Ioannis Konstantelos <i.konstantelos@imperial.ac.uk>
% and Mingyang Sun <mingyang.sun11@imperial.ac.uk> – Imperial College London
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

function [ X_NEW,n_cls ] = MODULE3_SINGLE( module1,module2)
% MODULE3: GENERATE A SINGLE SAMPLE WITH GIVRN DATA FROM MODULE 1 AND 2
% Inputs:
% module1: Output from Module1
% module2: Output from Module2
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
[m,k] = size(module1.clusters_p);   % Get the number of clusters k
%% Randomly take an number between 1 to k with given pmf
x0=1:k;
pmf=module1.clusters_p;
n_cls=rand_gen(x0,pmf,1);
%% Take the copula model for this cluster
PARA1 = module2.allparas;
PARA=PARA1{n_cls,1};
THETA_cls = PARA.THETA;
N_cls     = PARA.type;
Y_c       = module1.clusters;
Yi        = Y_c{n_cls,1};
[my,Nr]   = size(Yi);


%% Sampling a N-dimensional C-vine %%
XX=zeros(1,Nr);
V=[];
W=rand(1,Nr);
XX(:,1) = W(:,1);
V(:,1,1) = W(:,1);

for i=2:Nr

   V(:,i,1)=W(:,i);

       for k=i-1:-1:1
            r=THETA_cls{k,(i-k)}{1}; 
            V(:,i,1)=h_inv(V(:,i,1),V(:,k,k),r,N_cls(k,(i-k)));
       end

            XX(:,i)=V(:,i,1);

            if i==Nr
               break;
            end

                for j=1:i-1
                    r=THETA_cls{j,(i-j)}{1}; 
                    V(:,i,j+1)=h(V(:,i,j),V(:,j,j),r,N_cls(j,(i-j)));
                end
end
        
        % Data Modification
        [mxx,nxx]=size(XX);
        for i=1:mxx
            for j=1:nxx
                
                if XX(i,j)<10^-6
                    XX(i,j)=0.01;
                elseif XX(i,j)>1-10^-6
                     XX(i,j)=0.9999;
                end
                
            end
        end
        
        XX(isnan(XX))=0.5;
        
        for i=1:Nr      
           Y_NEW(:,i)=quantile(Yi(:,i),XX(:,i));
        end
        
     

%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%% After Adding Noise Back-projected data %%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
Reduced_N  = Nr;
N_sample   = 1;
Ev         = module1.Ev;
Nb         = module1.Nb;
MEAN       = module1.MEAN;
Ev_I       = Ev(:,Reduced_N+1:end);                   % Used for Generating Noise
sigma      = std(Ev_I);       
% keyboard% Variance of Noise
                           % Variance of Noise
noise=zeros(N_sample,(Nb-Reduced_N));
for cls=1:(Nb-Reduced_N)
noise(:,cls) = normrnd(0,sigma(cls),N_sample,1);      % Noise Generating
end
Y_ext = [Y_NEW  noise];% Add Gaussian noise to emulate discarded principal components
X_back_0_ext=Y_ext*Ev';   
X_NEW=X_back_0_ext+repmat(MEAN,N_sample,1); % Final Back-Project Points


end

