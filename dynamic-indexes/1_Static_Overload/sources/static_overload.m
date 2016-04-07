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
function [Over_S,i2h,ridh]=static_overload(t,S1,p_1,dev0);
%% Post-fault Over Load Index
% This index is used to observer if the post-fault flows surpass the network capacity,
% by monitoring the power flows through the transmission lines right after an outage occurs.
%
%
% [Sindex]=static_overload(t,t1,Signal,w,p,d);
%
% INPUTS
%  t - Time vector
%  S - Apparent power flow 
%  p - Exponent
%  d - Deviation allowed of power flow from nominal value, e.g. 10 for +10%    
% 
%  OUTPUTS
%  Sindex -Overload index 
%
%                                   Version 1.2


%% OVERLOAD 
pre0  = 5;
post0 = 100;

dev0  = dev0/100;  
nl    = size(S1,2);  
wf_i  = ones(1,nl);         % Uniform weights in all Lines is assumed 
Snom  = mean(S1(1:pre0,:)); % Nominal value (pre-contingency)
Smax  = Snom*(1+dev0);      % Over power flow limits 
Spost = S1(end-post0:end,:); 
Smean = mean(Spost);

% Remove lines out of service
xxx=[];k=1;
for i=1:nl
 if abs(Smean(1,i))<1e-2
     xxx(k,1)=i;
     S1(:,i)=0;
     Smean(1,i)=0;
     Snom(1,i)=0;
     k=k+1;
 end    
end
clear k


k=1; over_line=[];
for i=1:nl
indxs_loc=(wf_i(i)*((abs(Smean(1,i))/abs(Smax(1,i)))^p_1));
index_red(i)=indxs_loc;


if indxs_loc>=1
   indx(i)=indxs_loc;
   over_line(k)=i; % indices of lines violating limits
   k=k+1;
else
   indx(i)=1;
end


rid(i,1) = index_red(i).^(1/p_1);
if rid(i)<=1
    ridh(i,1)=1;
    i2h(i,1)=1;
    else               
   ridh(i,1)= rid(i); 
   i2h(i,1)=index_red(i);
end

end


Over_S=sum(i2h)/nl;

%% Plotting results (if required)
% if Over_S>=1
%     nvl=size(over_line,2); %Number of lines violating the limits
%     Sextra_pu= indx(over_line)/max(indx(over_line));
%     for i=1:nvl
%         if  indx(over_line(i)).^(1/p_1)<=2
%     indx_over_per(i,1)=((indx(over_line(i)).^(1/p_1))-1)*100;
%         else
%              indx_over_per(i,1)=((indx(over_line(i)).^(1/p_1)))*100;
%         end
%     end
%     
%  if nvl>=1
%     
% figure;bar(Smean(1,over_line),'g');hold on;bar(Smax(1,over_line),'y'); legend('S_{mean}','S_{max_{limit}}')
% xlabel('Line Number');ylabel('MVA'); title(['Lines violating the limits=',num2str(nvl)]);
% set(gca,'XTick',[1:nvl])
% set(gca,'XTickLabel',over_line)
% 
% figure;bar(Sextra_pu,'r')
% rr=find(indx==max(indx));
% title(['Line ',num2str(rr),' with maximum violation of ',num2str(max(indx_over_per)),'% more'])
% set(gca,'XTick',[1:nvl])
% set(gca,'XTickLabel',over_line)
% 
% figure;plot(t,S1(:,rr));axis tight;title(['Line number ', num2str(rr),' with maximum violation'])
% hold on; plot(t(end-post0:end),S1(end-post0:end,rr),'linewidth',3)
% hold on; horline(t,Smax(:,rr),'k:');hold on; horline(t,mean(Spost(:,rr)),'r')
% xlabel('Time (sec)'); ylabel('MVA')
% end
% 
% end




 
end
