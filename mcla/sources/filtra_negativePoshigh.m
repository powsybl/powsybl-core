%
% Copyright (c) 2016, Ricerca sul Sistema Energetico – RSE S.p.A. <itesla@rse-web.it>
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

function [Y inj_ID] = filtra_negativePoshigh(Y,inj_ID,soglia)

R = corr(Y);

perc = soglia;
corr_yy = R;
corr_yys = corr_yy - eye(size(corr_yy));


max_corr = max(max(corr_yys));
[QualeR QualeC] = find(corr_yys==max_corr);
ngro = 0;
m_y = mean(Y,1);
std_y = std(Y,0,1);

% FOR NEGATIVELY HIGHLY CORRELATED VARIABLES
while max_corr > perc
    ngro = ngro +1;
    
    SET(ngro,:)=[(QualeR(1)) (QualeC(1))];
    
    Y(:,(QualeR(1))) = sum(Y(:,([QualeR(1) QualeC(1)])),2);
    
    m_y(QualeR(1)) = mean(Y(:,(QualeR(1))));
    std_y(QualeR(1)) =std(Y(:,(QualeR(1)))); 
    
    if std_y(QualeR(1)) < 1e-5
        dimensione = max(max(abs(Y(:,(QualeR(1)))))*0.01,1e-4);
        Y(:,idx_fore(QualeR(1))) = Y(:,(QualeR(1))) + dimensione.*randn(size(Y,1),1);
        std_y(QualeR(1)) =std(Y(:,(QualeR(1)))); 
    end
    

    Y(:,(QualeC(1))) = [];
    inj_ID{QualeR(1)} = [inj_ID{QualeR(1)} '+' inj_ID{QualeC(1)}];
   inj_ID((QualeC(1)))=[];
   
    m_y(QualeC(1)) = [];
    std_y(QualeC(1)) = [];
    
    corr_yy = corr(Y);
    
    corr_yys = corr_yy - eye(size(corr_yy));
    
    max_corr = max(max(corr_yys));
    
    [QualeR QualeC] = find(corr_yys==max_corr);
end

min_corr = min(min(corr_yys));
[QualeRm QualeCm] = find(corr_yys==min_corr);
ngro = 0;
m_y = mean(Y,1);
std_y = std(Y,0,1);

% FOR NEGATIVELY HIGHLY CORRELATED VARIABLES
while min_corr < -1*perc
    ngro = ngro +1;
    
    SETM(ngro,:)=[(QualeRm(1)) (QualeCm(1))];
    
    Y(:,(QualeRm(1))) = diff(Y(:,([QualeRm(1) QualeCm(1)])),1,2);
    
    m_y(QualeRm(1)) = mean(Y(:,(QualeRm(1))));
    std_y(QualeRm(1)) =std(Y(:,(QualeRm(1)))); 
    
    if std_y(QualeRm(1)) < 1e-5
        dimensione = max(max(abs(Y(:,(QualeRm(1)))))*0.01,1e-4);
        Y(:,idx_fore(QualeRm(1))) = Y(:,(QualeRm(1))) + dimensione.*randn(size(Y,1),1);
        std_y(QualeRm(1)) =std(Y(:,(QualeRm(1)))); 
    end
    

    Y(:,(QualeCm(1))) = [];
    inj_ID{QualeRm(1)} = [inj_ID{QualeRm(1)} '-' inj_ID{QualeCm(1)}];
   inj_ID((QualeCm(1)))=[];
   
    m_y(QualeCm(1)) = [];
    std_y(QualeCm(1)) = [];
    
    corr_yy = corr(Y);
    
    corr_yys = corr_yy - eye(size(corr_yy));
    
    min_corr = min(min(corr_yys));
    
    [QualeRm QualeCm] = find(corr_yys==min_corr);
end
