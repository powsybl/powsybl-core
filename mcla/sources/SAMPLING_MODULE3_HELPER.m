%
% Copyright (c) 2016, Ricerca sul Sistema Energetico – RSE S.p.A. <itesla@rse-web.it>
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

function exitcode=SAMPLING_MODULE3_HELPER(m1file, ofile, s_scenarios, s_rng_seed)
close all;
mversion='1.8.0';
disp(sprintf('wp5 - MCLA - version: %s', mversion));
disp(sprintf(' m1m2file:  %s',m1file));
disp(sprintf(' ofile:  %s', ofile));
disp(sprintf(' unconditioned samples:  %s',s_scenarios));

moutput.errmsg='Ok';

% module1: struct, output from module1
load(m1file);
% module2:  module2 output
% s_scenarios: number of samples to generate
scenarios=str2double(s_scenarios);
%if seed is not specified, 'shuffle'  on current platform time
if nargin < 4
    rng('shuffle','twister');
    rng_data = rng;
    disp(sprintf(' rng seed:  not specified, default is shuffle on current platform time'));
else
    rng(str2double(s_rng_seed),'twister');
    rng_data = rng;
    disp(sprintf(' rng seed:  %s',s_rng_seed));
end

disp(sprintf('flagPQ:  %u', out(1).flagPQ));
disp(['preprocessing: type_x, etc.'])

try
    
    for iout = 1:length(out)
        mod_gaussian = out(iout).mod_gaussian;
        module1 = out(iout).module1;
        module2 = out(iout).module2;
        if mod_gaussian == 0
            [ X_NEW ] = run_module3(module1,module2,scenarios);
            
            
            module3.X_NEW=X_NEW;
        else
            module3 = [];
        end
        moutput(iout).errmsg='Ok';
        moutput(iout).module1=out(iout).module1;
        moutput(iout).dati_cond = out(iout).dati_cond;
        moutput(iout).dati_Q = out(iout).dati_Q;
        moutput(iout).dati_FPF = out(iout).dati_FPF;
        moutput(iout).flagesistenza=out(iout).flagesistenza;
        moutput(iout).module3=module3;
        moutput(iout).module2=out(iout).module2;
        moutput(iout).rng_data=out(iout).rng_data;
        moutput(iout).inj_ID=out(iout).inj_ID;
        moutput(iout).flagPQ=out(iout).flagPQ;
        moutput(iout).maxvalue=out(iout).maxvalue;
        moutput(iout).mversion=out(iout).mversion;
        moutput(iout).mod_gaussian=out(iout).mod_gaussian;
        moutput(iout).conditional_sampling=out(iout).conditional_sampling;
        totmoutput.out(iout) = moutput(iout);
    end
    
    exitcode=0;
    
catch err
    moutput(1).errmsg=err.message;
    disp(getReport(err,'extended'));
    exitcode=-1;
end
save(ofile, '-struct', 'totmoutput');
