%
% Copyright (c) 2016, Ricerca sul Sistema Energetico – RSE S.p.A. <itesla@rse-web.it>
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

%% m1file - sampling network data, to be read
%% m2file - FEA output file (output from module1 and module2), to be read  
%% ofile  - module3 output file, to be written
%% s_scenarios - number of samples to generate (in ofile)
%% s_rng_seed - int seed (optional, default is 'shuffle' on current date)
%% option_sign - option to avoid sign inversion in samples wrt relevant forecasts
function exitcode=MCLA_HELPER(m1file,m3file, ofile, s_scenarios,option_sign,centerings)
close all;
mversion='1.8.0';
disp(sprintf('wp5 - MCLA - version: %s', mversion));
disp(sprintf(' base case file:  %s',m1file));
disp(sprintf(' m3file:  %s',m3file));
disp(sprintf(' ofile:  %s', ofile));
disp(sprintf(' scenarios:  %s',s_scenarios));
disp(sprintf(' option_sign:  %s',option_sign));
disp(sprintf(' centering:  %s', centerings));

moutput.errmsg='Ok';
try
    % module1: struct, output from module1
    load(m1file);
    % module2:  module2 output
    load(m3file);
    % s_scenarios: number of samples to generate
    scenarios=str2double(s_scenarios);
    opt_sign = str2double(option_sign);
    centering = str2double(centerings);
    %if seed is not specified, 'shuffle'  on current platform time    
    
disp(sprintf('flagPQ:  %u', out(1).flagPQ));
disp(['preprocessing: type_x, etc.'])
tic;
% keyboard
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% here start RSE CODE, EXTRACTED FROM TEST_MCLA.m example
% with respect to the original TEST_MCLA.m :
%  - changed input matrix name from X to inj_ID
%  - 
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% type_X is the vector which specifies the nature of the stochastic
% injections (RES or load). here is an example with 3 RES and one stochastic load. the vector must be
% completed taking information from IIDM.

% flagPQ: =0 se ci sono solo P stocastiche, =1 se anche le Q sono
% stocastiche
%%%%% 
% conditional_sampling=0;

for iout = 1:length(out)
    conditional_sampling = out(iout).conditional_sampling;
    mod_gaussian = out(iout).mod_gaussian;
    dati_cond = out(iout).dati_cond;
    dati_Q = out(iout).dati_Q;
    flagPQ = out(iout).flagPQ;
    maxvalue = out(iout).maxvalue;
    module2 = out(iout).module2;
     module3 = out(iout).module3;
     inj_ID = out(iout).inj_ID;
     type_X = [];
     flagesistenza = out(iout).flagesistenza;
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    
    if conditional_sampling == 1 && mod_gaussian == 0
        idx_err0 = dati_cond.idx_err0;
        idx_fore0 = dati_cond.idx_fore0;
        inj_ID0 = dati_cond.inj_ID0;
        idx_err = dati_cond.idx_err;
        inj_IDQ = dati_Q.inj_IDQ;
        limits_reactive=[];
        
        if flagPQ == 0
            for jcol = 1:size(inj_ID0,2)
                idxgen = find(ismember({generatore.codice},inj_ID0{jcol}(1:end-2)));
                idxload = find(ismember({carico.codice},inj_ID0{jcol}(1:end-2)));
                if isempty(idxgen)==0
                    if generatore(idxgen(1)).conn == 1
                        if strcmp(inj_ID0{jcol}(end),'P')
                            type_X(:,jcol) = [1;idxgen(1)];
                            if ismember(jcol,idx_fore0)
                                y0(find(idx_fore0==jcol)) = generatore(idxgen(1)).P;
                                y1(jcol)=generatore(idxgen(1)).P;
                            end
                        else
                            type_X(:,jcol) = [4;idxgen(1)];
                            if ismember(jcol,idx_fore0)
                                y0(find(idx_fore0==jcol)) = generatore(idxgen(1)).Q;
                                y1(jcol)=generatore(idxgen(1)).Q;
                            end
                        end
                        
                    end
                end
                if isempty(idxload)==0
                    if carico(idxload(1)).conn == 1 && carico(idxload(1)).P ~= 0 && carico(idxload(1)).Q ~= 0
                        type_X(:,jcol) = [2;idxload(1)];
                        if ismember(jcol,idx_fore0)
                            y0(find(idx_fore0==jcol)) = carico(idxload(1)).P;
                            y1(jcol) = carico(idxload(1)).P;
                        end
                        if isempty(maxvalue)
                            limits_reactive(idxload(1),1:2)=[-9999 9999];
                        else
                            jquale = find(ismember(inj_IDQ,carico(idxload).codice));
                            
                            limits_reactive(idxload(1),1:2)=maxvalue(jquale,:);
                            
                        end
                        
                    end
                end
            end
        else
            for jcol = 1:size(inj_ID0,2)
                idxgen = find(ismember({generatore.codice},inj_ID0{jcol}(1:end-2)));
                idxload = find(ismember({carico.codice},inj_ID0{jcol}(1:end-2)));
                if isempty(idxgen)==0
                    if generatore(idxgen(1)).conn == 1
                        if strcmp(inj_ID0{jcol}(end),'P')
                            type_X(:,jcol) = [1;idxgen(1)];
                            if ismember(jcol,idx_fore0)
                                y0(find(idx_fore0==jcol)) = generatore(idxgen(1)).P;
                                y1((jcol)) = generatore(idxgen(1)).P;
                            end
                        else
                            type_X(:,jcol) = [4;idxgen(1)];
                            if ismember(jcol,idx_fore0)
                                y0(find(idx_fore0==jcol)) = generatore(idxgen(1)).Q;
                                y1((jcol)) = generatore(idxgen(1)).Q;
                            end
                        end
                    end
                end
                if isempty(idxload)==0
                    if carico(idxload(1)).conn == 1
                        if strcmp(inj_ID0{jcol}(end),'P')
                            type_X(:,jcol) = [2;idxload(1)];
                            if ismember(jcol,idx_fore0)
                                y0(find(idx_fore0==jcol)) = carico(idxload(1)).P;
                                y1(jcol) = carico(idxload(1)).P;
                            end
                        else
                            type_X(:,jcol) = [3;idxload(1)];
                            if ismember(jcol,idx_fore0)
                                y0(find(idx_fore0==jcol)) = carico(idxload(1)).Q;
                                y1(jcol) = carico(idxload(1)).Q;
                            end
                        end
                    end
                end
            end
        end
        
        idx_miss = find(~any(type_X,1));
        idx_available = setdiff([1:size(type_X,2)],idx_miss);
        type_X(4,idx_fore0(ismember(idx_fore0,idx_available)))=-1;
        type_X(4,idx_err0(ismember(idx_err0,idx_available)))=1;
        type_X(3,:) = zeros(1,size(type_X,2));
        type_X(3,idx_available) = [1];
        
        for ifore = 1:length(idx_fore0)
            quali_inj = type_X(1:2,idx_fore0(ifore));
            idx1 = find(ismember(type_X(1,:),quali_inj(1)));idx2 = find(ismember(type_X(2,:),quali_inj(2)));idx3 = find(ismember(type_X(4,:),1));
            idxqu = intersect(idx3,intersect(idx1,idx2));
            try
                if isempty(idxqu)==0
                    type_X(5,idxqu(1)) = [y1(idx_fore0(ifore))];
                    
                end
            catch err
                keyboard
            end
        end
        
    else
        inj_IDQ = dati_Q.inj_IDQ;
        
        limits_reactive=[];
        if flagPQ == 0
            
            for jcol = 1:size(inj_ID,2)
                idxgen = find(ismember({generatore.codice},inj_ID{jcol}(1:end-2)));
                idxload = find(ismember({carico.codice},inj_ID{jcol}(1:end-2)));
                if isempty(idxgen)==0
                    if generatore(idxgen(1)).conn == 1
                        if strcmp(inj_ID{jcol}(end),'P')
                            type_X(:,jcol) = [1;idxgen(1)];
                            
                        else
                            type_X(:,jcol) = [4;idxgen(1)];
                            
                        end
                        
                    end
                end
                if isempty(idxload)==0
                    if carico(idxload(1)).conn == 1 && carico(idxload(1)).P ~= 0 && carico(idxload(1)).Q ~= 0
                        type_X(:,jcol) = [2;idxload(1)];
                        
                        if isempty(maxvalue)
                            limits_reactive(idxload(1),1:2)=[-9999 9999];
                        else
                            jquale = find(ismember(inj_IDQ,carico(idxload).codice));
                            if flagesistenza
                                limits_reactive(idxload(1),1:2)=maxvalue(jquale,:);
                            else
                                limits_reactive(idxload(1),1:2)=[carico(idxload).Q + abs(carico(idxload).Q).*maxvalue(jquale,:).*module2.allparas.stddev(1)];
                            end
                        end
                        
                    end
                end
            end
        else
            for jcol = 1:size(inj_ID,2)
                idxgen = find(ismember({generatore.codice},inj_ID{jcol}(1:end-2)));
                idxload = find(ismember({carico.codice},inj_ID{jcol}(1:end-2)));
                if isempty(idxgen)==0
                    if generatore(idxgen(1)).conn == 1
                        if strcmp(inj_ID{jcol}(end),'P')
                            type_X(:,jcol) = [1;idxgen(1)];
                            
                        else
                            type_X(:,jcol) = [4;idxgen(1)];
                            
                        end
                    end
                end
                if isempty(idxload)==0
                    if carico(idxload(1)).conn == 1
                        if strcmp(inj_ID{jcol}(end),'P')
                            type_X(:,jcol) = [2;idxload(1)];
                            
                        else
                            type_X(:,jcol) = [3;idxload(1)];
                            
                        end
                    end
                end
            end
        end
        
        idx_miss = find(~any(type_X,1));
        idx_available = setdiff([1:size(type_X,2)],idx_miss);
        type_X(4,:)=1;
        type_X(3,:) = zeros(1,size(type_X,2));
        type_X(3,idx_available) = [1];
        y0=[];
    end
    
    %%% criterion to set the field "dispacc" of generator data
% here it is assumed that all units except for nuclear units are available
% for redispatching


gruppi_ridispacciabili = intersect(find([generatore.conn]==1),intersect(find([generatore.fuel]~=4),find([generatore.RES]>0)));
for jgen = 1:length(gruppi_ridispacciabili)
    generatore(gruppi_ridispacciabili(jgen)).dispacc=1;
end
%%%%%%%%%

       
%%% EURISTICA PARTICIPATION FACTORS
%%% EURISTICA DEI LIMITI DI POTENZA PMIN E PMAX
BANDA = 10; % perc of Pmax
anomalies = [];
quali_gen_stoch = unique(type_X(2,[find(type_X(1,:)==1) find(type_X(1,:)==4)]));
for u=1:length(generatore)
    if ismember(u,quali_gen_stoch)==0
    generatore(u).participationFactor=generatore(u).Pmax;PMAX = generatore(u).Pmax;
    if generatore(u).conn == 1 && (-generatore(u).P < generatore(u).Pmin)
        disp(['*** WARNING: CONNECTED GENERATOR ' generatore(u).codice ' HAS AN ACTIVE POWER SETPOINT LOWER THAN PMIN '])  
    generatore(u).Pmax=min(PMAX,generatore(u).Pmin+BANDA*0.01*PMAX);
    generatore(u).Pmin=max(generatore(u).Pmin,-generatore(u).P-BANDA*0.01*PMAX);
    anomalies = [anomalies u];
    end
    if generatore(u).conn == 1 && (-generatore(u).P > generatore(u).Pmax)
        disp(['*** WARNING: CONNECTED GENERATOR ' generatore(u).codice ' HAS AN ACTIVE POWER SETPOINT HIGHER THAN PMAX '])
        generatore(u).Pmax=min(PMAX,-generatore(u).P+BANDA*0.01*PMAX);
    generatore(u).Pmin=max(generatore(u).Pmin,PMAX-BANDA*0.01*PMAX);
    anomalies = [anomalies u];
    end
    if generatore(u).conn == 1 && (-generatore(u).P <= generatore(u).Pmax) && (-generatore(u).P >= generatore(u).Pmin)
    PMAX = generatore(u).Pmax;
    generatore(u).Pmax=min(PMAX,-generatore(u).P+BANDA*0.01*PMAX);
    generatore(u).Pmin=max(generatore(u).Pmin,-generatore(u).P-BANDA*0.01*PMAX);
    end
    end
end


toc;

disp(['STARTED MCLA'])
tic;
% keyboard
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
[PGENS PLOADS QLOADS ] = main_MCLA2PC3(generatore,carico,nodo,scenarios,type_X,module2,module3,flagPQ,limits_reactive,opt_sign,dati_cond,y0,conditional_sampling,mod_gaussian,centering);
    if iout == 1
          PGEN=PGENS;
   PLOAD=PLOADS;
   QLOAD=QLOADS;
    else
    idx_RES = (type_X(2,intersect(find(type_X(1,:)==1),find(type_X(4,:)==1))));
    idx_carichi = (type_X(2,intersect(find(type_X(1,:)==2),find(type_X(4,:)==1))));
    idx_carichiQ = (type_X(2,intersect(find(type_X(1,:)==3),find(type_X(4,:)==1))));
gen_attivi = find([generatore.conn]==1);
carichi_attivi = find([carico.conn]==1);
    PGEN(:,ismember(gen_attivi,idx_RES))=PGENS(:,ismember(gen_attivi,idx_RES));
   PLOAD(:,ismember(carichi_attivi,idx_carichi))=PLOADS(:,ismember(carichi_attivi,idx_carichi));
   QLOAD(:,ismember(carichi_attivi,idx_carichiQ))=QLOADS(:,ismember(carichi_attivi,idx_carichiQ));
    end
   
   disp(['MCLA COMPLETED.'])
toc;

%save output in .mat
   moutput.errmsg='Ok';
  moutput.rng_data=out(iout).rng_data;
   moutput.mversion=out(iout).mversion;
   moutput.PLOAD = PLOAD;
   moutput.QLOAD = QLOAD;
   moutput.PGEN = PGEN;
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
end




%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% here ends the  RSE CODE, extracted from TEST_MCLA.m
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%


   
   exitcode=0;
catch err
   moutput.errmsg=err.message;
   disp(getReport(err,'extended'));
   exitcode=-1;
end
save(ofile, '-struct', 'moutput');
end
