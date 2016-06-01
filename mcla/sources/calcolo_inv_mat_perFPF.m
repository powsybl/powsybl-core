%
% Copyright (c) 2016, Ricerca sul Sistema Energetico – RSE S.p.A. <itesla@rse-web.it>
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

function [dati_FPF] = calcolo_inv_mat_perFPF(err_filt,forec_filt,inj_ID,flagPQ,method,ofile_forFPF,ofileGUI,par_tolvar,par_Nmin_obs_fract,par_Nmin_obs_interv,par_outliers,Koutliers,par_imputation_meth,par_Ngaussians,check_mod0,percentil,tolerance,iterations,epsilo,negativeThresGUI)
% keyboard
[Y inj_ID idx_err idx_fore snapQ inj_IDQ idx_errA idx_foreA YFPF inj_IDFPF] = MODULE0(err_filt,forec_filt,inj_ID,flagPQ,method,par_tolvar,par_Nmin_obs_fract,par_Nmin_obs_interv,par_outliers,Koutliers,par_imputation_meth,par_Ngaussians,check_mod0,1);

%%%%%%%
YGUI=YFPF(:,idx_errA); % extraction of not conditioned forecast errors
injGUI = inj_IDFPF(idx_errA);
NObs = size(YGUI,1);
%% save variables for uncertainty GUI
mu          = mean(YGUI,1);                % Calculate mean of data
YGUI_centered  = YGUI - repmat(mu,NObs,1);  % Center data

[YGUI_centered injGUI] = filtra_negativePoshigh(YGUI_centered,injGUI,negativeThresGUI);
% keyboard
YGUI_normalised = YGUI_centered./repmat(std(YGUI_centered,0,1),NObs,1);

[Ev,D]      = eig(cov(YGUI_centered));               % Eigenvalues and Eigenvecors of covariance matrix
ED          = diag(D);                            % Diagonal matrix of Eigenvalues
[~,ind]     = sort(ED,'descend');                 % Sort the Eigenvalues in Decreasing Order
ED          = ED(ind);                            % Sorted Eigenvalues
Ev          = Ev(:,ind);   % Sorted Eigenvectors

uncertaintyGUI.Ev = Ev;
uncertaintyGUI.inj_IDGUI = injGUI;
uncertaintyGUI.correlatio = corr(YGUI_normalised);
uncertaintyGUI.eigenvalues = real(ED);
uncertaintyGUI.loadings = real(Ev).*repmat(sqrt(max(0,real(ED)')),size(Ev,1),1);

save(ofileGUI, '-struct', 'uncertaintyGUI');

%%%%%%%

inj_ID0 = inj_ID;
idx_err0 = idx_err;
idx_fore0 = idx_fore;
FORE0=Y(:,idx_fore0);

m_e = mean(Y(:,idx_err),1);
m_y = mean(Y(:,idx_fore),1);
std_e = std(Y(:,idx_err),0,1);
std_y = std(Y(:,idx_fore),0,1);

testfilterings2

FO=Y(:,idx_fore);
SN1 = Y(:,idx_err);

statisticals.means = m_e;
statisticals.stddevs = std_e;
inj_IDFPF1 = inj_IDFPF(idx_errA);
% keyboard
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
for iy = 1:size(FO,2)
FO(:,iy) = (FO(:,iy) - m_y(iy))/std_y(iy);
end
for ie = 1:size(SN1,2)
SNN(:,ie) = (SN1(:,ie) - m_e(ie))/std_e(ie);
end
outoflimit = [];
Nsa = size(FO,1);
tol = 10/Nsa;
% NATAF TRANSFORMATION FOR THE FORECASTS
for iy = 1:size(FO,2)
%     yp = y0(iy);

    [CDF_Y{iy} xfo{iy}] = ecdf(FO(:,iy));

    A = [xfo{iy},CDF_Y{iy}];
    
    [dummy1, uniq,dummy3]=unique(A(:,1));
        
    CDF_Y_NEW1(:,iy) = max(tol,min(1-tol,interp1(A(uniq,1),A(uniq,2),FO(:,iy),'linear','extrap')));
    INVG_Y_NEW1(:,iy) = norminv(CDF_Y_NEW1(:,iy),0,1);
   
end

% NATAF TRANSFORMATION FOR THE SNAPSHOTS
for ie = 1:length(idx_err)

[CDF_SN{ie} xsn{ie}] = ecdf(SNN(:,ie));

    A = [xsn{ie},CDF_SN{ie}];
    
    [dummy1, uniq,dummy3]=unique(A(:,1));
    
    CDF_E_NEW1(:,ie) =  max(tol,min(1-tol,interp1(A(uniq,1),A(uniq,2),SNN(:,ie),'linear','extrap')));
    INVG_E_NEW1(:,ie) = norminv(CDF_E_NEW1(:,ie),0,1);
end

X_NEW2S(:,idx_err)=INVG_E_NEW1;
X_NEW2S(:,idx_fore)=INVG_Y_NEW1;

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
cov_tot = cov(X_NEW2S);

cov_yy2 = cov_tot(idx_fore,idx_fore);
cov_ee2 = cov_tot(idx_err,idx_err);
cov_ey2 = cov_tot(idx_err,idx_fore);

corr_yy2 = (( diag(diag(cov_yy2)))^(-0.5))*cov_yy2*(( diag(diag(cov_yy2)))^(-0.5));

disp(['INVERSION OF FORECAST ERROR CORREALTION MATRIX WITH GIVEN ACCURACY'])
%
[invcorr_yy2x corr_yy2] = inversion_with_verify2(corr_yy2,tolerance,iterations,epsilo);
%%%%%%%%%%%%%%%%%%%%%%%%%%
%%% DATA STRUCTURE CONTAINING VARIABLES USED FOR CONDITIONAL
%%% SAMPLING ALGORITHM
B1 = inv(( diag(diag(cov_yy2)))^(0.5))*invcorr_yy2x*inv((( diag(diag(cov_yy2)))^(0.5)));

dati_FPF.YFPF = YFPF;
dati_FPF.X_NEW2S=X_NEW2S;
dati_FPF.idx_errA = idx_errA;
dati_FPF.idx_foreA = idx_foreA;
dati_FPF.idx_foreAR = idx_foreA;
dati_FPF.inj_ID = inj_IDFPF;
dati_FPF.invmatr_corryy = invcorr_yy2x;
dati_FPF.matrice_yy = matrice;
dati_FPF.idx_err = idx_err;
dati_FPF.idx_fore = idx_fore;

% ofile contains the following information:
% maxabserr = max absolute error after gap filling; maxrelerr = max
% relative error after gap filling; means = means of the "completed"
% variables Y; stdddevs = std deviations of the "completed" variables Y.
% the last two variables are used in Fuzzy Powerflow (FPF)

save(ofile_forFPF,'-struct','statisticals')

% dump inj_ID header, means and std deviations values to a csv file
csvFileName=sprintf('%s.csv',ofile_forFPF);
fid = fopen(csvFileName,'w');
fmtString = [repmat('%s,',1,size(inj_IDFPF1,2)-1),'%s\n'];
fprintf(fid,fmtString,inj_IDFPF1{:});
fclose(fid);
dlmwrite(csvFileName,statisticals.means,'delimiter',',','-append');
dlmwrite(csvFileName,statisticals.stddevs,'delimiter',',','-append');
