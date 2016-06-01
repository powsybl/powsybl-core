%
% Copyright (c) 2016, Ricerca sul Sistema Energetico – RSE S.p.A. <itesla@rse-web.it>
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

clear all
clc
close all
load feanalyzerinput.mat

delays = unique(forec_filt(:,2));
figure
visualizzazione = [1:10:length(delays)];

intervals_delay = [60*6 60*12 60*18 60*24 60*30];
Ndelays = length(intervals_delay)+1;
for ide = 1:Ndelays
  if ide == 1
    delay2{ide} = find(forec_filt(:,2)<=intervals_delay(ide));
  elseif ide == Ndelays
      delay2{ide} = find(forec_filt(:,2)>intervals_delay(ide-1));
  else
     delay2{ide} = find(forec_filt(:,2) <= intervals_delay(ide)),find(forec_filt(:,2) > intervals_delay(ide-1));
  end
    
end


quale_int = {'6' '12' '18' '24' '30' '> 30' };

s = {'go-' 'kx-' 'rs-' 'b.-' 'm>-' 'y^-'};
MEAN1=[]; STDDEV1=[];interv=[];
for ide = 1:length(delay2)
   if isempty(delay2{ide})==0
    idx_set{ide} = delay2{ide};
    
    delay_ForeSnap{ide,1} = [forec_filt(idx_set{ide},3:end)];
    delay_ForeSnap{ide,2} = [snap_filt(idx_set{ide},3:end)];
    delay_ForeSnap{ide,3} = [snap_filt(idx_set{ide},3:end) - forec_filt(idx_set{ide},3:end)];
    means = nanmean(delay_ForeSnap{ide,3},1);
    stds = nanstd(delay_ForeSnap{ide,3},0,1);
    MEAN1 = [MEAN1; means];
    STDDEV1 = [STDDEV1; stds];
    interv = [interv ide];
    subplot(2,ceil(length(delay2)/2),ide)
%     for var = 1:30%size(forec_filt,2)-2
%     [n x] = hist(delay_ForeSnap{ide,3}(:,var),floor(sqrt( length(idx_set{ide})/2)));
%     dx = diff(x);
%     delay_pdf{ide} = n./(dx(1)*sum(n));
%     
%     plot(x,delay_pdf{ide},s{rem(ide,5)+1}),hold on
%     end
    bar(stds),title(['std deviations for forecasts up to ' quale_int{ide} ' hours ahead'])
    xlabel('variable nr'),ylabel('std dev in MW')
   end
end


validi = intersect(find(all(1-isnan(STDDEV1),1)),find(all(1-(STDDEV1==0),1)));
Nvars = min(30*200,length(validi));
Nvars_page = 30;
vars = validi(1:Nvars);

for ivar = 1:Nvars
   if rem(ivar-1,Nvars_page)==0
    
    scrsz = get(0,'ScreenSize');
    figure('Position',[1 scrsz(4) scrsz(3) scrsz(4)])
   end
    subplot(5,ceil(Nvars_page/5),rem(ivar-1,Nvars_page)+1)
%     for var = 1:30%size(forec_filt,2)-2
%     [n x] = hist(delay_ForeSnap{ide,3}(:,var),floor(sqrt( length(idx_set{ide})/2)));
%     dx = diff(x);
%     delay_pdf{ide} = n./(dx(1)*sum(n));
%     
%     plot(x,delay_pdf{ide},s{rem(ide,5)+1}),hold on
%     end
    bar(MEAN1(:,vars(ivar)),'grouped')
    xlabel('k hour ahead')
    ylabel('mean value, MW')
    set(gca,'XTicklabel',{quale_int{interv}})
    title([ strrep(inj_ID{vars(ivar)},'_','-')])
    if rem(ivar,Nvars_page)==0
        save2word('means.doc')
        close(gcf)
    end
end


for ivar = 1:Nvars
   
    
    if rem(ivar-1,Nvars_page)==0
    scrsz = get(0,'ScreenSize');
figure('Position',[1 scrsz(4) scrsz(3) scrsz(4)])
   end
    subplot(5,ceil(Nvars_page/5),rem(ivar-1,Nvars_page)+1)
%     for var = 1:30%size(forec_filt,2)-2
%     [n x] = hist(delay_ForeSnap{ide,3}(:,var),floor(sqrt( length(idx_set{ide})/2)));
%     dx = diff(x);
%     delay_pdf{ide} = n./(dx(1)*sum(n));
%     
%     plot(x,delay_pdf{ide},s{rem(ide,5)+1}),hold on
%     end
    bar(STDDEV1(:,vars(ivar)),'grouped')
    xlabel('k hour ahead')
    ylabel('std deviation, MW')
    set(gca,'XTicklabel',{quale_int{interv}})
    title([ strrep(inj_ID{vars(ivar)},'_','-')])
    if rem(ivar,Nvars_page)==0
        save2word('stds.doc')
        close(gcf)
    end
end



for ivar = 1:Nvars
   
    
    if rem(ivar-1,Nvars_page)==0
    scrsz = get(0,'ScreenSize');
figure('Position',[1 scrsz(4) scrsz(3) scrsz(4)])
   end
    subplot(5,ceil(Nvars_page/5),rem(ivar-1,Nvars_page)+1)
    for iv=1:length(interv)%size(forec_filt,2)-2
    [n x] = hist(delay_ForeSnap{interv(iv),3}(:,vars(ivar)),floor(sqrt(length(idx_set{interv(iv)})/2)));
    dx = diff(x);
    delay_pdf{ide} = n./(dx(1)*sum(n));
    
    plot(x,delay_pdf{ide},s{rem(iv,5)+1},'markersize',5),hold on
    end
    
    xlabel('MW error')
    ylabel('pdf')
    if rem(ivar-1,Nvars_page)==0
    legend({quale_int{interv}},'Location','Best')
    end
    grid on
    title([strrep(inj_ID{vars(ivar)},'_','-')])
    if rem(ivar,Nvars_page)==0
        save2word('pdfs.doc')
        close(gcf)
    end
end
