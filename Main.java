import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Main {


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
		//TODO
	}

	
	public static void main(String ... args) throws FileNotFoundException{
		FileInputStream d = new FileInputStream("dictionnaire1000en.txt");
		List<String> dico = charger_dictionnaire(d);

		String baseAppPath = "baseapp";
		ClassifBayes classif = new ClassifBayes(dico);
			
		
		if(args[0].equals("filtre_mail")){
			classif = ClassifBayes.openSavedClassif(args[1]);
			FileInputStream f = new FileInputStream(args[2]);
			classif.test(f);
			System.out.println("\nd'après le classifieur "+ args[1]);
		}
		else if(args[0].equals("apprend_filtre")){
			classif.learning(args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]));
			classif.save(args[1]);
		} 
		else if(args[0].equals("filtre_en_ligne") && args[args.length-1].equals("HAM") || args[args.length-1].equals("SPAM")){
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
			classif.test(baseTestPath, Integer.parseInt(args[1]), Integer.parseInt(args[2]));
		}
		else{
			printMan();
		}
		
	}
	
	


}
