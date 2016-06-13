package com.yiyihealth.ai.dsmain.learning;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

import org.sgdtk.FeatureProvider;
import org.sgdtk.FeatureVector;
import org.sgdtk.exec.Train.Params;

import com.beust.jcommander.JCommander;

public class LearnMain {
	
	protected int featureVectorWidth = 0;

	private Hashtable<FeatureVector, String> comments = new Hashtable<>();

	private static final String PROJ_DIR = "../dsdata/training/drug_dataset/";

	public static void main(String[] args) {

		String[] paramsArr = { 
				"--train", PROJ_DIR + "feature_train.txt", 
				"--eval", PROJ_DIR + "feature_eval.txt",
				"--ftype", "txt", 
				"--epochs", "100", 
				"--model", PROJ_DIR + "drug_output.model",
			};
		Params params = new Params();
		JCommander jc = new JCommander(params, paramsArr);
		jc.parse();

	}
	
	public void train(Params params){
		List<FeatureVector> trainDatset = loadDataset(params.train);
		
	}

	public List<FeatureVector> loadDataset(String file) {
		try {
			FeatureProvider reader;
			long l0 = System.currentTimeMillis();
			List<FeatureVector> dataset;
			HitalesWordVecDatasetReader hitalesWordVecDatasetReader = new HitalesWordVecDatasetReader(true);
			reader = hitalesWordVecDatasetReader;
			dataset = hitalesWordVecDatasetReader.load(new File(file));
			comments = hitalesWordVecDatasetReader.getComments();
			int largest = reader.getLargestVectorSeen();
	        if (largest > featureVectorWidth)
	        {
	            featureVectorWidth = largest;
	        }
	        double elapsed = (System.currentTimeMillis() - l0) / 1000.;
	        System.out.println(file + " loaded in " + elapsed + "s");
	        return dataset;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
