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
function [ yh,osc_signal, var_s,ydh ] = signal_filter(sig,t,var_thr,step_min,fault_instant,fault_duration)
%  [Yh,IDX,VAR ]= signal_filter(Y,T,Thr)
%   Filter signal Y based on variance. The signals with threshold lower than Thr are removed. 
%   The time vector T is required, which has variable step size. The output of the function are:
%    Yh- Output signal after filter
%    IDX- indexes of the most relevant signals
%    VAR-Variance of signals Y.
%
% If no oscillation is detected, the function gives empty matrices.



nL      = size(sig,2); % number of signals
ts      = t;           % time vector with variable step
trshold = var_thr;   % select signals with variance higher than var_thr
ts_threshold   = step_min;   % select section of signal with step size grater than step_min 
tsmin   = 0.01;
tsmax   = 0.2; 

id_m = [];

% The part of the signal during the fault is removed
idx_fault = find(t>fault_instant+fault_duration);
idx_fault = idx_fault(2:end);
time = t(idx_fault);
signal = sig(idx_fault,:);

% figure;plot(time,signal)
% title('Signals after fault')
% xlabel('Time (sec)')

%keyboard

if isempty(time)
    osc_signal=[];
    var_s=[];
    yh=[];
    ydh=[];
%fprintf('\n Signals not suitable for SSS analysis! \n\n')
else
    j0=1; 
    time0        = zeros(size(time));
    time0(1)     = time(1);
    time0(2:end) = time(1:end-1);
    dt0          = time-time0;
    gg=size(time,1);
    timex=[]; signalx=[]; dt0x=[];
    for k=1:gg
        if dt0(k)>=tsmin
            dt0x(j0,1)   = dt0(k);
            timex(j0,1)  = time(k);
            signalx(j0,:)= signal(k,:);
            j0=j0+1;
        end   
    end
    idt=[]; pp=1;
    for k=1:j0-1,
        if dt0x(k)>=tsmax
            idt(1,pp)=k;
            pp=pp+1;          
        end
    end
    if isempty(idt)
        idt=j0-1;
    end
        timex_0=timex(1:idt(1));
        signalx_0=signalx(1:idt(1),:); 
        dt0x_0=dt0x(1:idt(1));
        %figure;plot(timex_0,signalx_0);

    if isempty(signalx)
        osc_signal=[];
    var_s=[];
    yh=[];
    ydh=[];
    else
        for k=1:nL,
            signalx0(:,k)=signalx_0(:,k)-mean(signalx_0(:,k));
        end
              % ydh    = [timex_0,signalx0];
              sig_var = var(signalx0)'; % variance
              sig_std = std(signalx0)'; % standard deviation    
    [ var_sort,s_idx] = sort(sig_var,'descend');
         
   %figure;plot(timex_0,signalx0);axis tight
   
   m0=1;
   for k=1:size(dt0x_0,1)
           if dt0x_0(k)>=ts_threshold
            id_m(m0,1)   = k;
            m0=m0+1;
        end
   end
   if isempty(id_m)
   id_m=1;
   end
   
   time_m=timex_0(id_m(1):end);
   signal_m=signalx0(id_m(1):end,:);
   
    ydh    = [time_m,signal_m];
   %figure;plot(time_m,signal_m);axis tight
       
     nL0=size(var_sort,1);
     vA=max(var_sort);
     var_sort0=var_sort/vA;
     
     for k=1:nL
        if var_sort0(k)>= trshold;
            osc_signal(k,1)=s_idx(k);
            var_s(k,1)=var_sort(k);
        end
     end
    end
    
    
if isempty(osc_signal)
    osc_signal=[];
    var_s=[];
    yh=[];
    ydh=[];
   % fprintf('\n Signals not suitable for SSS analysis! \n\n')
else
  
    
    
 yh=sig(:,osc_signal);
 
 
%    figure;plot(ts,sig(:,osc_signal));
%    title(['Signals with oscillations higher than the threshold (',num2str(trshold),')'])
%    xlabel('Time (sec)')

end
end
end
