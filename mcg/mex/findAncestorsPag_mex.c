#include "mex.h"

#if MX_API_VER < 0x07030000
typedef int mwIndex;
typedef int mwSize;
#endif /* MX_API_VER */

#include <math.h>
#include <stdlib.h>
#include <string.h>

void findAncestors(const double *dag, double *ancestors, int i, int n);

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
    if (mxGetM(dag) != mxGetN(dag) || mxIsSparse(dag) || !mxIsDouble(dag))
    {
        mexErrMsgTxt("Input must be a noncomplex square non-sparse matrix of type 'double'.");
    }
    
    plhs[0] = mxCreateDoubleMatrix(1,n,mxREAL);
    ancestors = mxGetPr(plhs[0]);
    
    findAncestors(mxGetPr(dag), ancestors, (int) *mxGetPr(s)-1, n);
}

void findAncestors(const double *dag, double *ancestors, int i, int n) {
    int cur;
        
    // Now visit each parent
    for(cur = 0; cur < n; ++cur) {
        if(!ancestors[cur] && *(dag + i * n + cur) == 2 && *(dag + cur * n + i) == 3) {
            ancestors[cur] = 1;
            findAncestors(dag, ancestors, cur, n);
        }
    }

}
