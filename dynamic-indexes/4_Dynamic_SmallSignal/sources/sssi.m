%
% Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

%
% Authors: Luigi Vanfretti, Venkata Satya Narasimham Arava, Rafael Segundo Sevilla
% Email: luigi.vanfretti@gmail.com
%
function [ sss,out,modelb,detail ] = sssi( signal,ts,stepsize_min,variance_min,Freq,Damp,Nm,f_instant,f_duration)
%Small-signal-stability index (SSI) function 
%
%[ INDEX ] = sssi( signal,ts,step_min,var_min,F,D,Nm)
%
% OUTPUTS
%
%      INDEX - Three layer sss index with smi, ami and gmi.
%
% INPUTS
%     signal - Active power flow of relevant lines 
%         ts - Time vector with varable step size 
%   step_min - Minimum varable step for signal analysis 
%    var_min - Filter signals with varianza lower than var_min
%         F  - [fmin,fmax] 1x2 vector with frequencies of interest in Hz
%         D  - [d0,d1,d2] 1x3 vector with damping ratios to compute index in
%                       percent
%         Nm - Number of modes used in Prony to reconstruct input signals
%  f_instant - Instant of the fault
% f_duration - Duration of the fault
%
%    * Empty matrices will be delivered if input signals used are not
%      suitable for sss analysis


[yh,x,l,yhd]=signal_filter(signal,ts,variance_min,stepsize_min,f_instant,f_duration);

if isempty(yhd)
out=[];
sss.smi=[];
sss.ami=[];
sss.gmi=[];
modelb=[];
detail.l=[];
detail.f=[];
detail.d=[];
fprintf('\n Signals not suitable for SSS analysis! \n\n')
else

t = yhd(:,1);
y = yhd(:,x+1);

%figure;plot(t,y);


if size(t,1)<=5
    out=[];
sss.smi=[];
sss.ami=[];
sss.gmi=[];
modelb=[];
detail.l=[];
detail.f=[];
detail.d=[];
fprintf('\n Signals not suitable for SSS analysis! \n\n')

else    
    
out.t =t;
out.y =y;

%%
fmin= Freq(1);  % Remove frequencies less than fmin Hz
fmax= Freq(2);  % Remove frequencies greater than fmax Hz
dmax=0.25; % Remove modes with damping greater than dmax
Nm;   % Number of modes used to reconstruct signals using Prony 

sig=y(:,:);
nL=size(sig,2);

[lamda,modelb]=pronyiTesla(t,sig,Nm,t(1)*ones(1,nL),t(end)*ones(1,nL),0.1,1,t(1),t(end),0);

Poles.sys=diag(lamda);

n_modes1 = size(lamda,1); %number of modes
fmodes0  = abs(imag(lamda))/2/pi; % frequency of modes in Hz
dmodes0  = -cos(atan2(imag(lamda),real(lamda))); %damping of modes
          
[fmodes1,mode_idx1]=sort(fmodes0,'ascend'); %sorted frequencies
dmodes1=dmodes0(mode_idx1); %sorted dampings

jj=1; kk=1; fmodes3=[];
for i=1:n_modes1,
    
    if (fmodes1(i)>=fmin)&(fmodes1(i)<=fmax) %modes within fmin and fmax
        fmodes2(jj,1)=fmodes1(i);
        dmodes2(jj,1)=dmodes1(i);
        mode_idx2(jj,1)=mode_idx1(i);
       if dmodes2(jj,1)<=dmax   % discard modes with large damping ratio
           fmodes3(kk,1)=fmodes2(jj);
           dmodes3(kk,1)=dmodes2(jj);
           mode_idx3(kk,1)=mode_idx2(jj);
           kk=kk+1;           
       end
        jj=jj+1;
    end
    
end

if isempty(fmodes3)
    out=[];
sss.smi=[];
sss.ami=[];
sss.gmi=[];
modelb=[];
detail.l=[];
detail.f=[];
detail.d=[];
fprintf('\n Signals not suitable for SSS analysis! \n\n')
else

n_modes2=size(fmodes3,1);
jj=[1:2:n_modes2]';
fmodes=fmodes3(jj);
dmodes=dmodes3(jj);

fprintf('\n f(Hz)   d(%%) \n')
fprintf('--------------')
for i=1:n_modes2/2
fprintf('\n%-7.3f %-7.3f ',fmodes(i),dmodes(i)*100)
end
fprintf('\n\n')

detail.l=lamda(mode_idx3);
detail.f=fmodes0(mode_idx3);
detail.d=dmodes0(mode_idx3);


%% Actual Three layer index SMI, AMI, GMI

%Damp=[0,5,10]; % Damping ratios, Calculate damping distance from each mode to the specified dampings, ie [0%, 5%, 10%]

th  = acos(Damp/100);
ths = pi-th;

thl  = acos(dmodes);
thls = pi-thl;

jj=1; kk=size(Damp,2);
for i=1:1:n_modes2/2
    for m=1:1:kk        
    SMI(jj,m)=thls(i)-ths(m); % Matrix, Single Mode Indicator(SMI) in rads
    end
    jj=jj+1;
end
 
for m=1:kk
AMI(1,m)=min(SMI(:,m)); %Vector, All Mode Indicator (AMI) in rads
end
 

GMI=min(AMI);  % Gain, Global Mode Indicator (GMI) in rads

sss.smi = SMI;
sss.ami = AMI;
sss.gmi = GMI;

end
end
end
end
