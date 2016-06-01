%
% Copyright (c) 2016, Ricerca sul Sistema Energetico – RSE S.p.A. <itesla@rse-web.it>
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

function [Y inj_ID idx_err1 idx_fore1 idx_err idx_fore ] = new_method_imputation(err_new,inj_ID,outliers,Koutliers,tolvar,Nmin_obs_fract,Nmin_obs_interv,check_mod0,idx_err0,idx_fore0)

% snap_new=snap_filt(:,3:end); %only snapshot fields without datetime and flag 0
% forec_new=forec_filt(:,3:end); %only forecast fields without datetime and flag 1440
% 
% if flagPQ == 0
%     forec_new(:,2:2:end)=[]; %odd columns of reactive injections are discarded
%     snap_new(:,2:2:end)=[];  %odd columns of reactive injections are discarded
%     inj_ID(:,2:2:end)=[];  %odd columns of reactive injections are discarded
% end
% 
% err_new=zeros(size(snap_new));
% clear snap_filt forec_filt
% err_new=snap_new-forec_new;

Y = err_new;

% grafico dei dati
Q1 = isnan(Y);

Q2 = sum(Q1,1);

% % % % %%%%%

% for perce = 1:perc
perce = 1;discarded=[];
Noriginalsamples = size(Y,1);
Nmin_obs=Nmin_obs_fract*Noriginalsamples; % valid points for calculating the ECDFs 

allowable=[];
idxallo=[];idxdis = [];qualefor = []; 
%%% verifiche iniziali
for jY = 1:size(Y,2)
%     [parametri B metrica] = EMalgorithm(Y(find(~isnan(Y(:,(jY)))),(jY)));
        idxlambda{jY} = find(~isnan(Y(:,(jY))));
        non_sono_zeri{jY} = idxlambda{jY}(find(Y(idxlambda{jY},(jY))~=0));
        QUANTIVALIDI(jY)=length(idxlambda{jY});
%         if metrica > 2
%             keyboard
%         end
        if (length(idxlambda{jY}) < Nmin_obs || length(non_sono_zeri{jY}) < Nmin_obs) || var(Y(idxlambda{jY},(jY))) < tolvar %|| metrica > 2% length(find(idxlambda{jY} == 0)) > 0.05*size(Y,1)
            discarded = [discarded (jY)];
 if ismember(jY,idx_err0)
            disp(['*** DISCARDED SNAPSHOT VARIABLE FOR INJ: ' inj_ID{jY} ' -> NaN SAMPLES (' num2str(100-100*length(idxlambda{jY})/size(Y,1)) '%), ZERO VALUES (' num2str(100-100*length(non_sono_zeri{jY})/size(Y,1)) '%), VARIANCE ' num2str(var(Y(idxlambda{jY},(jY)))) ' MW -- TREATED AS CONSTANT INJECTOR'])
        else
            disp(['*** DISCARDED FORECAST VARIABLE FOR INJ: ' inj_ID{jY} ' -> NaN SAMPLES (' num2str(100-100*length(idxlambda{jY})/size(Y,1)) '%), ZERO VALUES (' num2str(100-100*length(non_sono_zeri{jY})/size(Y,1)) '%), VARIANCE ' num2str(var(Y(idxlambda{jY},(jY)))) ' MW -- TREATED AS CONSTANT INJECTOR'])

            end
        else
            allowable = [allowable (jY)];
            if rem(jY,2)==0
               qualefor = [qualefor jY/2]; 
            end
        end
    
end


%%%%%%%%%%%
%    keyboard
idx_err1 = setdiff(idx_err0,discarded);
idx_fore1 = setdiff(idx_fore0,discarded);
% idx_errass = idx_err1;
idx_err = find(ismember(allowable,idx_err1));
idx_fore = find(ismember(allowable,idx_fore1));

Y = Y(:,allowable);
% fraz_non_zero = fraz_non_zero(allowable);
QUANTIVALIDI = QUANTIVALIDI(allowable);

FY = Y;

inj_ID(:,discarded) = [];

Nvars = size(Y,2);

for jQ = 1:size(Y,2)
    idxlambda{jQ} = find(~isnan(Y(:,jQ)));
    setLAMBDA{jQ} = [Y(find(~isnan(Y(:,jQ))),jQ)];
    idxx = find(abs(setLAMBDA{jQ}-mean(setLAMBDA{jQ})) > Koutliers*sqrt(var(setLAMBDA{jQ})));
    if isempty(idxx)==0 
    remaining = setdiff(idxlambda{jQ},idxlambda{jQ}(idxx));
    if var(Y(remaining,jQ)) < tolvar
        dimensione = min(0.1,max(1e-4,0.01*max(abs(Y(remaining,jQ)))));
        Y(remaining,jQ) = Y(remaining,jQ) + dimensione.*randn(length(remaining),1);
    end
    
    if outliers == 1
        Y(idxlambda{jQ}(idxx),jQ)=NaN;
        idxlambda{jQ} = find(~isnan(Y(:,jQ)));
    setLAMBDA{jQ} = [Y(find(~isnan(Y(:,jQ))),jQ)];
    end
    
    end
    [EMPCDF{jQ} xQ{jQ}] = ecdf( setLAMBDA{jQ});
%          
  
end
Y0=Y;
Q = isnan(Y);
disp(['***' num2str(sum(sum(Q,1))) ' gaps to fix'])
Qn = 1-Q;
% qualitutte = all(Qn,2);

[AY BY] = sort(QUANTIVALIDI,'ascend');
disp('Variables with fewest valid points')
disp('Variable      nr of valid points')
% keyboard
for jY2 = 1:min(length(BY),10)
   disp([num2str(BY(jY2)) '   '   num2str(AY(jY2))]) 
end


%  combinazioni = 2^Nvars;
combinazioni = 2^Nvars;
stringa='';nset = 0;set=cell(1,size(Y,1));
% keyboard
for jQ = 1:size(Q,1)
    S = num2str(Q(jQ,:));
    if isempty(strfind(stringa,S))
        nset = nset+1;
        set{nset} = [set{nset} jQ];
        stringa = [stringa '_' S];
    else
        idx = strfind(stringa,S);
        quale_set = 1+((idx - 2)/(1+length(S)));
        set{quale_set} = [set{quale_set} jQ];
    end
end
disp(['numero totali di set = ' num2str(nset)])
%%%% ECDF per singole variabili

disp(['calcolati empirical CDFs'])
% stima correlazione  tra le coppie di variabili
clear COR
COR = eye(size(Q,2));
for jQ = 1:size(Q,2)-1
%     if jQ == 1
%         keyboard
% jQ
%     end
    for iQ = jQ+1:size(Q,2)
        setLAMBDAINTER = intersect(idxlambda{jQ},idxlambda{iQ});
        
            if length(setLAMBDAINTER) > Nmin_obs_interv
                [R] = corr(Y(setLAMBDAINTER,[jQ iQ]),'type','kendall');
                
                RR = R(1,2);
                if isnan(RR)
                    RR = 0;
                end
            else
                RR = 0;
            end
        

        COR(jQ,iQ)=RR;
        COR(iQ,jQ)=RR;
    end
end
%  keyboard

thresl = 1e-2;D1=-1;
disp(['calcolate correlazioni lineari tra variabili'])
% keyboard

% tolgo righe identiche
DCOR = diff(COR,1,1);
RDCOR = sum(abs(DCOR),2);
idxelimin = find(RDCOR < thresl)+1;
idx_restanti = setdiff([1:size(COR,1)],idxelimin);

COR0=COR;

COR1 = COR;
COR1(idxelimin,:)=[];
COR1(:,idxelimin)=[];

COR2 = COR1;

% keyboard
if det(COR1) > 1e-7
    U = zeros(Noriginalsamples*1,size(COR,2));
%     keyboard
    COR3 = copulaparam('gaussian',COR1);
    if det(COR3) < 0
          COR3 = closest_corr(COR3)  ;
    end
    U1 = copularnd('gaussian',COR3,Noriginalsamples*1);
    U(:,idx_restanti)=U1;
    U(:,idxelimin)=U(:,idxelimin-1);
else
         COR2 = closest_corr(COR1)  ;
         
%     COR3 = stimaposdef(COR2,COR1);
    %%%%%%
    COR3 = copulaparam('gaussian',COR2);
    if det(COR3) > 1e-7
        COR4 = COR3;
    else
    COR4 = closest_corr(COR3);
    end
    U = zeros(Noriginalsamples*1,size(COR,2));
    U1 = copularnd('gaussian',COR4,Noriginalsamples*1);
    U(:,idx_restanti)=U1;
    U(:,idxelimin)=U(:,idxelimin-1);
end

%  keyboard

% stima dei dati mancanti per ogni set
for jset = 1:length(set)
%     jset
    if isempty(set{jset}) == 0 && isempty(find(isnan(Y(set{jset}(1),:))))==0
        % variables with available data
        av_vars = find(~isnan(Y(set{jset}(1),:)));
        % variables with missing data
        missing_vars = find(isnan(Y(set{jset}(1),:)));
         
        
            
        for h = 1:length(set{jset})

            dm = zeros(size(U,1),1);
            for m_vars = 1:length(av_vars)
                
                idx = find(abs(xQ{av_vars(m_vars)}-Y(set{jset}(h),av_vars(m_vars)))==min(abs(xQ{av_vars(m_vars)}-Y(set{jset}(h),av_vars(m_vars)))));
                    try
                    FY(h,m_vars) = EMPCDF{av_vars(m_vars)}(idx(1));
                    catch err
                        keyboard
                    end
                        
                dm = dm + (abs(U(:,av_vars(m_vars))-FY(h,m_vars))).^5;
            end
%             keyboard
            idxU = find(dm == min(dm));
%          
            
            epsilo(h)=idxU(1);
% % %             end
        end
        
        for m_vars = 1:length(missing_vars)
            FYM(:,m_vars) = U(epsilo,missing_vars(m_vars));
            for kv = 1:length(FYM(:,m_vars))
                
                    idxv = find(abs(EMPCDF{missing_vars(m_vars)}-FYM(kv,m_vars))==min(abs(EMPCDF{missing_vars(m_vars)}-FYM(kv,m_vars))));
                    Y(set{jset}(kv),missing_vars(m_vars)) = xQ{missing_vars(m_vars)}(idxv(1));             
                
            end
            
        end
        clear FYM epsilo FY
    end
    
end

if check_mod0
RHO = corr([Y],'type','kendall');
ERRO = [100*(RHO - COR0)./COR0];
ERROA = [(RHO - COR0)];

QUALI = (abs(COR0) > 0.);

RHOV = RHO - eye(size(RHO));
COR0V = COR0 - eye(size(COR0));

ERROV = ERRO.*QUALI;
ERROVA = ERROA.*QUALI;

% % 
maxabserr = max(max(abs(ERROVA)))
maxrelerr = max(max(abs(ERROV)))
end
% out.stddevs = std(Y,0,1);
% out.means = mean(Y,1);

