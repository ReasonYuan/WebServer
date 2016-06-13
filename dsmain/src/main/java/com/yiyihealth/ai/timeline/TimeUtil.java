package com.yiyihealth.ai.timeline;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.yiyihealth.ds.date.timeline.TimeLine;
import com.yiyihealth.ds.esearch.fol.Atom;
import com.yiyihealth.ds.esearch.fol.InherenceEngine;
import com.yiyihealth.ds.esearch.fol.MemDB;
import com.yiyihealth.nlp.deepstruct.analysis.sslogic.SimpleEvidenceSearcher.WordPos;
import com.yiyihealth.nlp.deepstruct.dict.EWord;

public class TimeUtil {

	public static final SimpleDateFormat normalFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/**
	 * 将需要结合上下文的时间具体化
	 * @param engine
	 * @param weici 需要修改的谓词
	 * @param pos atom的params中时间的位置所在位置对应的位置
	 * @param savePos 保存时间的Eword所在的位置对应的位置
	 */
	public static void timeSpecific(InherenceEngine engine, String weici, int timeOfPos, int savePos) {

		Atom[] tagDateatoms = engine.queryAtom(weici);
		Atom[] contextDateatoms = engine.queryAtom("ContextDate");
		MemDB memDB = engine.getFolNetwork().getMemDB();

		for (int j = 0; j < tagDateatoms.length; j++) {
			// 获取时间word所在位置
			ArrayList<Object> params = tagDateatoms[j].getParams();
			String timeOfPosStr = (String) params.get(timeOfPos);

			int timePos = new Integer(timeOfPosStr);

			String dateWord = memDB.getEWord(timePos).getWord();

			int saveTimePos = Integer.parseInt((String) tagDateatoms[j].getParams().get(savePos));

			// 和取出的上下文时间对比
			boolean isContextTime = false;
			for (int k = 0; k < contextDateatoms.length; k++) {
				ArrayList<Object> contextParams = contextDateatoms[k].getParams();
				int contextTimePos = new Integer((String) contextParams.get(0));
				int refTimePos = new Integer((String) contextParams.get(1));
				if (timePos == contextTimePos) {
					long time = memDB.getEWord(refTimePos).getTimestamp();
					if (time == 0) {
						tagDate(memDB.getWords(), saveTimePos, dateWord, dateWord);
					} else {
						Date refDate = new Date(time);
						Date newDate = TimeLine.convertDate(refDate, memDB.getEWord(timePos).getWord());
						String dateString = normalFormatter.format(newDate);
						System.out.println(refDate + "   " + newDate + "   " + dateString);
						isContextTime = true;
						tagDate(memDB.getWords(), saveTimePos, dateString, dateWord);
					}
				}

			}

			if (!isContextTime) {
				long time = memDB.getEWord(timePos).getTimestamp();
				if (time == 0) {
					tagDate(memDB.getWords(), saveTimePos, dateWord, dateWord);
				} else {
					Date date = new Date(memDB.getEWord(timePos).getTimestamp());
					String dateString = EWord.normalFormatter.format(date);
					System.out.println(date + "    " + dateString);
					tagDate(memDB.getWords(), saveTimePos, dateString, dateWord);
				}

			}

		}

	}

	/**
	 * 将某个Eword 对应的时间放进去
	 * 
	 * @param wordsArr
	 * @param saveTimePos 保存时间的Eword的位置
	 * @param date 具体化时间
	 * @param dateWord 未具体化的时间
	 */
	public static void tagDate(WordPos[] wordsArr, int saveTimePos, String date, String dateWord) {
		for (int i = 0; i < wordsArr.length; i++) {
			WordPos wordPos = wordsArr[i];
			if (wordPos.pos == saveTimePos) {
				if (!wordPos.eWord.getTaggedDates().contains(dateWord)) {
					wordPos.eWord.tagDate(dateWord);
					wordPos.eWord.tagActualDate(date);
				}

				break;
			}
		}
	}

}
