function save2word(filespec,prnopt)

% SAVE2WORD saves plots to Microsoft Word.
% function SAVE2WORD(filespec,prnopt) saves the current Matlab figure
%  window or Simulink model window to a Word file designated by
%  filespec.  If filespec is omitted, the user is prompted to enter
%  one via UIPUTFILE.  If the path is omitted from filespec, the
%  Word file is created in the current Matlab working directory.
%
%  Optional input argument prnopt is used to specify additional save
%  options:
%    -fHandle   Handle of figure window to save
%    -sName     Name of Simulink model window to save
%
%  Examples:
%  >> saveppt
%       Prompts user for valid filename and saves current figure
%  >> save2word('junk.doc')
%       Saves current figure to MS Word file called junk.doc
%  >> save2word('junk.doc','-f3')
%       Saves figure #3 to MS Word file called junk.doc 
%  >> save2word('models.doc','-sMainBlock')
%       Saves Simulink model named "MainBlock" to file called models.doc
%
%  The command-line method of invoking SAVEPPT will also work:
%  >> save2word models.doc -sMainBlock
%  
%  If the figure has to be pasted as a smaller size bitmap, go to 
%  File->preferences->Figure Copy Template->Copy Options and 
%  check "Match Figure Screen Size" checkbox.
%  Then make the figure small before by setting the position 
%  of the figure to a smaller size using
%  set(gca,'Position',[xpos,ypos,width,height])
%
%  Check also saveppt in Mathworks fileexchange

%Suresh E Joel, Mar 6,2003
%Virginia Commonwealth University
%Modification of 'saveppt' in Mathworks File Exchange 
%and valuable suggestions by Mark W. Brown, mwbrown@ieee.org
%

% Establish valid file name:
if nargin<1 | isempty(filespec);
  [fname, fpath] = uiputfile('*.doc');
  if fpath == 0; return; end
  filespec = fullfile(fpath,fname);
else
  [fpath,fname,fext] = fileparts(filespec);
  if isempty(fpath); fpath = pwd; end
  if isempty(fext); fext = '.doc'; end
  filespec = fullfile(fpath,[fname,fext]);
end

% Capture current figure/model into clipboard:
if nargin<2
  print -dmeta
else
  print('-dmeta',prnopt)
end

% Start an ActiveX session with PowerPoint:
word = actxserver('Word.Application');
%word.Visible = 1;

if ~exist(filespec,'file');
   % Create new presentation:
  op = invoke(word.Documents,'Add');
else
   % Open existing presentation:
  op = invoke(word.Documents,'Open',filespec);
end

% Find end of document and make it the insertion point:
end_of_doc = get(word.activedocument.content,'end');
set(word.application.selection,'Start',end_of_doc);
set(word.application.selection,'End',end_of_doc);

% Paste the contents of the Clipboard:
invoke(word.Selection,'Paste');

if ~exist(filespec,'file')
  % Save file as new:
  invoke(op,'SaveAs',filespec,1);
else
  % Save existing file:
  invoke(op,'Save');
end

% Close the presentation window:
invoke(op,'Close');

% Quit MS Word
invoke(word,'Quit');

% Close PowerPoint and terminate ActiveX:
delete(word);

return
