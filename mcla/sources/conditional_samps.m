%
% Copyright (c) 2016, Ricerca sul Sistema Energetico – RSE S.p.A. <itesla@rse-web.it>
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% FUNCTION AIMED TO GENERATE CONDITIONAL SAMPLES OF SNAPSHOTS GIVEN
% SPECIFIC CONFIGURATION OF FORECASTS
% Inputs:
% - Y0: NsxNv matrix with Ns uncoditioned samples of Nv variables
% consisting in snapshots and forecasts
% - y00: specific configuration of forecasts given by the online part of
% MCLA
% - dati_cond: structure with some variables calculated in offline part and
% needed to apply the Nataf based conditional sampling algorithm
% 
% Outputs:
% - snap_new1: Ns x Nsnap matrix with Ns samples of snapshots conditioned
% to the specific forecast configuration
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
function [snap_new1  quale_err var_out_of_lim] = conditional_samps(Y0,quale_forecast,y00,dati_cond,centering)
validation=0;
% initialise some variables for cond sampling

matrice_yy = dati_cond.matrice_yy;
invcorr_yy = dati_cond.invmatr_corryy;
corr_ee = dati_cond.corr_ee;
corr_ey = dati_cond.corr_ey;
y0 = matrice_yy*y00';
% cov_ey2 = dati_cond.cov_ey;
idx_err = dati_cond.idx_err;
idx_fore = dati_cond.idx_fore;
% OPTION TO SAMPLE CONDITIONAL SNAPSHOTS (NOT INTERESTING FOR PLATFORM
% USERS)
solution = 1; % 1 = MODIFYING SAMPLES APPLYING GAUSSIAN CONDITIONAL SAMPLING; 2 = CALCULATE CONDITIONED MEAN AND COVARIANCE OF MULTIVARIATE NORMAL DISTRIBUTION AND USE MVNRND TO SAMPLE NATAF VARIABLES
FO = Y0(:,idx_fore);
SN1 = Y0(:,idx_err);
% update means and std devs from samples of M 3
m_e = mean(SN1,1);
std_e = std(SN1,0,1);
m_y = mean(FO,1);
std_y = std(FO,0,1);
% keyboard
Nsa = size(Y0,1);
tol = min(5e-2,10/Nsa);
quale_err = zeros(1,length(dati_cond.idx_err));
% NORMALISE FORECASTS AND SNAPSHOTS

FO = (FO - repmat(m_y,size(FO,1),1))./repmat(std_y,size(FO,1),1);

SNN = (SN1 - repmat(m_e,size(SN1,1),1))./repmat(std_e,size(SN1,1),1);

% initialise empty matrices and cell arrays
CDF_Y_NEW1 = zeros(size(FO));
INVG_Y_NEW1 = zeros(size(FO));
CDF_E_NEW1 = zeros(size(SNN));
INVG_E_NEW1 = zeros(size(SNN));
CDF_Y = cell(size(FO,2),1);
CDF_SN = cell(size(SNN,2),1);
xfo = cell(size(FO,2),1);
xsn = cell(size(SNN,2),1);

outoflimit = [];
% NATAF TRANSFORMATION FOR THE FORECASTS
try
esiste_var= zeros(1,length(dati_cond.idx_err));
quale_err = zeros(1,length(dati_cond.idx_err));
var_out_of_lim = zeros(1,length(dati_cond.idx_err));

for iy = 1:size(FO,2)
 
%     yp = y0(iy);
    [CDF_Y{iy} xfo{iy}] = ecdf(FO(:,iy));
    A = [xfo{iy},CDF_Y{iy}];
    
    [dummy1, uniq,dummy3]=unique(A(:,1));
    
    dummy = interp1(A(uniq,1),A(uniq,2),FO(:,iy),'linear','extrap');

    CDF_Y_NEW1(:,iy) = max(tol,min(1-tol,dummy));

    INVG_Y_NEW1(:,iy) = norminv(CDF_Y_NEW1(:,iy),0,1);
    cdf_yp = max(tol,min(1-tol,interp1(A(uniq,1),A(uniq,2),(y0(iy) - m_y(iy))/std_y(iy),'linear','extrap')));
    idxfor = find(matrice_yy(iy,:)>0);
    
    if cdf_yp == tol || cdf_yp == 1-tol
        quale_var = [dati_cond.match_snap_to_fore(idxfor)];
        outoflimit = [outoflimit iy];
        if cdf_yp == 1- tol
        dyS(iy) = y0(iy) - (m_y(iy)+std_y(iy)*interp1(A(uniq,2),A(uniq,1),1-tol,'linear','extrap'));
        else
         dyS(iy) = y0(iy) - (m_y(iy)+std_y(iy)*interp1(A(uniq,2),A(uniq,1),tol,'linear','extrap'));    
        end
        
        dySS(find(matrice_yy(iy,:)>0))=dyS(iy);
        
        if isnan(quale_var)==0
           quale_err(quale_var)=dySS(find(matrice_yy(iy,:)>0)); 
           var_out_of_lim(quale_var)=iy;
        end
    end
    
        yp = norminv(cdf_yp,0,1);
%     end
    yyo(iy)=yp;
    dy(:,iy) = yp - INVG_Y_NEW1(:,iy);
end

% NATAF TRANSFORMATION FOR THE SNAPSHOTS
for ie = 1:length(idx_err)

[CDF_SN{ie} xsn{ie}] = ecdf(SNN(:,ie));

    A = [xsn{ie},CDF_SN{ie}];
    
    [dummy1, uniq,dummy3]=unique(A(:,1));
    
    CDF_E_NEW1(:,ie) =  max(tol,min(1-tol,interp1(A(uniq,1),A(uniq,2),SNN(:,ie),'linear','extrap')));
    INVG_E_NEW1(:,ie) = norminv(CDF_E_NEW1(:,ie),0,1);
end
catch err
%     keyboard
    save variables_test_conditioning.mat
    return
end
X_NEW2S(:,idx_err)=INVG_E_NEW1;
X_NEW2S(:,idx_fore)=INVG_Y_NEW1;

% EVALUATE UPDATED COVARIANCE MATRIX
cov_tot = cov(X_NEW2S);
cov_yy2 = cov_tot(idx_fore,idx_fore);
cov_ee2 = cov_tot(idx_err,idx_err);
covs_yy2 = diag(cov_yy2);
covs_ee2 = diag(cov_ee2);
%
switch solution
    case 1
        % SOLUTION PREFERRED: CONDITIONING THE SAMPLES FROM m3 TAKING INTO ACCOUNT
        % VECTOR Y0
        
        B1 = ( diag(covs_yy2.^(-0.5)))*invcorr_yy*( diag(covs_yy2.^(-0.5)));
        cov_ey2 = ( diag(covs_ee2.^(0.5)))*corr_ey*( diag(covs_yy2.^(0.5)));
        snap_new1G = INVG_E_NEW1 + (cov_ey2*B1*dy')';
        %
    case 2
        % CALCULATE THE CONDITIONED MEAN AND COVARIANCE MATRIX OF THE NATAF TRANSFORMED VARIABLES AND USE MVNRND TO SAMPLE THEM 
        cov_ee3 = ( diag(covs_ee2.^(0.5)))*corr_ee*( diag(covs_ee2.^(0.5)));
        cov_ey3 = ( diag(covs_ee2.^(0.5)))*corr_ey*( diag(covs_yy2.^(0.5)));
        B1 = ( diag(covs_yy2.^(-0.5)))*invcorr_yy*( diag(covs_yy2.^(-0.5)));
        
        mm_y = mean(X_NEW2S(:,idx_fore),1);
        mm_e = mean(X_NEW2S(:,idx_err),1);
        
        mm_ec = mm_e' + cov_ey3*B1*(yyo - mm_y)';
        SIG = cov_ee3 - cov_ey3*B1*cov_ey3';
        
        snap_new1G = mvnrnd(mm_ec,(SIG+SIG')/2,size(X_NEW2S,1));
end
%%%%%%%%

for ie = 1:length(idx_err)
    A = [CDF_SN{ie},xsn{ie}];
    
    [dummy1, uniq,dummy3]=unique(A(:,1));
    
    CDF_EG_NEW1(:,ie) =  max(tol,min(1-tol,normcdf(snap_new1G(:,ie),0,1)));
    if centering
    snap_new10(:,ie) = quale_err(ie) + m_e(ie) + std_e(ie)*interp1(A(uniq,1),A(uniq,2),CDF_EG_NEW1(:,ie),'linear','extrap');
    
    snap_new1(:,ie) = quale_forecast(ie) - median(snap_new10(:,ie)) + snap_new10(:,ie) ;
    
    else
        
   snap_new1(:,ie) = quale_err(ie) + m_e(ie) + std_e(ie)*interp1(A(uniq,1),A(uniq,2),CDF_EG_NEW1(:,ie),'linear','extrap');   
        
    end
    
end
% keyboard
%%%%% CHECK VALIDATION
if validation
Y = dati_cond.Y;T=5;
SSNAP = Y(:,idx_err);
FOREE = dati_cond.FORE0;

for jsnap = 1:size(SSNAP,2)
   idx = find(abs(FOREE(:,jsnap)-y00(jsnap))<T);
   SSNAPC{jsnap} = SSNAP(idx,jsnap);
  meansO(jsnap)=mean(SSNAPC{jsnap} );
  stddevO(jsnap)=std(SSNAPC{jsnap} );
end
meansX = mean(snap_new1,1);
stddevX = std(snap_new1,0,1);

figure
interv5sigX_UP = meansX + 5.*stddevX;
interv5sigX_DWN = meansX - 5.*stddevX;
interv5sigO_UP = meansO + 5.*stddevO;
interv5sigO_DWN = meansO - 5.*stddevO;

plot(interv5sigX_UP,'bs')
hold on
plot(interv5sigX_DWN,'bs')
hold on
plot(interv5sigO_UP,'r+')
hold on
plot(interv5sigO_DWN,'r+')


outputnataf.xs = snap_new1;

for jvar = 1:size(outputnataf.xs,2)
    [fN,xN] = ecdf(outputnataf.xs(:,jvar));
    [fB,xB] = ecdf(outputbruteforce.xs(:,jvar));
    A = [xN fN];
    [A1, index] = sort(A(:,1));
    A2          = A(index, 2);
    uniq        = find([diff(A1) ~= 0]);
    fBN = min(max(0,interp1(A(uniq,1),A(uniq,2),xB,'linear','extrap')),1);
   
    ARMS_N(jvar) = sqrt(sum((fBN-fB).^2))/length(xB);
    end
    
%     for jvar = 1:size(outputkern.statistiche_marg{1},2)
%     [fN,xN] = ecdf(outputkern.xs{jset}(:,jvar));
%     [fB,xB] = ecdf(outputbruteforce.xs(:,jvar));
%     A = [xN fN];
%     [A1, index] = sort(A(:,1));
%     A2          = A(index, 2);
%     uniq        = find([diff(A1) ~= 0]);
%     fBN = min(max(0,interp1(A(uniq,1),A(uniq,2),xB,'linear','extrap')),1);
%    
%     ARMS_K(jvar) = sqrt(sum((fBN-fB).^2))/length(xB);
%     end
    
    figure
    bar([ARMS_N])
    xlabel('var nr'),title('Average Root Mean Square Errors on marginal CDFs')
    legend({'Nataf vs Brute Force' })

end
%%%%%%%%%%%%%%%%%%%%%%%%%
