%
% Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

%
% Authors: Luigi Vanfretti, Venkata Satya Narasimham Arava, Rafael Segundo Sevilla
% Email: luigi.vanfretti@gmail.com
%

% POST-FAULT UNDER/OVER VOLTAGE

% Sample script
clear all;clc;close all

%% Load time series
load OverUnder_Voltage_zerosincoulmns

% POST-FAULT UNDER/OVER VOLTAGE
%% Input data and options into the index
t;               % time vector
V    = BVm;      % Voltage mangitudes - Matrix of size t x N
p    = 3;        % Exponent used to scale the index, value between 1 to "inf"
d    = 1;        % Maximum voltage variation allowed (in %) of the nominal value, i.e. 2 for 2%
                   
% Compute the index
[v_x] = static_voltage(t,V,p,d);

% Display the index in the Command Window
v_x

% outputs
%      v_x        = Final index
%% eof
