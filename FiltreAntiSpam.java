import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.ObjectOutputStream;

public class FiltreAntiSpam {


	public static List<String> charger_dictionnaire(FileInputStream fStream){
		List<String> list = new ArrayList<String>();

		try {

			DataInputStream in = new DataInputStream(fStream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = br.readLine()) != null)   {
				if(line.length() >= 3){
					list.add(line.toUpperCase());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return list;
	}



	public static void printMan(){
		System.out.println("erreur: commande inconnue");
		System.out.println("les commandes disponibles sont:\n"
							+ "java FiltreAntiSpam filtre_mail fichier_classif mail_a_tester\n"
							+ "java FiltreAntiSpam apprend_filtre fichier_classif base_apprentissage nb_Ham nb_Spam\n"
							+ "java FiltreAntiSpam filtre_en_ligne fichier_classif liste_fichier_a_ajouter HAM/SPAM\n"
							+ "java FiltreAntiSpam base_test nb_Ham nb_Spam\n");
	}

	public static void courbe(ClassifBayes classif){
		
		try {
			FileOutputStream f = new FileOutputStream("out");
			String baseAppPath = "baseapp";

			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(f));
	 
			for(int i = 0 ; i < 500 ; i++){
				FileInputStream fis;
				for(int j = i*5 ; j < i*5+5 ; j++){
					fis = new FileInputStream(baseAppPath+"/ham/" + j+".txt");
					classif.learnFile(fis, ClassifBayes.HAM);
				}
				fis = new FileInputStream(baseAppPath+"/spam/" + i+".txt");
				classif.learnFile(fis, ClassifBayes.SPAM);
				if(i%10 == 0){
					classif.test("basetest", 499, 499);
			//	bw.write(i + ", " + (1-classif.stat));
				bw.newLine();
			}
			}
			bw.close();
			f.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String ... args) throws FileNotFoundException{
		FileInputStream d = new FileInputStream("dictionnaire1000en.txt");
		List<String> dico = charger_dictionnaire(d);

		String baseAppPath = "baseapp";
		ClassifBayes classif = new ClassifBayes(dico);
	/*
		courbe(classif);
		*/
		if(args.length == 3 && args[0].equals("filtre_mail")){
			classif = ClassifBayes.openSavedClassif(args[1]);
			FileInputStream f = new FileInputStream(args[2]);
			classif.test(f);
			System.out.println("\nd'après le classifieur "+ args[1]);
		}
		else if(args.length == 5 && args[0].equals("apprend_filtre")){
			classif.learning(args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]));
			classif.save(args[1]);
		} 
		else if(args.length > 3 && args[0].equals("filtre_en_ligne") && (args[args.length-1].equals("HAM") || args[args.length-1].equals("SPAM")) ){
			classif = ClassifBayes.openSavedClassif(args[1]);
			for(int i = 2 ; i < args.length-1 ; i++){
				FileInputStream f = new FileInputStream(args[i]);
				classif.learnFile(f, args[args.length-1].equals("HAM") ? ClassifBayes.HAM : ClassifBayes.SPAM);
			}
			classif.save(args[1]);
		}
		else if(args.length == 3){
			String baseTestPath = args[0];
			classif.learning(baseAppPath);
			try{
				classif.test(baseTestPath, Integer.parseInt(args[1]), Integer.parseInt(args[2]));
			}
			catch(Exception e){
				printMan();
			}
		}
		else{
			printMan();
		}
		
		
	}
	
	


}
