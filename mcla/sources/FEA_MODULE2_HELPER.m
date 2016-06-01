%
% Copyright (c) 2016, Ricerca sul Sistema Energetico – RSE S.p.A. <itesla@rse-web.it>
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

function exitcode=FEA_MODULE2_HELPER(ifile, ofile, s_ncluster,IRs,Tflags)
close all; %% delete all figures
mversion='1.8.0';
disp(sprintf('wp5 - module2 - version: %s', mversion));
disp(sprintf(' ifile: %s', ifile));
disp(sprintf(' ofile: %s', ofile));
disp(sprintf(' cluster number: %s', s_ncluster));
disp(sprintf(' IR: %s', IRs));
disp(sprintf(' Tflag: %s', Tflags));


IR = str2double(IRs);
Tflag = str2double(Tflags);
clust = str2double(s_ncluster)+1;
moutput.errmsg='Ok';

load(ifile);
% moutput = out;
for iout = 1:length(out1)
    mod_gaussian = out1(iout).modality_gaussian;
    module1 = out1(iout).module1;
    try
        
        %assuming param ncluster starts from 0
        if ~mod_gaussian
            ncluster=str2double(s_ncluster)+1;
            
            %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
            % here starts RSE code, extracted from module2_output2.m  (module2, on specific cluster k)
            %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
            disp(sprintf('Processing cluster: %u', ncluster));
            tic;
            mod1.k      = clust;
            mod1.w      = module1.w(clust);
            
            mod1.Z_c    = module1.Z_c{clust,1};
            
            [MOD2] = MODULE2(mod1,IR,Tflag);
            toc;
            
            %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
            % here ends RSE code, extracted from module2_output2.m
            %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
            
        else
            ncluster = [];
            para=[];
            MOD2=[];
            
        end
        moutput(iout).errmsg='Ok';
        moutput(iout).para=MOD2;
        moutput(iout).ncluster=ncluster;
        moutput(iout).mversion=mversion;
        exitcode=0;
    catch err
        moutput(iout).errmsg=err.message;
        disp(getReport(err,'extended'));
        exitcode=-1;
    end
    totmoutput.out2(iout) = moutput(iout);
end

save(ofile, '-struct', 'totmoutput');
% exit(exitcode);

end

