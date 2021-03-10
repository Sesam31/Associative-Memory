import java.awt.image.AffineTransformOp;
import java.awt.image.*;
import javax.imageio.*;
import javax.swing.*;
import java.io.*;
import java.util.Random;
import java.util.Scanner;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.io.IOException;
import java.io.File;
import javax.imageio.ImageIO;

public class AssociativeMemory{

		public static void main(String[] args) {

				System.out.println("##########################################");
				System.out.println("##           Associative Memory         ##");
				System.out.println("##       Developed by Daniele Facco     ##");
				System.out.println("##   Email: FarSideVirtual@airmail.cc   ##");
				System.out.println("##########################################");
				System.out.println();

				//INIZIALIZZAZIONE CON ESEMPI
				//Nota: immagini con bit non randomici => 
				//\alpha diminiusce a circa 0,005 => 
				//il massimo numero di esempi immagazzinabili 
				//a livello teorico è 12

				int[][] x = buildX();       											//Realizza matrice esempi X unendo gli esempi inclusi nella cartella memory
				//REALIZZAZIONE MATRICE PESI
				double[][] w = buildW(x);           									//Realizza matrice W eseguendo operazioni tra matrici (W = 1/n*(X*Xt))
				//OTTIMIZZAZIONE
				w = setDiagonalTo0(w);              									//Elimina connessioni neuroni con se stessi

				//RICEZIONE IMMAGINE ROVINATA
				String path;
				File s;

				if (0 < args.length) {				
						path = args[0];
						s = new File(path);												//Riceve path da stdin
				}
				else{
						Scanner scanner = new Scanner(System.in);
						System.out.print("Path to broken image: ");
						System.out.flush();
						path = scanner.nextLine();
						s = new File(path);												//Riceve path con richiesta
				}
				printImage(path);

				//ELABORAZIONE IMMAGINE
				int[] sa = buildS(s);               									//Converte immagine ricevuta in array utilizzabile

				//GENERAZIONE ORDINE AGGIORNAMENTO RANDOMICO
				int[] updateOrder = new int[2500];
				for (int i = 0; i < updateOrder.length; i ++) {
						updateOrder[i] = i;												//Crea array ordinato
				}
				updateOrder = randomizer(updateOrder);									//Randomizza ordine aggiornamento

				//AGGIORNAMENTO NEURONI
				for (int i = 0; i < sa.length; i ++) {
						sa[updateOrder[i]] = updateNeuron(sa, w, updateOrder[i]);		//Si=signOperator(somma elmenti riga i di W * il loro corrispettivo in S)
				}

				//RICERCA RISULTATI DIRETTI E SPECULARI
				findMatch(sa);
		}

		public static int[][] buildX(){
				int imagenum = new File("./memory/").listFiles().length;
				int a = 0;
				int [][] w = new int[2500][imagenum];
				for(int n = 0; n < imagenum; n++){

						BufferedImage image = null;
						File input = null;

						try{
								input = new File("./memory/image" + n + ".png");
								image = ImageIO.read(input);
						}
						catch(IOException e){
								System.out.println("Wrong path!");
						}
						BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
						Graphics2D graphic = result.createGraphics();
						graphic.drawImage(image, 0, 0, Color.WHITE, null);
						graphic.dispose();
						/*
						   try{
						   File output = new File("./output/output.png");
						   ImageIO.write(result, "png", output);
						   }
						   catch(IOException e){
						   System.out.println(e);
						   }*/	
						for(int y = 0; y < image.getHeight(); y++){
								for(int x = 0; x < image.getWidth(); x++){
										if(result.getRGB(x,y) == -1){
												w[a][n] = 1;
										}
										else{
												w[a][n] = -1;
										}
										a ++;
								}
						}
						a=0;
				}
				return w;
		}

		public static double[][] buildW(int[][] x){
				double[][] buildW = new double[x.length][x.length];
				int[][] xt = transposeX(x);
				//W = 1/n (X * Xt)
				int n = x.length;
				int[][] W = multiply2matrix(x, xt);
				buildW = divideByN(W, n);
				return buildW;
		}

		public static int[][] transposeX(int[][] x){
				int[][] xt = new int[x[0].length][x.length];
				for (int i = 0; i < x[0].length; i++) {
						for (int j = 0; j < x.length; j++) {
								xt[i][j] = x[j][i];
						}
				}
				return xt;
		}

		public static int[][] multiply2matrix(int[][] m1, int[][] m2){
				int [][] multiplied = new int[m1.length][m2[0].length];
				for (int i = 0; i < m1.length; i++) {
						for (int j = 0; j < m2[0].length; j++) {
								for (int k = 0; k < m1[0].length; k++) {
										multiplied[i][j] += m1[i][k] * m2[k][j];
								}
						}
				}
				return multiplied;
		}

		public static void displayMatrix(double[][] m){
				for (int i = 0; i < m.length; i++) {
						for (int j = 0; j < m[0].length; j++) {
								System.out.print(m[i][j] + " ");
						}
						System.out.print("\n");
				}
		}

		public static double[][] divideByN(int[][] m, int n){
				double[][] newm = new double[m.length][m[0].length];
				for (int i = 0; i < m.length; i++) {
						for (int j = 0; j < m[0].length; j++) {
								newm[i][j] = (1.0/n) * m[i][j];
						}
				}
				return newm;
		}

		public static double[][] setDiagonalTo0(double[][] m){
				for (int i = 0; i < m.length; i++) {
						m[i][i] = 0;
				}
				return m;
		}

		public static int signOperator(double a){
				if(a > 0){
						a = 1;
				}
				if(a < 0){
						a = -1;
				}
				return (int)a;
		}

		public static int[] buildS(File s){

				int[] sa = new int[2500];
				BufferedImage image = null;
				File input = s;
				try{
						image = ImageIO.read(input);
				}
				catch(IOException e){
						System.out.println("Wrong path!");
				}
				BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
				Graphics2D graphic = result.createGraphics();
				graphic.drawImage(image, 0, 0, Color.WHITE, null);
				graphic.dispose();

				int a=0;
				for(int y = 0; y < image.getHeight(); y++){
						for(int x = 0; x < image.getWidth(); x++){
								if(result.getRGB(x,y) == -1){
										sa[a] = 1;
								}
								else{
										sa[a] = -1;
								}
								a ++;
						}
				}
				return sa;
		}

		public static int[] randomizer(int[] a){
				Random random = new Random();		//initialize randomizer
				for(int x=0; x < a.length; x++){
						int randomInt = random.nextInt(a.length);
						int store = a[x];
						a[x] = a[randomInt];
						a[randomInt] = store;
				}
				return a;
		}
		public static int updateNeuron(int[] s, double[][] w, int i){
				int newS;
				double sum = 0;
				for(int j = 0; j < s.length; j++){
						sum += w[i][j] * s[j];
				}
				newS = signOperator(sum);
				return newS;
		}

		public static boolean findMatch (int[] sa){
				int imagenum = new File("./memory/").listFiles().length;
				System.out.println();
				for(int n = 0; n < imagenum; n++){

						BufferedImage image = null;
						File input = null;
						int checks = 0; 
						try{
								input = new File("./memory/image" + n + ".png");
								image = ImageIO.read(input);
						}
						catch(IOException e){
								System.out.println(e);
						}

						int[] matchTest = buildS(input);
						for(int a = 0; a < 2500; a++){
								if(matchTest[a] != sa[a]){

								}
								else{
										checks ++;
								}
						}
						System.out.println("Checks passed on image " + n + ": " + checks + " [" + checks/2500.00*100 + "%]");
						if(checks == 2500){
								System.out.println();
								System.out.println("Match found on image" + n + ".png!");
								printImage("./memory/image" + n + ".png");
								return true;
						}
						if(checks == 0){
								System.out.println();
								System.out.println("Specular match found on image" + n + ".png!");
								printImage("./memory/image" + n + ".png");
								return true;
						}
				} 
				System.out.println("Match not found!");
				return false;
		}

		public static void printImage(final String filename){
				SwingUtilities.invokeLater(new Runnable(){
						public void run(){
								JFrame frame = new JFrame("Associative Memory");
								frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
								BufferedImage image = null;
								try{
										image = ImageIO.read(new File(filename));
								}
								catch (Exception e){
										System.out.println("Wrong path!");
								}

								BufferedImage resizedImage=resize(image,300,300);//resize the image to 300x300
								ImageIcon imageIcon = new ImageIcon(resizedImage);
								JLabel jLabel = new JLabel();
								jLabel.setIcon(imageIcon);
								frame.getContentPane().add(jLabel, BorderLayout.PAGE_START);

								frame.pack();
								frame.setLocationRelativeTo(null);
								frame.setVisible(true);
						}
				});
		}

		public static BufferedImage resize(BufferedImage image, int width, int height) {
				BufferedImage bi = new BufferedImage(width, height, BufferedImage.TRANSLUCENT);
				Graphics2D g2d = (Graphics2D) bi.createGraphics();
				g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
				g2d.drawImage(image, 0, 0, width, height, null);
				g2d.dispose();
				return bi;
		}
}

//EOF
