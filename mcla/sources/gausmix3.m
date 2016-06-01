%
% Copyright (c) 2016, Ricerca sul Sistema Energetico – RSE S.p.A. <itesla@rse-web.it>
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

function [Y OBG ] = gausmix3(Y,inputation,Ns)
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
for jvar = 1:size(Y,2)
FY(~isnan(Y(:,jvar)),jvar) = estract_cdf(Y(~isnan(Y(:,jvar)),jvar));
FY(isnan(Y(:,jvar)),jvar)=0.5;
end
Q = isnan(Y);
idx = sum(Q,1);
[A BQ] = sort(idx,'ascend');
% keyboard
K = length(find(A==0));
Yord = Y(:,BQ);
FYord = FY(:,BQ);

Nsamples = size(Y,1);
Nvars = size(Y,2);
% Y([2:51],3)=NaN;
% Y([12:71],2)=NaN;
% keyboard
opt = statset('MaxIter',300);

for s = 1:Ns
    obj{s} = gmdistribution.fit(Yord,s,'Regularize',1,'Options',opt);
    AIC(s)= obj{s}.AIC;
end
[minAIC,numComponents] = min(AIC);
% numComponents=3;
% keyboard
obj1 = obj{numComponents};
Ns = numComponents;
pigre = obj1.PComponents;
xmu = obj1.mu;
xsigma = obj1.Sigma;
idxelim = find(any(isnan(Yord),2));
% figure
% scatter(Y0(:,4),Y0(:,5),'g')
for jel = 1:length(idxelim)
  dmis = find(isnan(Yord(idxelim(jel),:)));
  dok = find(~isnan(Yord(idxelim(jel),:)));
  mut = xmu(:,dok);
  sigmat = xsigma(dok,dok,:);
  objred = gmdistribution(mut,sigmat,pigre);
  pigreco = posterior(objred,Yord(idxelim(jel),dok));
  clear E Z_k
  for k = 1:Ns
      r_k = xsigma(dmis,dok,k);
      R_k = xsigma(dok,dok,k);
      Rmis_k = xsigma(dmis,dmis,k);
      
      for j = 1:length(dok)
      Z_k(j) = norminv(FYord(idxelim(jel),dok(j)),xmu(k,dok(j)),sqrt(xsigma(dok(j),dok(j),k)));
      end
      try
           invRk = inv(R_k);
          switch inputation
             
              case 1 
                  
                E(:,k) = xmu(k,dmis)' + r_k*invRk*(Z_k-xmu(k,dok))';
              case 2
             medie = xmu(k,dmis)' + r_k*invRk*(Z_k-xmu(k,dok))';     
             sigme = Rmis_k - r_k*invRk*r_k';
             sigme = closest_corr(sigme);
             E(:,k) = mvnrnd(medie,sigme);
          end
      catch err
          keyboard
      end
  end
    casuale = mnrnd(1,pigreco);
    try
    Zguest = E*casuale';
    %E*casuale';
    for jmis = 1:length(dmis)
        FZguest(jmis) = normcdf(Zguest(jmis),xmu(find(casuale==1),dmis(jmis)),sqrt(xsigma(dmis(jmis),dmis(jmis),find(casuale==1))));
        
        [cdfx x] = ecdf(Yord(:,dmis(jmis)));

        A = [x,cdfx];
    
    [dummy1, uniq,dummy3]=unique(A(:,2));    
        
    dummy =  interp1(A(uniq,2),A(uniq,1),FZguest(jmis),'linear','extrap');
       
%     dummy = ksdensity(Yord(:,dmis(jmis)),FZguest(jmis),'function','icdf');
    
     Yord(idxelim(jel),dmis(jmis)) = dummy(1);
    end
    catch err
        keyboard
    end
%     hold on
%     scatter(Yord(idxelim(jel),4),Yord(idxelim(jel),5),'ms')
%    if abs(Yord(idxelim(jel),5))<5 && abs(Yord(idxelim(jel),4))>40
%        keyboard
%    end
%     if jel > 30
%    scatter(Yord(idxelim(jel),4),Yord(idxelim(jel),5),'ks') 
%    if jel == 50
%     keyboard
%    end
%     end
end

for jcol = 1:size(Y,2)

    Y(:,BQ(jcol)) = Yord(:,jcol); 

end
OBG = obj;
tempo=toc;
disp(['MISSING DATA IMPUTATION FINISHED IN ' num2str(tempo) ' SECONDS'])

% figure
% Quali = isnan(Y0);
% quali_righe = find(any(Quali,2));
% scatter(Y(quali_righe,4),Y(quali_righe,5),'marker','s')
% hold on
% scatter(Y0(:,4),Y0(:,5),'g')
% 
% keyboard
% figure
% scatter(Y(:,2),Y(:,3),'marker','s')
% hold on
% % scatter(Y0(:,2),Y0(:,3),'g')
% figure
% plotmatrix(Y)
% figure
% plotmatrix(Y0,'k.')
