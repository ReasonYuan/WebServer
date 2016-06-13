package com.yiyihealth.ds.esearch.fol;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Load evidences, predicates, and query
 * @author qiangpeng
 *
 * @param <T>
 * @param <M>
 */
public class LogicLoader<T extends LogicParser<M>, M> {
	
	private T parser;
	
	public LogicLoader(T parser) {
		this.parser = parser;
	}

	public ArrayList<M> loadLogics(String file) {
		ArrayList<M> evidences = new ArrayList<M>();
		ArrayList<String> evidencesText = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(
					new InputStreamReader(new BufferedInputStream(new FileInputStream(file))));
			String line = br.readLine();
			ArrayList<String> lines = new ArrayList<String>();
			while (line != null) {
				line = line.trim();
				if (line.startsWith("＃") || line.startsWith("#") || line.startsWith("//") || line.equals("")) {
					line = br.readLine();
					continue;
				}
				if (!line.equals("")) {
					lines.add(line);
				}
				line = br.readLine();
			}
			br.close();
			String realLine = "";
			for (int i = 0; i < lines.size(); i++) {
				String oneLine = lines.get(i);
				if (oneLine.endsWith(")")) {
					realLine += oneLine;
					evidencesText.add(realLine);
					realLine = "";
				} else {
					realLine += oneLine;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (String str : evidencesText) {
			M evidence = parser.parse(str);
			evidences.add(evidence);
		}
		return evidences;
	}

}
