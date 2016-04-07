%
% Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

% Authors: Quinary <itesla@quinary.com>

function exitcode=transient_HELPER(ifile, ofile, furtherparsfile)

close all; %% delete all figures

%% TRANSIENT STABILITY
disp('wp4.3 - Dynamic Transient - v1.3');
disp(sprintf(' ifile: %s', ifile));
disp(sprintf(' ofile: %s', ofile));

exitcode=0;

try
	load(ifile);

	%% Set input parameters and call main function

	% INPUTS
	%  t     - Time vector
	%  delta - Machine angles vector 
	%  M     - Mechanical Starting time, two time interia (H) of the machines (M=2*H)
	%
	% OUTPUTS
	%  J - Transient stability index
    
    if (nargin > 2)
        disp(sprintf(' further parameters file: %s', furtherparsfile));
        load(furtherparsfile);
    end 
    
    %%disp(' ');
    %%disp(sprintf(' : %f', XYZ));

    [J]=dynamic_transient(t,delta,M);
    
    % dumps results in xml file
	fileID = fopen(ofile,'w');
	fprintf(fileID,'<?xml version="1.0" encoding="UTF-8"?>\n');
	fprintf(fileID,'<indexes>\n');
	fprintf(fileID,'<index name=\"transient\">\n');
	%% J section 
	fprintf(fileID,'<j>%.4f</j>\n',J);
	fprintf(fileID,'</index>\n');
	fprintf(fileID,'</indexes>\n');
	fclose(fileID);
    
    %% enable if executable must fail, when results are empty
    %%if isempty(J)
    %%    exitcode=-1;
    %%end
    
catch err
   disp(getReport(err,'extended'));
   exitcode=-1;
   %    write empty index xml
   try
       fileID = fopen(ofile,'w');
       fprintf(fileID,'<?xml version="1.0" encoding="UTF-8"?>\n');
       fprintf(fileID,'<indexes/>\n');
       fclose(fileID);
   catch err1
       disp(getReport(err1,'extended'));
   end
end
exit(exitcode);
end
