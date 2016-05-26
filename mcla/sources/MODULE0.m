%
% Copyright (c) 2016, Ricerca sul Sistema Energetico – RSE S.p.A. <itesla@rse-web.it>
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

function [Y inj_ID idx_err idx_fore snapQ  inj_IDQ1 idx_errA idx_foreA YFPF inj_IDFPF] = MODULE0(snap_filt,fore_filt,inj_IDx,flagPQ,methods,tolvar,Nmin_obs_fr,Nmin_obs_interv,outliers,Koutliers,imputation_meth,Ngaussians,check_mod0,conditional_sampling)


    snap_new=snap_filt(:,3:end); %only snapshot fields without datetime and flag 0
    fore_new=fore_filt(:,3:end); %only forecast fields without datetime and flag 1440
          
    idxQ = [2:2:size(snap_new,2)];
    snapQ =   snap_new(:,idxQ);
    inj_IDQ = inj_IDx(:,2:2:end);
    for iq = 1:size(snapQ,2)
    inj_IDQ1{iq} = inj_IDQ{iq}(1:end-2);
    end
    if flagPQ == 0        
        fore_new(:,2:2:end)=[]; %odd columns of reactive injections are discarded
        snap_new(:,2:2:end)=[];  %odd columns of reactive injections are discarded
        inj_IDx(:,2:2:end)=[];  %odd columns of reactive injections are discarded
    end
    
    YFPF = snap_new-fore_new;
    inj_IDFPF = inj_IDx;
    
if conditional_sampling
    Y_new(:,1:2:2*size(snap_new,2)-1) = snap_new;
    Y_new(:,2:2:2*size(snap_new,2)) = fore_new;
    
    idx_err = [1:2:2*size(snap_new,2)-1];
    idx_fore = [2:2:2*size(snap_new,2)];
    inj_ID0(:,idx_err)=inj_IDx;
    inj_ID0(:,idx_fore)=inj_IDx;
 
    
for i =1:size(Y_new,2)
    idx_nan(i)=length(find(isnan(Y_new(:,i))));
    disp(['nr of gaps: ' num2str(idx_nan(i)) ' for variable ' num2str(i)])
end

switch methods
        case 1
            [Y inj_ID  idx_errA idx_foreA idx_err idx_fore ] = new_method_imputation(Y_new,inj_ID0,outliers,Koutliers,tolvar,Nmin_obs_fr,Nmin_obs_interv,check_mod0,idx_err,idx_fore);
        case 2
            [Y inj_ID idx_errA idx_foreA idx_err idx_fore ] = gaussian_conditional(Y_new,inj_ID0,outliers,Koutliers,tolvar,Nmin_obs_fr,Nmin_obs_interv,check_mod0,idx_err,idx_fore);
        case 3
            %             keyboard
            righe_valide = find(any(isnan(Y_new),2)==0);
            righe_non_valide = find(any(isnan(Y_new),2));
            if length(righe_valide) < size(Y_new,2)
                
                disp(['METHOD 3 NOT APPLICABLE: VALID SAMPLES LOWER THAN NR OF VARIABLES! '])
                disp(['SWITCH TO METHOD 2 ...'])
                [Y inj_ID idx_errA idx_foreA idx_err idx_fore ] = gaussian_conditional(Y_new,inj_ID0,outliers,Koutliers,tolvar,Nmin_obs_fr,Nmin_obs_interv,check_mod0,idx_err,idx_fore);
            else
                [Y inj_ID obj idx_errA idx_foreA idx_err idx_fore] = gaussian_mixture(Y_new,inj_ID0,outliers,Koutliers,Ngaussians,imputation_meth,tolvar,Nmin_obs_fr,Nmin_obs_interv,check_mod0,idx_err,idx_fore);
            end
    case 4
        [Y inj_ID idx_errA idx_foreA idx_err idx_fore ] = new_method_imputation4(Y_new,inj_ID0,outliers,Koutliers,tolvar,Nmin_obs_fr,Nmin_obs_interv,check_mod0,idx_err,idx_fore);
        otherwise
            error('invalid method')
end
    YFPF(:,idx_errA)=Y(:,idx_err);
    YFPF(:,idx_foreA)=Y(:,idx_fore);
    inj_IDFPF(:,idx_errA)=inj_ID(:,idx_err);
    inj_IDFPF(:,idx_foreA)=inj_ID(:,idx_fore);
    dummys = setdiff([1:length(inj_IDFPF)],[idx_errA idx_foreA]);
    for idum=1:length(dummys)
      inj_IDFPF(:,dummys(idum))={'0000'};  
    end
    
else
    Y_new = snap_new-fore_new;
    inj_ID0=inj_IDx;
    idx_err = [1:size(Y_new,2)];
    idx_fore = [];
    
    for i =1:size(Y_new,2)
    idx_nan(i)=length(find(isnan(Y_new(:,i))));
    disp(['nr of gaps: ' num2str(idx_nan(i)) ' for variable ' num2str(i)])
end

switch methods
    case 1
        [Y inj_ID idx_errA idx_foreA  idx_err idx_fore ] = new_method_imputation(Y_new,inj_ID0,outliers,Koutliers,tolvar,Nmin_obs_fr,Nmin_obs_interv,check_mod0,idx_err,idx_fore);
    case 2
        [Y inj_ID idx_errA idx_foreA idx_err idx_fore ] = gaussian_conditional(Y_new,inj_ID0,outliers,Koutliers,tolvar,Nmin_obs_fr,Nmin_obs_interv,check_mod0,idx_err,idx_fore);
    case 3
        [Y inj_ID  obj idx_errA idx_foreA idx_err idx_fore ] = gaussian_mixture(Y_new,inj_ID0,outliers,Koutliers,Ngaussians,imputation_meth,tolvar,Nmin_obs_fr,Nmin_obs_interv,check_mod0,idx_err,idx_fore);
    case 4
        [Y inj_ID idx_errA idx_foreA idx_err idx_fore ] = new_method_imputation4(Y_new,inj_ID0,outliers,Koutliers,tolvar,Nmin_obs_fr,Nmin_obs_interv,check_mod0,idx_err,idx_fore);
    otherwise
        error('invalid method')
end
YFPF(:,idx_errA)= Y;

end


