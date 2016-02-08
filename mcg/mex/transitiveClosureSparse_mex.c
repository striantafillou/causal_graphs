#include "mex.h"

#if MX_API_VER < 0x07030000
typedef int mwIndex;
typedef int mwSize;
#endif /* MX_API_VER */

#include <math.h>
#include <stdlib.h>
#include <string.h>

void computeClosure(const mxArray *dag, double *closure);

void mexFunction(int nlhs, mxArray *plhs[],
        int nrhs, const mxArray *prhs[])
{
    mwSize n;
    
    /* output data */
    double *closure;
    
    /* arguments */
    const mxArray* dag;
    
    if (nrhs != 1)
    {
        mexErrMsgTxt("1 input required.");
    }
    
    dag = prhs[0];
    n = mxGetN(dag);
    
    /* The first input must be a sparse matrix. */
    if (mxGetM(dag) != mxGetN(dag) ||
            !mxIsSparse(dag))
    {
        mexErrMsgTxt("Input must be a noncomplex square sparse matrix.");
    }
    
    plhs[0] = mxCreateDoubleMatrix(n,n,mxREAL);
    closure = mxGetPr(plhs[0]);
    
    computeClosure(dag, closure);
}

void innerFunction(const mxArray *dag, double *ancestors, int i, mwIndex *ir, mwIndex *jc) {
    mwIndex   starting_row_index, stopping_row_index, current_row_index;
    mwSize tmpElem;
    
    // Now visit each parent
    starting_row_index = jc[i];
    stopping_row_index = jc[i+1];
    for(current_row_index = starting_row_index; current_row_index < stopping_row_index; ++current_row_index) {
        tmpElem = ir[current_row_index];
        if(!ancestors[tmpElem]) {
            ancestors[tmpElem] = 1;
            innerFunction(dag, ancestors, tmpElem, ir, jc);
        }
    }
}

void computeClosure(const mxArray *dag, double *closure)
{
    mwIndex *ir, *jc;
    mwSize n, i;
    double *ancestors;
    
    n = mxGetN(dag);
    ir = mxGetIr(dag);
    jc = mxGetJc(dag);
    
    ancestors = malloc(n * sizeof(double));
    
    for(i = 0; i < n; ++i) {
        memset(ancestors, 0, n*sizeof(double));
        innerFunction(dag, ancestors, i, ir, jc);
        memcpy(closure + i * n, ancestors, n * sizeof(double));
    }
    
    free(ancestors);
}

