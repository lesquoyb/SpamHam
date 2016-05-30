import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ClassifBayes implements Serializable{

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



	
	public void save(String file){
		try{
			FileOutputStream fileOut = new FileOutputStream(file);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(this);
			out.close();
			fileOut.close();
			System.out.println("classifieur enregistré dans " + file);
		}catch(IOException i){
			i.printStackTrace();
		}
	}
	
	public static ClassifBayes openSavedClassif(String file){
		ClassifBayes c = null;
		try{
			FileInputStream fileIn = new FileInputStream(file);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			c = (ClassifBayes) in.readObject();
			in.close();
			fileIn.close();
			System.out.println("classifieur chargé depuis " + file);
		}catch(IOException i){
			i.printStackTrace();
		}catch(ClassNotFoundException d){
			d.printStackTrace();
		}
		return c;
	}

	public void learnFile(FileInputStream file, int type){
		for(String s : lire_message(file) ){
			if(dic.contains(s)){
				occurences[ dic.indexOf(s) ][ type ] ++;
			}						
		}
		nb_mess[type]++;
	}
	
	public void learning(String appDir, int nbHam, int nbSpam){
		System.out.println("repertoire: " + appDir + " nbHam: " + nbHam + " nbSpam: " + nbSpam);
		System.out.println("learning ... ");
		String[] dirs = new String[2];
		dirs[SPAM] = "spam";
		dirs[HAM] = "ham";
		int[] max = new int[2];
		max[SPAM] = nbSpam;
		max[HAM] = nbHam;

		for(int type = 0 ; type < 2 ; type++){
			for(FileInputStream file : listFilesForFolder(appDir + File.separator + dirs[type], max[type])){
				learnFile(file, type);
				try {
					file.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
	public void learning(String appDir){
		int nbHam;
		int nbSpam;
		Scanner sc = new Scanner(System.in);
		do{
			System.out.println("nombre de HAM à apprendre ?");
			nbHam = sc.nextInt();
			System.out.println("nombre de SPAM à apprendre ?");
			nbSpam = sc.nextInt();
		}while(nbHam < 0 || nbSpam < 0);
		learning(appDir, nbHam, nbSpam);
	}


	public int test(FileInputStream f){
		double[] proba = new double[2];
		List<String> vec_file = lire_message(f);
		for(String word : dic){
			for(int type = 0 ; type < 2 ; type++){
				proba[type] += Math.log10(proba(word, type, vec_file));
			}
		}

		proba[SPAM] += Math.log10(proba(SPAM));
		proba[HAM] += Math.log10(proba(HAM));
		boolean spam = proba[SPAM] >= proba[HAM];
		System.out.println(": log10(P(Y=SPAM | X=x)) = "+ proba[SPAM] +", log10(P(Y=HAM | X=x)) = " + proba[HAM]);
		System.out.print("\t=> identifié comme un " + ((spam) ? "spam" : "ham"));
		return spam  ? SPAM : HAM;
	}


	public void test(String testDir, int nbSpam, int nbHam){
		String[] dirs = new String[2];
		dirs[SPAM] = "spam";
		dirs[HAM] = "ham";
		int[] max = new int[2];
		Scanner sc = new Scanner(System.in);
		max[HAM] = nbHam;
		max[SPAM] = nbSpam;

		int[] wins = new int[2];
		int[] total = new int[2];
		for(int type = 0 ; type < 2 ; type++){
			int i = 0;
			for(FileInputStream file : listFilesForFolder(testDir + File.separator + dirs[type], max[type])){
				System.out.print( (type == SPAM ? "SPAM" : "HAM") + " numéro: " + i);
				if(test(file) == type){
					wins[type]++;
					System.out.println("");
				}
				else{
					System.out.println("\t*** erreur ***");
				}
				total[type]++;
				i++;
				try {
					file.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("réussite SPAM: " + wins[SPAM] + "/" + total[SPAM] + "="+(double)wins[SPAM]/total[SPAM]);
		System.out.println("réussite HAM: " + wins[HAM] + "/" + total[HAM] + "="+(double)wins[HAM]/total[HAM]);
		System.out.println("réussite totale: " + (wins[SPAM]+wins[HAM]) + "/" + (total[SPAM]+total[HAM]) + "="+ ((double)(wins[SPAM]+wins[HAM])/(total[SPAM]+total[HAM]) ));
	}


	public double proba(String word, int type, List<String> vec_file){
		double epsilon = 1;
		double prob = 	( (double)occurences[dic.indexOf(word)][type] + epsilon )  
						/ (nb_mess[type] + epsilon * 2) ;
		
		return (vec_file.contains(word)) ? prob
										 : 1. - prob ;
	}
	
	public double proba(int type){
		return ((double)nb_mess[type]) / (nb_mess[SPAM]+nb_mess[HAM]);
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
