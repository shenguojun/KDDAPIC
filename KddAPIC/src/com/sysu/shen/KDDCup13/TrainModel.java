package com.sysu.shen.KDDCup13;

import java.io.File;
import java.util.Random;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;

import weka.core.*;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils;

public class TrainModel {

    public static void main(String[] argv) throws Exception {

        boolean generateFeatures = true;
        Instances data = null;
        
        System.out.println("generateFeatures: " + generateFeatures);
        
        if (generateFeatures) {

            System.out.println("Getting features for confirmed papers from the database");
            Instances dataTrainConfirmed = DataIO.getDatasetDB("TrainConfirmed");
            dataTrainConfirmed.setClassIndex(dataTrainConfirmed.numAttributes() - 1);

            System.out.println("Getting features for deleted papers from the database");
            Instances dataTrainDeleted = DataIO.getDatasetDB("TrainDeleted");
            dataTrainDeleted.setClassIndex(dataTrainDeleted.numAttributes() - 1);

            data = new Instances(dataTrainConfirmed, 0);
            data.setClassIndex(data.numAttributes() - 1);

            for (int i = 0; i < dataTrainConfirmed.numInstances(); i++) {
                Instance inst = dataTrainConfirmed.instance(i);
                inst.setClassValue(0); // 0.0 stands for T
                data.add(inst);
            }
            for (int i = 0; i < dataTrainDeleted.numInstances(); i++) {
                Instance inst = dataTrainDeleted.instance(i);
                inst.setClassValue(1); // 1.0 stands for F
                data.add(inst);
            }

            data.deleteAttributeAt(0); // remove the Auhtor ID attribute       
            data.deleteAttributeAt(0); // remove the Paper ID attribute 

            data.randomize(new Random(1));
            data.setRelationName("Dataset");

            ArffSaver saver = new ArffSaver();
            saver.setInstances(data);
            saver.setFile(new File(Config.arffTrainingPath));
            saver.writeBatch();
            System.out.println("Dataset saved");

        } else {
            System.out.println("Loading dataset");
            ConverterUtils.DataSource source = new ConverterUtils.DataSource(Config.arffTrainingPath);
            data = source.getDataSet();   
            data.setClassIndex(data.numAttributes() - 1);
        }
       
        ////////////////////////////////////////////
        Classifier cls = AbstractClassifier.makeCopy(AlgoPool.getRandomForest());
        ////////////////////////////////////////////
        
        System.out.println("Building model...");
        cls.buildClassifier(data);

        System.out.println("Serializing model...");
        weka.core.SerializationHelper.write(Config.modelPath, cls);

    }
}