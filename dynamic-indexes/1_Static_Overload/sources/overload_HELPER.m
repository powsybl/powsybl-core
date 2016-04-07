%
% Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

% Authors: Quinary <itesla@quinary.com>

function exitcode=overload_HELPER(ifile, ofile, furtherparsfile)

close all; %% delete all figures

%% STATIC OVERLOAD
disp('wp4.3 - Static Overload - v1.3');
disp(sprintf(' ifile: %s', ifile));
disp(sprintf(' ofile: %s', ofile));

exitcode=0;

try
	load(ifile);

	%% Set input parameters and call main function
	%% POST-FAULT OVER LOADS
	% INPUTS

	%t;                 % Time vector
	%S  = S1;       % Input signal, Apparent power (S) size t x N
	%p  = 3;        % Exponent used to magnify problems, value between 1 to "inf"
	%d  = 10;       % Maximum Variation allowed from the nominal value (in %), e.g, 10 for 10%

	% OUTPUTS
	%  f_x - Transient stability index
	% output interpretation (cfr. 2.1.1 in D4.3)
	%The final value of the overload index fx is a scalar, and its interpretation is as follows:
		%fx = 1 → All lines are within the limits
		%fx > 1 → At least one line has violated its limit
		%fx ≫1 → A sever violation has ocurred
    
    
    
    if (nargin > 2)
        disp(sprintf(' further parameters file: %s', furtherparsfile));
        load(furtherparsfile);
    end 

	f_x = static_overload(t,S,p,d);

	% dumps results in xml file
	fileID = fopen(ofile,'w');
	fprintf(fileID,'<?xml version="1.0" encoding="UTF-8"?>\n');
	fprintf(fileID,'<indexes>\n');
	fprintf(fileID,'<index name=\"overload\">\n');
	%% J section 
	fprintf(fileID,'<fx>%.4f</fx>\n',f_x);
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
%exit(exitcode);
end
