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
function [V_index,i2h,ridh]=static_voltage(t,BVm,p_2,dev);
%% Post-fault Under/Over Voltage Index 
% Index for voltage of the transmission network right after an outage has occurred,
% the index indicates if the voltage (V), surpass the limits of the operational standards
%
%
% [Vindex]=satatic_voltage(t,V,p,d);
%
% INPUTS
%  t - Time vector
%  V - Voltages vector 
%  p - Exponent
%  d - Deviation allowed of voltage from nominal value, e.g. 2 for +-2%    
%
% OUTPUTS
%  Vindex -Under/Over voltage index 
%
%
%                       Version 1.2
% 

dev=dev/100;


%% The following parameters are used to calculate the index:
pre0  = 5;
post0 = 100;

tpost = t(end-post0:end);     % Period of time analyzed
Vnom =  mean(BVm(1:pre0,:));  % Nominal  value (pre-contingency)
Vpost = BVm(end-post0:end,:); % Voltage values post-contingency to be analyzed
Vmin =  Vnom*(1-dev);         % Under voltage limits 
Vmax =  Vnom*(1+dev);         % Over voltage limits 
nb =  size(Vnom,2);           % Number of buses 
wv_i = ones(1,nb);            % Uniform weights in all Buses  


delV   = (Vmax - Vmin)/2;     %Equation 
Vmean  = mean(Vpost);         %Equation


% Remove bus out of service
xxx=[];k=1;
for i=1:nb
 if abs(Vmean(1,i))<1e-2
     xxx(k,1)=i;
     BVm(:,i)=0;
     Vmean(1,i)=0;
     Vnom(1,i)=0;
     Vmin(1,i)=0;
     Vmax(1,i)=0;
     wv_i(1,i)=0;
     k=k+1;
 end    
end
clear k


%% Calculation of the actual index
B0=[];co=[]; rr=1;

for i=1:nb

    
% Index equation base on average value    
indxs =wv_i(1,i)*((abs(Vnom(i)-Vmean(:,i))./delV(i)).^p_2)';

B0(i,1)=isnan(indxs); % check for Not-a-Number (NaN) Value

if B0(i)==0 

% Applying considerations to understand the value of the index
i2(i,1)=max(indxs);


rid(i,1) = i2(i,1).^(1/p_2);

if rid(i)<=1
    ridh(i,1)=1;
    i2h(i,1)=1;
else if (rid(i)>=1) & (rid(i)<=5*(dev*100))
     ridh(i,1)=rid(i);
     i2h(i,1)=i2(i);
    else               
   ridh(i,1)= rid(i);
   i2h(i,1)=i2(i);
    end
end

else
ridh(i,1)=0;    
i2h(i,1)=0;
co(rr,1)=i; % number of signal with NaN 
rr=rr+1;
end
end

nb0=size(co,1);
i2_tot=sum(i2);


%%%%%%%%%%%% Under/Over Voltage Index %%%%%%%%%
V_index = sum(i2h)/(nb-nb0);  % The actual index normalized
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%


%% Ploting results
%  r1 = find(i2>=1);   % index of lines violating limits
%  nvl= size(r1,1);  % number of lines violating limits
%  r2 = (i2).^(1/p_2);
%  nvlsevere=0; 
% 
% if nvl>0
%     figure;plot(t,BVm(:,r1));axis tight
%     title(['Lines violating limits = ', num2str(nvl),'   Index=',num2str(V_index)])
%     xlabel('Time (sec)')
%     ylabel('Voltage')
%     if nvlsevere>0
%         figure;plot(t,BVm(:,r20));axis tight
%         title(['Lines (',num2str(nvlsevere), ') violating limits ',num2str(dev*100),' %'])
%         xlabel('Time (sec)')
%         ylabel('Voltage')
%     end
%     a= find(i2(r1)==max(i2(r1)));
%     mm=r1(1);
% 
%     figure;plot(t,BVm(:,mm));axis tight
%     hold on; plot(t(end-post0:end),BVm(end-post0:end,mm),'linewidth',3)
%     hold on; horline(t,Vmin(mm),'k:');horline(t,Vmax(mm),'k:')
%     hold on; horline(t,mean(Vpost(:,mm)),'r')
%     title(['Line ', num2str(mm),' with maximum violation,  v=',num2str(V_index)])
%     xlabel('Time (sec)')
%     ylabel('Voltage')
% end 
 
end



 
