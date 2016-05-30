import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ClassifBayes{

	static final int SPAM = 0;
	static final int HAM = 1;
	List<String> dic;
	int[][] occurences;
	int[] nb_mess;

	public ClassifBayes(List<String> dictionnary){
		dic = dictionnary;
		occurences = new int[dic.size()][2];//tout est init à 0
		nb_mess = new int[2];
	}


	/**
	 * 
	 * @param word: le mot à ajouter
	 * @param type: HAM ou SPAM
	 */
	/*	private void addOccurence(String word, int type){
		if(dic.contains(word)){
			occurences[dic.indexOf(word)][type]++;
		}
	}
	 */

	public void apprentissage(String appDir){
		int nbHam = 10000;
		int nbSpam = 10000;

		String[] dirs = new String[2];
		dirs[SPAM] = "spam";
		dirs[HAM] = "ham";
		int[] max = new int[2];
		max[SPAM] = nbSpam;
		max[HAM] = nbHam;

		for(int type = 0 ; type < 2 ; type++){
			for(FileInputStream file : listFilesForFolder(appDir + File.separator + dirs[type], max[type])){
				for(String s : lire_message(file) ){
					if(dic.contains(s)){
						occurences[ dic.indexOf(s) ][ type ] ++;
					}						
				}
				nb_mess[type]++;
			}
		}
	}


	private int test(FileInputStream f){
		double[] proba = new double[2];
		List<String> vec_file = lire_message(f);
		for(String word : dic){
			for(int type = 0 ; type < 2 ; type++){
				if(type == HAM && dic.contains(word)){
				//	System.out.println(word);
				}
				proba[type] += Math.log10(proba(word, type, vec_file));
			}
		}
		return (proba[SPAM] >= proba[HAM]) ? SPAM : HAM;
	}


	public void test(String testDir){
		String[] dirs = new String[2];
		dirs[SPAM] = "spam";
		dirs[HAM] = "ham";
		int[] max = new int[2];
		max[SPAM] = 10000;
		max[HAM] = 10000;
		for(int type = 0 ; type < 2 ; type++){
			for(FileInputStream file : listFilesForFolder(testDir + File.separator + dirs[type], max[type])){
				boolean win = (test(file) == type);
				System.out.println(file.toString() + "  "  +  win);

			}
		}
	}


	public double proba(String word, int type, List<String> vec_file){
		double prob = ((double)occurences[dic.indexOf(word)][type] ) / nb_mess[type] ;
		return (vec_file.contains(word)) ? prob
										 : 1. - prob ;
	}




	public List<FileInputStream> listFilesForFolder(String folder, int max) {

		int i = 0;
		List<FileInputStream> list = new ArrayList<>();
		try {
			for(File fileEntry : new File(folder).listFiles()) {
				if ( ! fileEntry.isDirectory()) {
					list.add(new FileInputStream(fileEntry));
					if( i++ == max) return list;

				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		System.out.println("la limite: "+ max + " n'a pas été dépassée: " + i + " fichiers lus");
		return list;
	}


	public List<String> load_words(FileInputStream fStream){
		List<String> list = new ArrayList<>();
		try {

			DataInputStream in = new DataInputStream(fStream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = br.readLine()) != null)   {
				String regexp =   "[ "
						+ "|\t"
						+ "|\r"
						+ "|,"
						+ "|'"
						+ "|@"
						+ "|:"
						+ "|\\-"
						+ "|."
						+ "|!"
						+ "|?"
						+ "|%"
						+ "|;"
						+ "|$"
						+ "|°"
						+ "|="
						+ "|&"
						+ "|<"
						+ "|>"
						+ "|/"
						+ "|\\["
						+ "|\\]"
						+ "|0-9"
						+ "|("
						+ "|)"
						+ "|+"
						+ "|*"
						+ "|\""
						+ "|_"
						+ "|']";
				String[] words = line.split(regexp);
				for(String s : words){
					if(s.length() >= 3){
						list.add(s.toUpperCase());
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return list;
	}

	public List<String> lire_message(FileInputStream file){
		List<String> m = new ArrayList<>();
		for(String s : load_words(file)){
			if(dic.contains(s) && ! m.contains(s)){
				m.add(s);
			}
		}

		return m;
	}


}