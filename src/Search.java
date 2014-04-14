import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import com.ire.engine.Stemmer;

public class Search {

	public static void find() {

	}
	static String to = "/media/shashank/Windows/Wikipedia/";
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			// String to = args[0];
			// String search = args[1];

			
			String query = "sachin";

			long time1 = System.currentTimeMillis();
			// 1-Title 2-Body 3-Infobox 4-Link 5-Category
			
			String searchvalues[] = query.split(" ");
			String[] s=new String[100];
			int wcount=0;
			for (String sea : searchvalues) {
				int type=0;
				String search;
				String cat[]=sea.split(":");
				if(cat.length==2){
					if(cat[0].equalsIgnoreCase("t"))
						type=1;
					else if(cat[0].equalsIgnoreCase("b"))
						type=2;
					else if(cat[0].equalsIgnoreCase("i"))
						type=3;
					else if(cat[0].equalsIgnoreCase("l"))
						type=4;
					else if(cat[0].equalsIgnoreCase("c"))
						type=5;
					search=cat[1];
				}else{
					search=cat[0];
				}
				
				StringBuilder sb = new StringBuilder();
				sb.append(search);
				StringBuilder sb2 = new StringBuilder();
				for (int i = 0; i < search.length(); i++) {
					char c = sb.charAt(i);
					if (c < 0x30 || (c >= 0x3a && c <= 0x40)
							|| (c > 0x5a && c <= 0x60) || c > 0x7a)
						sb.setCharAt(i, ' ');
					else
						sb2.append(c);
				}
				search = sb2.toString();
				search = search.trim();
				Stemmer stemmer = new Stemmer();
				search = search.toLowerCase();
				char[] cArray = search.toCharArray();
				stemmer.add(cArray, search.length());
				stemmer.stem();
				search = stemmer.toString();

				//System.out.println(search);

				char ch = search.charAt(0);
				File toread = new File(to + "\\" + ch + ".txt");
				//System.out.println(to + "/" + search.charAt(0) + ".txt");
				FileReader fr = new FileReader(toread);
				BufferedReader br = new BufferedReader(fr);

				s[wcount] = binarySearch(search, toread,type);
				//System.out.println(s[wcount]);
				wcount++;
				br.close();
				fr.close();
			}
			
			LinkedHashSet<String> lhs = new LinkedHashSet<String>();
			
			String set1[]=s[0].split(",");
			TreeMap<Integer, Integer> tmap=new TreeMap<Integer,Integer>();
			//System.out.println(set1.length);
			tmap.put(set1.length, 0);
			for(String s2:set1){
				lhs.add(s2);
			}
			for(int i=1;i<wcount;i++){
				LinkedHashSet<String> lhs2 = new LinkedHashSet<String>();
				set1=s[i].split(",");
				tmap.put(set1.length, i);
				
				for(String s1:set1){
					lhs2.add(s1);
				}
				lhs.retainAll(lhs2);
			}
			
			int count=0;
			TreeSet<String> answerset=new TreeSet<String>();
			for(String ans:lhs){
				
				//System.out.print(ans+",");
				String result=searchtitle(ans);
				System.out.print(result);
				if(!(result.equalsIgnoreCase(""))){
					count++;
					System.out.println();
				}
				answerset.add(ans);
				count++;
				if(count==10)
					break;
			}
			//System.out.println("Here"+wcount);
			if(count<10){
					for(Map.Entry<Integer,Integer> entry : tmap.entrySet()) {
					  
						int index=entry.getValue();
						String arr[]=s[index].split(",");
						for(String rem:arr){
							if(!answerset.contains(rem)){
								//System.out.print(rem+",");
								String result=searchtitle(rem);
								System.out.print(result);
								if(!(result.equalsIgnoreCase(""))){
									count++;
									System.out.println();
								}
								answerset.add(rem);
							}
							if(count==10)
								break;
						}
						if(count==10)
							break;
					}
			}
			
			
			long time2 = System.currentTimeMillis() - time1;
			System.out.println("\n\nTotal time taken:" + time2 / 1000.0);
		} catch (Exception e) {
			// e.printStackTrace();
		}

	}
	
	private static String searchtitle(String ans) throws IOException {
		// TODO Auto-generated method stub
		
		File f=new File(to+"\\title.txt");
		RandomAccessFile raf = new RandomAccessFile(f, "r");

		long start=0;
		long end=f.length();
		long middle;
		
		while(start<end){
			middle=(start+end)/2;
			raf.seek(middle);
			raf.readLine();
			String line=raf.readLine();
			String tok[]=line.split(":");
			if(tok[0].equalsIgnoreCase(ans)){
				return tok[1];
			}else{
				if(tok[0].compareTo(ans)>0){
					end=middle-1;
				}else{
					start=middle+1;
				}
			}
		}
		
		return "";
	}

	public static boolean checkField(String val,int type){
		int field=Integer.parseInt(val);
		int newval=0;
		int flag=0;
		switch(type){
		case 1:
			newval=1<<4;
			newval=newval&field;
			if(newval==16)
				flag=1;
			break;
		case 2:
			newval=1;
			newval=newval&field;
			if(newval==1)
				flag=1;
			break;
		case 3:
			newval=1<<3;
			newval=newval&field;
			if(newval==8)
				flag=1;
			break;
		case 4:
			newval=1<<1;
			newval=newval&field;
			if(newval==2)
				flag=1;
			break;
		case 5:
			newval=1<<2;
			newval=newval&field;
			if(newval==4)
				flag=1;
			break;
		}
		
		if(flag==0)
			return false;
		else
			return true;
	}

	public static String binarySearch(String s, File to,int type) {
		try {
			RandomAccessFile raf = new RandomAccessFile(to, "r");
			String searchValue = s;

			StringBuilder res = new StringBuilder();
			long bottom = 0;
			long top = to.length();
			long middle;
			int flag=0;
			while (bottom <= top) {
				middle = (bottom + top) / 2;
				raf.seek(middle);

				//middle += (str.length()/2);
				//System.out.println(str);
				String nline = raf.readLine();
				nline=raf.readLine();
				String toks[];
				if(nline!=null)
					toks = nline.split(":");
				else
					continue;
				int comparison = toks[0].compareTo(searchValue);
				//if(toks[0].equalsIgnoreCase("actric")){
				//	break;
				//}
				//System.out.println("Low:"+bottom+" Mid:"+middle+" High:"+top+" Val:"+toks[0]);
				
				if (comparison == 0) {
					String vals[] = toks[1].split("#");
					for (int i = 0; i < vals.length; i++) {
						String rankdocspair[] = vals[i].split("/");
						String docs[] = rankdocspair[1].split(",");
						for (String doc : docs) {
							String docid[] = doc.split("-");
							boolean ret=false;
							if(type!=0){
								ret=checkField(docid[1],type);
								if(ret)
									res.append(docid[0] +",");
							}
							else
								res.append(docid[0] +",");
						}
					}
					flag=1;
					break;
				} else if (comparison < 0) {
					// line comes before searchValue
					bottom = middle+1 ;
				} else {
					top = middle-1 ;
				}
			}

			raf.close();
			if(flag==0)
				return "";
			return res.toString();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}

}