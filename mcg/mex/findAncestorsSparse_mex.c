#include "mex.h"

#if MX_API_VER < 0x07030000
typedef int mwIndex;
typedef int mwSize;
#endif /* MX_API_VER */

#include <math.h>
#include <stdlib.h>
#include <string.h>

void findAncestors(const mxArray *dag, double *ancestors, int i, mwIndex *ir, mwIndex *jc);

void mexFunction(int nlhs, mxArray *plhs[],
        int nrhs, const mxArray *prhs[])
{
    mwSize n;
    
    /* output data */
    double *ancestors;
    
    /* arguments */
    const mxArray* dag;
    const mxArray* s;
    
    if (nrhs != 2)
    {
        mexErrMsgTxt("2 inputs required.");
    }
    
    dag = prhs[0];
    s = prhs[1];
    n = mxGetN(dag);
    
    /* The first input must be a sparse matrix. */
    if (mxGetM(dag) != mxGetN(dag) || !mxIsSparse(dag))
    {
        mexErrMsgTxt("Input must be a noncomplex square sparse matrix.");
    }
    
    plhs[0] = mxCreateDoubleMatrix(1,n,mxREAL);
    ancestors = mxGetPr(plhs[0]);
    
    findAncestors(dag, ancestors, (int) *mxGetPr(s)-1, mxGetIr(dag),mxGetJc(dag));
}

void findAncestors(const mxArray *dag, double *ancestors, int i, mwIndex *ir, mwIndex *jc) {
    mwIndex   starting_row_index, stopping_row_index, current_row_index;
    mwSize cur;
    
    // Now visit each parent
    starting_row_index = jc[i];
    stopping_row_index = jc[i+1];
    for(current_row_index = starting_row_index; current_row_index < stopping_row_index; ++current_row_index) {
        cur = ir[current_row_index];
        if(!ancestors[cur]) {
            ancestors[cur] = 1;
            findAncestors(dag, ancestors, cur, ir, jc);
        }
    }
}
