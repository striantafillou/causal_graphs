#include "mex.h"

#if MX_API_VER < 0x07030000
typedef int mwIndex;
typedef int mwSize;
#endif /* MX_API_VER */

#include <math.h>
#include <stdlib.h>
#include <string.h>

void orient(double *dag, int x, int y, int n);

void mexFunction(int nlhs, mxArray *plhs[],
        int nrhs, const mxArray *prhs[])
{
    mwSize n;
        
    /* arguments */
    const mxArray* dag;
    const mxArray* s;
    const mxArray* t;
    
    if (nrhs != 3)
    {
        mexErrMsgTxt("2 inputs required.");
    }
    
    dag = prhs[0];
    s = prhs[1];
    t = prhs[2];
    n = mxGetN(dag);
  //  mexPrintf("\n %d variables", n);
   // mexPrintf("\n %d source",(int) *mxGetPr(s)-1);
   // mexPrintf("\n %d target",(int) *mxGetPr(t)-1);
  //  mexCallMATLAB(0,NULL,1,&dag,"disp");            

    /* The first input must be a sparse matrix. */
    if (mxGetM(dag) != mxGetN(dag) || mxIsSparse(dag))
    {
        mexErrMsgTxt("Input must be a noncomplex square non-sparse matrix.");
    }
    
    orient(mxGetPr(dag), (int) *mxGetPr(s)-1, (int) *mxGetPr(t)-1, n);
    //mexCallMATLAB(0,NULL,1,&dag,"disp");       
    plhs[0] =mxDuplicateArray(prhs[0]);

  //  plhs[0] =  &dag;

}

void orient(double *dag, int x, int y, int n) {
    int cur;
    
    for(cur = 0; cur < n; ++cur) {
   //     mexPrintf("\n step %d", cur);
        if(*(dag + x * n + cur) == 0 && *(dag + y * n + cur) == 1) {
            *(dag + cur * n + y) = 2;
            *(dag + y * n + cur) = 3;
            orient(dag, y, cur, n);
        }
   }
}
