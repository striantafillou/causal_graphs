#include "mex.h"

#if MX_API_VER < 0x07030000
typedef int mwIndex;
typedef int mwSize;
#endif /* MX_API_VER */

#include <math.h>
#include <stdlib.h>
#include <string.h>

void isReachable(const mxArray *dag, double *result, int s, int t);

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
    if (mxGetM(dag) != mxGetN(dag) || !mxIsSparse(dag))
    {
        mexErrMsgTxt("Input must be a noncomplex square sparse matrix.");
    }
    
    plhs[0] = mxCreateDoubleMatrix(1,1,mxREAL);
    result = mxGetPr(plhs[0]);
    
    isReachable(dag, result, (int) *mxGetPr(s)-1, (int) *mxGetPr(t)-1);
}

void innerFunction(const mxArray *dag, double *result, int *visited, int s, int i, mwIndex *ir, mwIndex *jc, mwSize n) {
    mwIndex   starting_row_index, stopping_row_index, current_row_index;
    mwSize cur;
    
    // Visit
    visited[i] = 1;
    
    // Now visit each parent
    starting_row_index = jc[i];
    stopping_row_index = jc[i+1];
    for(current_row_index = starting_row_index; current_row_index < stopping_row_index; ++current_row_index) {
        cur = ir[current_row_index];
        
        if(cur == s){
            *result = 1;
            return;
        }

        if(!visited[cur]) {
            innerFunction(dag, result, visited, s, cur, ir, jc, n);
        }
    }
}

void isReachable(const mxArray *dag, double *result, int s, int t)
{
    mwIndex *ir, *jc;
    mwSize n;
    int *visited;
    
    n = mxGetN(dag);
    ir = mxGetIr(dag);
    jc = mxGetJc(dag);
    
    // Create visited matrix
    visited = malloc(n * sizeof(int));
    memset(visited,0,n*sizeof(int));
    
    *result = 0;
    innerFunction(dag, result, visited, s, t, ir, jc, n);
    
    free(visited);
}

