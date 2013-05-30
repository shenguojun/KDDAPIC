package com.sysu.shen.KDDCup13;

import weka.classifiers.*;
import weka.classifiers.meta.ensembleSelection.*;
import weka.classifiers.meta.*;
import weka.core.*;
import java.util.*;
import java.io.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import weka.classifiers.Classifier;

public class AlgoPool {
    public static Classifier getRandomForest() throws Exception {
        weka.classifiers.trees.RandomForest rf = new weka.classifiers.trees.RandomForest();
        rf.setNumTrees(100);        
        return rf;
    }
    
    public static Classifier getBagging() throws Exception {
        weka.classifiers.meta.Bagging bagging = new weka.classifiers.meta.Bagging();       
        return bagging;
    }
}