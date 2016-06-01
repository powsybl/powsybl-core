%
% Copyright (c) 2016, Ricerca sul Sistema Energetico – RSE S.p.A. <itesla@rse-web.it>
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

t2=cputime;
snap_new=snap_filt(:,3:end); %only snapshot fields without datetime and flag 0
forec_new=forec_filt(:,3:end); %only forecast fields without datetime and flag 1440

forec_new(:,2:2:end)=[]; %odd columns of reactive injections are discarded
snap_new(:,2:2:end)=[];  %odd columns of reactive injections are discarded
max_snap(:,2:2:end)=[];  %odd columns of reactive injections are discarded
inj_ID(:,2:2:end)=[];  %odd columns of reactive injections are discarded

err_new=zeros(size(snap_new));
err_pu=zeros(size(snap_new));
clear snap_filt forec_filt 

%**************************************************************************
%******************************DECLARATIONS********************************
err_new=snap_new-forec_new;
sigma=zeros(1,size(err_new,2));
avera=zeros(1,size(err_new,2));
sigma_err_pu=zeros(1,size(err_new,2));
avera_err_pu=zeros(1,size(err_new,2));
err_pu_nf=zeros(size(forec_new)); %err normalized to mean of forecasts
err_pu_ms=zeros(size(snap_new)); %err normalized to max of snapshots
err_pu_MAXSNAP=zeros(size(forec_new)); %err normalized to max of snapshots

avera_forec=zeros(1,size(forec_new,2));
sigma_err_pu_nf=zeros(1,size(forec_new,2));
avera_err_pu_nf=zeros(1,size(forec_new,2));
sigma_err_pu_ms=zeros(1,size(forec_new,2));
avera_err_pu_ms=zeros(1,size(forec_new,2));
sigma_err_pu_MAXSNAP=zeros(1,size(forec_new,2));
avera_err_pu_MAXSNAP=zeros(1,size(forec_new,2));

%**************************************************************************
%*******************************PARAMETERS*********************************
min_observations=50; %minimum needed number of significant error observations
               %per injection (sdt) and per couples of injections (correl)


%**************************SOLUTIONS 1) and 2)*****************************
%*******************************PROCEDURE ERR MW and PU********************
% find proper couple of snapshot and forecast
%aus=forec_new~=0 & snap_new~=0;
aus= abs(forec_new)>0.5 & abs(snap_new)>0.5 & abs((snap_new-forec_new)./forec_new)<1;
sum0_aus=sum(aus);
inj_OK1=find(sum0_aus>=min_observations); %indeces on analyzable injections
inj_NO1=find(sum0_aus<min_observations); %indeces on not analyzable injections

injID_OK_MW_PU=inj_ID(inj_OK1); %list of analysed injections for err in MW and pu of forecast
injID_NO_MW_PU=inj_ID(inj_NO1); %list of discarded injections err in MW and pu of forecast

%calculation of relative errors 
temp=find(aus==1);
err_pu(temp)=(snap_new(temp)-forec_new(temp));
err_pu(temp)=err_pu(temp)./forec_new(temp);

% series with few "snapshot&forecast" (less than 50 fields) are set as fully null
% because correlation needs for many significant fields
aus_corr=aus;
sum_aus=sum(aus_corr);
index_new=find(sum_aus(1,:)<min_observations); %if sum < min_observations less few good fields are present
aus_corr(:,index_new)=0;

%****st.deviation calcultion on significant fields(datetimes) per injection
for inj=1:size(err_new,2)
    temp=find(aus(:,inj)==1);
    if sum0_aus(inj)>min_observations
        sigma(inj)=std(err_new(temp,inj));
        avera(inj)=mean(err_new(temp,inj));
        sigma_err_pu(inj)=std(err_pu(temp,inj));
        avera_err_pu(inj)=mean(err_pu(temp,inj));
    end
end


%****CORRELATION calculation on significant fields per couple of injections
% if significan fiels number is less than 50, the correlation is set 0

corr_matrix=eye(size(err_new,2)); %diagonal is made of ones(self-correlations)
for inj_j=1:(size(err_new,2)-1)
    for inj_k=(inj_j+1):size(err_new,2)
        index_temp= find(aus_corr(:,inj_j)~=0 & aus_corr(:,inj_k)~=0);
        if length(index_temp)>=min_observations
            X=[err_new(index_temp,inj_j),err_new(index_temp,inj_k)];
            R=corrcoef(X);
            corr_matrix(inj_j,inj_k)=R(1,2);
            corr_matrix(inj_k,inj_j)=R(1,2);
        else
            corr_matrix(inj_j,inj_k)=0;
            corr_matrix(inj_k,inj_j)=0; 
        end
        %prints for debug
        %inj_j
        %inj_k
    end
end

%**********************SOLUTIONS 3) and 4)*********************************
%*****PROCEDURE ERR PU ON MEAN FORECAST AND MAX SNAPSHOT*******************
%calculation of relative errors normalized to mean of forecasts and maximum
%of snapshot
ausf=abs(forec_new)>0.5 & abs(snap_new)>0.5;
tempf1=find(ausf==1);
sum_ausf=sum(ausf);
inj_OK=find(sum_ausf>=min_observations); %indeces on analyzable injections
inj_NO=find(sum_ausf<min_observations); %indeces on not analyzable injections

injID_OK_mnF_mxW=inj_ID(inj_OK); %list of analysed injections for err in pu of mean of forecast and max of snapshot
injID_NO_mnF_mxW=inj_ID(inj_NO); %list of discarded injections for err in pu of mean of forecast and max of snapshot

%tempfs=find(sum_ausf)>min_observations;
for inj=1:size(forec_new,2)
    tempf=find(ausf(:,inj)==1);    
    if sum_ausf(inj)>=min_observations
       avera_forec(inj)=mean(abs(forec_new(tempf,inj)));
    end
end
err_pu_nf(tempf1)=(snap_new(tempf1)-forec_new(tempf1));
err_pu_ms(tempf1)=(snap_new(tempf1)-forec_new(tempf1));
clear tempf1
for inj=1:size(forec_new,2)
    tempr1=find(ausf(:,inj)==1);
    if sum_ausf(inj)>=min_observations
        err_pu_nf(tempr1,inj)=err_pu_nf(tempr1,inj)/avera_forec(inj);
        err_pu_ms(tempr1,inj)=err_pu_ms(tempr1,inj)/max_snap(inj);
    else
        err_pu_nf(tempr1,inj)=0; %null errors where number of observations is <50
        err_pu_ms(tempr1,inj)=0; %null errors where number of observations is <50
    end
end

%****st.deviation calculation(pu) on significant fields(datetimes) per injection
for inj=1:size(forec_new,2)
    temp=find(ausf(:,inj)==1);
    if sum_ausf(inj)>min_observations
        sigma_err_pu_nf(inj)=std(err_pu_nf(temp,inj));
        avera_err_pu_nf(inj)=mean(err_pu_nf(temp,inj));
        sigma_err_pu_ms(inj)=std(err_pu_ms(temp,inj));
        avera_err_pu_ms(inj)=mean(err_pu_ms(temp,inj));        
    end
end


%********************************SOL 5)************************************
%*****PROCEDURE ERR PU ON MEAN FORECAST AND MAX SNAPSHOT*******************
%Assessment of relative errors normalized to maximum of snapshot

ausf3=abs(forec_new)>0.5 & abs(snap_new)>0.5;
tempf3=find(ausf3==1);
err_pu_MAXSNAP(tempf3)=abs(snap_new(tempf3))-abs(forec_new(tempf3));
sum_ausf3=sum(ausf3);
for inj=1:size(forec_new,2)
    tempr3=find(ausf3(:,inj)==1);
    err_pu_MAXSNAP(tempr3,inj)=err_pu_MAXSNAP(tempr3,inj)/max_snap(inj);
end
ausf4=abs(err_pu_MAXSNAP)>0.25;
ausf5=ausf3==1 & ausf4==0;
sum_ausf5=sum(ausf5);
clear ausf3 ausf4
inj_OK5=find(sum_ausf5>=min_observations);%indeces on analyzable injections
inj_NO5=find(sum_ausf5<min_observations); %indeces on not analyzable inj.

%****st.deviation calculation(pu) on significant fields(datetimes) per injection
for inj=1:size(forec_new,2)
    temp=find(ausf5(:,inj)==1);
    if sum_ausf5(inj)>min_observations
        sigma_err_pu_MAXSNAP(inj)=std(err_pu_MAXSNAP(temp,inj));
        avera_err_pu_MAXSNAP(inj)=mean(err_pu_MAXSNAP(temp,inj));
    end
end


clear j t0 inj inj_j inj_k tempr1 tempf1 tempf ans
t3=cputime-t2;
disp('seconds for data analysis=')
disp(t3)
clear t1 t2 t3
