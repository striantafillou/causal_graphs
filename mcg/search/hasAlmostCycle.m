function res = hasAlmostCycle(pag, closure)
res = any(any((closure | closure') & pag == 2 & pag' == 2));
end
