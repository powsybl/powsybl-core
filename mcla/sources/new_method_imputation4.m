%
% Copyright (c) 2016, Ricerca sul Sistema Energetico – RSE S.p.A. <itesla@rse-web.it>
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

function [Y inj_ID idx_err1 idx_fore1 idx_err idx_fore ] = new_method_imputation4(err_new,inj_ID,outliers,Koutliers,tolvar,Nmin_obs_fract,Nmin_obs_interv,check_mod0,idx_err0,idx_fore0)

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
%         fraz_non_zero(jY) = length(non_sono_zeri{jY})/QUANTIVALIDI(jY);
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

disp(['calcolati empirical CDFs'])
% stima correlazione  tra le coppie di variabili
if check_mod0
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
end
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
Xas = [1:size(Y,1)]';
% keyboard
for jvar = 1:Nvars
    disp(['done var nr ' num2str(jvar) ' over ' num2str(Nvars) ' variables'])
    nans = isnan(Y(:,jvar));
    nansidx = find(nans);
    if isempty(nans)==0
   
    Y(nans,jvar) = interp1(Xas(~nans),Y(~nans,jvar),Xas(nans),'spline');
   
    idx_fi = union(find(Y(nans,jvar) > (0.1*abs(max(Y(~nans,jvar)))+max(Y(~nans,jvar)))),find(Y(nans,jvar) < (min(Y(~nans,jvar))-0.1*abs(min(Y(~nans,jvar))))));
    
    if isempty(idx_fi)==0
%         keyboard
        mediana = median(Y(~nans,jvar));
        MAD = median(abs(Y(~nans,jvar) - mediana));
        Y(nansidx(idx_fi),jvar) = mediana + randn(length(idx_fi),1).*MAD;
        
    end
  
%     idx_nans = find(isnan(Y(:,jvar)));
%     
%     for jnan = 1:length(idx_nans)
%         ncount=idx_nans(jnan);
%         try
%             while isnan(Y(ncount,jvar)) && ncount < size(Y,1)
%                 if ncount < size(Y,1)
%                     ncount = ncount +1;
%                 end
%             end
%         catch err
%             keyboard
%         end
%         
%         if isnan(Y(ncount,jvar))
%             Y(idx_nans(jnan),jvar)=Y(idx_nans(jnan)-1,jvar);
%         else
%             if idx_nans(jnan) > 1
%                 Y(idx_nans(jnan),jvar)=0.5*(Y(idx_nans(jnan)-1,jvar)+Y(ncount,jvar));
%                 
%             else
%                 Y(idx_nans(jnan),jvar)=Y(ncount,jvar);
%             end
%             %         idx_nans = find(isnan());
%         end
%        

%     end
%     keyboard
    end
end


% Yas = Y(:,jvar);



% keyboard
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
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
maxabserr = max(max(abs(ERROVA)));
maxrelerr = max(max(abs(ERROV)));
end


