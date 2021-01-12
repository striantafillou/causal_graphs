library("causaleffect")
library("igraph")
fig1 <- graph.formula(X4-+X3,X4-+X5,X3-+X1,X4-+X1,X4-+X2,X5-+X2,X1-+X3,X1-+X4,X2-+X4,X2-+X5,simplify =TRUE)
fig1 <- set.edge.attribute(graph = fig1, name = "description", index = 3:10, value = "U")
ce1 <- causal.effect(y = "X2", x = "X1", z = "X4", G = fig1, expr = TRUE)
ce1
