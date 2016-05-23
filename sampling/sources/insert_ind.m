function NewMat=insert_ind(Mat,Ind,Val2Insert,Dim);
%---------------------------------------------------------------------------
% insert_ind function                                               General
% Description: Insert a column/s or row/s to a specific position in
%              a matrix.
% Input  : - Matrix.
%          - Index in which, afterwards, to insert the new column/s row/s.
%          - Column/s or row/s to insert.
%          - Dimension: 1 - Insert rows; 2 - Insert columns, default is 1.
% Output : - A new matrix with new column/s or row/s inserted.
% Tested : Matlab 7.0
%     By : Eran O. Ofek             December 2005
%    URL : http://wise-obs.tau.ac.il/~eran/matlab.html
% See also: delete_ind.m
% Example: NewMat=insert_ind(zeros(3,2),2,[1 1; 2 2],1)
% Reliable: 1
%---------------------------------------------------------------------------
if (nargin==3),
   Dim = 1;
elseif (nargin==4),
   % do nothing
else
   error('Illegal number of input arguments');
end


if (Ind==0),
   % begining of matrix
   switch Dim
    case 1
       NewMat = [Val2Insert; Mat];
    case 2
       NewMat = [Val2Insert, Mat];
    otherwise
       error(sprintf('%d-dimension is unspported - use only 1/2-d',Dim));
   end
elseif (Ind>=size(Mat,Dim)),
   % end of matrix
   switch Dim
    case 1
       NewMat = [Mat; Val2Insert];
    case 2
       NewMat = [Mat, Val2Insert];
    otherwise
       error(sprintf('%d-dimension is unspported - use only 1/2-d',Dim));
   end
else
   % middle of matrix
   switch Dim
    case 1
       NewMat = [Mat(1:Ind,:); Val2Insert; Mat(Ind+1:end,:)];
    case 2
       NewMat = [Mat(:,1:Ind), Val2Insert, Mat(:,Ind+1:end)];
    otherwise
       error(sprintf('%d-dimension is unspported - use only 1/2-d',Dim));
   end
end

