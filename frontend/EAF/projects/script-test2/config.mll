import "definitions" from de.eaf.base;
import "definitions" from de.evoal.optimisation.core;
import "definitions" from de.evoal.surrogate.ml;
import "definitions" from de.evoal.surrogate.optimisation;
import "definitions" from de.evoal.surrogate.smile.ml;

import "data" from 'config';

module 'config' {
prediction svr
maps 'x:0'
to 'y:0', 'y:1', 'y:2', 'y:3', 'y:4', 'y:5', 'y:6', 'y:7' 
using
layer transfer
with
function 'gaussian-svr'
mapping 'x:0'
to 'y:0'
with parameters
'σ' := 3.0;
'soft-margin' := 0.15;
'ε' := 1.4;
'tolerance' := 0.1;
function 'hellinger-svr'
mapping 'x:0'
to 'y:1'
with parameters
'σ' := 1.4;
'soft-margin' := 3.0;
'ε' := 0.15;
'tolerance' := 0.1;
function 'hyperbolic-tangent-svr'
mapping 'x:0'
to 'y:2'
with parameters
'scale' := 1.0;
'σ' := 3.0;
'soft-margin' := 0.15;
'offset' := 1.0;
'ε' := 1.4;
'tolerance' := 0.1;
function 'laplacian-svr'
mapping 'x:0'
to 'y:3'
with parameters
'σ' := 3.0;
'soft-margin' := 0.15;
'ε' := 1.4;
'tolerance' := 0.1;
function 'linear-svr'
mapping 'x:0'
to 'y:4'
with parameters
'σ' := 3.0;
'soft-margin' := 0.15;
'ε' := 1.4;
'tolerance' := 0.1;
function 'pearson-svr'
mapping 'x:0'
to 'y:5'
with parameters
'ω' := 0.2;
'σ' := 3.0;
'soft-margin' := 0.15;
'ε' := 1.4;
'tolerance' := 0.1;
function 'polynomial-svr'
mapping 'x:0'
to 'y:6'
with parameters
'σ' := 3.0;
'soft-margin' := 0.15;
'offset' := 1.0;
'ε' := 1.4;
'degree' := 2;
'scale' := 1.0;
'tolerance' := 0.1;
function 'thin-plate-spline-svr'
mapping 'x:0'
to 'y:7'
with parameters
'σ' := 3.0;
'soft-margin' := 0.15;
'ε' := 1.4;
'tolerance' := 0.1;
predict svr from "data.json"
and measure
'R²'();
end
and store to "svr.pson"
}