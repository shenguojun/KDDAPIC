package com.sysu.shen.KDDCup13;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import weka.classifiers.Classifier;
import weka.core.*;

public class Predict {

    public static void main(String[] argv) throws Exception {

        System.out.println("Getting features for valid papers from the database");
        Instances dataTrainValidPaper = DataIO.getDatasetDB("ValidPaper");
        dataTrainValidPaper.setClassIndex(dataTrainValidPaper.numAttributes() - 1);

        Hashtable authorIDs = new Hashtable();
        for (int i = 0; i < dataTrainValidPaper.numInstances(); i++) {
            Instance inst = dataTrainValidPaper.instance(i);
            int authorID = (int) inst.value(0);
            int paperID = (int) inst.value(1);

            if (authorIDs.containsKey(authorID)) {
                String paperIDs = (String) authorIDs.get(authorID);
                paperIDs += "," + paperID;
                authorIDs.put(authorID, paperIDs);
            } else {
                authorIDs.put(authorID, paperID + "");
            }
        }

        Hashtable authorIDPaperIDProb = new Hashtable();

        Instances dataTrainValidPaperWithoutIDs = new Instances(dataTrainValidPaper);
        dataTrainValidPaperWithoutIDs.setClassIndex(dataTrainValidPaperWithoutIDs.numAttributes() - 1);
        dataTrainValidPaperWithoutIDs.deleteAttributeAt(0);
        dataTrainValidPaperWithoutIDs.deleteAttributeAt(0);

        System.out.println("Loading model...");
        Classifier cls = (Classifier) weka.core.SerializationHelper.read(Config.modelPath);

        for (int i = 0; i < dataTrainValidPaper.numInstances(); i++) {
            int authorID = (int) dataTrainValidPaper.instance(i).value(0);
            int paperID = (int) dataTrainValidPaper.instance(i).value(1);

            Instance inst = dataTrainValidPaperWithoutIDs.instance(i);
            double prob = cls.distributionForInstance(inst)[0];

            String key = authorID + "-" + paperID;
            authorIDPaperIDProb.put(key, prob);
        }

        System.out.println("Generating subm file...");

        StringBuilder sb = new StringBuilder();
        sb.append("AuthorID,PaperIDs").append(System.getProperty("line.separator"));

        Enumeration e = authorIDs.keys();
        while (e.hasMoreElements()) {

            int authorID = (Integer) (e.nextElement());
            String paperIDsStr = (String) authorIDs.get(authorID);
            String[] paperIDs = paperIDsStr.split(",");

            PaperIDScore papers[] = new PaperIDScore[paperIDs.length];
            for (int i = 0; i < paperIDs.length; i++) {
                int pID = Integer.parseInt(paperIDs[i].trim());
                String key = authorID + "-" + pID;
                double score = (Double) authorIDPaperIDProb.get(key);
                papers[i] = new PaperIDScore(pID, score);
            }
            
            Arrays.sort(papers);

            String line = authorID + ",";
            for (int i = 0; i < paperIDs.length; i++) {
                line += " " + papers[i].getID();
                System.out.print(papers[i].getScore() + " ");
            }
            
            System.out.println(" ");
            
            sb.append(line).append(System.getProperty("line.separator"));
        }

        //
        BufferedWriter writer = new BufferedWriter(new FileWriter(Config.submFilePath));
        writer.write(sb.toString());
        writer.flush();
        writer.close();
    }

    private static class PaperIDScore implements Comparable {

        public double m_Score = 0;
        public int m_ID = -1;

        public double getScore() {
            return m_Score;
        }
        
        public int getID() {
            return m_ID;
        }
        
        public PaperIDScore(int pID, double prob) {
            m_Score = prob;
            m_ID = pID;
        }

        public int compareTo(Object X) throws ClassCastException {
            if (!(X instanceof PaperIDScore)) {
                throw new ClassCastException("A PaperIDScore object expected.");
            }
            double scoreX = ((PaperIDScore) X).getScore();
            
            if (scoreX > this.getScore()) {
                return 1;
            }
            if (scoreX < this.getScore()) {
                return -1;
            }
            return 0;
        }
    }
}