%
% Copyright (c) 2016, Ricerca sul Sistema Energetico – RSE S.p.A. <itesla@rse-web.it>
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

%% m1file - sampling network data, to be read
%% m2file - FEA output file (output from module1 and module2), to be read  
%% ofile  - module3 output file, to be written
%% s_scenarios - number of samples to generate (in ofile)
%% s_rng_seed - int seed (optional, default is 'shuffle' on current date)
function exitcode=FPF_HELPER(m1file, m2file, ofile, s_rng_seed)
close all;
mversion='1.8.0';
disp(sprintf('wp5 - FEA STATS FOR FPF - version: %s', mversion));
disp(sprintf(' m1file:  %s',m1file));
disp(sprintf(' m2file:  %s',m2file));
disp(sprintf(' ofile:  %s', ofile));

moutput.errmsg='Ok';
try
    % module1: struct, output from module1
    load(m1file);
    % module2:  module2 output
    load(m2file);
    % s_scenarios: number of samples to generate
   
    %if seed is not specified, 'shuffle'  on current platform time    
    if nargin < 4
        rng('shuffle','twister');
        disp(sprintf(' rng seed:  not specified, default is shuffle on current platform time'));
    else 
        rng(str2double(s_rng_seed),'twister');
        disp(sprintf(' rng seed:  %s',s_rng_seed));
    end

disp(sprintf('flagPQ:  %u', out(1).flagPQ));
disp(['preprocessing: type_x, etc.'])
tic;

% type_X is the vector which specifies the nature of the stochastic
% injections (RES or load). here is an example with 3 RES and one stochastic load. the vector must be
% completed taking information from IIDM.

% flagPQ: =0 se ci sono solo P stocastiche, =1 se anche le Q sono
% stocastiche
%%%%% 
inj_print = {};
m_print=[];
std_print=[];
for iout = 1:length(out)
   dati_FPF = out(iout).dati_FPF;
   module1 = out(iout).module1;
   flagPQ = out(iout).flagPQ;
   type_X=[];y0=[];
   %%%%%%%%%%%%%%%%%%%%%%%%%%%%%
   idx_errA = dati_FPF.idx_errA;
idx_foreA = dati_FPF.idx_foreA;
inj_ID0 = dati_FPF.inj_ID;

if flagPQ == 0
    for jcol = 1:size(inj_ID0,2)
        idxgen = find(ismember({generatore.codice},inj_ID0{jcol}(1:end-2)));
        idxload = find(ismember({carico.codice},inj_ID0{jcol}(1:end-2)));
        if isempty(idxgen)==0
            if generatore(idxgen(1)).conn == 1
            if strcmp(inj_ID0{jcol}(end),'P')
            type_X(:,jcol) = [1;idxgen(1)];
            if ismember(jcol,idx_foreA)
            y0(find(idx_foreA==jcol)) = generatore(idxgen(1)).P;
            end
                else
              type_X(:,jcol) = [4;idxgen(1)];      
              if ismember(jcol,idx_foreA)
            y0(find(idx_foreA==jcol)) = generatore(idxgen(1)).Q;
            end
            end
                
            end
        end
        if isempty(idxload)==0
            if carico(idxload(1)).conn == 1 && carico(idxload(1)).P ~= 0 && carico(idxload(1)).Q ~= 0
                type_X(:,jcol) = [2;idxload(1)];
                if ismember(jcol,idx_foreA)
                    y0(find(idx_foreA==jcol)) = carico(idxload(1)).P;
                end
                   
            end
        end
    end
else
    for jcol = 1:size(inj_ID0,2)
        idxgen = find(ismember({generatore.codice},inj_ID0{jcol}(1:end-2)));
        idxload = find(ismember({carico.codice},inj_ID0{jcol}(1:end-2)));
        if isempty(idxgen)==0
            if generatore(idxgen(1)).conn == 1
                if strcmp(inj_ID0{jcol}(end),'P')
            type_X(:,jcol) = [1;idxgen(1)];
            if ismember(jcol,idx_foreA)
            y0(find(idx_foreA==jcol)) = generatore(idxgen(1)).P;
            end
                else
              type_X(:,jcol) = [4;idxgen(1)];
              if ismember(jcol,idx_foreA)
            y0(find(idx_foreA==jcol)) = generatore(idxgen(1)).Q;
            end
                end
            end
        end
        if isempty(idxload)==0
            if carico(idxload(1)).conn == 1
            if strcmp(inj_ID0{jcol}(end),'P')
            type_X(:,jcol) = [2;idxload(1)];
             if ismember(jcol,idx_foreA)
            y0(find(idx_foreA==jcol)) = carico(idxload(1)).P;
             end
            else
            type_X(:,jcol) = [3;idxload(1)];
             if ismember(jcol,idx_foreA)
            y0(find(idx_foreA==jcol)) = carico(idxload(1)).Q;
             end
            end
            end
        end
    end
end

idx_miss = find(~any(type_X,1));
idx_available = setdiff([1:size(type_X,2)],idx_miss);
type_X(4,idx_foreA(ismember(idx_foreA,idx_available)))=-1;
type_X(4,idx_errA(ismember(idx_errA,idx_available)))=1;
type_X(3,:) = zeros(1,size(type_X,2));
type_X(3,idx_available) = [1];

are_forerrors = intersect(find(type_X(4,:)==1),find(type_X(3,:)==1));
inj_ID0C = inj_ID0(idx_errA(find(ismember(idx_errA,are_forerrors))));

%%%%%%
% save fpf_test.mat

if isempty(module1) == 0

    %%%%% conditional sampling for forecast errors accounting for the data
    %%%%% stored in dati_FPF
YFPF = dati_FPF.YFPF;
matrice = dati_FPF.matrice_yy;
y1 = (matrice*y0')';

invcorr_yy2x = dati_FPF.invmatr_corryy;

YFPF_ER= YFPF(:,are_forerrors);
YFPF_FO0= YFPF(:,idx_foreA);

idx_err1 = dati_FPF.idx_err;
idx_err=idx_err1(find(ismember(idx_errA,are_forerrors)));
idx_fore = dati_FPF.idx_fore;

YFPF_FO= YFPF_FO0*matrice';

YFPF1(:,idx_err1(find(ismember(idx_errA,are_forerrors)))) = YFPF_ER;
YFPF1(:,idx_fore) = YFPF_FO;

%%% CONDITIONAL MEANS AND STD DEVS
m_e = nanmean(YFPF_ER,1);
m_y = nanmean(YFPF_FO,1);
std_e = std(YFPF_ER,0,1);
std_y = std(YFPF_FO,0,1);

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
for iy = 1:size(YFPF_FO,2)
FO(:,iy) = (YFPF_FO(:,iy) - m_y(iy))/std_y(iy);
end
for ie = 1:size(YFPF_ER,2)
ERN(:,ie) = (YFPF_ER(:,ie) - m_e(ie))/std_e(ie);
end
outoflimit = [];tol=10/size(YFPF_ER,1);
% NATAF TRANSFORMATION FOR THE FORECASTS
for iy = 1:size(YFPF_FO,2)
%     yp = y0(iy);
    [CDF_Y{iy} xfo{iy}] = ecdf(FO(:,iy));
    A = [xfo{iy},CDF_Y{iy}];
   
    [dummy1, uniq,dummy3]=unique(A(:,1));
    
    CDF_Y_NEW1(:,iy) = max(tol,min(1-tol,interp1(A(uniq,1),A(uniq,2),FO(:,iy),'linear','extrap')));
    INVG_Y_NEW1(:,iy) = norminv(CDF_Y_NEW1(:,iy),0,1);
    cdf_yp = max(tol,min(1-tol,interp1(A(uniq,1),A(uniq,2),(y1(iy) - m_y(iy))/std_y(iy),'linear','extrap')));
    if cdf_yp == tol || cdf_yp == 1-tol
        outoflimit = [outoflimit iy];
    end
    yp = norminv(cdf_yp,0,1);
    yyo(iy)=yp;
    dy(:,iy) = yp - INVG_Y_NEW1(:,iy);
end

% NATAF TRANSFORMATION FOR THE ERRORS
for ie = 1:length(idx_err)

[CDF_ER{ie} xsn{ie}] = ecdf(ERN(:,ie));

    A = [xsn{ie},CDF_ER{ie}];
    
   [dummy1, uniq,dummy3]=unique(A(:,1));
    
    CDF_E_NEW1(:,ie) =  max(tol,min(1-tol,interp1(A(uniq,1),A(uniq,2),ERN(:,ie),'linear','extrap')));
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
corr_ey2 = (( diag(diag(cov_ee2)))^(-0.5))*cov_ey2*(( diag(diag(cov_yy2)))^(-0.5));
corr_ee2 = (( diag(diag(cov_ee2)))^(-0.5))*cov_ee2*(( diag(diag(cov_ee2)))^(-0.5));
%
B1 = inv(( diag(diag(cov_yy2)))^(0.5))*invcorr_yy2x*inv((( diag(diag(cov_yy2)))^(0.5)));

mm_y = mean(X_NEW2S(:,idx_fore),1);
mm_e = mean(X_NEW2S(:,idx_err),1);

mm_ec = mm_e' + cov_ey2*B1*(yyo - mm_y)';
SIG = cov_ee2 - cov_ey2*B1*cov_ey2';

SIG = closest_corr(SIG);
SIG = real(SIG);

% % add a small eps to main diagonal in case det=0
% %
% ntempt = 0;
% while det(SIG)==0 & ntempt < 100
% SIG_FIL = SI+1e-4*eye(size(SIG_FIL,1));
% ntempt = ntempt+1;
% end
if det(SIG) > 0
snap_new1G = mvnrnd(mm_ec,(SIG+SIG')/2,1000);
else
 snap_new1G = mvnrnd(mm_ec,diag(diag((SIG+SIG')/2)),1000); 
end
for ie = 1:length(idx_err)
    A = [CDF_ER{ie},xsn{ie}];
   [dummy1, uniq,dummy3]=unique(A(:,1));
    
    CDF_EG_NEW1(:,ie) =  max(tol,min(1-tol,normcdf(snap_new1G(:,ie),0,1)));
    snap_new1(:,ie) = m_e(ie) + std_e(ie)*interp1(A(uniq,1),A(uniq,2),CDF_EG_NEW1(:,ie),'linear','extrap');
end

m_ec = mean(snap_new1,1)';
std_ec = std(snap_new1,0,1)';

else
   %%%% MODALITY GAUSSIAN ACTIVATED 
   module2 = out(iout).module2;
   m_ec = zeros(length(are_forerrors),1);
   idx_loads = find(ismember(type_X(1,:),[2 3]));
   idx_RES = find(ismember(type_X(1,:),[1 4]));
   stddevs(intersect(idx_loads,are_forerrors))=module2.allparas.stddev(1);
   stddevs(intersect(idx_RES,are_forerrors))=module2.allparas.stddev(2);
   std_ec = stddevs(find(ismember(idx_errA,are_forerrors)))'.*max(1e-6,abs(y0(find(ismember(idx_errA,are_forerrors))))');
end
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% here ends the  RSE CODE, extracted from TEST_MCLA.m
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

disp(['FPF COMPUTATION COMPLETED.'])
toc;


   moutput(iout).errmsg='Ok';
   moutput(iout).m_ec=m_ec;
   moutput(iout).std_ec=std_ec;
   moutput(iout).inj_ID=inj_ID0C;%(:,idx_errA);
   moutput(iout).rng_data=out(iout).rng_data;
   moutput(iout).mversion=out(iout).mversion;
   inj_print = [inj_print inj_ID0C];
   m_print = [m_print m_ec'];
   std_print = [std_print std_ec'];
   clear m_ec std_ec inj_ID0C y0 type_X module1 module2
   
   %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    totmoutput.outcomeFPF(iout) = moutput(iout);

end

%save output in .mat
csvFileName=sprintf('%s.csv',[ofile]);
fid = fopen(csvFileName,'w');
fmtString = [repmat('%s,',1,size(inj_print,2)-1),'%s\n'];
fprintf(fid,fmtString,inj_print{:});
fclose(fid);
dlmwrite(csvFileName,m_print,'delimiter',',','-append');
dlmwrite(csvFileName,std_print,'delimiter',',','-append');


   exitcode=0;
catch err
   moutput(1).errmsg=err.message;
   disp(getReport(err,'extended'));
   exitcode=-1;
end
save(ofile, '-struct', 'totmoutput');
% dump inj_ID header, means and std deviations values to a csv file

end
