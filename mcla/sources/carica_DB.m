%
% Copyright (c) 2016, Ricerca sul Sistema Energetico – RSE S.p.A. <itesla@rse-web.it>
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

clear all 
t0=cputime;
%**************************************************************************
%loads snapshot and forecast from downloaded CVS files of iTESLA DB
% input files must by put in the current directiry re-called by "pwd"
%loads injection IDs and verifies that snapshot&forecast list are equal 

%************LOADING of VALUES*********************************************
snap_LOAD=csvread('S_LOADS.csv',1,0);
forec_LOAD=csvread('F_LOADS.csv',1,0);
snap_RES=csvread('S_WINDSOLAR.csv',1,0);

forec_RES=csvread('F_WINDSOLAR.csv',1,0);
temp=find(forec_RES(:,2)==0); %in order to retrieve only forecast rows (deleting snapshots wrongly included in this matrix)
forec_RES(temp,:)=[];%in order to retrieve only forecast rows 
clear temp

%************LOADING of STRINGS of INJECTION ID****************************
fid = fopen('S_LOADS.csv','rt+');
inj_S_L = fgetl(fid); %only first row with injection ID is loaded
fclose(fid);
loc_virg = strfind(inj_S_L,',');
S_L{1} = inj_S_L(1:loc_virg(1)-1);
for j = 2:length(loc_virg)
    S_L{j} = inj_S_L(loc_virg(j-1)+1:loc_virg(j)-1);
end
S_L{length(loc_virg)+1} = inj_S_L(loc_virg(end)+1:end);
S_L(1:2)=[]; %Datetime e forecastTime are discarded
clear fid inj_S_L loc_virg

fid = fopen('F_LOADS.csv','rt+');
inj_F_L = fgetl(fid);
fclose(fid);
loc_virg = strfind(inj_F_L,',');
F_L{1} = inj_F_L(1:loc_virg(1)-1);
for j = 2:length(loc_virg)
    F_L{j} = inj_F_L(loc_virg(j-1)+1:loc_virg(j)-1);
end
F_L{length(loc_virg)+1} = inj_F_L(loc_virg(end)+1:end);
F_L(1:2)=[]; %Datetime e forecastTime are discarded
clear fid inj_F_L loc_virg

fid = fopen('S_WINDSOLAR.csv','rt+');
inj_S_WS = fgetl(fid);
fclose(fid);
loc_virg = strfind(inj_S_WS,',');
S_WS{1} = inj_S_WS(1:loc_virg(1)-1);
for j = 2:length(loc_virg)
    S_WS{j} = inj_S_WS(loc_virg(j-1)+1:loc_virg(j)-1);
end
S_WS{length(loc_virg)+1} = inj_S_WS(loc_virg(end)+1:end);
S_WS(1:2)=[]; %Datetime e forecastTime are discarded
clear fid inj_S_WS loc_virg

fid = fopen('F_WINDSOLAR.csv','rt+');
inj_F_WS = fgetl(fid);
fclose(fid);
loc_virg = strfind(inj_F_WS,',');
F_WS{1} = inj_F_WS(1:loc_virg(1)-1);
for j = 2:length(loc_virg)
    F_WS{j} = inj_F_WS(loc_virg(j-1)+1:loc_virg(j)-1);
end
F_WS{length(loc_virg)+1} = inj_F_WS(loc_virg(end)+1:end);
F_WS(1:2)=[]; %Datetime e forecastTime are discarded
clear fid inj_F_WS loc_virg

snap_inj=[S_L,S_WS];
forec_inj=[F_L,F_WS];
checkID=strcmp(snap_inj,forec_inj);
if checkID(1,:)==1
    disp('ok: equivalent injections ID (forecast Vs snapshot)')
else
    disp('error: not equivalent injections ID (forecast Vs snapshot)')
end
inj_ID=snap_inj;
clear S_L S_WS F_L F_WS checkID j snap_inj forec_inj

%********SET-UP of global matrices of snapshots and forecasts**************
[common,IA,IB] = intersect(snap_LOAD(:,1),snap_RES(:,1)); 
snap_LOAD_filt=snap_LOAD(IA,:);
snap_RES_filt=snap_RES(IB,:);
clear IA IB common snap_LOAD snap_RES

[common,IA,IB] = intersect(forec_LOAD(:,1),forec_RES(:,1)); 
forec_LOAD_filt=forec_LOAD(IA,:);
forec_RES_filt=forec_RES(IB,:);
clear IA IB common forec_LOAD forec_RES

snap=[snap_LOAD_filt,snap_RES_filt(:,3:end)]; %eliminated datetime and forecast of RES
forec=[forec_LOAD_filt,forec_RES_filt(:,3:end)];%eliminated datetime and forecast of RES

max_snap=max(abs(snap(:,3:end))); %max of snapshots used for err pu normalization 

clear snap_LOAD_filt forec_LOAD_filt snap_RES_filt forec_RES_filt

%**************************************************************************
%identify only snapshots having related forecasts
[common,IA,IB] = intersect(snap(:,1),forec(:,1)); %only first columns
snap_filt=snap(IA,:); %IA indexes of snapshots rows having related forecast datetime (first column)
forec_filt=forec(IB,:); %IA indexes of forecasts rows having related forecast datetime (first column)

clear snap forec;

%check
if snap_filt(:,1)==forec_filt(:,1) 
    disp('ok, number or related rows=')
    disp(length(forec_filt(:,1)));
else
    disp('error')
end

clear IA IB common

t1=cputime-t0;
disp('seconds for data loading=')
disp(t1)
