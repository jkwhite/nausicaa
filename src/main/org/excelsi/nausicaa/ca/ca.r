library(caret)

ca1 <- read.table('~/testrule.tsv', header=T, sep='\t')

tr <- trainControl(method='repeatedCV',number=6,repeats=2,returnResamp='all',classProbs=T,summaryFunction=twoClassSummary)

ca1$t_f <- ifelse(ca1$t==1, 'A', 'B')

m <- train(t_f~p0+p1+p2+p3+p4+p5+p6+p7+p8, ca1, method='glm', metric='ROC', family=binomial(link='logit'), trControl=tr)

m2 <- train(t_f~p0+p1+p2+p3+p4+p5+p6+p7+p8, ca1, method='nnet', maxit=400, metric='ROC', family=binomial(link='logit'), trControl=tr, tuneGrid=expand.grid(.decay=c(0,0.01,0.02,0.08,0.1), .size=c(4,5,7,18)))
