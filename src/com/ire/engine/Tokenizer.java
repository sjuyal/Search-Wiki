package com.ire.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.TreeMap;


import com.ire.basic.Page;

public class Tokenizer {

	private static String stopwords[] = { "a", "b", "c", "d", "e", "f", "g",
			"h", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u",
			"v", "w", "x", "y", "z", "able", "about", "across", "after", "all",
			"almost", "also", "am", "among", "another", "an", "and", "any",
			"are", "as", "at", "be", "because", "been", "but", "by", "can",
			"cannot", "could", "dear", "did", "do", "does", "either", "else",
			"ever", "every", "for", "from", "get", "got", "had", "has", "have",
			"he", "her", "hers", "him", "his", "how", "however", "i", "if",
			"in", "into", "is", "it", "its", "just", "least", "let", "like",
			"likely", "may", "me", "might", "most", "must", "my", "neither",
			"no", "nor", "not", "now", "of", "off", "often", "on", "only",
			"or", "other", "our", "own", "put", "rather", "said", "say",
			"says", "she", "should", "since", "so", "some", "take", "than",
			"that", "the", "their", "them", "then", "there", "these", "they",
			"this", "tis", "to", "too", "twas", "us", "wants", "was", "we",
			"were", "what", "when", "where", "which", "while", "who", "whom",
			"why", "will", "with", "would", "yet", "you", "your" ,"http","cite","www","com"};

	private static HashSet<String> stop = new HashSet<String>();

	static {
		for (String str : stopwords) {
			stop.add(str);
		}

	}

	private Stack<String> stack = new Stack<String>();

	private HashMap<String, ArrayList<Integer>> map = new HashMap<String, ArrayList<Integer>>();
	private TreeMap<String, ArrayList<Integer>> treeMap = new TreeMap<String, ArrayList<Integer>>();

	private int titlecount=1;
	private int bodycount=1;
	private int infocount=1;
	private int catcount=1;
	private int linkcount=1;

	public int getTitlecount() {
		return titlecount;
	}

	public void setTitlecount(int titlecount) {
		this.titlecount = titlecount;
	}

	public int getBodycount() {
		return bodycount;
	}

	public void setBodycount(int bodycount) {
		this.bodycount = bodycount;
	}

	public int getInfocount() {
		return infocount;
	}

	public void setInfocount(int infocount) {
		this.infocount = infocount;
	}

	public int getCatcount() {
		return catcount;
	}

	public void setCatcount(int catcount) {
		this.catcount = catcount;
	}

	public int getLinkcount() {
		return linkcount;
	}

	public void setLinkcount(int linkcount) {
		this.linkcount = linkcount;
	}
	
	// Body-0,Title-1,Info-2,Ref-3,Category-4,Link-5

	public TreeMap<String, ArrayList<Integer>> tokenize(Page page) {
		try {

			long pageid = page.getId();

			// Indexing Title in the Page
			indexer(page.getTitle(), pageid, 1);

			// Indexing Body in the Page
			String text = page.getRevision().getText();
			StringBuilder sb = new StringBuilder();
			sb.append(text);

			char[] txtArray = text.toCharArray();
			int i = 0, j = 0;
			int flag = 0;
			int txlen = txtArray.length;
			for (j = 0; j < txlen - 9; j++) {
				String chk = new String(txtArray, j, 9);
				if (chk.equalsIgnoreCase("{{Infobox")) {
					stack.push("{{");
					for (i = j + 2; i < txlen; i++) {
						if (txtArray[i] == '{') {
							if (txtArray[i + 1] == '{') {
								stack.push("{{");
								i++;
							}
						} else if (txtArray[i] == '}') {
							if (txtArray[i + 1] == '}') {
								if (stack.size() == 1) {
									i++;
									break;
								}
								stack.pop();
								i++;
							}
						}
					}
					flag = 1;
					break;
				}
			}

			String resbody;
			if (flag == 1) {
				String infobox = new String(txtArray, j, i - j);
				// Indexing Infobox of a page
				indexer(infobox, pageid, 2);
				resbody = new String(txtArray, i + 1, txlen - infobox.length()
						- 1 - j);
			} else {
				resbody = text;
			}

			// String references = findRef(resbody);
			// Indexing references in the body
			// indexer(references, pageid, 3);

			String categories = findCategory(resbody);
			// Indexing category in the body
			indexer(categories, pageid, 4);

			String external = findLinks(resbody);
			// Indexing external links in the body
			indexer(external, pageid, 5);

			// Indexing rest body apart from infobox of a page
			indexer(resbody, pageid, 0);
			// bWriter.write(key + ":" + pageid + "-b" + val);

			treeMap.putAll(map);

			for (Map.Entry<String, ArrayList<Integer>> entry : treeMap
					.entrySet()) {
				ArrayList<Integer> alist = entry.getValue();

				String key = entry.getKey();
				// int title = alist.get(1);
				// int info = alist.get(2);
				// int ref = alist.get(3);
				int cat = alist.get(4);
				int link = alist.get(5);
				int body = alist.get(0) - cat - link;
				alist.set(0, body);
				treeMap.put(key, alist);
			}
			// System.out.println(entry.getKey() + "-b" + body + ":t" +
			// title + ":i" + info + ":r" + ref + ":c" + cat + ":l" + link);

			return treeMap;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return treeMap;

	}

	public String findCategory(String resbody) {
		int reslen = resbody.length();
		char[] resarr = resbody.toCharArray();
		String catbuf = "";
		for (int i = 0; i < reslen - 10; i++) {
			String chk = new String(resarr, i, 10);
			if (chk.equalsIgnoreCase("[[Category")) {
				i = i + 10;
				for (int j = i; j < reslen - 2; j++) {
					if (resarr[j] == ']' && resarr[j + 1] == ']') {
						// System.out.println(new String(resarr,i,j-i));
						catbuf += new String(resarr, i, j - i);
						i = j;
						break;
					}
				}
			}
		}
		return catbuf;
	}

	public String findLinks(String resbody) {
		int reslen = resbody.length();
		char[] resarr = resbody.toCharArray();
		String linkbuf = "";
		int i = 0, flag = 0;
		for (i = 0; i < reslen - 18; i++) {
			String chk = new String(resarr, i, 18);
			if (chk.equalsIgnoreCase("==External links==")) {
				flag = 1;
				i = i + 18;
				break;
			}
		}
		if (flag == 1) {
			for (int j = i; j < reslen - 2; j++) {
				if ((resarr[j] == '*' && resarr[j + 1] == ' ' && resarr[j + 2] == '[')
						|| (resarr[j] == '*' && resarr[j + 1] == '[')) {
					for (int k = j + 1; k < reslen; k++) {
						if (resarr[k] == ']') {
							linkbuf += new String(resarr, j + 1, k - j);
							j = k;
							break;
						}
					}
				}
			}
		}
		return linkbuf;
	}

	public String findRef(String resbody) {
		int reslen = resbody.length();
		char[] resarr = resbody.toCharArray();
		stack.clear();
		String refbuf = "";
		for (int i = 0; i < reslen - 5; i++) {
			String chk = new String(resarr, i, 5);
			if (chk.equalsIgnoreCase("<ref>") || chk.equalsIgnoreCase("<ref ")) {
				i = i + 5;
				stack.push(i + "");
			}
			if (chk.equalsIgnoreCase("</ref")) {
				if (!stack.empty()) {
					int start = Integer.parseInt(stack.pop());
					refbuf += new String(resarr, start, i - start);
				}
			}
		}
		// System.out.println(refbuf);
		return refbuf;
	}

	public void indexer(String data, long pageid, int type) {

		try {

			StringBuilder sb = new StringBuilder();
			sb.append(data);
			for (int i = 0; i < data.length(); i++) {
				char c = sb.charAt(i);
				if (c < 0x30 || (c >= 0x3a && c <= 0x40)
						|| (c > 0x5a && c <= 0x60) || c > 0x7a)
					sb.setCharAt(i, ' ');
			}

			StringTokenizer st = new StringTokenizer(sb.toString());
			while (st.hasMoreTokens()) {
				String s = st.nextToken();
				s = s.toLowerCase();
				boolean found = false;
				for (char c : s.toCharArray()) {
					if (Character.isDigit(c)) {
						found = true;
						break;
					}
				}

				if (found) {
					continue;
				}

				if (stop.contains(s)) {
					continue;
				}

				// Porter 2 Stemming Algorithm
				/*
				 * Class stemClass = Class
				 * .forName("org.tartarus.snowball.ext.englishStemmer");
				 * SnowballStemmer stemmer = (SnowballStemmer) stemClass
				 * .newInstance();
				 * 
				 * stemmer.setCurrent(s); stemmer.stem(); s =
				 * stemmer.getCurrent();
				 */
				
				// Porter Stemming Algorithm
				Stemmer stemmer = new Stemmer();
				char[] cArray = s.toCharArray();
				stemmer.add(cArray, s.length());
				stemmer.stem();
				s = stemmer.toString();

				if(type==0){
					bodycount++;
				}else if(type==1){
					titlecount++;
				}else if(type==2){
					infocount++;
				}else if(type==4){
					catcount++;
				}else if(type==5){
					linkcount++;
				}
				
				if (map.containsKey(s)) {
					ArrayList<Integer> alist = map.get(s);
					int val = alist.get(type);
					alist.set(type, val + 1);
					map.put(s, alist);

				} else {
					ArrayList<Integer> alist = new ArrayList<Integer>(
							Collections.nCopies(6, 0));
					alist.set(type, 1);
					map.put(s, alist);
				}

			}

		} catch (Exception e) {
				e.printStackTrace();
		}

	}
}
