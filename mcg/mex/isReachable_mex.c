#include "mex.h"

#if MX_API_VER < 0x07030000
typedef int mwIndex;
typedef int mwSize;
#endif /* MX_API_VER */

#include <math.h>
#include <stdlib.h>
#include <string.h>

void isReachable(const double *dag, double *result, int s, int t, int n);

void mexFunction(int nlhs, mxArray *plhs[],
        int nrhs, const mxArray *prhs[])
{
    mwSize n;
    
    /* output data */
    double *result;
    
    /* arguments */
    const mxArray* dag;
    const mxArray* s;
    const mxArray* t;
    
    if (nrhs != 3) {
        mexErrMsgTxt("3 inputs required.");
    }
    
    dag = prhs[0];
    s = prhs[1];
    t = prhs[2];
    n = mxGetN(dag);
    
    /* The first input must be a sparse matrix. */
    if (mxGetM(dag) != mxGetN(dag) || mxIsSparse(dag) || !mxIsDouble(dag))
    {
        mexErrMsgTxt("Input must be a noncomplex square non-sparse matrix of type 'double'.");
    }
    
    plhs[0] = mxCreateDoubleMatrix(1,1,mxREAL);
    result = mxGetPr(plhs[0]);
    
    isReachable(mxGetPr(dag), result, (int) *mxGetPr(s)-1, (int) *mxGetPr(t)-1, n);
}

void innerFunction(const double *dag, double *result, int *visited, int s, int i, mwSize n) {
    int cur;
    
    // Visit
    visited[i] = 1;
    
    // Now visit each parent
    for(cur = 0; cur < n; ++cur) {
        if(!visited[cur] && *(dag + i * n + cur) == 1) {
            if(s == cur) {
                *result = 1;
                return;
            }
            innerFunction(dag, result, visited, s, cur, n);
        }
    }
}

void isReachable(const double *dag, double *result, int s, int t, int n)
{
    int *visited;
        
    // Create visited matrix
    visited = malloc(n * sizeof(int));
    memset(visited,0,n*sizeof(int));
    
    *result = 0;
    innerFunction(dag, result, visited, s, t, n);
    
    free(visited);
}

