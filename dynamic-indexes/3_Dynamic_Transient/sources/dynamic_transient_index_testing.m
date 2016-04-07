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

clear all;clc;close all

%% Load time series

load transient

%% TRANSIENT STABILITY

t;                 % Time vector size t x 1                
delta;  % Angle of the machines size t x N;
M;      % Two time interia (H) of the machines (M=2*H), size 1 x N

[J]=dynamic_transient(t,delta,M)


% outputs
%      J       = Final index


