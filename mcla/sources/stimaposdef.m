%
% Copyright (c) 2016, Ricerca sul Sistema Energetico – RSE S.p.A. <itesla@rse-web.it>
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

%% Rebonato e JAckel
function Ct = stimaposdef(C0,C)

teta1 = zeros(size(C,1),size(C,1)-1);
Q = chol(C0);
B = Q';
teta0 = invfunzioneB(B)

for jt = 1:size(teta0,1)
    teta0(jt,find(isnan(teta0(jt,:))))=0;
    immaginari = find(~isreal(teta0(jt,:)));
    teta0(jt,immaginari)=real(teta0(jt,immaginari));
end
B = funzioneB(teta0,C);
% save  variabili.mat C
% options.Display = 'iter';
% options.MaxFunEvals = 8e4;
% options.TolFun = 1e-4;
% options.TolX = 1e-4;
% options = saoptimset('Display','iter','MaxMeshSize',1e20,'MaxFunEvals',2000,'MeshAccelerator','on');
options = psoptimset('Display','iter','MaxFunEvals',100);

problem.objective = @(x) funzione(x,C);
problem.X0 = teta0;
problem.options = options;
% problem.solver = 'simulannealbnd';

[x fval exitf] = patternsearch(problem);%fminsearch(@(x) funzione(x,C),teta0,options);%simulannealbnd(problem);%fminunc(@(x) funzione(x,C),teta0,options);%patternsearch(problem);%fminunc(@(x) funzione(x,C),teta0,options);%patternsearch(problem);% fminunc(@(x) funzione(x,C),teta0,options);%fsolve(@(x) funzione(x,C),teta0);%simulannealbnd(problem);% patternsearch(problem);%fminsearch(@(x) funzione(x,C),teta0,options);%
exitf
teta = x;

Cp = ones(size(C));

n = size(Cp,1);

for jB = 1:size(Cp,1)
   
       Cp(jB,1) = cos(teta(jB,1));
       for k = 2:n-1
           Cp(jB,k) = cos(teta(jB,k));
           for p = 1:k-1
        Cp(jB,k) = Cp(jB,k)*sin(teta(jB,p));
           end
       end
       for p = 1:n-1
       Cp(jB,n) = Cp(jB,n)* sin(teta(jB,p));
       end
end

Ct = Cp*Cp';
