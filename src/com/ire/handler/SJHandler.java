package com.ire.handler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.ire.basic.Contributor;
import com.ire.basic.Page;
import com.ire.basic.Revision;
import com.ire.engine.Tokenizer;

public class SJHandler extends DefaultHandler {

	private Page page = null;
	private Revision revision = null;
	private Contributor contributor = null;
	private final Stack<String> stackoftags = new Stack<String>();
	public static TreeMap<String, ArrayList<String>> globalmap = new TreeMap<String, ArrayList<String>>();
	public static TreeMap<String, String> globaltreemap = new TreeMap<String, String>();
	private int pagecount = 0;
	private int filecount=0;
	private boolean first = true;
	private static boolean switcher = false;
	private String finalfile = null;
	private static int Splitsize = 25000;
	public String to = new String();
	private int totalpages=0;

	
	
	public static int getSplitsize() {
		return Splitsize;
	}

	public static void setSplitsize(int splitsize) {
		Splitsize = splitsize;
	}

	public int getFilecount() {
		return filecount;
	}

	public void setFilecount(int filecount) {
		this.filecount = filecount;
	}

	public boolean isFirst() {
		return first;
	}

	public void setFirst(boolean first) {
		this.first = first;
	}

	public int getPagecount() {
		return pagecount;
	}

	public void setPagecount(int pagecount) {
		this.pagecount = pagecount;
	}

	public String getFinalfile() {
		return finalfile;
	}

	public void setFinalfile(String finalfile) {
		this.finalfile = finalfile;
	}

	public static TreeMap<String, ArrayList<String>> getGlobalmap() {
		return globalmap;
	}

	public static void setGlobalmap(TreeMap<String, ArrayList<String>> globalmap) {
		SJHandler.globalmap = globalmap;
	}

	public static TreeMap<String, String> getGlobaltreemap() {
		return globaltreemap;
	}

	public static void setGlobaltreemap(TreeMap<String, String> globaltreemap) {
		SJHandler.globaltreemap = globaltreemap;
	}

	StringBuilder tmpString;

	@Override
	public void startDocument() throws SAXException {
		// TODO Auto-generated method stub
		super.startDocument();
		try {
			tmpString = new StringBuilder();
		} catch (Exception e) {
			
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		// System.out.println("\n"+tmpString);
		try {
			tmpString.setLength(0);
			stackoftags.push(qName);
			if (qName.equalsIgnoreCase("page")) {
				page = new Page();
			} else if (qName.equalsIgnoreCase("revision")) {
				revision = new Revision();
			} else if (qName.equalsIgnoreCase("contributor")) {
				contributor = new Contributor();
			}
		} catch (Exception e) {
			
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {

		try {
			stackoftags.pop();

			if (qName.equalsIgnoreCase("page")) {
				// System.out.println("Indexing Page:"+page.getId());
				totalpages++;
				pagecount++;
				Tokenizer token=new Tokenizer();
				TreeMap<String,ArrayList<Integer>> localmap = token.tokenize(page);
				
				int titlecount=token.getTitlecount();
				int infocount=token.getInfocount();
				int catcount=token.getCatcount();
				int linkcount=token.getLinkcount();
				int bodycount=token.getBodycount()-catcount-linkcount;
				
				for (Map.Entry<String, ArrayList<Integer>> entry : localmap.entrySet()) {
					String key = entry.getKey();
					ArrayList<Integer> alist = entry.getValue();
					int title = alist.get(1);
					int info = alist.get(2);
					//int ref = alist.get(3); 
					int cat = alist.get(4); 
					int link = alist.get(5);
					int body = alist.get(0);
					StringBuilder sb = new StringBuilder();
					sb.append(page.getId());
					sb.append("-");
					
					StringBuilder sbyte=new StringBuilder();
					sbyte.append("000");
					if (title > 0){
						sbyte.append("1");
					}else{
						sbyte.append("0");
					}
					if(info>0){
						sbyte.append("1"); 
					}else{
						sbyte.append("0");
					}
					/*if(ref>0){
						sb.append("r"+ref);
					}*/
					if(cat>0){
						sbyte.append("1");
					}else{
						sbyte.append("0");
					}
					
					if(link>0){
						sbyte.append("1");
					}else{
						sbyte.append("0");
					}
					
					if (body > 0){
						sbyte.append("1");
					}else{
						sbyte.append("0");
					}
					
					int b = Integer.parseInt(sbyte.toString(), 2);
					
					
					double weightage=(0.5*title)/titlecount+(0.3*info)/infocount+(0.2*body)/bodycount+(0.05*cat)/catcount+(0.05*link)/linkcount;
					weightage*=100000;
					int x=(int)weightage;
					//sb.append(String.format("%.02f", weightage));
					sb.append(x+"-"+b);
					ArrayList<String> val;
					if (globalmap.containsKey(key)) {
						val = globalmap.get(key);
						val.add(sb.toString());
						globalmap.put(key, val);
					} else {
						val = new ArrayList<String>();
						val.add(sb.toString());
						globalmap.put(key, val);
					}
				}
				if (pagecount == Splitsize && first) {
					String filename="/info"+filecount+".txt";
					File towrite = new File(to + filename);
					filecount++;
					System.out.println("Page count:"+totalpages+" File:"+filename);
					//finalfile = to + filename;
					FileWriter fw;
					BufferedWriter bWriter = null;

					fw = new FileWriter(towrite, false);
					bWriter = new BufferedWriter(fw);
					for (Map.Entry<String, ArrayList<String>> entry : globalmap
							.entrySet()) {
						// System.out.print(entry.getKey() + ":");
						bWriter.write(entry.getKey() + ":");
						ArrayList<String> alist = entry.getValue();
						StringBuilder sb = new StringBuilder();
						for (String s : alist) {
							sb.append(s + ",");
						}
						// System.out.println(sb.toString());
						bWriter.write(sb.toString());
						bWriter.newLine();
					}
					bWriter.close();
					fw.close();
					globalmap = new TreeMap<String, ArrayList<String>>();
					pagecount = 0;

				} else if (pagecount == Splitsize && !switcher && !first) {
					switcher = true;
					finalfile = to + "/info2.txt";
					File towrite = new File(to + "/info2.txt");
					File toread = new File(to + "/info1.txt");
					FileWriter fw;
					FileReader fr;
					BufferedWriter bw = null;
					BufferedReader br = null;

					String line;
					fw = new FileWriter(towrite, false);
					bw = new BufferedWriter(fw);
					fr = new FileReader(toread);
					br = new BufferedReader(fr);

					Collection<String> c = globalmap.keySet();
					Iterator<String> itr = c.iterator();
					String val1, val2;
					val1 = (String) itr.next();
					line = br.readLine();

					while (itr.hasNext() && line != null) {
						String ar[] = line.split(":");
						val2 = ar[0];
						int compare = val1.compareTo(val2);
						if (compare < 0) {
							ArrayList<String> alist = globalmap.get(val1);
							StringBuilder sb = new StringBuilder();
							sb.append(val1 + ":");
							for (String s : alist) {
								sb.append(s + ",");
							}
							// System.out.println(sb.toString());
							bw.write(sb.toString());
							bw.newLine();
							val1 = (String) itr.next();
						} else if (compare > 0) {
							// System.out.println(line);
							bw.write(line);
							bw.newLine();
							line = br.readLine();
						} else {
							ArrayList<String> alist = globalmap.get(val1);
							StringBuilder sb = new StringBuilder();
							sb.append(line);
							for (String s : alist) {
								sb.append(s + ",");
							}
							// System.out.println(sb.toString());
							bw.write(sb.toString());
							bw.newLine();
							val1 = (String) itr.next();
							line = br.readLine();
						}
					}

					if (!itr.hasNext() && line != null) {
						while ((line = br.readLine()) != null) {
							// System.out.println(line);
							bw.write(line);
							bw.newLine();
						}
					} else if (itr.hasNext() && line == null) {
						while (itr.hasNext()) {
							ArrayList<String> alist = globalmap.get(val1);
							StringBuilder sb = new StringBuilder();
							sb.append(val1 + ":");
							for (String s : alist) {
								sb.append(s + ",");
							}
							// System.out.println(sb.toString());
							bw.write(sb.toString());
							bw.newLine();
							val1 = (String) itr.next();
						}
					}
					bw.close();
					br.close();
					fr.close();
					fw.close();
					globalmap = new TreeMap<String, ArrayList<String>>();
					pagecount = 0;

				} else if (pagecount == Splitsize && switcher && !first) {
					switcher = false;
					finalfile = to + "/info1.txt";
					File towrite = new File(to + "/info1.txt");
					File toread = new File(to + "/info2.txt");
					FileWriter fw;
					FileReader fr;
					BufferedWriter bw = null;
					BufferedReader br = null;

					String line;
					fw = new FileWriter(towrite, false);
					bw = new BufferedWriter(fw);
					fr = new FileReader(toread);
					br = new BufferedReader(fr);
					
					Collection<String> c = globalmap.keySet();
					Iterator<String> itr = c.iterator();
					String val1, val2;
					val1 = (String) itr.next();
					line = br.readLine();

					while (itr.hasNext() && line != null) {
						String ar[] = line.split(":");
						val2 = ar[0];
						int compare = val1.compareTo(val2);
						if (compare < 0) {
							ArrayList<String> alist = globalmap.get(val1);
							StringBuilder sb = new StringBuilder();
							sb.append(val1 + ":");
							for (String s : alist) {
								sb.append(s + ",");
							}
							// System.out.println(sb.toString());
							bw.write(sb.toString());
							bw.newLine();
							val1 = (String) itr.next();
						} else if (compare > 0) {
							// System.out.println(line);
							bw.write(line);
							bw.newLine();
							line = br.readLine();
						} else {
							ArrayList<String> alist = globalmap.get(val1);
							StringBuilder sb = new StringBuilder();
							sb.append(line);
							for (String s : alist) {
								sb.append(s + ",");
							}
							// System.out.println(sb.toString());
							bw.write(sb.toString());
							bw.newLine();
							val1 = (String) itr.next();
							line = br.readLine();
						}
					}

					if (!itr.hasNext() && line != null) {
						while ((line = br.readLine()) != null) {
							// System.out.println(line);
							bw.write(line);
							bw.newLine();
						}
					} else if (itr.hasNext() && line == null) {
						while (itr.hasNext()) {
							ArrayList<String> alist = globalmap.get(val1);
							StringBuilder sb = new StringBuilder();
							sb.append(val1 + ":");
							for (String s : alist) {
								sb.append(s + ",");
							}
							// System.out.println(sb.toString());
							bw.write(sb.toString());
							bw.newLine();
							val1 = (String) itr.next();
						}
					}

					bw.close();
					br.close();
					fr.close();
					fw.close();
					globalmap = new TreeMap<String, ArrayList<String>>();
					pagecount = 0;

				}
			} else if (qName.equalsIgnoreCase("contributor")) {
				revision.setContributor(contributor);
			} else if (qName.equalsIgnoreCase("revision")) {
				page.setRevision(revision);
			} else if (qName.equalsIgnoreCase("id")) {
				String parent = stackoftags.peek();
				int tid = Integer.valueOf(tmpString.toString().trim());
				if (parent.equalsIgnoreCase("page")) {
					page.setId(tid);
				} else if (parent.equalsIgnoreCase("revision")) {
					revision.setId(tid);
				} else if (parent.equalsIgnoreCase("contributor")) {
					contributor.setId(tid);
				}
			} else if (qName.equalsIgnoreCase("text")) {
				revision.setText(tmpString.toString());
			} else if (qName.equalsIgnoreCase("title")) {
				page.setTitle(tmpString.toString());
			} else if (qName.equalsIgnoreCase("timestamp")) {
				revision.setTimestamp(tmpString.toString());
			} else if (qName.equalsIgnoreCase("username")) {
				contributor.setUsername(tmpString.toString());
			} else if (qName.equalsIgnoreCase("comment")) {
				revision.setComment(tmpString.toString());
			} else if (qName.equalsIgnoreCase("minor")) {
				revision.setMinor(tmpString.toString());
			} else if (qName.equalsIgnoreCase("file")) {
				if (finalfile == null) {
					finalfile = to + "/info1.txt";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	@Override
	public void characters(char ch[], int start, int length)
			throws SAXException {
		try {
			tmpString.append(new String(ch, start, length));
		} catch (Exception e) {
			
		}
	}
}
