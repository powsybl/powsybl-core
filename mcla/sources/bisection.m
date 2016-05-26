%
% Copyright (c) 2016, Ioannis Konstantelos <i.konstantelos@imperial.ac.uk>
% and Mingyang Sun <mingyang.sun11@imperial.ac.uk> – Imperial College London
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

function x = bisection( theta,U1,U2 )
    % Check that that neither end-point is a root
    % and if f(a) and f(b) have the same sign, throw an exception.
    r=theta;
    x0=0; xn=1;N=10000;
    if  h_gumbel( x0,U1,U2,r )==0
       x=x0;
       return;
    elseif h_gumbel( xn,U1,U2,r )==0
        x=xn;
       return;
    end
    
    for k = 1:N
        % Find the mid-point
        xc = (x0 + xn)/2;

        % Check if we found a root or whether or not
        % we should continue with:
        %          [a, c] if f(a) and f(c) have opposite signs, or
        %          [c, b] if f(c) and f(b) have opposite signs.

        if (  h_gumbel( xc,U1,U2,r )==0 )
            x = xc;
            return;
        elseif ( h_gumbel( xc,U1,U2,r )*h_gumbel( x0,U1,U2,r ) < 0 )
            xn = xc;
        else
            x0 = xc;
        end

        % If |b - a| < eps_step, check whether or not
        %       |f(a)| < |f(b)| and |f(a)| < eps_abs and return 'a', or
        %       |f(b)| < eps_abs and return 'b'.

        if ( xn - x0 < 10^-5 )
            if ( abs( h_gumbel( x0,U1,U2,r ) ) < abs( h_gumbel( xn,U1,U2,r ) ) && abs( h_gumbel( x0,U1,U2,r )) < 10^-5)
                x = x0;
                return;
            elseif ( abs( h_gumbel( xn,U1,U2,r )) < 10^-5 )
                 x = xn;
                return;
            end
        end
        
        if k==N
          x=0.5;
        end
    end
% 
%     error( 'the method did not converge' );
end 

