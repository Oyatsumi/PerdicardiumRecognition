import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;

public class MetaheuristicCoracao {
	private static int teta = 10, x0 = 175, y0 = 319, a = 180, b = 161; //elipse - limite de 250 para a e para b
	static WritableRaster raster;
	static BufferedImage img;
	static File imgFile;

	static String path = "C:\\Users\\erick\\Documents\\Outros Projetos\\Coracao-Metaheuristica-Contorno\\Imagens\\";
	
	static ArrayList<String> output = new ArrayList<String>();
	static BufferedWriter writer;
	
	public static void main(String[] args) throws IOException{
		File f = new File(args[0]);
		writer = new BufferedWriter(new FileWriter(f.getParentFile().getAbsolutePath() + "/nout_" + args[1] + ".txt"));
		File[] files = f.listFiles();
		File[] files2;
		String[] arg = new String[2];
		arg[1] = args[1];
		for (int k=0; k<files.length; k++){
			files2 = files[k].listFiles();
			for (int k2=0; k2<files2.length; k2++){
				arg[0] = files2[k2].getAbsolutePath();
				if (files2[k2].getName().length() > 8) continue;
				main2(arg);
			}
		}
		writer.close();
	}
	
	public static void main2(String[] args) throws IOException{
		//imgFile = new File(path + "036.bmp");
		//img = ImageIO.read(imgFile);
		
		System.out.println("Processing image: " + args[0]);
		output.add(args[0].split("\\\\")[args[0].split("\\\\").length - 2]);
		output.add(args[0].split("\\\\")[args[0].split("\\\\").length - 1]);
		
		imgFile = new File(args[0]);
		img = ImageIO.read(imgFile);
		
		MAX_IT = Integer.parseInt(args[1]);
		
		
		raster = img.getRaster();
		
		double startT = System.nanoTime();
		
		float threshold = 0.1f;
		/*
		
		for (int i=0; i<raster.getHeight(); i++){
			for (int j=0; j<raster.getWidth(); j++){
				if (isWithin(j,i)) {
					raster.setSample(j, i, 0, 0);
					raster.setSample(j, i, 1, 0);
					raster.setSample(j, i, 2, 255);
				}
			}
		}
		
		
		ImageIO.write(img, "PNG", new File(path + "out.png"));
		System.out.println("terminou");
		*/
		double startG = System.nanoTime();
		geneticAlgorithm();
		System.out.println("Completed the genetic algorithm in: " + (System.nanoTime() - startG)/1000000000); output.add(((System.nanoTime() - startG)/1000000000) + "");
		writeElipse();
		System.out.println("Completed in: " + ((System.nanoTime() - startT)/1000000000) + " s"); output.add(((System.nanoTime() - startT)/1000000000) + "");
		
		for (int i=0; i<output.size()-1; i++)
			writer.write(output.get(i) + ",");
		writer.write(output.get(output.size()-1));
		writer.newLine(); writer.flush();
		output.clear();
	}
	
	public static void writeElipse() throws IOException{
		boolean isGreen, isBlue, isRed, isGrey, isBlack;
		double cBlue = 0, cRed = 0, cGreen = 0, cGrey = 0, cBlack = 0,
				gcBlue = 0, gcRed = 0, gcGreen = 0, gcGrey = 0, gcBlack = 0;
		for (int i=0; i<raster.getHeight(); i++){
			for (int j=0; j<raster.getWidth(); j++){
				isRed = raster.getSample(j, i, 0) > raster.getSample(j, i, 1) && raster.getSample(j, i, 0) > raster.getSample(j, i, 2);
				isGreen = raster.getSample(j, i, 1) > raster.getSample(j, i, 0) && raster.getSample(j, i, 1) > raster.getSample(j, i, 2);
				isBlue = raster.getSample(j, i, 2) > raster.getSample(j, i, 0) || raster.getSample(j, i, 2) > raster.getSample(j, i, 1);
				isGrey = raster.getSample(j, i, 0) == raster.getSample(j, i, 1) && raster.getSample(j, i, 1) == raster.getSample(j, i, 2);
				isBlack = isGrey && raster.getSample(j, i, 0) == 0;
				if (isRed) gcRed ++;
				else if (isGreen) gcGreen ++;
				else if (isBlack) gcBlack ++;
				else if (isGrey) gcGrey ++;	
				else if (isBlue) gcBlue ++;
				
				if (isWithinPerIndividual(j, i, topIndividuals.get(0))){
					if (isRed) cRed ++;
					else if (isGreen) cGreen ++;
					else if (isBlack) cBlack ++;
					else if (isGrey) cGrey ++;		
					else if (isBlue) cBlue ++;
				}
				if (isOnLinePerIndividual(j,i,0.015f,topIndividuals.get(0))) {
					raster.setSample(j, i, 0, 0);
					raster.setSample(j, i, 1, 0);
					raster.setSample(j, i, 2, 255);
				}
			}
		}
		if (gcGreen == 0) {gcGreen = 1;cGreen = 1;}
		System.out.println("Number of pixels within elipse, Red: " + cRed*100/gcRed + ", Green: " + cGreen*100/gcGreen + ", Blue: " + cBlue*100/gcBlue + ", Grey: " + cGrey*100/gcGrey + ", Black: " + cBlack*100/gcBlack + ".");
		output.add((cRed*100/gcRed) + ""); output.add(cGreen*100/gcGreen + ""); output.add((cGrey*100/gcGrey) + ""); output.add((cBlack*100/gcBlack) + "");
		System.out.println("General Fit (GF): " +  ((cRed*100/gcRed)/(cGreen*100/gcGreen + cGrey*100/gcGrey +  cBlack*100/gcBlack))); output.add(((cRed*100/gcRed)/(cGreen*100/gcGreen + cGrey*100/gcGrey +  cBlack*100/gcBlack)) + "");
		ImageIO.write(img, "PNG", new File(imgFile.getAbsolutePath() + "_it" + MAX_IT + "_out.png"));
	}
	public static boolean isOnLine(int x, int y, float threshold){
		double r = Math.pow((x - x0)*Math.cos(teta) + (y - y0)*Math.sin(teta), 2)/Math.pow(a, 2) + 
				Math.pow((x - x0)*Math.sin(teta) - (y - y0)*Math.cos(teta), 2)/Math.pow(b, 2);
		return (r > 1 - threshold && r < 1 + threshold);
	}
	public static boolean isOnLinePerIndividual(int x, int y, float threshold, Individual ind){
		double r = Math.pow((x - ind.x0)*Math.cos(ind.teta) + (y - ind.y0)*Math.sin(ind.teta), 2)/Math.pow(ind.a, 2) + 
				Math.pow((x - ind.x0)*Math.sin(ind.teta) - (y - ind.y0)*Math.cos(ind.teta), 2)/Math.pow(ind.b, 2);
		return (r > 1 - threshold && r < 1 + threshold);
	}
	public static boolean isWithin(int x, int y){
		double r = Math.pow((x - x0)*Math.cos(teta) + (y - y0)*Math.sin(teta), 2)/Math.pow(a, 2) + 
				Math.pow((x - x0)*Math.sin(teta) - (y - y0)*Math.cos(teta), 2)/Math.pow(b, 2);
		return (r < 1);
	}
	public static boolean isWithinPerIndividual(int x, int y, Individual ind){
		double r = Math.pow((x - ind.x0)*Math.cos(ind.teta) + (y - ind.y0)*Math.sin(ind.teta), 2)/Math.pow(ind.a, 2) + 
				Math.pow((x - ind.x0)*Math.sin(ind.teta) - (y - ind.y0)*Math.cos(ind.teta), 2)/Math.pow(ind.b, 2);
		return (r < 1);
	}
	public static double computeScore(Individual ind){//greater is better
		boolean isGreen, isBlue, isRed, isGrey, isBlack;
		double score = 0;
		for (int i=0; i<raster.getHeight(); i++){
			for (int j=0; j<raster.getWidth(); j++){
				isRed = raster.getSample(j, i, 0) > raster.getSample(j, i, 1) && raster.getSample(j, i, 0) > raster.getSample(j, i, 2);
				isGreen = raster.getSample(j, i, 1) > raster.getSample(j, i, 0) && raster.getSample(j, i, 1) > raster.getSample(j, i, 2);
				isBlue = raster.getSample(j, i, 2) > raster.getSample(j, i, 0) && raster.getSample(j, i, 2) > raster.getSample(j, i, 1);
				isGrey = raster.getSample(j, i, 0) == raster.getSample(j, i, 1) && raster.getSample(j, i, 1) == raster.getSample(j, i, 2);
				isBlack = isGrey && raster.getSample(j, i, 0) == 0;
				
				if (isWithinPerIndividual(j, i, ind)){
					if (isRed) score += 140;
					else if (isGreen) score -= 3;
					else if (isBlack) score -= 2.5;
					else if (isGrey) score -= 4;
				}
				
			}
		}
		ind.score = score;
		return score;
	}
	
	
	public static void order(ArrayList<Individual> individuals){
		ForFora:
		for (int k=individuals.size()-1; k>=0; k--){
			for (int k2=0; k2<individuals.size(); k2++){
				if (individuals.get(k).score == -1) computeScore(individuals.get(k));
				if (individuals.get(k2).score == -1) computeScore(individuals.get(k2));
				
				if (individuals.get(k).score > individuals.get(k2).score){
					individuals.add(k2,individuals.remove(k));
					continue ForFora;
				}else if (individuals.get(k2).score > individuals.get(k).score){
					individuals.add(k, individuals.remove(k2));
					continue ForFora;
				}
			}
		}
	}
	public static void insert(ArrayList<Individual> individuals, Individual ind){
		if (ind.score == -1) computeScore(ind);
		for (int k=0; k<individuals.size(); k++){
			if (individuals.get(k).score == -1) computeScore(individuals.get(k));
			if (ind.score > individuals.get(k).score){
				individuals.add(k, ind);
				individuals.remove(individuals.size()-1);
				notChanged = 0;
				return;
			}
				
		}
	}
	
	/*
	 * teta = 3 dígitos (max: 180)
	 * x0 = 3 dígitos (max 412, min 100)
	 * y0 = mesma coisa do de cima
	 * a (tamanho na horizontal) = 3 dígitos (max 250, min 80)
	 * b (tamanho vertical) = mesma coisa do de cima
	 * -- total de 15 digitos
	 */
	static int MAX_IND = 60, TOP = 20, MAX_IT = 100, MAX_IT_WITHOUT_CHANGE = MAX_IT/10;
	public static int notChanged = 0;
	public static ArrayList<Individual> topIndividuals = new ArrayList<Individual>();
	public static void geneticAlgorithm(){
		
		for (int k=0; k<TOP; k++){
			topIndividuals.add(new MetaheuristicCoracao().new Individual());
		}
			
		order(topIndividuals);
		
		Individual id1, id2, out, bestInd = null;
		double maxScore = Long.MIN_VALUE;
		
		int iterations = 0, aux = r.nextInt(5);
		while (iterations < MAX_IT){
			maxScore = Long.MIN_VALUE;
			for (int k=0; k<MAX_IND - TOP; k++){//gerar individuos
				aux = r.nextInt(5);
				id1 = topIndividuals.get(r.nextInt(TOP));
				id2 = topIndividuals.get(r.nextInt(TOP));
				out = new MetaheuristicCoracao().new Individual(id1);//copy of id1
				//cross over
				for (int i=0; i<aux; i++){
					switch (r.nextInt(5)){
						case 0: out.teta = id2.teta; break;
						case 1: out.x0 = id2.x0; break;
						case 2: out.y0 = id2.y0; break;
						case 3: out.a = id2.a; break;
						case 4: out.b = id2.b; break;
					}
				}
				aux = r.nextInt(10);
				//random mutations
				for (int i=0; i<aux; i++){
					switch (r.nextInt(5)){
						case 0: out.teta += -Math.pow(r.nextInt(21)*r.nextFloat(), 2) + Math.pow(r.nextInt(21)*r.nextFloat(), 2); if (out.teta > 360) out.teta = 360; if (out.teta < 0) out.teta = 0; break;
						case 1: out.x0 += -Math.pow(r.nextInt(21)*r.nextFloat(), 2) + Math.pow(r.nextInt(21)*r.nextFloat(), 2); if (out.x0 > 412) out.x0 = 412; if (out.x0 < 100) out.x0 = 100; break;
						case 2: out.y0 += -Math.pow(r.nextInt(21)*r.nextFloat(), 2) + Math.pow(r.nextInt(21)*r.nextFloat(), 2); if (out.y0 > 412) out.y0 = 412; if (out.y0 < 100) out.y0 = 100; break;
						case 3: out.a += -Math.pow(r.nextInt(21)*r.nextFloat(), 2) + Math.pow(r.nextInt(21)*r.nextFloat(), 2); if (out.a > 250) out.a = 250; if (out.a < 80) out.a = 80; break;
						case 4: out.b += -Math.pow(r.nextInt(21)*r.nextFloat(), 2) + Math.pow(r.nextInt(21)*r.nextFloat(), 2); if (out.b > 250) out.b = 250; if (out.b < 80) out.b = 80; break;
					}
				}
				
				computeScore(out);
				/*if (out.score > maxScore){
					maxScore = out.score;
					bestInd = out;
				}*/
				insert(topIndividuals, out);
			}
			
			//System.out.println("Iteration n.: " + iterations);
			iterations ++;
			notChanged ++;
			if (notChanged > MAX_IT_WITHOUT_CHANGE) break;
		}
		
		System.out.println("Number of iterations: " + iterations); output.add(iterations + "");
		System.out.println("Maximum achieved score: " + topIndividuals.get(0).score); output.add(topIndividuals.get(0).score + "");
		double aux2 = 0;
		for (int k=0; k<topIndividuals.size(); k++) aux2 += topIndividuals.get(k).score;
		System.out.println("Mean score of the top individuals: " + (aux2/TOP)); output.add((aux2/TOP) + "");
		System.out.println("Best individual: " + topIndividuals.get(0).teta + ", " + topIndividuals.get(0).x0 + ", " + topIndividuals.get(0).y0
				+ ", " + topIndividuals.get(0).a + ", " + topIndividuals.get(0).b);
		
	}
	
	static final Random r = new Random();
	class Individual{
		double teta = 0, x0 = 100, y0 = 256, a = 80, b = 80; 
		double score = -1;
		
		Individual(){
			teta = r.nextInt(361);
			x0 = 100 + r.nextInt(312);
			y0 = 100 + r.nextInt(312);
			a = 80 + r.nextInt(170);
			b = 80 + r.nextInt(170);
		}
		Individual(Individual i){
			teta = i.teta; x0 = i.x0; y0 = i.y0; a = i.a; b = i.b; score = i.score;
		}
	}
}
