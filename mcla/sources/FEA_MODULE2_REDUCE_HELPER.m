%
% Copyright (c) 2016, Ricerca sul Sistema Energetico – RSE S.p.A. <itesla@rse-web.it>
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

% m1file: output of module1
% mspartspath: directory where module2 partial clusters files results are stored
% m2fileprefix: string prefix of module2 partial clusters files (e.g. m2_ )
% s_k : number of cluster to process  (s_k will be appended to the m2fileprefix, to build the complete module2 partial cluster filename
%   e.g. m2_0.m, m2_1.m,    ....  , m2_k.m
% ofile: output file for FEA, will contain structs module1, module2, inj_ID, flagPQ
function exitcode=FEA_MODULE2_REDUCE_HELPER(m1file, m2partspath, m2fileprefix, s_k, ofile,percpu_fict_gauss_load,percpu_fict_gauss_RES,correlationS)
close all;
mversion='1.8.0';
disp(sprintf('wp5 - module2 reduce - version: %s', mversion));
disp(sprintf(' m1 file: %s', m1file));
disp(sprintf(' m2 parts path: %s', m2partspath));
disp(sprintf(' m2 file prefix: %s', m2fileprefix));
disp(sprintf(' k (total number of clusters): %s', s_k));
disp(sprintf(' ofile: %s', ofile));
disp(sprintf(' percpu_fict_gauss_load: %s', percpu_fict_gauss_load));
disp(sprintf(' percpu_fict_gauss_RES: %s', percpu_fict_gauss_RES));

moutput.errmsg='Ok';
percpu_gau_load=str2double(percpu_fict_gauss_load);
percpu_gau_RES=str2double(percpu_fict_gauss_RES);
correlation = str2double(correlationS);

k=str2double(s_k);
try
    load(m1file);
    for cls=0:k-1
        filename = sprintf('%s%d.mat', m2fileprefix, cls );
        filename =fullfile(m2partspath,filename);
        disp(filename);
        load(filename);
        for iout = 1:length(out1)
            mod_gaussian = out1(iout).modality_gaussian;
            if mod_gaussian == 0
                para = out2(iout).para;
                [allparas{cls+1,1}] = para;
                moutput(iout).module2.allparas=allparas;
            else
                moutput(iout).module2.allparas.stddev(1)=percpu_gau_load;
                moutput(iout).module2.allparas.stddev(2)=percpu_gau_RES;
                moutput(iout).module2.allparas.corre = correlation;
            end
            moutput(iout).errmsg=out1(iout).errmsg;
            moutput(iout).module1=out1(iout).module1;
            moutput(iout).dati_cond = out1(iout).dati_cond;
            moutput(iout).dati_Q = out1(iout).dati_Q;
            moutput(iout).dati_FPF = out1(iout).dati_FPF;
            moutput(iout).flagesistenza=out1(iout).flagesistenza;
            moutput(iout).rng_data=out1(iout).rng_data;
            moutput(iout).inj_ID=out1(iout).inj_ID;
            moutput(iout).flagPQ=out1(iout).flagPQ;
            moutput(iout).maxvalue=out1(iout).maxvalue;
            moutput(iout).mversion=out1(iout).mversion;
            moutput(iout).mod_gaussian=out1(iout).modality_gaussian;
            moutput(iout).conditional_sampling=out1(iout).conditional_sampling;
            totmoutput.out(iout) = moutput(iout);
        end
    end

    exitcode=0;
    
catch err
    moutput(1).errmsg=err.message;
    disp(getReport(err,'extended'));
    exitcode=-1;
end

save(ofile, '-struct', 'totmoutput');
% exit(exitcode);
end
