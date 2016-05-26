%
% Copyright (c) 2016, Ricerca sul Sistema Energetico – RSE S.p.A. <itesla@rse-web.it>
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

X = Y;
%%%%
CoreNum  = 2;
scenarios = 10;
%X=csvread('IEEE_FRENCH_LOAD.csv');
% X=csvread('IEEE_FRENCH_WIND.csv');
% % % % load variables_grid1.mat

%    load WP5SamplingData_france.mat
 load WP5SamplingData_7nodes.mat

%%%%%%% ESEMPIO DI DATI
% number of stochastic variables = number of RES + number of stoch loads
% if field '.RES' of struct generatore is > 0 -> the generator is a stochastic RES
%
%
%%%%

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% % % % if flagPQ == 0
% % % %     Q = eye(N_RES+N_LOADS);
% % % % else
% % % %     Q = eye(N_RES+2*N_LOADS);
% % % % end
% % % % Q(1,2) = 0.7; Q(2,1) = 0.7;
% % % % Q(1,3) = 0.4; Q(3,1) = 0.4;
% % % % Q(1,5) = 0.4; Q(5,1) = 0.4;
% % % % Q(2,3) = 0.3; Q(3,2) = 0.3;
% % % % Q(5,4) = 0.8; Q(4,5) = 0.8;
% % % % 
% % % % U = copularnd('gaussian',Q,200);
% % % % 
% % % % 
% % % % % forecast errors with FORECAST VALUES included in struct 'generatore' and
% % % % % nodo'
% % % % for i = 1:size(Q,1)
% % % %     m(i) = -5+10*rand;
% % % %     va(i) = 5+30*rand;
% % % %     X(:,i) = norminv(U(:,i),m(i),va(i));
% % % % end

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

[ module1 ] = MODULE1( X,0.9,3 );

[ module2 ] = MODULE2_OUTPUT2( module1,CoreNum );

% error is defined as 'error = snapshot - forecast'. the forecast values for
% injections are reported in generatore.P and nodo.P fields

% running MC sampling and redispatching of conventional dispatchable
% generators


% type_X is the vector which specifies the nature of the stochastic
% injections (RES or load). here is an example with 3 RES and one stochastic load. the vector must be
% completed taking information from IIDM.

% flagPQ: =0 se ci sono solo P stocastiche, =1 se anche le Q sono
% stocastiche
%%%%% 
% keyboard
if flagPQ == 0
    %Q for jcol = 1:size(X,2)
    for jcol = 1:size(inj_ID,2)
         disp(type_X);
 %       if strcmp(inj_ID{jcol}(end-1),'_')
        idxgen = find(ismember({generatore.codice},inj_ID{jcol}(1:end-2)));
        idxload = find(ismember({carico.codice},inj_ID{jcol}(1:end-2)));
 %       else
 %        idxgen = find(ismember({generatore.codice},inj_ID{jcol}));
 %       idxload = find(ismember({carico.codice},inj_ID{jcol}));   
 %       end

        if isempty(idxgen)==0
            if generatore(idxgen(1)).conn == 1
            type_X(:,jcol) = [1;idxgen(1)];
            end
        end
        if isempty(idxload)==0
            if carico(idxload(1)).conn == 1
            type_X(:,jcol) = [2;idxload(1)];
            end
        end
    end
else
    %Q for jcol = 1:size(X,2)
    for jcol = 1:size(inj_ID,2)
  %        if strcmp(inj_ID{jcol}(end-1),'_')
         idxgen = find(ismember({generatore.codice},inj_ID{jcol}(1:end-2)));
         idxload = find(ismember({carico.codice},inj_ID{jcol}(1:end-2)));
   %       else
   %         idxgen = find(ismember({generatore.codice},inj_ID{jcol}));
   %     idxload = find(ismember({carico.codice},inj_ID{jcol}));     
   %       end
        if isempty(idxgen)==0
            if generatore(idxgen(1)).conn == 1
            type_X(:,jcol) = [1;idxgen(1)];
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
type_X(:,idx_miss) = [];

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

for u=1:length(generatore)
    generatore(u).participationFactor=generatore(u).Pmax;
    PMAX = generatore(u).Pmax;
    generatore(u).Pmax=min(PMAX,generatore(u).P+BANDA*0.01*PMAX);
    generatore(u).Pmin=max(generatore(u).Pmin,generatore(u).P-BANDA*0.01*PMAX);
end

disp(['STARTED MCLA'])

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
[PGEN PLOAD QLOAD] = main_MCLA2PC3(generatore,carico,nodo,scenarios,type_X,module1,module2,flagPQ);
