#include "mex.h"

#if MX_API_VER < 0x07030000
typedef int mwIndex;
typedef int mwSize;
#endif /* MX_API_VER */

#include <math.h>
#include <stdlib.h>
#include <string.h>

void computeClosure(const double *dag, double *closure, int n);

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
    if (mxGetM(dag) != mxGetN(dag) || mxIsSparse(dag) || !mxIsDouble(dag))
    {
        mexErrMsgTxt("Input must be a noncomplex square non-sparse matrix of type 'double'.");
    }
    
    plhs[0] = mxCreateDoubleMatrix(n,n,mxREAL);
    closure = mxGetPr(plhs[0]);
    
    computeClosure(mxGetPr(dag), closure, n);
}

void innerFunction(const double *dag, double *ancestors, int i, int n) {
    int cur;
        
    // Now visit each parent
    for(cur = 0; cur < n; ++cur) {
        if(!ancestors[cur] && *(dag + i * n + cur)) {
            ancestors[cur] = 1;
            innerFunction(dag, ancestors, cur, n);
        }
    }
}

void computeClosure(const double *dag, double *closure, int n)
{
    mwSize i;
    double *ancestors;
        
    ancestors = malloc(n * sizeof(double));
    
    for(i = 0; i < n; ++i) {
        memset(ancestors, 0, n*sizeof(double));
        innerFunction(dag, ancestors, i, n);
        memcpy(closure + i * n, ancestors, n * sizeof(double));
    }
    
    free(ancestors);
}
