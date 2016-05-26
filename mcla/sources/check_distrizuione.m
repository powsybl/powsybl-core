%
% Copyright (c) 2016, Ricerca sul Sistema Energetico – RSE S.p.A. <itesla@rse-web.it>
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

function percentile = check_distrizuione(snap_filt,alfa)

snap_new=snap_filt(:,3:end); %only snapshot fields without datetime and flag 0

for i = 1:size(snap_filt,2)
    idx_ok = find(~isnan(snap_filt(:,i)));
[f,x] = ecdf(snap_filt(idx_ok,i));
dummy = find(abs(f-alfa)==min(abs(f-alfa)));
percentile(i) = x(dummy(1));
end
