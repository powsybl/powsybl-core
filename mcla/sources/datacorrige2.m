%
% Copyright (c) 2016, Ricerca sul Sistema Energetico – RSE S.p.A. <itesla@rse-web.it>
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

function Y = datacorrige2(Y)
% 
disp(['MISSING DATA IMPUTATION STARTED ...'])
tic
% clear all
% clc
% close all
% Nsamples = 200;
% T = [1 0.1 0.2 0.4; 0.1 1 0.2 0.3; 0.2 0.2 1 0 ; 0.4 0.3 0 1];
% FY = copularnd('Gaussian',T,Nsamples);
% 
% Y(:,1)=norminv(FY(:,1),1,0.2);
% Y(:,2)=norminv(FY(:,2),3,0.5);
% Y(:,3)=norminv(FY(:,3),4,0.1);
% Y(:,4)=norminv(FY(:,4),0.9,0.03);

Y0 = Y;
Nsamples = size(Y,1);
Nvars = size(Y,2);
% Y([2:51],3)=NaN;
% Y([12:71],2)=NaN;

Q = isnan(Y);

idx = sum(Q,1);
[A BQ] = sort(idx,'ascend');

% keyboard
K = length(find(A==0));
for jvar = 1:size(Y,2)
    MEDIE(jvar) = mean(Y(~isnan(Y(:,jvar)),jvar));
DEVSTD(jvar) = std(Y(~isnan(Y(:,jvar)),jvar));
Y(:,jvar) = (Y(:,jvar) - mean(Y(~isnan(Y(:,jvar)),jvar)))./std(Y(~isnan(Y(:,jvar)),jvar));
end
Yord = Y(:,BQ);
MEDIEORD = MEDIE(BQ);
DEVSTDORD=DEVSTD(BQ);

for j = 1:Nvars
    
    [EMPCDFY{j} XY{j}] = ecdf(Yord(:,j));
    
    A = [XY{j},EMPCDFY{j}];
    [dummy1,uniq,dummy11 ] = unique(A(:,1));
    
    Z(:,j) = norminv(interp1(A(uniq,1),A(uniq,2),Yord(:,j),'linear','extrap'),0,1);
    
            idxin = find(isinf(Z(:,j)));% isinf()
            Z(idxin,j) = sign(Z(idxin,j))*1e3;
        
        empiriche{j,1}=XY{j};
        empiriche{j,2}=EMPCDFY{j};
         
%     end
    
end

    FZ0=[];
    Q = 1-isnan(Yord);
    Zmod=Z;
    for iy = 1:K
%         tic
  
    FZ(:,iy) = normcdf(Z(:,iy),0,1);%interp1(A(uniq,1),A(uniq,2),Z(idxvalid,iy),'linear','extrap');
%     toc
    FZ0 =[FZ0 FZ(:,iy)];
    FZ0(find(isnan(FZ(:,iy))),iy)=0.5;
    FZ0(find((FZ(:,iy))>1-1e-3),iy)=0.999;
    FZ0(find((FZ(:,iy))<1e-3),iy)=1e-3;
    
    end
     
    
    

for k = K+1:Nvars
%     keyboard
% disp(['input missing data to var ' num2str(k) ' over ' num2str(Nvars) ' variables'])
    [EMPCDFY{k} XY{k}] = ecdf(Yord(:,k));
%     keyboard
     A = [XY{k},EMPCDFY{k}];
    
%     end
    % kendal e pearson in seguito %%%%%%%%%%
    idnan=find(Q(:,k)==0);
    
    empiriche{k,1}=XY{k};
        empiriche{k,2}=EMPCDFY{k};
        
        Zmod(idnan,k)=0;
end
try
        
        rr = copulafit('gaussian',min(0.99,max(0.01,normcdf(Zmod))));
catch err
    keyboard
    end
Nv=10;
% keyboard
for iss = 1:size(Yord,1)

    idxgap= find(isnan(Yord(iss,:)));
    clear vettoreBdum

    if isempty(idxgap)==0
        disp(['done sample nr ' num2str(iss) ' over ' num2str(size(Yord,1)) ' samples'])

    dok= setdiff([1:Nvars],idxgap);
    
    for idok=1:length(dok)
    qualeid=find(abs(rr(idok,idxgap))==max(abs(rr(idok,idxgap))));
    vettoreBdum(idok)=rr(idok,idxgap(qualeid(1)));
    end
    
    %%% 
    [quali2 idx2] = sort(abs(vettoreBdum),'descend');
    
    try
    dok = dok(idx2(1:min(Nv,length(idx2))));
    catch  err
        keyboard
    end
    %     casuale = randn(length(idxelim),1);
    R = rr(dok,dok);
    r = rr(dok,idxgap);
    
     % check righe uguali
    if det(R)<1e-10

        R = R + eye(size(R)).*1e-5;
        
    end
    
        invR = R\eye(size(R,1));
        sigma = 1-r'*invR*r;

        try
        dummy3 = r'*invR*Z(iss,dok)';
        catch err
            keyboard
        end
        Zp(iss,idxgap) = dummy3;%+sqrt(sigma)*casuale(jk);
        Z(iss,idxgap) = Zp(iss,idxgap);
        FZ = normcdf(Zp(iss,idxgap));
        
        for igap = 1:length(idxgap)
        A=[empiriche{idxgap(igap),1}  empiriche{idxgap(igap),2}];
        
        [dummy1, uniq,dummy3]=unique(A(:,1));
        
        dummy1 = interp1(A(uniq,2),A(uniq,1),FZ(igap),'linear','extrap');
%         if any(isnan(Yord(iss,:)))
%             keyboard
%         end
    
        Yord(iss,idxgap(igap)) = dummy1;
        end
% if any(isnan(Yord(iss,:)))
%             keyboard
%         end
    end
end
% keyboard
for jcol = 1:size(Y,2)
    Yabs(:,jcol) = MEDIEORD(jcol) + DEVSTDORD(jcol)* Yord(:,jcol);
    Y(:,BQ(jcol)) = Yabs(:,jcol); 
    
end

tempo=toc;
disp(['MISSING DATA IMPUTATION FINISHED IN ' num2str(tempo) ' SECONDS'])
