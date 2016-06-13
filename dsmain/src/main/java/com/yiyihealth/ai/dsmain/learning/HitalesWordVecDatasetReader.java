package com.yiyihealth.ai.dsmain.learning;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

import org.sgdtk.ArrayDouble;
import org.sgdtk.DenseVectorN;
import org.sgdtk.FeatureVector;
import org.sgdtk.io.DatasetReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HitalesWordVecDatasetReader implements DatasetReader {
	
	private static final Logger log = LoggerFactory.getLogger(HitalesWordVecDatasetReader.class);
	
	BufferedReader reader;
	private int lineNumber = 0;
	private long embeddingSize;
	
	private boolean loadComment = false;
	
	private Hashtable<FeatureVector, String> comments = new Hashtable<FeatureVector, String>();
	
	public HitalesWordVecDatasetReader(boolean loadComment) {
		this.loadComment = loadComment;
	}

	@Override
	public FeatureVector next() throws IOException {
		String line = reader.readLine();
		String lastComment = "";
		while (line != null) {
			line = line.trim();
			if(line.startsWith("//")){
				lastComment = line;
			}
			if (line.length() > 0 && !line.startsWith("//")) {
				//解析一行
				StringTokenizer tokenizer = new StringTokenizer(line, ", \t");
				int tokenSize = tokenizer.countTokens();
				if (tokenSize >= 2) {
					float label = Float.parseFloat(tokenizer.nextToken());
					int[] featureValues = new int[tokenSize - 1];
					int counter = 0;
					while (tokenizer.hasMoreElements()) {
						String strFValue = tokenizer.nextToken();
						int v = Integer.parseInt(strFValue);
						featureValues[counter++] = v;
					}
					if (counter != featureValues.length) {
						throw new RuntimeException("特征解析后发现数目不对！");
					}
					DenseVectorN x = new DenseVectorN(counter);
					ArrayDouble xArray = x.getX();
					for (int i = 0; i < featureValues.length; i++) {
						xArray.addi(i, featureValues[i]);
					}
					if (embeddingSize == 0) {
						embeddingSize = counter;
					}
					lineNumber++;
					FeatureVector featureVector = new FeatureVector(label, x);
					featureVector.getX().organize();
					if (loadComment) {
						comments.put(featureVector, lastComment);
					}
					lastComment = "";
					return featureVector;
				} else {
					throw new RuntimeException("行数据错误, 特征值数目必需>0");
				}
			}
			line = reader.readLine();
		}
		return null;
	}
	
	public Hashtable<FeatureVector, String> getComments() {
		return comments;
	}

    @Override
    public int getLargestVectorSeen()
    {
        return (int) embeddingSize;
    }

	@Override
	public List<FeatureVector> load(File... file) throws IOException {
		open(file);
		List<FeatureVector> fvs = new ArrayList<FeatureVector>();

        FeatureVector fv;

        while ((fv = next()) != null)
        {
            fvs.add(fv);
        }

        close();
        // Read a line in, and then hash it into the bin vector
        return fvs;
	}
	
	public int getLineNumber() {
		return lineNumber;
	}

	@Override
	public void open(File... file) throws IOException {
		lineNumber = 0;
        reader = new BufferedReader(new FileReader(file[0]));
	}

	@Override
	public void close() throws IOException {
		if (reader != null) {
			try {
				reader.close();
			} catch (Exception e) {
				log.error("error when close reader!", e);
			}
			reader = null;
		}
	}

}
