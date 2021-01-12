% script to fnd the prob. of type I and II errors in case I of Simulation 1.

% script to test how the p values change with the
% dimensionality of the conditional set

T = 200; % or 400
It_max = 100;  % you may change the total number of replications... 5000...
Width = 0; % automatically determines the kernel width depending on the sample size

% Independent case: type I error
for iter = 1:It_max
    if ~mod(iter,50)
        fprintf('%d replications in finding the type I errors in Case I...\n', iter),
    end
    x = normrnd(0,1,T,1);
    y = normrnd(0,1,T,1);
    z = normrnd(0,1,T,1);
    zz1 = .7 * ( (z.^3)/5 + z/2);
    x = zz1 + tanh(x);
    x = x + (x.^3)/3 + tanh(x/3)/2;
    zz2 = (z.^3/4 + z)/3;
    y = y + zz2;
    y = y + tanh(y/3);
    x = x - mean(x); x = x/std(x);
    y = y - mean(y); y = y/std(y);
    z = z - mean(z); z = z/std(z);
    
    
    
    % testing...
    % conditioal on one variable
    [tmp1, tmp2, tmp3, tmp4, tmp5] =...
        CInd_test_new_withGP(x, y, [z], 0.01, Width);
    input.ds.data = [x y z];
    input.folds = kFoldPlain(T, 2); % 2-fold cross validation
    [pval_1_pcit(iter), statsPCIT(iter)] = pcit(1,2,3, input, 'dataset');
    Sta_1(iter) = tmp1; Cri_1(iter) = tmp2; p_val_1(iter) = tmp3; Cri_appr_1(iter) = tmp4;
    p_appr_1(iter) = tmp5;
    % on two variables
    %     %% this is for the non-spurious Z case
    %     z2 = normrnd(0,1,T,1);
    %     x = normrnd(0,1,T,1);
    %     y = normrnd(0,1,T,1);
    %     zz1_2 = zz1/2 + z2;
    %     zz1_2 = zz1_2/2 + .7 * tanh(zz1_2);
    %     x = zz1_2 + tanh(x);
    %     x = x + (x.^3)/3 + tanh(x/3)/2;
    %     zz2_2 = zz2/2 + z2;
    %     zz2_2 = zz2_2/2 + .7 * tanh(zz2_2);
    %     y = y + zz2_2;
    %     y = y + tanh(y/3);
    %     x = x - mean(x); x = x/std(x);
    %     y = y - mean(y); y = y/std(y);
    %     z2 = z2 - mean(z2); z2 = z2/std(z2); z = [z z2];
    
    rD =normrnd(0,1,T,1);
    [tmp1, tmp2, tmp3, tmp4, tmp5] =...
        CInd_test_new_withGP(x, y, [z rD], 0.01, Width);
    input.ds.data =[x y z rD];
    [pval_2_pcit(iter), statsPCIT(iter)] = pcit(1, 2, [3 4], input, 'dataset');
    %     [tmp1, tmp2, tmp3, tmp4, tmp5, tmp6, tmp7, tmp8, tmp9] =...
    %         CInd_test_new_withGP(x, y, [z], 0.01, Width);
    Sta_2(iter) = tmp1; Cri_2(iter) = tmp2; p_val_2(iter) = tmp3; Cri_appr_2(iter) = tmp4;
    p_appr_2(iter) = tmp5;
   
end
%
Error_bs(1) = sum(p_val_1(1:It_max)<0.01)/It_max; Error_a(1) = sum(p_appr_1(1:It_max)<0.01)/It_max;Error_pcit(1) = sum(pval_1_pcit(1:It_max)<0.01)/It_max; 
Error_bs(2) = sum(p_val_2(1:It_max)<0.01)/It_max; Error_a(2) = sum(p_appr_2(1:It_max)<0.01)/It_max;Error_pcit(2) = sum(pval_2_pcit(1:It_max)<0.01)/It_max; 

Error_bs5(1) = sum(p_val_1(1:It_max)<0.05)/It_max; Error_a5(1) = sum(p_appr_1(1:It_max)<0.05)/It_max;Error_pcit5(1) = sum(pval_1_pcit(1:It_max)<0.05)/It_max;
Error_bs5(2) = sum(p_val_2(1:It_max)<0.05)/It_max; Error_a5(2) = sum(p_appr_2(1:It_max)<0.05)/It_max;Error_pcit5(2) = sum(pval_2_pcit(1:It_max)<0.05)/It_max; 

figure, plot(Error_bs, 'ro--'), hold on, plot(Error_a, 'ko--'),hold on, plot(Error_pcit, 'go--'),
plot(Error_bs5, 'r^--'), hold on, plot(Error_a5, 'k^--'),hold on, plot(Error_pcit5, 'g^--'),

legend('Bootstrap (\alpha=0.01)','Gamma appr. (\alpha=0.01)','PCIT (\alpha=0.01)', 'Bootstrap (\alpha=0.05)','Gamma appr. (\alpha=0.05)','PCIT (\alpha=0.05)');
title(['Type I error (T=' int2str(T) ')']);
%save new_compare_Ind_200_2000_CaseI.mat
%%
% Dependent case: type II error
for iter = 1:It_max
    if ~mod(iter,50)
        fprintf('%d replications in finding the type II errors in Case I...\n', iter),
    end
    x = normrnd(0,1,T,1);
    y = normrnd(0,1,T,1);
    z = normrnd(0,1,T,1);
    zz1 = .7 * ( (z.^3)/5 + z/2);
    x = zz1 + tanh(x);
    x = x + (x.^3)/3 + tanh(x/3)/2;
    zz2 = (z.^3/4 + z)/3;
    y = y + zz2;
    y = y + tanh(y/3);
    x = x - mean(x); x = x/std(x);
    y = y - mean(y); y = y/std(y);
    z = z - mean(z); z = z/std(z);
    ff = normrnd(0,1,T,1) * 0.5;
    x = x + ff; y = y + ff;
    
    % testing...
    % conditioning on one variable
    [tmp1, tmp2, tmp3, tmp4, tmp5] =...
        CInd_test_new_withGP(x, y, [z], 0.01, Width);
    input.ds.data = [x y z];
    input.folds = kFoldPlain(T, 2); % 2-fold cross validation
    [pval_1_pcit(iter), statsPCIT(iter)] = pcit(1,2,3, input, 'dataset');
    Sta_1(iter) = tmp1; Cri_1(iter) = tmp2; p_val_1(iter) = tmp3; Cri_appr_1(iter) = tmp4;
    p_appr_1(iter) = tmp5;
    % on two variables
    %         %% this is for the non-spurious Z case
    %     z2 = normrnd(0,1,T,1);
    %     x = 1.5 * normrnd(0,1,T,1);
    %     y = normrnd(0,1,T,1);
    %     zz1_2 = zz1/2 + z2;
    %     zz1_2 = zz1_2/2 + .7 * tanh(zz1_2);
    %     x = zz1_2 + tanh(x);
    %     x = x + (x.^3)/3 + tanh(x/3)/2;
    %     zz2_2 = zz2/2 + z2;
    %     zz2_2 = zz2_2/2 + .7 * tanh(zz2_2);
    %     y = y + zz2_2;
    %     y = y + tanh(y/3);
    %     x = x - mean(x); x = x/std(x);
    %     y = y - mean(y); y = y/std(y);
    %     z2 = z2 - mean(z2); z2 = z2/std(z2); z = [z z2];
    %     x = x + ff; y = y + ff;
    rD =normrnd(0,1,T,1);
    [tmp1, tmp2, tmp3, tmp4, tmp5] =...
        CInd_test_new_withGP(x, y, [z rD], 0.01, Width);
    %     [tmp1, tmp2, tmp3, tmp4, tmp5, tmp6, tmp7, tmp8, tmp9] =...
    %         CInd_test_new_withGP(x, y, [z], 0.01, Width);
    input.ds.data =[x y z rD];
    [pval_2_pcit(iter), statsPCIT(iter)] = pcit(1, 2, [3 4], input, 'dataset');
    [pval_2_pcit(iter), statsPCIT(iter)] = pcit(1, 2, [3 4], input, 'dataset');
    Sta_2(iter) = tmp1; Cri_2(iter) = tmp2; p_val_2(iter) = tmp3; Cri_appr_2(iter) = tmp4;
    p_appr_2(iter) = tmp5;
    % on three variables
    %         %% this is for the non-spurious Z case
    %     z3 = normrnd(0,1,T,1);
    %     x = 1.5 * normrnd(0,1,T,1);
    %     y = normrnd(0,1,T,1);
    %     zz1_3 = zz1_2*2/3 + z3*5/6;
    %     zz1_3 = zz1_3/2 + .7 * tanh(zz1_3);
    %     x = zz1_3 + tanh(x);
    %     x = x + (x.^3)/3 + tanh(x/3)/2;
    %     zz2_3 = zz2_2*2/3 + z3*5/6;
    %     zz2_3 = zz2_3/2 + .7 * tanh(zz2_3);
    %     y = y + zz2_3;
    %     y = y + tanh(y/3);
    %     x = x - mean(x); x = x/std(x);
    %     y = y - mean(y); y = y/std(y);
    %     z3 = z3 - mean(z3); z3 = z3/std(z3); z = [z z3];
    %     x = x + ff; y = y + ff;
   
end

%save new_compare_Ind_200_2000_CaseI.mat
Error_bs(1) = sum(p_val_1(1:It_max)>0.01)/It_max; Error_a(1) = sum(p_appr_1(1:It_max)>0.01)/It_max;Error_pcit(1) = sum(pval_1_pcit(1:It_max)>0.01)/It_max; 
Error_bs(2) = sum(p_val_2(1:It_max)>0.01)/It_max; Error_a(2) = sum(p_appr_2(1:It_max)>0.01)/It_max;Error_pcit(2) = sum(pval_2_pcit(1:It_max)>0.01)/It_max; 

Error_bs5(1) = sum(p_val_1(1:It_max)>0.05)/It_max; Error_a5(1) = sum(p_appr_1(1:It_max)>0.05)/It_max;Error_pcit(1) = sum(pval_1_pcit(1:It_max)>0.05)/It_max; 
Error_bs5(2) = sum(p_val_2(1:It_max)>0.05)/It_max; Error_a5(2) = sum(p_appr_2(1:It_max)>0.05)/It_max;Error_pcit(2) = sum(pval_1_pcit(1:It_max)>0.05)/It_max; 

figure, plot(Error_bs, 'ro--'), hold on, plot(Error_a, 'ko--'),hold on, plot(Error_pcit, 'go--'),hold on,
plot(Error_bs5, 'r^--'), hold on, plot(Error_a5, 'k^--'),hold on, plot(Error_pcit5, 'g^--'),

legend('Bootstrap (\alpha=0.01)','Gamma appr. (\alpha=0.01)','PCIT (\alpha=0.01)', 'Bootstrap (\alpha=0.05)','Gamma appr. (\alpha=0.05)','PCIT (\alpha=0.05)');
title(['Type II error (T=' int2str(T) ')']);
save new_compare_Dep_CaseI.mat