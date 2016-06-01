%
% Copyright (c) 2016, Ricerca sul Sistema Energetico – RSE S.p.A. <itesla@rse-web.it>
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

%***************pu on max snap ********************************************
weighted_sigma_err_pu_ms=sum(sigma_err_pu_ms(inj_OK).*max_snap(inj_OK))/sum(max_snap(inj_OK));
disp(' average of sigma [pu of max snapshot] weighted on maximum snapshots')
disp(weighted_sigma_err_pu_ms);

mean_sigma_err_pu_ms=mean(sigma_err_pu_ms(inj_OK));
disp(' average of sigma [pu of max snapshot]')
disp(mean_sigma_err_pu_ms);

MAX_sigma_err_pu_ms=max(sigma_err_pu_ms(inj_OK));
disp(' MAX of sigma [pu of max snapshot]')
disp(MAX_sigma_err_pu_ms);


disp('number of analysed injections for sigma_err_pu_ms')
disp(length(inj_OK))
temp=find(sigma_err_pu_ms>=0 & sigma_err_pu_ms<0.03);
disp('number of 0<=sigma_err_pu_ms<0.03')
disp(length(temp))
clear temp
temp=find(sigma_err_pu_ms>=0.03 & sigma_err_pu_ms<0.1);
disp('number of 0.03<=sigma_err_pu_ms<0.1')
disp(length(temp))
clear temp
temp=find(sigma_err_pu_ms>=0.1 & sigma_err_pu_ms<0.2);
disp('number of 0.1<=sigma_err_pu_ms<0.2')
disp(length(temp))
clear temp
temp=find(sigma_err_pu_ms>=0.2 & sigma_err_pu_ms<0.3);
disp('number of 0.2<=sigma_err_pu_ms<0.3')
disp(length(temp))
clear temp
temp=find(sigma_err_pu_ms>=0.3);
disp('number of 0.3<=sigma_err_pu_ms')
disp(length(temp))
clear temp

%***************pu on max snap with filter <25%****************************
weighted_sigma_err_pu_ms=sum(sigma_err_pu_MAXSNAP(inj_OK5).*max_snap(inj_OK5))/sum(max_snap(inj_OK5));
disp(' average of sigma [pu of max snapshot] weighted on maximum snapshots - <25%')
disp(weighted_sigma_err_pu_ms);

mean_sigma_err_pu_ms=mean(sigma_err_pu_MAXSNAP(inj_OK5));
disp(' average of sigma [pu of max snapshot] - <25%')
disp(mean_sigma_err_pu_ms);

MAX_sigma_err_pu_ms=max(sigma_err_pu_MAXSNAP(inj_OK5));
disp(' MAX of sigma [pu of max snapshot] - <25%')
disp(MAX_sigma_err_pu_ms);

%************pictures******************************************************

figure(1)
mesh(corr_matrix)

figure(2)
plot(sigma(inj_OK1),'b')
hold on
plot(sort(sigma(inj_OK1)),'r')
xlabel('number of analysed injections')
ylabel('errors [MW or Mvar] standard deviation')
title('St.deviations of forecast errors')
legend('not monotone','monotone');
print -dbmp16m sigma.bmp

figure(3)
plot(avera(inj_OK1))
xlabel('number of analysed injections')
ylabel('errors [MW or Mvar] mean')
title('Means of forecast errors')
print -dbmp16m avera.bmp

figure(4)
plot(sigma_err_pu(inj_OK1),'b')
hold on
plot(sort(sigma_err_pu(inj_OK1)),'r')
xlabel('number of analysed injections')
ylabel('errors [p.u. of forecast] standard deviation')
title('St.deviations of "pu" forecast errors')
legend('not monotone','monotone');
print -dbmp16m sigma_err_pu.bmp

figure(5)
plot(avera_err_pu(inj_OK1))
xlabel('number of analysed injections')
ylabel('errors [p.u. of forecast] mean')
title('Means of "pu" forecast errors')
print -dbmp16m avera_err_pu.bmp

figure(6)
plot(max_snap(inj_OK),'b')
hold on
plot(sort(max_snap(inj_OK)),'r')
xlabel('number of analysed injections')
ylabel('maximum snapshot "per injection"')
title('Maximum of snapshots')
legend('not monotone','monotone');

figure(7)
plot(sigma_err_pu_nf(inj_OK),'b')
hold on
plot(sort(sigma_err_pu_nf(inj_OK)),'r')
xlabel('number of analysed injections')
ylabel('errors [p.u. of mean of forecast] standard deviation')
title('St.deviations of "pu" forecast errors normalized to mean of forecast')
legend('not monotone','monotone');

figure(8)
plot(sigma_err_pu_ms(inj_OK),'b')
hold on
plot(sort(sigma_err_pu_ms(inj_OK)),'r')
xlabel('number of analysed injections')
ylabel('errors standard deviation  [p.u. of max of snapshots]')
title('St.deviations of "pu" forecast errors normalized to maximum of snapshots')
legend('not monotone','monotone');

figure(9)
plot(avera_err_pu_ms(inj_OK),'b')
hold on
plot(sort(avera_err_pu_ms(inj_OK)),'r')
xlabel('number of analysed injections')
ylabel('mean errors [p.u. of max of snapshots]')
title('Means of "pu" forecast errors normalized to maximum of snapshots')
legend('not monotone','monotone');

figure(10)
plot(sort(sum_ausf(inj_OK)),'b')
xlabel('Number of analysed injections')
ylabel('Number of significant observations')
title('Number of significant observations - "pu" maximum of snapshots')

figure(11)
plot(sort(sum_ausf3),'r');
hold on
plot(sort(sum_ausf5),'b');
xlabel('Number of analysed injections')
ylabel('Number of significant observations case 4 case 5')
title('Number of significant observations')
legend('case4','case5');

figure(12)
plot(sigma_err_pu_MAXSNAP(inj_OK5),'b')
hold on
plot(sort(sigma_err_pu_MAXSNAP(inj_OK5)),'r')
xlabel('number of analysed injections')
ylabel('errors standard deviation  [p.u. max snapshots] - (<25% of Max)')
title('St.dev. of "pu" for. errors normalized to max snapshots - (<25% of Max)')
legend('not monotone','monotone');



