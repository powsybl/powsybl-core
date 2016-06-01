%
% Copyright (c) 2016, Ioannis Konstantelos <i.konstantelos@imperial.ac.uk>
% and Mingyang Sun <mingyang.sun11@imperial.ac.uk> – Imperial College London,
% Ricerca sul Sistema Energetico – RSE S.p.A. <itesla@rse-web.it>
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

function [Y_c ] = MODULE3( MOD1, module2, NSam_c,quale)
%--------------------------------------------------------------
% MODULE3 samples NSam_c samples from the truncated C-Vine model of module2
% 
% INPUTS:   -- module1:         input struct from module 1
%           -- module2:         input struct from module 2
%           -- NSam_c:          samples to be generated
%
% OUTPUTS:  -- k:               cluster index
%           -- Y_c:             output samples in actual domain
%           -- seeds:           seed to reproduce same random numbers
%
%--------------------------------------------------------------
%% Save some input data locally 
k    = module2.k;
% Nr is number of retained dimensions
Nr   = module2.Nr;
%Nr_T is the number of dimensions modelled using Gaussian copula (including Nr)
Nr_T = size(module2.R,2);
      
module1.k      = quale;
    module1.w      = MOD1.w(quale);
    
    module1.Z_c    = MOD1.Z_c{quale,1};

[NObs_c,NVar] = size(module1.Z_c); 

fprintf('[MODULE3 | cluster %d] %d samples to be generated\n',module2.k,NSam_c)

%% Sample a truncated C-vine 
if NSam_c == 0 
    % Skip sampling if number of samples to be generated is zero
    Y_c = [];
else
    %% Sample from the Gaussian copula
    if Nr_T > 0      
        fprintf('[MODULE3 | cluster %d] Sampling %d variables from the Gaussian copula and %d variables independently in [0,1]..',k,Nr_T,NVar-Nr_T)
        tStart = tic;
        % reconstruct the full correlation matrix from the upper diagonal
        % and sample from the resulting Gaussian copula
        W = copularnd('Gaussian',module2.R + transpose(module2.R) - diag(diag(module2.R)),NSam_c);
        % Append the remaining dimensions as independent [0,1] variables
        W = [modU(W) rand(NSam_c,NVar - Nr_T)];

        % W1 is the high-information part to be further sampled from C-Vine
        W1 = W(:,1:Nr);

        % W2 is the low-information part 
        W2 = W(:,Nr+1:end);
        fprintf(' %.2f seconds\n',toc(tStart));
    else
        W1 = rand(NSam_c,Nr);
        W2 = rand(NSam_c,NVar - Nr);      
    end    
    
    %% Sample from the C-Vine -> output signal is U [NSam_c x Nr]
    if Nr <= 1 
        U = W1;
    else
        fprintf('[MODULE3 | cluster %d] Conditioning the first %d variables through the C-Vine ..',k,Nr)
        tStart = tic;
                
        U        = zeros(NSam_c,Nr);
        V        = zeros(NSam_c,Nr,Nr-1);
        U(:,1)   = W1(:,1);
        V(:,1,1) = W1(:,1);

        for i = 2:Nr     
            V(:,i,1) = W1(:,i);
            for j = i-1:-1:1
                V(:,j,j) = W1(:,j);
                V(:,i,1) = h_inv_ex(V(:,i,1),V(:,j,j),module2.theta{j,(i-j)}{1},module2.family(j,(i-j)));
            end
            U(:,i) = V(:,i,1);
        end
        fprintf(' %.2f seconds\n',toc(tStart));      
    end
    
    %% Perform PCA again to construct ecdf in PC domain
    fprintf('[MODULE3 | cluster %d] Performing eigenanalysis..',k)
    tStart = tic;
    
    mu          = mean(module1.Z_c,1);                % Calculate mean of data
    Z_centered  = module1.Z_c - repmat(mu,NObs_c,1);  % Center data
    [Ev,D]      = eig(cov(Z_centered));               % Eigenvalues and Eigenvecors of covariance matrix
    ED          = diag(D);                            % Diagonal matrix of Eigenvalues
    [~,ind]     = sort(ED,'descend');                 % Sort the E igenvalues in Decreasing Order
    Ev          = Ev(:,ind);                          % Sorted Eigenvectors   
    X           = Z_centered*Ev;                      % PC-transformed data   
    
    fprintf(' %.2f seconds\n',toc(tStart));
         
    % ecdf of high information signal
    X1          = X(:,1:Nr);
    % ecdf of low information signal
    X2          = X(:,Nr+1:end);
    
    %% Transform data from uniform to PC domain
    fprintf('[MODULE3 | cluster %d] Tranforming sampled data to PC domain ..',k)
    tStart = tic;
    
    % Transform 1:Nr dimensions from uniform to PC domain
    U = modU(U);
    Q1 = zeros(NSam_c,Nr);
    for i = 1:Nr 
        Q1(:,i) = sQuantile(sort(X1(:,i)),U(:,i)); 
        %Q1(:,i) = quantile(X1(:,i),U(:,i)); 
    end
    
    % Transform Nr:NVar dimensions from uniform to PC domain
    Q2 = zeros(NSam_c,NVar-Nr);
    for i = 1:(NVar-Nr)
        Q2(:,i) = sQuantile(sort(X2(:,i)),W2(:,i)); 
        %Q2(:,i) = quantile(X2(:,i),W2(:,i));
    end
    % Combine dataset in PC domain
    Q = [Q1  Q2]; 
    fprintf(' %.2f seconds\n',toc(tStart));
    
    %% Backproject from PC to actual domain
    fprintf('[MODULE3 | cluster %d] Back-projecting sampled data to actual domain ..',k)
    tStart = tic; 
    Y_centered  = Q*Ev';                            % Backproject to actual domain 
    Y_c         = Y_centered + repmat(mu,NSam_c,1); % Add the mean of actual domain to get final output
    fprintf(' %.2f seconds\n',toc(tStart));
end
end



