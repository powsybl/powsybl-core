%
% Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

% Authors: Quinary <itesla@quinary.com>

function exitcode=underovervoltage_HELPER(ifile, ofile, furtherparsfile)

close all; %% delete all figures

%% STATIC OVER UNDER VOLTAGE
disp('wp4.3 - Static UnderOver Voltage - v1.31');
disp(sprintf(' ifile: %s', ifile));
disp(sprintf(' ofile: %s', ofile));

exitcode=0;

try
	load(ifile);

	%% Set input parameters and call main function
	% POST-FAULT UNDER/OVER VOLTAGE
	% INPUTS

	%t;                 % Time vector
	%V                % Voltage magnitudes - Matrix of size t x N
	%p    = 3;        % Exponent used to scale the index, value between 1 to "inf"
	%d    = 1;        % Maximum voltage variation allowed (in %) of the nominal value, i.e. 2 for 2%

	% OUTPUTS
	% output interpretation (cfr. 2.2.3 in D4.3)
		%vx = 1 → All buses are within the limits
		%vx > 1 → At least one bus has violated its limit
		%vx ≫1 → A sever violation has ocurred
    
    if (nargin > 2)
        disp(sprintf(' further parameters file: %s', furtherparsfile));
        load(furtherparsfile);
    end 

	[v_x] = static_voltage(t,V,p,d);

	% dumps results in xml file
	fileID = fopen(ofile,'w');
	fprintf(fileID,'<?xml version="1.0" encoding="UTF-8"?>\n');
	fprintf(fileID,'<indexes>\n');
	fprintf(fileID,'<index name=\"underovervoltage\">\n');
	%% J section 
	fprintf(fileID,'<vx>%.4f</vx>\n',v_x);
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
