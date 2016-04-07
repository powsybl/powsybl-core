%
% Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

% Authors: Quinary <itesla@quinary.com>

function exitcode=smallsignal_HELPER(ifile, ofile, furtherparsfile)

close all; %% delete all figures

disp('wp4.3 - Dynamic SmallSignal - v1.3');
disp(sprintf(' ifile: %s', ifile));
disp(sprintf(' ofile: %s', ofile));

exitcode=0;

try
	load(ifile);

	 %% Set input parameters and call main function

	time   = t;      % Time vector of order Nx1 with variable time step
	signal  = LP1; % Active power flow on relevant lines of order NxM, where M is the number of signals

    %	step_min = 0.04;  % Minimum step size (full details in future documentation)
    %	var_min = 0.3;    % [0-1] Filter signals with varianza smaller than "var_min" 
    %	f = [0.1,2.5];    % [fmin,fmax] Range of  frequencies of interest in Hz
    %	d =[0,5,10];      % Damping values in percent (%) where the index distances will be calculated
    %	Nm  =10;          % Number of modes used in Prony to reconstruct the signals
	%   f_instant		  % Instant of the fault
	%   f_duration        % Duration of the fault
    
    if (nargin > 2)
        disp(sprintf(' further parameters file: %s', furtherparsfile));
        load(furtherparsfile);
    end 
    
    disp(' ');
    disp(sprintf(' step_min: %f', step_min));
    disp(sprintf(' var_min: %f', var_min));
    disp(sprintf(' f: [%f,%f]', f));
    disp(sprintf(' d: [%f,%f,%f]', d));
    disp(sprintf(' Nm: %i', Nm));
    disp(sprintf(' f_instant: %f', f_instant));
    disp(sprintf(' f_duration: %f', f_duration));

    [ss,y0,G,det]=sssi(signal,time,step_min,var_min,f,d,Nm,f_instant,f_duration);

	ss.smi
	ss.ami
	ss.gmi

	%% Outputs
	%
	% ss is a structure with the actual small-signal-stability index
	%       ss.smi single mode indicator
	%       ss.ami all modes indicator
	%       ss.gmi global mode indicator
	%
	% y0 is a structure with the section of signals that where used as input in Prony  
	%       y0.t   time vector 
	%       y0.y   signals
	%
	% G is a structure with Prony output 
	%       G.Poles detected modes 
	%       G.Res   residue of each mode
	%       G.K     gain of each mode
	%       G.that  time vector
	%       G.yhat  re-constructed signal
	%
	% det is a structure summarizing information
	%       det.l  Complex value of detected modes in the range of frequency
	%       det.f  Frequency in Hz of modes
	%       det.d Damping in percent of modes
    
    % dumps results in xml file
	fileID = fopen(ofile,'w');
	fprintf(fileID,'<?xml version="1.0" encoding="UTF-8"?>\n');
	fprintf(fileID,'<indexes>\n');
	fprintf(fileID,'<index name=\"smallsignal\">\n');
	%% GMI section 
	fprintf(fileID,'<matrix name=\"gmi\"><m><r>%.4f</r></m></matrix>\n',ss.gmi);

	%% AMI section 
	fprintf(fileID,'<matrix name=\"ami\">');
	fprintf(fileID,'<m>');
	for i=1:size(ss.ami,1)
	    fprintf(fileID,'<r>');      
	    for j=1:size(ss.ami,2)
	        fprintf(fileID,'%.4f ',ss.ami(i,j));
	    end 
	fprintf(fileID,'</r>');      
	end
	fprintf(fileID,'</m>');      
	fprintf(fileID,'</matrix>\n');

	%% SMI section 
	fprintf(fileID,'<matrix name=\"smi\">\n');
	fprintf(fileID,' <m>\n');
	for i=1:size(ss.smi,1)
	    fprintf(fileID,'  <r>');      
	    for j=1:size(ss.smi,2)
	        fprintf(fileID,'%.4f ',ss.smi(i,j));
	    end 
	fprintf(fileID,'</r>\n');      
	end
	fprintf(fileID,' </m>\n');      
	fprintf(fileID,'</matrix>\n');
	fprintf(fileID,'</index>\n');
	fprintf(fileID,'</indexes>\n');
	fclose(fileID);
    
    %% enable if executable must fail, when results are empty
    %%if isempty(ss.gmi)
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
% exit(exitcode);
end

