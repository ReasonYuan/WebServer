package com.yiyihealth.ai.dsmain.learning;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.sgdtk.ArrayDouble;
import org.sgdtk.DenseVectorN;
import org.sgdtk.FeatureProvider;
import org.sgdtk.FeatureVector;
import org.sgdtk.LinearModel;
import org.sgdtk.exec.Train;
import org.sgdtk.io.SVMLightFileFeatureProvider;

public class HitalesTrain extends Train {
	
	private Hashtable<FeatureVector, String> comments = new Hashtable<>();
	
	private static final String PROJ_DIR = "../dsdata/training/dataset1/";
	
	public static void main(String[] args) {
		String[] params = {
			"--train", PROJ_DIR + "feature_train.txt",
			"--eval", PROJ_DIR + "feature_eval.txt",
			"--ftype", "txt",
			"--epochs", "5",
			"--model", PROJ_DIR + "drug_output.model",
		};
		HitalesTrain hitalesTrain = new HitalesTrain();
		hitalesTrain.exec(params);
		
		
		//测试
		LinearModel model = (LinearModel) hitalesTrain.getModel();
		List<FeatureVector> featureVectors;
		try {
			featureVectors = hitalesTrain.load(PROJ_DIR + "feature_test.txt", "txt", 1);
//			featureVector.setY(-2);
//			double value1 = model.predict(featureVector);
//			featureVector = featureVectors.get(1);
//			double value2 = model.predict(featureVector);
//			featureVector = featureVectors.get(2);
//			double value3 = model.predict(featureVector);
//			featureVector = featureVectors.get(8);
//			double value4 = model.predict(featureVector);
			
			for (int i = 0; i < featureVectors.size(); i++) {
//				if (i > 1000) {
//					break;
//				}
				DenseVectorN denseVectorN = (DenseVectorN) featureVectors.get(i).getX();
				//denseVectorN.
				ArrayDouble weights = model.getWeights();
				for (int j = 0; j < weights.size(); j++) {
					denseVectorN.set(j, denseVectorN.getX().get(j) * weights.get(j));
				}
				double predictDotValue = model.predict(featureVectors.get(i));
				
				
				double predictDisValue = new EuclideanDistance().compute(weights.v, denseVectorN.getX().v);
				
				System.out.println(i + " predictValue: " + predictDotValue + ", " + " predictDisValue: " + predictDisValue + ", " + hitalesTrain.comments.get(featureVectors.get(i)));
			}
			
			ArrayDouble weights = model.getWeights();
			String s = "";
			for (int i = 0; i < weights.size(); i++) {
				s += (i ==0 ? "" : ", ") + weights.get(i);
			}
			System.out.println("s: " + s);
			
		} catch (IOException e) {
			//TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public List<FeatureVector> load(String file, String ftype, int ngrams) throws IOException
    {
        if (file == null)
        {
            return null;
        }
        FileType fileType = FileType.valueOf(ftype.toUpperCase());
        FeatureProvider reader;

        long l0 = System.currentTimeMillis();
        List<FeatureVector> dataset;

        if (fileType == FileType.TSV || fileType == FileType.TXT)
        {
            System.out.println("Loading 2-class TSV (-1, 1)\tContent");
            HitalesWordVecDatasetReader hitalesWordVecDatasetReader = new HitalesWordVecDatasetReader(true);
            reader = hitalesWordVecDatasetReader;
            dataset = hitalesWordVecDatasetReader.load(new File(file));
            comments = hitalesWordVecDatasetReader.getComments();
        }
        else
        {
            System.out.println("Loading SVM light file");
            SVMLightFileFeatureProvider svmLight = new SVMLightFileFeatureProvider();
            reader = svmLight;
            dataset = svmLight.load(new File(file));
        }
        int largest = reader.getLargestVectorSeen();
        if (largest > featureVectorWidth)
        {
            featureVectorWidth = largest;
        }
        double elapsed = (System.currentTimeMillis() - l0) / 1000.;
        System.out.println(file + " loaded in " + elapsed + "s");


        return dataset;
    }
	
}
