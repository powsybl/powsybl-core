%
% Copyright (c) 2016, Ricerca sul Sistema Energetico – RSE S.p.A. <itesla@rse-web.it>
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

% SCRIPT FOR ELIMINATING LINEAR DEPENDENT ROWS OF CORRELATION MATRIX TO
% REDUCE CONDITIONING NUMBER OF MATRIX AND MAKE ITS INVERSION EASIER
perc = 0.9999;
corr_yy = corr(Y(:,idx_fore));
corr_yys = corr_yy - eye(size(corr_yy));

max_corr = max(max(corr_yys));
[QualeR QualeC] = find(corr_yys==max_corr);

idx_fore0 = idx_fore;
ngro = 0;
matrice=eye(length(idx_fore0));
% FOR POSITIVELY HIGHLY CORRELATED VARIABLES
while max_corr > perc
    ngro = ngro +1;
    SET(ngro,:)=[idx_fore(QualeR(1)) idx_fore(QualeC(1))];
    Y(:,idx_fore(QualeR(1))) = sum(Y(:,idx_fore([QualeR(1) QualeC(1)])),2);

    matrice(QualeR(1),:) = sum(matrice(([QualeR(1) QualeC(1)]),:),1);
    
    m_y(QualeR(1)) = mean(Y(:,idx_fore(QualeR(1))));
    std_y(QualeR(1)) =std(Y(:,idx_fore(QualeR(1)))); 
    if std_y(QualeR(1)) < 1e-5
%         keyboard
        dimensione = max(max(abs(Y(:,idx_fore(QualeR(1)))))*0.01,1e-4);
        Y(:,idx_fore(QualeR(1))) = Y(:,idx_fore(QualeR(1))) + dimensione.*randn(size(Y,1),1);
        std_y(QualeR(1)) =std(Y(:,idx_fore(QualeR(1)))); 
    end
    
    Y(:,idx_fore(QualeC(1))) = [];

    inj_ID(idx_fore(QualeC(1)))=[];
    m_y(QualeC(1)) = [];
    std_y(QualeC(1)) = [];
    matrice(QualeC(1),:) = [];
    
    idx_err_post = find(idx_err > idx_fore(QualeC(1)));
    
    idx_fore_post = find(idx_fore > idx_fore(QualeC(1)));
    
    idx_fore(idx_fore_post)=[idx_fore(idx_fore_post)-1];
    idx_err(idx_err_post)=[idx_err(idx_err_post)-1];
    idx_fore(QualeC(1))=[];
    
    corr_yy = corr(Y(:,idx_fore));
    
    corr_yys = corr_yy - eye(size(corr_yy));
    
    max_corr = max(max(corr_yys));
    
    [QualeR QualeC] = find(corr_yys==max_corr);
end
min_corr = min(min(corr_yys));
[QualeRm QualeCm] = find(corr_yys==min_corr);
ngro = 0;

% FOR NEGATIVELY HIGHLY CORRELATED VARIABLES
while min_corr < -1*perc
    ngro = ngro +1;
    
    SETM(ngro,:)=[idx_fore(QualeRm(1)) idx_fore(QualeCm(1))];
    
    Y(:,idx_fore(QualeRm(1))) = diff(Y(:,idx_fore([QualeRm(1) QualeCm(1)])),1,2);
    
    m_y(QualeRm(1)) = mean(Y(:,idx_fore(QualeRm(1))));
    std_y(QualeRm(1)) =std(Y(:,idx_fore(QualeRm(1)))); 
    
    if std_y(QualeRm(1)) < 1e-5
        dimensione = max(max(abs(Y(:,idx_fore(QualeRm(1)))))*0.01,1e-4);
        Y(:,idx_fore(QualeRm(1))) = Y(:,idx_fore(QualeRm(1))) + dimensione.*randn(size(Y,1),1);
        std_y(QualeRm(1)) =std(Y(:,idx_fore(QualeRm(1)))); 
    end
    
    matrice(QualeRm(1),:) = diff(matrice(([QualeRm(1) QualeCm(1)]),:),1,1);
    Y(:,idx_fore(QualeCm(1))) = [];
   inj_ID(idx_fore(QualeCm(1)))=[];
    m_y(QualeCm(1)) = [];
    std_y(QualeCm(1)) = [];
     matrice(QualeCm(1),:) = [];
    idx_err_post = find(idx_err > idx_fore(QualeCm(1)));
    
    idx_fore_post = find(idx_fore > idx_fore(QualeCm(1)));
    
    idx_fore(idx_fore_post)=[idx_fore(idx_fore_post)-1];
    idx_err(idx_err_post)=[idx_err(idx_err_post)-1];
    idx_fore(QualeCm(1))=[];
    
    corr_yy = corr(Y(:,idx_fore));
    
    corr_yys = corr_yy - eye(size(corr_yy));
    
    min_corr = min(min(corr_yys));
    
    [QualeRm QualeCm] = find(corr_yys==min_corr);
end
