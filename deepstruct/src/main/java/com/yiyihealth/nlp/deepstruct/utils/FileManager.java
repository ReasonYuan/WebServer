package com.yiyihealth.nlp.deepstruct.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;

import org.codehaus.plexus.util.FileUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yiyihealth.nlp.deepstruct.dict.EWord;
import com.yiyihealth.nlp.deepstruct.dict.Punctuation;
import com.yiyihealth.nlp.deepstruct.dict.WordNatures;

public class FileManager {

	public static interface OnRecordLoadListener {
		public void onRecordLoad(String filename, ArrayList<EWord> words);
	}

	/**
	 * @param withPuncuation
	 *            规则是否包含标点符号
	 * @return
	 */
	public void loadWords(String recordsFolder, OnRecordLoadListener onRecordLoadListener) throws Exception {
		loadWords(recordsFolder, onRecordLoadListener, -1);
	}

	/**
	 * @param withPuncuation
	 *            规则是否包含标点符号
	 * @return
	 */
	public void loadWords(String recordsFolder, OnRecordLoadListener onRecordLoadListener, int size) throws Exception {
		File dir = new File(recordsFolder);
		String[] files = dir.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".json");
			}
		});
		// Hashtable<String, Integer> prePoss = new Hashtable<String,
		// Integer>();
		// Hashtable<String, Integer> nextPoss = new Hashtable<String,
		// Integer>();
		// EWord preWord = null;
		// 读取数据
		// ArrayList<ArrayList<EWord>> samples = new
		// ArrayList<ArrayList<EWord>>();
		for (int i = 0; i < files.length; i++) {
			String text = FileUtils.fileRead(recordsFolder + "/" + files[i]);
			JSONObject jsonObject = JSONObject.parseObject(text);
			ArrayList<EWord> words = new ArrayList<EWord>();// jsonObject.getJSONArray("data");
			// samples.add(words);
			JSONArray jsonArray = jsonObject.getJSONArray("data");
			int wordSize = jsonArray.size();
			for (int j = 0; j < wordSize; j++) {
				JSONObject word = jsonArray.getJSONObject(j);
				EWord eWord = new EWord(word.getString("word"), word.getString("natureFull"));
				JSONObject attributes = word.getJSONObject("attributes");
				if (attributes != null) {
					for (String key : attributes.keySet()) {
						eWord.putAttribute(key, attributes.getString(key));
					}
				}

				// TODO 这里withPuncuation无效了，目前仅双引号不要
				// if (!withPuncuation &&
				// eWord.getNature().equals(WordNatures.getNatureFullByName(WordNatures.PUNC))
				// && !eWord.getWord().equals(Punctuation.STR_FULLSTOP)) {
				// continue;
				// }
				if (eWord.getNature().equals(WordNatures.PUNC) && (eWord.getWord().equals(Punctuation.QUOTES_C_L)
						|| eWord.getWord().equals(Punctuation.QUOTES_C_R)
						|| eWord.getWord().equals(Punctuation.QUOTES_E))) {
					continue;
				}

				words.add(eWord);
				// if (preWord != null) {
				// String keyPre = preWord.getNature() + "_" +
				// eWord.getNature();
				// String keyNext = eWord.getNature() + "_" +
				// preWord.getNature();
				// Integer preCnt = prePoss.get(keyPre);
				// Integer nextCnt = prePoss.get(keyNext);
				// if (preCnt == null) {
				// prePoss.put(keyPre, 1);
				// } else {
				// prePoss.put(keyPre, preCnt + 1);
				// }
				// if (nextCnt == null) {
				// nextPoss.put(keyNext, 1);
				// } else {
				// nextPoss.put(keyNext, nextCnt + 1);
				// }
				// }
				// preWord = eWord;
			}
			if (size != -1 && (i + 1) >= size) {
				// 只load限定数目的记录
				break;
			}
			onRecordLoadListener.onRecordLoad(files[i], words);
		}
		// System.out.println("pre_poss cnt: " + prePoss.keySet().size());
		// System.out.println("next_poss cnt: " + nextPoss.keySet().size());
		// return samples;
	}

	/**
	 * Writes a string to a file, using UTF-8 encoding.
	 */
	public static void writeToFile(String filename, String content) {

		BufferedWriter bufferedWriter = null;
		try {
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF8"));
			bufferedWriter.write(content);
			bufferedWriter.flush();
			bufferedWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void writeObject(String file, Serializable object) throws FileNotFoundException, IOException {
		int index = file.lastIndexOf("/");
		String path = file.substring(0, index);
		File mFile = new File(path);
		if (!mFile.exists()) {
			mFile.mkdirs();
		}
		File file2 = new File(file);
		if (!file2.exists()) {
			file2.createNewFile();
		}
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(new File(file)));
		try {
			objectOutputStream.writeObject(object);
			objectOutputStream.flush();
		} finally {
			objectOutputStream.close();
		}
	}

	public static Object readObject(String file) throws IOException, ClassNotFoundException {
		ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(new File(file)));
		try {
			return objectInputStream.readObject();
		} finally {
			objectInputStream.close();
		}
	}

	/**
	 * Reads lines from a text file.
	 */
	public static ArrayList<String> getLines(String filename) {
		try {
			FileReader reader = new FileReader(filename);
			BufferedReader lreader = new BufferedReader(reader);
			String line = lreader.readLine();
			ArrayList<String> lines = new ArrayList<String>();
			while (line != null) {
				lines.add(line);
				line = lreader.readLine();
			}
			lreader.close();
			return lines;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String BufferedReaderLargeFile(String path) throws IOException {
		File file = new File(path);
		if (!file.exists() || file.isDirectory())
			throw new FileNotFoundException();
		BufferedReader br = new BufferedReader(new FileReader(file));
		String temp = null;
		StringBuffer sb = new StringBuffer();
		temp = br.readLine();
		while (temp != null) {
			sb.append(temp + " ");
			temp = br.readLine();
		}
		br.close();
		return sb.toString();
	}

	public static ArrayList<String> readLargeFileToLines(String path) throws IOException {
		ArrayList<String> results = new ArrayList<String>();
		File file = new File(path);
		if (!file.exists() || file.isDirectory())
			throw new FileNotFoundException();
		BufferedReader br = new BufferedReader(new FileReader(file));
		String temp = null;
		temp = br.readLine();
		while (temp != null) {
			String line = temp.trim();
			if (line.length() > 0) {
				results.add(temp.trim());
			}
			temp = br.readLine();
		}
		br.close();
		return results;
	}
}
