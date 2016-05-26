%
% Copyright (c) 2016, Ricerca sul Sistema Energetico – RSE S.p.A. <itesla@rse-web.it>
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

clear all
close all
clc
load('mcsamplerinput_20130115_1830_FO2_FR0.mat')
load('feanalyzerinput (2).mat')
snap_filt = snap_filt(:,3:end);
forec_filt = forec_filt(:,3:end);

for i = 1:size(snap_filt,2)
    if strfind(inj_ID{i},'SM_P')
        idxf = find(strcmp({generatore.codice},inj_ID{i}(1:end-2)));
        if isempty(idxf)==0
   if max(forec_filt(:,i)) < generatore(idxf(1)).P 
     disp([ inj_ID{i} ' with P assignment = ' num2str(generatore(idxf(1)).P) ' MW and max among historical forecasts = ' num2str(max(forec_filt(:,i))) ' MW']) 
   elseif min(forec_filt(:,i)) > generatore(idxf(1)).P
     disp([ inj_ID{i} ' with P assignment = ' num2str(generatore(idxf(1)).P) ' MW and min among historical forecasts = ' num2str(min(forec_filt(:,i))) ' MW'])   
   end
        end
    end
end
