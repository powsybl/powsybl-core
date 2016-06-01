%
% Copyright (c) 2016, Ricerca sul Sistema Energetico – RSE S.p.A. <itesla@rse-web.it>
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

function exitcode=FEA_MODULE1_HELPER(ifile, ofile,natS,ofile_forFPF,ofileGUI, IRs, Ks, s_flagPQ,s_method,tolvar,Nmin_obs_fract,Nmin_obs_interv,outliers,koutlier,imputation_meth,Ngaussians,percentile_historical,check_module0,toleranceS,iterationsS,epsiloS,conditional_samplingS,histo_estremeQs,thresGUIs,s_rng_seed)
%% s_rng_seed - int seed (optional, default is 'shuffle' on current date)
close all;
mversion='1.8.0';
disp(sprintf('wp5 - module1 - version: %s', mversion));
disp(sprintf(' ifile:  %s',ifile));
disp(sprintf(' ofile:  %s',ofile));
disp(sprintf(' nat:  %s',natS));
disp(sprintf(' IR:  %s',IRs));
disp(sprintf(' K:  %s',Ks));
disp(sprintf(' flagPQ:  %s',s_flagPQ));
disp(sprintf(' method:  %s',s_method));
disp(sprintf(' tolvar:  %s',tolvar));
disp(sprintf(' Nmin_obs_fract:  %s',Nmin_obs_fract));
disp(sprintf(' Nmin_obs_interv:  %s',Nmin_obs_interv));
disp(sprintf(' outliers:  %s',outliers));
disp(sprintf(' n for n-sigma rule to filter out outliers:  %s',koutlier));
disp(sprintf(' imputation_meth:  %s',imputation_meth));
disp(sprintf(' Ngaussians:  %s',Ngaussians));
disp(sprintf(' percentile_historical:  %s',percentile_historical));
disp(sprintf(' check_module0:  %s',check_module0));
disp(sprintf(' tolerance:  %s',toleranceS));
disp(sprintf(' iterations:  %s', iterationsS));
disp(sprintf(' epsilo:  %s', epsiloS));
disp(sprintf(' conditional_sampling:  %s', conditional_samplingS));
disp(sprintf(' histo_estremeQ:  %s', histo_estremeQs));
disp(sprintf(' thresGUI:  %s', thresGUIs));

par_tolvar = str2double(tolvar);% minimum variance of imput variables, in MW
par_Nmin_obs_fract = str2double(Nmin_obs_fract);% minim fraction of valid samples for each stochastic variables
par_Nmin_obs_interv = str2double(Nmin_obs_interv);% min nr of samples in common for each pair of variables
par_outliers = str2double(outliers);% 0 = outliers are included as valid samples, 1 = outliers are excluded
par_imputation_meth = str2double(imputation_meth); % type of inputation techniques for gaussian mixture: 2= maximum likelihood, 1 = highest prob. gaussian
par_Ngaussians = str2double(Ngaussians); % nr of gaussians of the mixture
percentil = str2double(percentile_historical); % percentage of quantile to identify min and max Q values for Q samples in case flagPQ = 0
check_mod0 = str2double(check_module0); % if 1, calculate some performance metrics
Koutliers = str2double(koutlier); % multiplier of std dev in n sigma rule
tolerance = str2double(toleranceS); % accuracy in correlation matrix inversion
iterations = str2double(iterationsS); % max nr of iterations for matrix inversion
epsilo = str2double(epsiloS); % increment at each iteration in matrix inversion procedure
conditional_sampling = str2double(conditional_samplingS); % activation of conditional sampling
histo_estremeQ=str2double(histo_estremeQs);
thresGUI=str2double(thresGUIs);
moutput.errmsg='Ok';



IR=str2double(IRs);
K=str2double(Ks);
% if seed is not specified, 'shuffle'  on current platform time
if nargin < 25
    rng('shuffle','twister');
    disp(sprintf(' rng seed:  not specified, default is shuffle on current platform time'));
else
    rng(str2double(s_rng_seed),'twister');
    disp(sprintf(' rng seed:  %s',s_rng_seed));
end
rng_data=rng;
flagPQ=str2double(s_flagPQ);
try
    
    load(ifile);
    
    %%%%% NAT_ID
    quali_nats = unique(nat_ID);
    for inat = 1:length(quali_nats)
        inj_nat{inat} = find(strcmp(nat_ID,quali_nats{inat}));
        FOnat{inat} = forec_filt(:,2+inj_nat{inat});
        SNnat{inat} = snap_filt(:,2+inj_nat{inat});
        quanti_fovalid(inat) = length(find(sum(not(isnan(FOnat{inat})),1)>par_Nmin_obs_fract*size(FOnat{inat},1)));
        quanti_snvalid(inat) = length(find(sum(not(isnan(SNnat{inat})),1)>par_Nmin_obs_fract*size(SNnat{inat},1)));
        if quanti_fovalid(inat) > 0.5*size(FOnat{inat},2)
            modality_gaussians(inat) = 0;
        else
            modality_gaussians(inat) = 1;
        end
        
        nations{inat,1}=    [FOnat{inat}];
        nations{inat,2} =     [SNnat{inat}];
        nations{inat,3} =   inj_ID(inj_nat{inat});
        if strcmp(lower(natS),'all')
            selezionate(inat) =1;
        else
            if isempty(strfind(natS,quali_nats{inat}))
                selezionate(inat) =0;
            else
                selezionate(inat) =1;
            end
        end
    end
    
    qua_selezionate = find(selezionate == 1);
    
    quali_normal = intersect(find(modality_gaussians == 0),qua_selezionate);
    
    quali_gauss = setdiff([1:length(quali_nats)],quali_normal);
    
    if isempty(quali_normal) == 0
        nations_normal.forec_filt = [forec_filt(:,1:2) nations{quali_normal,1}];
        nations_normal.snap_filt = [snap_filt(:,1:2) nations{quali_normal,2}];
        nations_normal.inj_ID = [nations{quali_normal,3}];
        k_norm = 1;
    else
        k_norm = 0;
        nations_normal.forec_filt=[];
        nations_normal.snap_filt=[];
    end
    nations_gauss.forec_filt = [];
    nations_gauss.snap_filt = [];
    nations_gauss.inj_ID = {};
    for ino = 1:length(quali_gauss)
        nations_gauss(ino).forec_filt = [];
        nations_gauss(ino).snap_filt = [];
        nations_gauss(ino).inj_ID = nations{quali_gauss(ino),3};
    end
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% NORMAL TREATMENT
    
    %
    
    if ~isempty(nations_normal.forec_filt)
        %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        % here starts RSE CODE, extracted from TEST_MCLA,m
        %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        forec_filt = nations_normal.forec_filt;
        snap_filt =  nations_normal.snap_filt;
        flagesistenza = not(isempty(snap_filt));
        inj_ID = nations_normal.inj_ID;
        
        method = str2double(s_method);
        
        disp(['STARTED ALGORITHM TO RETRIEVE DATA FOR UNCERTAINTY GUI'])
        disp(['STARTED ALGORITHM TO RETRIEVE DATA FOR FPF CONDITIONED ERROR FORECASTS'])
        %         keyboard
        [dati_FPF] = calcolo_inv_mat_perFPF(snap_filt-forec_filt,forec_filt,inj_ID,flagPQ,method,ofile_forFPF,ofileGUI,par_tolvar,par_Nmin_obs_fract,par_Nmin_obs_interv,par_outliers,Koutliers,par_imputation_meth,par_Ngaussians,check_mod0,percentil,tolerance,iterations,epsilo,thresGUI);
        
        
        disp(['STARTED ALGORITHM TO FIX NaN VALUES IN FORECAST ERROR MATRIX'])
        tic;
        
        
        %%% CONDITIONAL SAMPLING ACTIVATED
        if conditional_sampling == 1
            
            
            [Y inj_ID idx_err idx_fore snapQ inj_IDQ dummy1 dummy2 dummy3 dummy4] = MODULE0(snap_filt,forec_filt,inj_ID,flagPQ,method,par_tolvar,par_Nmin_obs_fract,par_Nmin_obs_interv,par_outliers,Koutliers,par_imputation_meth,par_Ngaussians,check_mod0,conditional_sampling);
            
            t_module0= toc;
            disp(['GAP FILLING MODULE COMPLETED IN ' num2str(t_module0) ' SECONDS'])
            
            disp(['EVALUATING THE MAXIMUM VALUES OF VARIABLES (PERCENTILES)'])
            inj_ID0 = inj_ID;
            idx_err0 = idx_err;
            idx_fore0 = idx_fore;
            
            % find matching between idx err0 and idx fore 0
            for jfor = 1:length(idx_fore0)
                dummys = find(ismember(inj_ID0(idx_err0),inj_ID0(idx_fore0(jfor))));
                if isempty(dummys)==0
                    match_snap_to_fore(jfor)=dummys;
                else
                    match_snap_to_fore(jfor)=NaN;
                end
            end
            
            FORE0=Y(:,idx_fore0);
            
            m_e = mean(Y(:,idx_err),1);
            m_y = mean(Y(:,idx_fore),1);
            std_e = std(Y(:,idx_err),0,1);
            std_y = std(Y(:,idx_fore),0,1);
            
            disp(['FILTERING OUT HIGHLY CORRELATED FORECAST VARIABLES'])
            tic
            testfilterings2
            
            %%%%%%%%%%%%%%%%%%%%%%%%%%%%%
            tempo(1)=toc;
            %%%%% put into a function
            
            for ncol = 1:size(snapQ,2)
                validi = find(~isnan(snapQ(:,ncol)));
                if length(validi) < 100
                    maxvalue(ncol,:)=[0 0];
                else
                    [x1,x2] = ecdf(snapQ(validi,ncol));
                    maxvalue(ncol,1) = x2(find(abs(x1-percentil)==min(abs(x1 - percentil))));
                    maxvalue(ncol,2) = x2(find(abs(x1-(1-percentil))==min(abs(x1 - (1-percentil)))));
                end
            end
            
            
            disp(['STARTED ALGORITHM TO ESTIMATE COPULAS AND CDFS'])
            tic;
            X = Y;
            [ module1 ] = MODULE1( X,K);
            
            toc;
            
            tic
            media(idx_err) = m_e;
            media(idx_fore) = m_y;
            stddev(idx_err)=std_e;
            stddev(idx_fore)=std_y;
            disp(['NATAF TRANSFORMATION OF FORECASTS AND SNAPSHOTS'])
            for ij = 1:size(Y,2)
                X_NEW1S(:,ij) = (Y(:,ij) - media(ij))./(stddev(ij));
            end
            
            for ie = 1:length(idx_err)
                %             tic
                % THIS VERSION WITH KSDENSITY IS VERY TIME CONSUMING ... SWITCHED TO A MUCH FASTER VERSION
                %             CDF_E_NEW1(:,ie) = ksdensity(X_NEW1S(:,idx_err(ie)),X_NEW1S(:,idx_err(ie)),'function','cdf');
                %             toc
                CDF_E_NEW1(:,ie) = estract_cdf(X_NEW1S(:,idx_err(ie)));
                
                %             [CDF_SN{ie} xsn{ie}] = ecdf(X_NEW1S(:,idx_err(ie)));
                INVG_E_NEW1(:,ie) = norminv(CDF_E_NEW1(:,ie),0,1);
            end
            for iy = 1:length(idx_fore)
                %             CDF_Y_NEW1(:,iy) = ksdensity(X_NEW1S(:,idx_fore(iy)),X_NEW1S(:,idx_fore(iy)),'function','cdf');
                CDF_Y_NEW1(:,iy) = estract_cdf(X_NEW1S(:,idx_fore(iy)));
                INVG_Y_NEW1(:,iy) = norminv(CDF_Y_NEW1(:,iy),0,1);
                %             [CDF_Y{iy} xfo{iy}] = ecdf(X_NEW1S(:,idx_fore(iy)));
            end
            
            X_NEW2S(:,idx_err)=INVG_E_NEW1;
            X_NEW2S(:,idx_fore)=INVG_Y_NEW1;
            
            cov_tot = cov(X_NEW2S);
            
            cov_yy2 = cov_tot(idx_fore,idx_fore);
            cov_ey2 = cov_tot(idx_err,idx_fore);
            cov_ee2 = cov_tot(idx_err,idx_err);
            
            corr_yy2 = (( diag(diag(cov_yy2)))^(-0.5))*cov_yy2*(( diag(diag(cov_yy2)))^(-0.5));
            corr_ey2 = (( diag(diag(cov_ee2)))^(-0.5))*cov_ey2*(( diag(diag(cov_yy2)))^(-0.5));
            corr_ee2 = (( diag(diag(cov_ee2)))^(-0.5))*cov_ee2*(( diag(diag(cov_ee2)))^(-0.5));
            disp(['INVERSION OF FORECAST CORREALTION MATRIX WITH GIVEN ACCURACY'])
            %
            [invcorr_yy2x corr_yy2] = inversion_with_verify2(corr_yy2,tolerance,iterations,epsilo);
            %%%%%%%%%%%%%%%%%%%%%%%%%%
            %%% DATA STRUCTURE CONTAINING VARIABLES USED FOR CONDITIONAL
            %%% SAMPLING ALGORITHM
            dati_cond.invmatr_corryy = invcorr_yy2x;
            dati_cond.matrice_yy = matrice;
            dati_cond.cov_ey = cov_ey2;
            dati_cond.corr_ee = corr_ee2;
            dati_cond.corr_ey = corr_ey2;
            dati_cond.idx_err = idx_err;
            dati_cond.idx_fore = idx_fore;
            dati_cond.idx_err0 = idx_err0;
            dati_cond.idx_fore0 = idx_fore0;
            dati_cond.match_snap_to_fore = match_snap_to_fore;
            dati_cond.inj_ID0 = inj_ID0;
            dati_cond.tolerance = tolerance;
            dati_cond.iterations = iterations;
            dati_cond.epsilo = epsilo;
            dati_cond.Y=Y;
            dati_cond.FORE0=FORE0;
            %%%%%% 	ARRAY OF STRINGS TO IDENTIFY REACTIVE INJECTIONS IN THE
            %%%%%% 	MAXVALUE MATRIX
            dati_Q.inj_IDQ = inj_IDQ;
            
        else
            %%%% CALCOLO QUANTIT� PER FPF
            
            
            %%% CONDITIONAL SAMPLING DEACTIVATED -> UNCODITIONED SAMPLING
            %%% OF FORECAST ERRORS
            [Y inj_ID idx_err idx_fore snapQ  inj_IDQ dummy1 dummy2 dummy3 dummy4] = MODULE0(snap_filt,forec_filt,inj_ID,flagPQ,method,par_tolvar,par_Nmin_obs_fract,par_Nmin_obs_interv,par_outliers,Koutliers,par_imputation_meth,par_Ngaussians,check_mod0,conditional_sampling);
            
            t_module0= toc;
            disp(['GAP FILLING MODULE COMPLETED IN ' num2str(t_module0) ' SECONDS'])
            
            disp(['EVALUATING THE MAXIMUM VALUES OF VARIABLES (PERCENTILES)'])
            
            
            for ncol = 1:size(snapQ,2)
                validi = find(~isnan(snapQ(:,ncol)));
                if length(validi) < 100
                    maxvalue(ncol,:)=[0 0];
                else
                    [x1,x2] = ecdf(snapQ(validi,ncol));
                    maxvalue(ncol,1) = x2(find(abs(x1-percentil)==min(abs(x1 - percentil))));
                    maxvalue(ncol,2) = x2(find(abs(x1-(1-percentil))==min(abs(x1 - (1-percentil)))));
                end
            end
            
            tic;
            X = Y;
            [ module1 ] = MODULE1( X,K);
            toc;
            
            dati_Q.inj_IDQ = inj_IDQ;
            dati_cond=[];
        end
        
        moutput(1).errmsg='Ok';
        moutput(1).rng_data=rng_data;
        moutput(1).inj_ID=inj_ID;
        moutput(1).flagPQ=flagPQ;
        moutput(1).method=method;
        moutput(1).module1=module1;
        moutput(1).modality_gaussian=0;
        moutput(1).dati_cond = dati_cond;
        moutput(1).dati_Q = dati_Q;
        moutput(1).dati_FPF = dati_FPF;
        moutput(1).mversion=mversion;
        moutput(1).flagesistenza=flagesistenza;
        % added field
        moutput(1).maxvalue=maxvalue;
        moutput(1).conditional_sampling=conditional_sampling;
        clear maxvalue  inj_ID
    end
    
    if  ~isempty(nations_gauss(1).inj_ID)
        %         keyboard
        
        for ino = 1:length(nations_gauss)
            snap_filt = nations_gauss(ino).snap_filt;
            forec_filt = nations_gauss(ino).forec_filt;
            inj_ID = nations_gauss(ino).inj_ID;
            flagesistenza = not(isempty(snap_filt));
            %%% CALCULATION OF HISTORICAL LIMIT QUANTILES ALSO IN CASE OF
            %%% GAUSSIAN FICTITIOUS FORECAST ERRORS
            if isempty(snap_filt)==0
                snap_new1 = snap_filt(:,3:end);
                snapQ = snap_new1(:,2:2:end);
                inj_IDQ1 = inj_ID(2:2:end);
                for ncol = 1:size(snapQ,2)
                    validi = find(~isnan(snapQ(:,ncol)));
                    if length(validi) < 100
                        maxvalue(ncol,:)=[0 0];
                    else
                        [x1,x2] = ecdf(snapQ(validi,ncol));
                        maxvalue(ncol,1) = x2(find(abs(x1-percentil)==min(abs(x1 - percentil))));
                        maxvalue(ncol,2) = x2(find(abs(x1-(1-percentil))==min(abs(x1 - (1-percentil)))));
                    end
                    inj_IDQ{ncol}=inj_IDQ1{ncol}(1:end-2);
                end
            else
                inj_IDQ1 = inj_ID(2:2:end);
                for ncol = 1:length(inj_IDQ1)
                    
                    maxvalue(ncol,1) = -histo_estremeQ;
                    maxvalue(ncol,2) = histo_estremeQ;
                    
                    inj_IDQ{ncol}=inj_IDQ1{ncol}(1:end-2);
                end
            end
            if flagPQ == 0
                inj_ID(:,2:2:end)=[];  %odd columns of reactive injections are discarded
            end
            method = 0;
            module1 = [];
            %         maxvalue=[];
            dati_Q.inj_IDQ = inj_IDQ;
            dati_FPF.idx_errA = [1:length(inj_ID)];
            dati_FPF.idx_foreA = [1:length(inj_ID)];
            dati_FPF.inj_ID = inj_ID;
            dati_cond=[];
            moutput(k_norm+ino).errmsg='Ok';
            moutput(k_norm+ino).rng_data=rng_data;
            moutput(k_norm+ino).inj_ID=inj_ID;
            moutput(k_norm+ino).modality_gaussian=1;
            moutput(k_norm+ino).flagPQ=flagPQ;
            moutput(k_norm+ino).method=method;
            moutput(k_norm+ino).module1=module1;
            moutput(k_norm+ino).dati_cond = dati_cond;
            moutput(k_norm+ino).dati_Q = dati_Q;
            moutput(k_norm+ino).dati_FPF = dati_FPF;
            moutput(k_norm+ino).mversion=mversion;
            moutput(k_norm+ino).flagesistenza=flagesistenza;
            % added field
            moutput(k_norm+ino).maxvalue=maxvalue;
            moutput(k_norm+ino).conditional_sampling=conditional_sampling;
            clear maxvalue inj_IDQ inj_IDQ1 snap_new1 snapQ inj_ID
        end
    end
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    % here ends RSE CODE, extracted from TEST_MCLA,m
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    
    %save output in .mat
    
    exitcode=0;
catch err
    moutput(1).errmsg=err.message;
    disp(getReport(err,'extended'));
    exitcode=-1;
end
totmoutput.out1 = moutput;

save(ofile, '-struct', 'totmoutput');
% exit(exitcode);
end
