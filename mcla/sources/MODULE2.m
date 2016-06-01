%
% Copyright (c) 2016, Ioannis Konstantelos <i.konstantelos@imperial.ac.uk>
% and Mingyang Sun <mingyang.sun11@imperial.ac.uk> – Imperial College London,
% Ricerca sul Sistema Energetico – RSE S.p.A. <itesla@rse-web.it>
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

function module2 = MODULE2(module1,IR, Tflag)
%--------------------------------------------------------------
% MODULE2 performs PCA and truncated C-Vine parameterization
% 
% INPUTS:   -- module1:         input containing clustered data in actual domain
%           -- IR:              Information Retainment criterion 
%           -- Tflag:           0 -> no truncation with multivariate Gaussian copula
%                               1 -> truncation with multivariate Gaussian copula
%
% OUTPUTS:  -- module2.k:       [scalar] cluster-model index
%           -- module2.Nr_MAX   [scalar] number of dimensions fitted to Gaussian copula
%           -- module2.R:       [NVar x NVar] Gaussian copula parameter (upper triangular) 
%           -- module2.theta:   [(Nr-1 x Nr-1) cell] C-Vine copula parameters 
%           -- module2.family:  [(Nr-1 x Nr-1) cell] C-Vine copula family
%           -- module2.pvalue:  [(Nr-1 x Nr-1) cell] p-value for each bivariate copula fit
%           -- module2.pvalue_c:[scalar] p-value of overall C-Vine model fit
%
%--------------------------------------------------------------
k             = module1.k;
[NObs_c,NVar] = size(module1.Z_c);

%% Perform PCA to the k cluster of historical data
fprintf('[MODULE2 | cluster %d] Cluster contains %d observations (weight = %.2f%%)\n',k,NObs_c,module1.w*100)
fprintf('[MODULE2 | cluster %d] Performing eigenanalysis..',k)
tStart = tic;
mu          = mean(module1.Z_c,1);                % Calculate mean of data
Z_centered  = module1.Z_c - repmat(mu,NObs_c,1);  % Center data
[Ev,D]      = eig(cov(Z_centered));               % Eigenvalues and Eigenvecors of covariance matrix
ED          = diag(D);                            % Diagonal matrix of Eigenvalues
[~,ind]     = sort(ED,'descend');                 % Sort the Eigenvalues in Decreasing Order
ED          = ED(ind);                            % Sorted Eigenvalues
Ev          = Ev(:,ind);                          % Sorted Eigenvectors
fprintf(' %.2f seconds\n',toc(tStart));

%% Calculate the Information Retain
fprintf('[MODULE2 | cluster %d] Computing IR criterion (target = %.2f%%) ..',k,IR*100)
tStart = tic;
% Compute information retainment ratios
infoRatio = cumsum(ED)./sum(ED);
% Number of dimensions to be retained
Nr     = find(infoRatio >= IR,1,'first');
% Number of dimensions that contain 100% of information
Nr_MAX = find(infoRatio >= 1,1,'first');
fprintf(' %d dimensions retained (max = %d) .. %.2f seconds\n',Nr,Nr_MAX,toc(tStart));

%% Perform PCA to rotate the data
fprintf('[MODULE2 | cluster %d] Performing PCA rotation ..',k)
tStart = tic;
X1     =  Z_centered*Ev(:,1:Nr);       % Retained PCs for cluster k
X2     =  Z_centered*Ev(:,Nr+1:end);   % Truncated PCs     
fprintf(' %.2f seconds\n',toc(tStart));
 
%% Map PCs to the uniform domain through the ecdf 
fprintf('[MODULE2 | cluster %d] Transforming historical data to uniform domain ..',k)
tStart = tic;
% Important part
U1     =  modU(map_ecdf(X1));
% Non-important 
U2     =  modU(map_ecdf(X2));
fprintf(' %.2f seconds\n',toc(tStart));

%% Check if Nr <= 1
if Nr <= 1 
    % T is the input to the multivariate Gaussian copula parameterization
    T        = U1;
    theta    = 0;
    family   = 0;
    pvalue   = 0;
    pvalue_c = 0;
else
%% C-Vine copula parameterization
    fprintf('[MODULE2 | cluster %d] Parameterizing the C-Vine ..',k)
    tStart = tic;

    theta  = cell(Nr-1);        % C-Vine parameters    
    family = zeros(Nr-1,Nr-1);  % C-Vine families
    pvalue = zeros(Nr-1,Nr-1);  % C-Vine p-values
    H_cls  = zeros(NObs_c,Nr);  

    % First sets of Rows of H matrix
    for i = 1:Nr
       H_cls(:,i) = U1(:,i);  % Historical data in unit domain
    end

    % Compute Parameters of the First Tree
    fprintf('\n                      [T 1]'); 
    for j = 1:Nr-1  
        [temp_theta,temp_pvalue,temp_family] = copulachoose([H_cls(:,(j+1)),H_cls(:,1)]); 
        theta{1,j}  = {temp_theta};
        family(1,j) = temp_family;
        pvalue(1,j) = temp_pvalue;  
        fprintf(' %s (%1.3f)',copulaName(family(1,j)),pvalue(1,j));
    end
    fprintf('\n');

    % Compute Parameters of remaining Tree
    Nt = Nr-1;     % Nr-1 is the number of trees in C-Vine
    B = [];
    for i = 2:Nt   % forall all trees except the already-computed 1st tree
        fprintf('                      [T%2d]',i);
        B(:,1) = H_cls(:,1);
        for s = 1:Nr-i+1       
            r = theta{(i-1),s}{1}; 
            H_cls(:,s) = h(H_cls(:,s+1), B(:,1),r,family(i-1,s));    
        end
        for j=1:Nr-i; % Number of branches         
            [temp_theta,temp_pvalue,temp_family] = copulachoose([H_cls(:,j+1),H_cls(:,1)]);
            theta{i,j}  = {temp_theta};   
            family(i,j) = temp_family;
            pvalue(1,j) = temp_pvalue;
            fprintf(' %s (%1.3f)',copulaName(family(i,j)),pvalue(i,j));
        end  
        fprintf('\n');
    end
    fprintf('[MODULE2 | cluster %d] CVine parameterization took %.2f seconds\n',k,toc(tStart));

%% PIT transformation 
    fprintf('[MODULE2 | cluster %d] Performing Kolmogorov-Smirnoff test ..',k)
    tStart = tic;
    T = zeros(NObs_c,Nr);
    for t = 1:NObs_c 
        T(t,1) = U1(t,1);
        for i = 2:Nr
            T(t,i) = U1(t,i);  
               for j = 1:i-1 
                   r = theta{j,(i-j)}{1};        
                   T(t,i) = h(T(t,i),T(t,j),r,family(j,(i-j)));
               end
         end
    end   

%% Overall K-S GOF TEST
    % Tranform Z to a chi-square distribution 
    S = zeros(NObs_c,1);
    for i = 1:Nr   
      S = S + (norminv(T(:,i),0,1)).^2;
    end 
    % Sort S to create ecdf of S
    S = unique(round(S*10000)/10000,'sorted');
    % Create chi^2 hypothesized distribution as a two-column matrix (see help)       
    chi2_cdf = [S,cdf('chi2',S,Nr)];
    % Test whether data are from the hypothesized distribution
    [~,pvalue_c] = kstest(S,chi2_cdf);  
    % Output cluaster-model p-value
    fprintf(' (p-value = %.4f) .. %.2f seconds\n',pvalue_c,toc(tStart))

%% Plot empirical and chi^2 cdfs to compare with K-S test
    % [f,x_values] = ecdf(S);
    % F = plot(x_values,f);
    % set(F,'LineWidth',2);
    % hold on;
    % G = plot(x_values,cdf('chi2',x_values,Nr),'r-');
    % set(G,'LineWidth',2);
    % legend([F G],...
    %        'Empirical CDF','Chi-Squared CDF',...
    %        'Location','SE'); 
end
%% Fit Gaussian copula to historical dataset (in uniform domain)
if Tflag > 0 
    % Compute number of dimensions to be fitted with Gaussian copula       
    Nr_T = ceil(Tflag*(Nr_MAX - Nr));
    fprintf('[MODULE2 | cluster %d] Parameterizing the Gaussian copula (Tflag = %.4f -> %d dimensions) ..',k,Tflag,Nr_T)
    tStart = tic;
    
    % Fit Gaussian copula to data
    R = copulafit('Gaussian',[modU(T) U2(:,1:Nr_T)]);
    
    % Save only upper diagonal part of the correlation matrix R 
    R = triu(R);
    
    fprintf(' %.2f seconds\n',toc(tStart));
else
    % If Tflag == 0, a simpler model is used that does not consider truncation
    R = 0;
    fprintf('[MODULE2 | cluster %d] No truncation with Gaussian copula (Tflag = 0)\n',k)
end

%% Save all parameters to module2 structure
module2.k         = k;              % cluster being processed
module2.Nr        = Nr;             % number of dimensions retained in CVine
module2.Nr_MAX    = Nr_MAX;         % maximum number of dimensions for IR = 100%
module2.R         = R;              % Parameters of Gaussian copula (correlation matrix)
module2.theta     = theta;          % Parameters of C-Vine structure
module2.family    = family;         % Copula types structure
module2.pvalue    = pvalue;         % Copula types structure
module2.pvalue_c  = pvalue_c;       % C-Vine p-value

end
