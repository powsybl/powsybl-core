%
% Copyright (c) 2016, Ricerca sul Sistema Energetico – RSE S.p.A. <itesla@rse-web.it>
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% function to run the MC sampling like approach
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% Inputs:
% - generatore, nodo: data structure for nodes and generators of the grid
% containing the FORECAST VALUES for LOADS and GENERATION
% % generatore =
% %
% %     estremo_ID = terminal node ID of generator
% %     estremo = terminal node of generator
% %     codice = code of unit
% %     conn (1= available, 0 = out of service)
% %     P (actual active power, MW) from FORECAST <--
% %     Qmax (max reactive power, MVAr)
% %     Qmin (min reactive power,MVAr)
% %     Pmin (min active power, MW)
% %     Pmax (maximum active power, MW)
% %     Anom (MVA of generator)
% %     RES (1 o 2 = RES, = = conventional units)
% %     Tavv (starting time of units in second)
% %     fuel (type of fuel -> 0=RES, 1 = hydro, 2 = gas, 3 = oil, 4 = nuclear, 5 = coal)
% %     dispacc (1 = dispatchable, 0 = not dispatchable): this field is
% useful for redispatching and it is derived from fuel types on the basis
% of realistic assumptions
%
% Fields of interest of struct 'carico' are  =
%
%     node : reference to the node the load is connected to
%     conn: 1 = in service 0= out of service
%     P (actual active power, MW) from FORECAST <--
%     Q (actual reactive power, MVAr) from FORECAST <--
%
% Fields of interest of struct 'nodo' are nodo =
%
%     type (1 = slack, 2 = PQ, 3 = PV)
%     carichi: indexes referring to the loads connected to the node
%     generatori: indexes referring to the generators connected to the node
%
% - scenarios: number of MC samples to be generated , (scalar quantity)
% - dispatch: type of dispatching of conventional dispatchable generators
% (1=max power; 2= actual power; 3= inertia, 0= lumped slack )
% - type_X is the vector (dimension = Nvar) which specifies the nature of the stochastic
% injections (RES or load).
% - module 3 : outputs from previous module 3 containing uncoditioned
% samples
% - module 2: outputs from previous modules containing information about
% estiation of copula families for each pair copula
% - flagPQ: if 1, P and Q are sampled as separate variables, while if
% flagPQ=0 Q samples are derived starting from P samples, by applying a
% constant power factor
% - limits_reactive: the alpha and 1-alpha quantiles of the historical Q
% distributions, to limit Q values in case flagPQ = 0
% - opt_sign: option to avoid sign inversion in samples with respect to
% forecast
% - dati_cond: structure containing the quantities - such as correlation matrixes,
% ...- used for conditional sampling
% - y0: vector of forecasts adsigned by on-line part of MCLA
% - conditional sampling: if 1 conditional sampling is activated. if 0 ->
% uncoditioned sampling of forecast errors
% - mod_gauss -> if 1 gaussian fictitious forecast errors are adopted (used
% for validation purposes)
% - centering -> option to center conditioned samples onto the basecase
% DACF (valid only for conditional_sampling ==1)
function [PGEN PLOAD QLOAD ] = main_MCLA2PC3(generatore,carico,nodo,scenarios,type_X0,module2,module3,flagPQ,limits_reactive,opt_sign,dati_cond,y0,conditional_sampling,mod_gauss,centering)

%%%% THIS PARAMETER IS ONLY USED FOR VALIDATION PURPOSES, TO PROVIDE PLOTS
%%%% FOR VALIDATION PHASE
validation = 0;
% additional checks: tool generates at most a number of conditional samples
% equal to unconditioned samples

%%%%%

type_X = type_X0(:,find(type_X0(3,:)==1));

n_vars = size(type_X,2);

are_snapshots = find(type_X0(4,:)==1);

idx_RES = (type_X0(2,intersect(find(type_X0(1,:)==1),find(type_X0(4,:)==1))));
idxq_RES = (type_X0(2,intersect(find(type_X0(1,:)==4),find(type_X0(4,:)==1))));
idx_carichi = (type_X0(2,intersect(find(type_X0(1,:)==2),find(type_X0(4,:)==1))));
idx_carichiQ = (type_X0(2,intersect(find(type_X0(1,:)==3),find(type_X0(4,:)==1))));
if conditional_sampling == 1 && mod_gauss == 0
    idx_err0 = dati_cond.idx_err0;
    idx_err = dati_cond.idx_err;
    i_RES = (find(ismember(idx_err0,intersect(find(type_X0(1,:)==1),find(type_X0(4,:)==1)))));
    iq_RES = (find(ismember(idx_err0,intersect(find(type_X0(1,:)==4),find(type_X0(4,:)==1)))));
    Pi_carichi = (find(ismember(idx_err0,intersect(find(type_X0(1,:)==2),find(type_X0(4,:)==1)))));
    Qi_carichi = (find(ismember(idx_err0,intersect(find(type_X0(1,:)==3),find(type_X0(4,:)==1)))));
else
    i_RES = find(type_X0(1,:)==1);
    iq_RES = find(type_X0(1,:)==4);
    Pi_carichi = find(type_X0(1,:)==2);
    Qi_carichi = find(type_X0(1,:)==3);
end
%%%
idx_loads = find([carico.conn]==1);
if mod_gauss
   
    %%% GAUSSIAN FICTITIOUS FORECAST ERRORS
    numero_RES = length(idx_RES);
    pres_mc=[];pl_mc=[];ql_mc=[];
    percpu_load = module2.allparas.stddev(1);
    percpu_RES = module2.allparas.stddev(2);
    correlation_gauss = module2.allparas.corre;
    % different correlation cases
    if correlation_gauss == 0
        A = randn([scenarios length(idx_RES)+length(Pi_carichi)+length(Qi_carichi)]);
    elseif correlation_gauss == 1
        A = repmat(randn([scenarios 1]),1,length(idx_RES)+length(Pi_carichi)+length(Qi_carichi));
    else
        %%%%% correlation = 1 ainside same category (loads or RES) and
        %%%%% equal to rho between categories
        A0 = mvnrnd(zeros(1,2),[1 correlation_gauss;correlation_gauss 1],scenarios);
        ALOAD = repmat(A0(:,1),1,length(Pi_carichi)+length(Qi_carichi));
        ARES = repmat(A0(:,2),1,length(idx_RES));
        A = [ARES ALOAD];
        %%% toeplitz model of correlation matrix
%         S = correlation_gauss.^[1:length(idx_RES)+length(Pi_carichi)+length(Qi_carichi)-1];
%         Toe = toeplitz(S);
%         A = mvnrnd(zeros(length(idx_RES)+length(Pi_carichi)+length(Qi_carichi),1),Toe);
    end
    for kRES = 1:length(idx_RES)
        
        dummys =  (1 + percpu_RES*A(:,kRES));
        if opt_sign == 1
            dummys = max(0,dummys);
        end
        pres_mc(:,kRES) = max(-generatore(idx_RES(kRES)).Pmax,min(-generatore(idx_RES(kRES)).Pmin,generatore(idx_RES(kRES)).P*dummys));
        
    end
    for kLOAD = 1:length(Pi_carichi)
        dummys =  (1 + percpu_load*A(:,kLOAD+length(idx_RES)));

        pl_mc(:,kLOAD) = carico(idx_carichi(kLOAD)).P*dummys;
    end
    if flagPQ == 1
        
        for kLOAD = 1:length(Qi_carichi)
            dummys =  (1 + percpu_load*A(:,kLOAD+length(idx_RES)+length(Pi_carichi)));

            ql_mc(:,kLOAD) = carico(idx_carichiQ(kLOAD)).Q*dummys;
        end
    end
    
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
else
     if scenarios > size(module3.X_NEW,1)
    disp(['**WARNING: NR OF REQUIRED CONDITIONED SAMPLES HIGHER THAN AVAILABLE UNCONDITIONED SAMPLES GENERATED OFF-LINE! **' ])
    disp(['**NR OF CONDITIONED SAMPLES SET TO THE MAXIMUM NR OF AVAILABLE UNCONDITIONED SAMPLES**' ])
    scenarios = size(module3.X_NEW,1);
     elseif scenarios < 1
      disp(['**WARNING: INCONSISTENTE NUMBER OF SAMPLES. NR OF SAMPLES SET TO 1! **' ])    
         scenarios = 1;
     end
     
    %%% SAMPLING IN MODULE3 FROM OUTPUTS OF MODULES 1 AND 2
    if conditional_sampling == 1
        %%%% ACTIVATION OF CONDITIONAL SAMPLING
        [ X_NEW0 ] = module3.X_NEW;
        
        quale_forecast = type_X0(5,dati_cond.idx_err0);
        [ X_NEWall  quale_err var_out_of_lim] = conditional_samps(X_NEW0,quale_forecast,y0,dati_cond,centering);
         X_NEW=X_NEWall(randi(size(X_NEWall,1),scenarios,1),:);

        %%% PRINT SAMPLES of injections FOR OUTPUTS %%%%
        
        vectorsGARP = [];
        for inh = 1:size(X_NEW,2)
            if ismember(inh,Pi_carichi)
                nome{inh}=[carico(idx_carichi(find(ismember(Pi_carichi,inh)))).codice '_P'];
                vectorsGARP = [vectorsGARP inh];
            elseif ismember(inh,Qi_carichi)
                nome{inh}=[carico(idx_carichiQ(find(ismember(Qi_carichi,inh)))).codice '_Q'];
                vectorsGARP = [vectorsGARP inh];
            elseif ismember(inh,i_RES)
                nome{inh}=[generatore(idx_RES(find(ismember(i_RES,inh)))).codice '_P'];
                vectorsGARP = [vectorsGARP inh];
            elseif ismember(inh,iq_RES)
                nome{inh}=[generatore(idxq_RES(find(ismember(iq_RES,inh)))).codice '_Q'];
                vectorsGARP = [vectorsGARP inh];
            end
        end
        %         keyboard
        csvFileName=sprintf('printSamples.csv');
        fid = fopen(csvFileName,'w');
        fmtString = [repmat('%s,',1,length(vectorsGARP)-1),'%s\n'];
        fprintf(fid,fmtString,nome{vectorsGARP});
        fclose(fid);
         dlmwrite(csvFileName,quale_forecast(vectorsGARP),'delimiter',',','-append');
         for inse = 1:size(X_NEW,1)
             dlmwrite(csvFileName,X_NEW(inse,vectorsGARP),'delimiter',',','-append');
         end
         
         %%% LOG OF DACFs OUT OF THE DOMAIN OF EXISTENCE
         for iee = 1:length(var_out_of_lim)
             if ismember(var_out_of_lim(iee),vectorsGARP)
        disp(['*WARNING: DACF OF VARIABLE ' nome{vectorsGARP(ismember(vectorsGARP,var_out_of_lim(iee)))} ' OUTSIDE THE DOMAIN OF EXISTENCE OF FORECAST PDF: THE PDF IS MOVED BY ' num2str(quale_err(iee)) ' MWs'])
             end
        end
         
        %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        
        numero_RES = length(idx_RES);
        pres_mc=[];pl_mc=[];ql_mc=[];
        % keyboard
        for kRES = 1:length(idx_RES)
            if opt_sign == 1
                pres_mc(:,kRES) = sign(generatore(idx_RES(kRES)).P).*max(0,sign(generatore(idx_RES(kRES)).P).*X_NEW(:,i_RES(kRES)));
            else
                pres_mc(:,kRES) = X_NEW(:,i_RES(kRES));
            end
            pres_mc(:,kRES) = max(-generatore(idx_RES(kRES)).Pmax,min(-generatore(idx_RES(kRES)).Pmin,pres_mc(:,kRES)));
        end
        for kLOAD = 1:length(Pi_carichi)
            
            pl_mc(:,kLOAD) = X_NEW(:,Pi_carichi(kLOAD));
            
        end
        if flagPQ == 1
            
            for kLOAD = 1:length(Qi_carichi)
                
                ql_mc(:,kLOAD) = X_NEW(:,Qi_carichi(kLOAD));
                
            end
        end
    else
%         keyboard
        %%%% DEACTIVATED CONDITIONAL SAMPLING - UNCONDITIONED SAMPLING OF FORECAST ERRORS FROM C VINES        
         [ X_NEW ] = module3.X_NEW(randi(size(module3.X_NEW,1),scenarios,1),:);
        X_NEW1 = X_NEW;
        
        %%% PRINT SAMPLES of forecast errors FOR OUTPUTS %%%%
        vectorsGARP = [];
        for inh = 1:size(X_NEW,2)
            if ismember(inh,Pi_carichi)
                nome{inh}=[carico(idx_carichi(find(ismember(Pi_carichi,inh)))).codice '_P'];
                vectorsGARP = [vectorsGARP inh];
                X_NEW1(:,inh) = carico(idx_carichi(find(ismember(Pi_carichi,inh)))).P + X_NEW(:,inh);
               quale_forecast(inh) = carico(idx_carichi(find(ismember(Pi_carichi,inh)))).P;
            elseif ismember(inh,Qi_carichi)
                try
                nome{inh}=[carico(idx_carichiQ(find(ismember(Qi_carichi,inh)))).codice '_Q'];
                vectorsGARP = [vectorsGARP inh];
                X_NEW1(:,inh) = carico(idx_carichiQ(find(ismember(Qi_carichi,inh)))).Q + X_NEW(:,inh);
                quale_forecast(inh) = carico(idx_carichiQ(find(ismember(Qi_carichi,inh)))).Q;
                catch err
                    keyboard
                end
            elseif ismember(inh,i_RES)
                try
                nome{inh}=[generatore(idx_RES(find(ismember(i_RES,inh)))).codice '_P'];
                catch err
                    keyboard
                end
                vectorsGARP = [vectorsGARP inh];
                if opt_sign == 1
                X_NEW1(:,inh) = sign(generatore(idx_RES(find(ismember(i_RES,inh)))).P).*max(0,sign(generatore(idx_RES(find(ismember(i_RES,inh)))).P).*(X_NEW(:,i_RES(find(ismember(i_RES,inh))))+generatore(idx_RES(find(ismember(i_RES,inh)))).P));
            else
                X_NEW1(:,inh) = X_NEW(:,i_RES(find(ismember(i_RES,inh))))+generatore(idx_RES(find(ismember(i_RES,inh)))).P;
                end
            quale_forecast(inh) = generatore(idx_RES(find(ismember(i_RES,inh)))).P;
            elseif ismember(inh,iq_RES)
                nome{inh}=[generatore(idxq_RES(find(ismember(iq_RES,inh)))).codice '_Q'];
                vectorsGARP = [vectorsGARP inh];
                X_NEW1(:,inh) = X_NEW(:,iq_RES(find(ismember(iq_RES,inh))))+generatore(idxq_RES(find(ismember(iq_RES,inh)))).Q;
                quale_forecast(inh) = generatore(idxq_RES(find(ismember(iq_RES,inh)))).Q;
            end
        end
        %         keyboard
        csvFileName=sprintf('printSamples.csv');
        fid = fopen(csvFileName,'w');
        fmtString = [repmat('%s,',1,length(vectorsGARP)-1),'%s\n'];
        fprintf(fid,fmtString,nome{vectorsGARP});
        fclose(fid);        
        dlmwrite(csvFileName,quale_forecast(vectorsGARP),'delimiter',',','-append');
        for inse = 1:size(X_NEW1,1)
            dlmwrite(csvFileName,X_NEW1(inse,vectorsGARP),'delimiter',',','-append');
        end
        
        %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        
        numero_RES = length(idx_RES);
        pres_mc=[];pl_mc=[];ql_mc=[];
        for kRES = 1:length(idx_RES)
            if opt_sign == 1
                pres_mc(:,kRES) = sign(generatore(idx_RES(kRES)).P).*max(0,sign(generatore(idx_RES(kRES)).P).*(X_NEW(:,i_RES(kRES))+generatore(idx_RES(kRES)).P));
            else
                pres_mc(:,kRES) = X_NEW(:,i_RES(kRES))+generatore(idx_RES(kRES)).P;
            end
            pres_mc(:,kRES) = max(-generatore(idx_RES(kRES)).Pmax,min(-generatore(idx_RES(kRES)).Pmin,pres_mc(:,kRES)));
        end
        for kLOAD = 1:length(Pi_carichi)
            
            pl_mc(:,kLOAD) = X_NEW(:,Pi_carichi(kLOAD))+carico(idx_carichi(kLOAD)).P;
            
        end
        if flagPQ == 1
            for kLOAD = 1:length(Qi_carichi)
                
                ql_mc(:,kLOAD) = X_NEW(:,Qi_carichi(kLOAD))+carico(idx_carichiQ(kLOAD)).Q;
                
            end
        end
        
    end
    
end
concentrato = 0;
% save provaW3.mat
generatore0=generatore;
carico0=carico;
idx_conv = intersect(find([generatore.conn]==1),intersect(find([generatore.dispacc]==1),intersect(intersect(find([generatore.P]<0),find([generatore.RES]==0)),find([nodo([generatore.estremo_ID]).type] ~= 2))));%intersect(find([nodo([scenario(1).rete.generatore.nodo]).tipo]==3),intersect([find([scenario(1).rete.generatore.P]<0)],intersect([find([scenario(1).rete.generatore.RES]==0)], [scenario(1).rete.nodi([scenario(1).rete.sezioni([scenario(1).rete.stazioni([scenario(1).rete.area([find([scenario(1).rete.area.nazione]==1)]).stazioni]).sezioni]).nodi]).generatore]))); %#ok<NBRAK>
idx_conv = intersect(intersect(idx_conv,find(-[generatore.P] <= [generatore.Pmax])),find(-[generatore.P] >= [generatore.Pmin]));
gen_attivi = find([generatore.conn]==1);
PGEN=[];
PLOAD=[];
QLOAD=[];
tanfi0 = ([carico0.Q]./[carico0.P]);
idxnaninf = union(find(isnan(tanfi0)),find(isinf(tanfi0)));
tanfi0(idxnaninf)=0;
idx_gen=idx_conv;
%%% FOR ANY STOCHASTIC SCENARIOS, THE REDISPATCHING OF CONVENTIONAL
%%% GENERATION IS PERFORMED TO COMPENSATE THE POWER UNBALANCES DUE TO
%%% UNCERTAINTIES
for ns=1:scenarios
    disp(['generating scenario nr. ' num2str(ns)])
    if isempty(pres_mc)==0
        OP(ns).PRES=pres_mc(ns,:);
    end
    if isempty(pl_mc)==0
        OP(ns).PL=pl_mc(ns,:);
    end
    if flagPQ == 1 && isempty(ql_mc)==0
        OP(ns).QL=ql_mc(ns,:);
    end
    generatore=generatore0;
    carico = carico0;
    
    try
        for g=1:length(idx_RES)
            [generatore(idx_RES(g)).P] = [OP(ns).PRES(g)];
        end
        for il=1:length(idx_carichi)
            [carico(idx_carichi(il)).P] = [OP(ns).PL(il)];
        end
        if  flagPQ == 1
            for il=1:length(idx_carichiQ)
                
                [carico(idx_carichiQ(il)).Q] = [OP(ns).QL(il)];
                
            end
        else
            for il=1:length(idx_carichi)
                [carico(idx_carichi(il)).Q] = [OP(ns).PL(il)].*tanfi0(idx_carichi(il));%
            end
        end
    catch err
        keyboard
    end
    
    if concentrato == 0
        
        if isempty(idx_carichi)
            sbilancio_eolico = sum([generatore(idx_RES).P]) - sum([generatore0(idx_RES).P]);
        else
            sbilancio_eolico = sum([generatore(idx_RES).P]) - sum([generatore0(idx_RES).P]) - (-sum([carico(idx_carichi).P]) + sum([carico0(idx_carichi).P]));
        end
        SB(ns)=-sbilancio_eolico;

        %
        idx_marg_down=[];idx_marg_up=[];
        sbi=sbilancio_eolico;
        if sbilancio_eolico~=0
            
            
            for g=1:length(idx_gen)
                [generatore(idx_gen(g)).P] = [generatore(idx_gen(g)).P] - sbi*[generatore(idx_gen(g)).participationFactor]./sum([generatore(idx_gen).participationFactor]);
                if -generatore(idx_gen(g)).P >=  generatore(idx_gen(g)).Pmax
                    idx_marg_up = [idx_marg_up idx_gen(g)];
                end
                if -generatore(idx_gen(g)).P <=  generatore(idx_gen(g)).Pmin
                    idx_marg_down = [idx_marg_down idx_gen(g)];
                end
            end
            
            idx_marg=union(idx_marg_up,idx_marg_down);
            idx_lib=idx_gen;
            
            while isempty(idx_marg)==0 && isempty(idx_lib)==0
                
                sbi = sum([generatore(idx_marg_up).P] + [generatore(idx_marg_up).Pmax]) + sum([generatore(idx_marg_down).P]  +  [generatore(idx_marg_down).Pmin]);
                
                for g=1:length(idx_marg_up)
                    generatore(idx_marg_up(g)).P =  -generatore(idx_marg_up(g)).Pmax;
                end
                for g=1:length(idx_marg_down)
                    generatore(idx_marg_down(g)).P =  -generatore(idx_marg_down(g)).Pmin;
                end
                
                idx_lib = setdiff(idx_lib,idx_marg);
                idx_marg_down=[];idx_marg_up=[];
                for g = 1:length(idx_lib)
                    [generatore(idx_lib(g)).P] = [generatore(idx_lib(g)).P] + sbi.*[generatore(idx_lib(g)).participationFactor]./sum([generatore(idx_lib).participationFactor]);
                    
                    if -generatore(idx_lib(g)).P >=  generatore(idx_lib(g)).Pmax
                        idx_marg_up = [idx_marg_up idx_lib(g)];
                    end
                    if -generatore(idx_lib(g)).P <=  generatore(idx_lib(g)).Pmin
                        idx_marg_down = [idx_marg_down idx_lib(g)];
                    end
                    
                end
                
                
                idx_marg=union(idx_marg_up,idx_marg_down);
            end
            sbi = sum([generatore(idx_marg_up).P] + [generatore(idx_marg_up).Pmax]) + sum([generatore(idx_marg_down).P]  +  [generatore(idx_marg_down).Pmin]);
            if sbi~=0
                disp('power imbalance not completely covered by conventional generation - the residual is covered by the slack node!')
            end
        end
        
    end
    PGEN=[PGEN; [generatore(gen_attivi).P]];
    PLOAD=[PLOAD; [carico(idx_loads).P]];
    if flagPQ == 0
        QLOAD=[QLOAD; [carico(idx_loads).Q]];
        %%% IN CASE REACTIVE POWERS ARE DERIVED FROM p SAMPLES, THEIR
        %%% SAMPLES ARE LIMITED CONSIDERING THE HISTORICAL LIMIT QUANTILES
        %%% OF THEIR DISTRIBUTIONS
        if length(idx_carichi)>0
        QLOAD(size(QLOAD,1),find(ismember(idx_loads,idx_carichi)))=min(max([carico(idx_carichi).P].*tanfi0(idx_carichi),limits_reactive(idx_carichi,1)'),limits_reactive(idx_carichi,2)');
        end
    else
        QLOAD=[QLOAD; [carico(idx_loads).Q]];
    end
end

if validation == 1
    save test_redispatching.mat
    ratios = -(PGEN(:,ismember(gen_attivi,idx_conv)) - repmat([generatore0(idx_conv).P],size(PGEN,1),1))./repmat([generatore(idx_conv).participationFactor],size(PGEN,1),1);
    
    figure
    for j = 1:20
        subplot(4,5,j),hist(ratios(j,:)),legend(['imbalance = ' num2str(SB(j)) ' MW'])
    end
    s={'b' 'r' 'c' 'k' 'g' 'm'};
    figure
    for j = 1:size(PLOAD,1)
        plot(ratios(j,:),[1:length(idx_conv)],s{1+rem(j,6)}),hold on
    end
    set(gca,'YTickLabel',{generatore(gen_attivi(ismember(gen_attivi,idx_conv(1:end)))).codice},'YTickMode','auto','YTick',[1:length(idx_conv)])
    figure
    for j = 1:size(PLOAD,1)
        if SB(j)>=0
            plot(ratios(j,:),[1:length(idx_conv)],'r'),hold on
        else
            plot(ratios(j,:),[1:length(idx_conv)],'b'),hold on
        end
    end
    set(gca,'YTickLabel',{generatore(gen_attivi(ismember(gen_attivi,idx_conv(1:5:end)))).codice},'YTickMode','auto','YTick',[1:5:length(idx_conv)])
    xlabel('DP/Pmax'),title('Contributions of conventional units to active power imbalance (red = deficit, blue = surplus)')
    figure
    for j = 1:size(PLOAD,1)
        plot(ratios(j,1:50),[1:50],s{1+rem(j,6)}),hold on
    end
    set(gca,'YTickLabel',{generatore(gen_attivi(ismember(gen_attivi,idx_conv(1:50)))).codice},'YTickMode','auto','YTick',[1:50])
    
    
    
    figure
    s={'b' 'r' 'c' 'k' 'g' 'm' 'y'};
    idxs = find(ismember(idx_loads,idx_carichi));
    for i = 1:length(idxs)
        plot(cos(atan(QLOAD(:,idxs(i))./PLOAD(:,idxs(i)))),s{1+rem(i,7)}),hold on
    end
    grid on
    legend(strrep({carico(idxs).codice},'_','-'))
    xlabel('sample nr')
    ylabel('p.f.')
end
