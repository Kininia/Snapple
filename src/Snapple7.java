//Tom Postema & Kristoffer Rothstein NV36  

import java.applet.Applet;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JOptionPane;

@SuppressWarnings("serial")
public class Snapple7 extends Applet implements Runnable, KeyListener {

	private int p = 0, hastighet = 10;// po�ng f�r spelare && antal pixlar ormen flyttar sig/uppdatering
	private int[][] coord = new int[250][2], intKo = new int[250][2];//intf�lt f�r koordinater f�r kroppen && inf�lt som sparar koordinaterna
	private int[] tangentKo = new int[15];//intf�lt som h�ller reda p� intryckta tangenter
	private int a = 0, b = 0;// omodifierade koordinater f�r ett "start�pple"
	int vx = hastighet, vy = 0;// hastigheter f�r ormen
	int kropp = 3/*antal kroppsdelar*/ , antalK = 0/*antal sparade kroppsdelar*/, antalT = 0/*antal sparade tangenter*/;
	int forts�tt = 0/*se l�ngre ner*/, v�nta = 1/*se l�ngre ner*/, placeringar = 0/*antal spelade omg�ngar*/;

	private Highscore[] highscore = new Highscore[100];//Highscore-f�lt inneh�llande en int och en string/position
	private Font st�rre = new Font("Arial", Font.BOLD, 15); // typsnitt f�r gameover text
	private boolean north = false, south = false, west = false, east = true;// riktningar
	boolean gameOver = false/*game over?*/, taken = true/*�pple taget?*/, newGame = false/*nytt spel?*/, pause = false/*paus?*/;
	Image bufferImage;
	Graphics bufferGraphics;

	public void init() {//Initialiseringsmetod, anropas automatiskt i b�rjan
		setSize(500, 400);//Appletens storlek
		setBackground(Color.black);// Bakgrundsf�rg

		// Startkoordinater
		for (int i = 0; i < kropp; i++) {
			coord[i][0] = 100 + 10 * i;
			coord[i][1] = 100;
		}
		addKeyListener(this);// Appleten lyssnar p� tangenttryck

		Thread th = new Thread(this);// en egen aktivitet till programmet
		th.start();

		setVisible(true); // ist�llet f�r i main

		bufferImage = createImage(500, 400);// Skapar en buffert till grafiken i appleten
		bufferGraphics = bufferImage.getGraphics();
		bufferGraphics.setColor(Color.black);//F�rgrundsf�rg
		bufferGraphics.fillRect(0, 0, 500, 400);
	}

	public void update(Graphics g) {
		paint(g);
	}

	public void paint(Graphics g) {
		requestFocus();

		if (!gameOver) {// om icke gameover
			bufferGraphics = bufferImage.getGraphics();
			bufferGraphics.fillRect(0, 0, 500, 400);
			bufferGraphics.setColor(Color.red);
			bufferGraphics.drawRect(40, 40, 410, 310);//Ritar ut kanten
			bufferGraphics.setColor(Color.white);
			bufferGraphics.setFont(st�rre);
			bufferGraphics.drawString(" " + p, 250, 20);//Skriver ut spelarens po�ng
			
			if (pause)//Om det �r paus skrivs "Pause"
				bufferGraphics.drawString("Pause", 240, 100);

			for (int i = 0; i < kropp; i++) {//Ritar ut ormen
				bufferGraphics.setColor(Color.white);
				bufferGraphics.fillOval(coord[i][0], coord[i][1], 10, 10);// Kroppsdel
			}

			if (coord[0][0] == a && coord[0][1] == b)// Om ormens huvud och �pplet hamnar p� samma koordinater
			{
				taken = true;
				p++;//Po�ngen �kar med 1
				kropp++;//L�ngden p� kroppen �kar med 1
			}

			if (taken)// Om �pplet �r taget skapas ett nytt slumpvis
			{
				int x = (int) (Math.random() * 37);
				int y = (int) (Math.random() * 27);
				a = (10 * x) + 40;
				b = (10 * y) + 40;
				taken = false;
			}

			bufferGraphics.setColor(Color.green);
			bufferGraphics.fillOval(a, b, 10, 10);//ritar ett gr�nt �pple
			g.drawImage(bufferImage, 0, 0, this);
		}

		else//Om det blir game over
		{
			saveGame();//se l�ngre ner
			
			bufferGraphics.setColor(Color.black);
			bufferGraphics.fillRect(0, 0, 500, 400);//Ritar �ver ormen och �pplena
			bufferGraphics.setColor(Color.red);
			bufferGraphics.setFont(st�rre);
			bufferGraphics.drawString("Highscore:", 190, 110);//Skriver rubriken "Highscore"
			
			//Skriver ut highscorelistan (om det �r mindre �n 5 spelomg�ngar)
			if(placeringar<5)
			{
			for (int i=0; i<placeringar; i++)
				bufferGraphics.drawString(""+highscore[i], 190, 135+(i*25));//Skriver ut po�ng och namn
			}
			//Skriver ut highscorelistan, top 5 (om det �r mer �n 5 spelomg�ngar)
			else
			{
				for (int i=0; i<5; i++)
					bufferGraphics.drawString(""+highscore[i], 190, 135+(i*25));//Skriver ut po�ng och namn
			}
			
			for(int x=3; x>0; x--){//Starta om spelet "timer"
				bufferGraphics.setColor(Color.black);
				bufferGraphics.fillRect(145, 260, 250, 30);
				bufferGraphics.setColor(Color.red);
				bufferGraphics.drawString("The game will restart in " + x + " second(s)", 150, 280);
				g.drawImage(bufferImage, 0, 0, this);
				try {Thread.sleep(1000);} catch (InterruptedException ie) {}
			}
			startNewGame();//se l�ngre ner
		}
	}

	public void run(){

		while(true)
		{
			if (!gameOver)//S� l�nge det inte �r game over...
			{
				
				if (!pause && v�nta == 1)//..och spelet inte �r pausat...
				{
					for (int i = 0; i < kropp - 1; i++)
					{
						pushCoord(coord[i][0], coord[i][1]);//...l�ggs alla kroppsdelar utom sista i ett f�lt
					}
					
					if(antalT==1)//Sv�ng sker bara om en enda tangent �r intryckt
					{
						switch(popTangent())//Tar emot f�rsta tangentv�rdet
						{
						case 1://upp
							north=true;
					  		west=false;
					  		east=false;
					  		break;
						case 2://ner
							south=true;
					  		west=false;
					  		east=false;
					  		break;
						case 3://v�nster
							west=true;
							north=false;
							south=false;
							break;
						case 4://h�ger
							east=true;
					  		north=false;
					  		south=false;
					  		break;
						default:
							System.out.print("Error");
						}
					}
					
					//Ormens huvud flyttas ett steg...
					coord[0][0] += vx;
					coord[0][1] += vy;
					
					//...och resterande kroppsdelar f�r koordinaterna som kroppsdelen framf�r hade
					for (int i = 1; i < kropp; i++)
					{
						coord[i][0] = (popX());
						coord[i][1] = (popY());
						
						//Om huvudet och en kroppsdel �r p� samma koordinater blir det game over - man har krockat med sig sj�lv
						if (coord[0][0] == coord[i][0] && forts�tt == 2)
							if (coord[0][1] == coord[i][1] && !pause)
								gameOver = true;
					}
				}
				
				if(antalT>1)//Om fler �n en tangent trycktes in nollst�lls tangenterna
					antalT=0;
				
				//"T�mmer" f�rlyttningsk�n efter att man pausat spelet
				if (v�nta < 1)
					v�nta++;
				
				//Forts�tt ser till att det inte blir game over direkt i b�rjan av spelet
				if (forts�tt < 2)
					forts�tt++;

				if (north)//upp
				{
					vx = 0;
					vy = -hastighet;
				}

				if (south)//ner
				{
					vx = 0;
					vy = hastighet;
				}

				if (west)//v�nster
				{
					vx = -hastighet;
					vy = 0;
				}

				if (east)//h�ger
				{
					vx = hastighet;
					vy = 0;
				}
				
				//Kommer ormen mot kanten blir det game over
				if (coord[0][0] < 40 || coord[0][0] > 440)
					gameOver = true;

				if (coord[0][1] < 40 || coord[0][1] > 340)
					gameOver = true;

				repaint();

				try {Thread.sleep(150);} catch (InterruptedException ie) {}
			}
		}
	}

	public void keyPressed( KeyEvent ke){//tangent trycks ned
		if(ke.getKeyCode()==KeyEvent.VK_W && !south && !pause)//upp
		    pushTangent(1);
		
		if(ke.getKeyCode()==KeyEvent.VK_S && !north && !pause)//ner
			pushTangent(2);
		  
		if(ke.getKeyCode()==KeyEvent.VK_A && !east && !pause)//v�nster
			pushTangent(3);
		
		if(ke.getKeyCode()==KeyEvent.VK_D && !west && !pause)//h�ger
			pushTangent(4);
		
		if(ke.getKeyCode()==KeyEvent.VK_SPACE && !pause)//paus
		{
			pause=true;
			forts�tt=0;
			v�nta=0;
		}
		
		if(ke.getKeyCode()==KeyEvent.VK_ENTER && pause)//Spelet forts�tter efter paus
			pause=false;
	}

	public void pushCoord(int i, int x){//K�metod som tar emot koordinater f�r samtliga kroppsdelar
		intKo[antalK][0] = i;
		intKo[antalK][1] = x;
		antalK++;
	}
	
	public int popX(){//K�metod som returnerar X-koordinaten f�r samtliga kroppsdelar
		int f�rstaX = intKo[0][0];
		for (int i = 0; i < antalK - 1; i++)
			intKo[i][0] = intKo[i + 1][0];
		antalK--;
		return f�rstaX;
	}

	public int popY(){//K�metod som returnerar Y-koordinaten f�r samtliga kroppsdelar
		int f�rstaY = intKo[0][1];
		for (int i = 0; i < antalK; i++)
			intKo[i][1] = intKo[i + 1][1];
		return f�rstaY;
	}
	
	public void pushTangent(int t){//K�metod som tar emot tangenttryckningar
		tangentKo[antalT] = t;
		antalT++;
	}
	
	public int popTangent(){//K�metod som returnerar f�rsta tangenten i k�n
		int f�rstaT = tangentKo[0];
		for (int i = 0; i < antalT - 1; i++)
			tangentKo[i] = tangentKo[i+1];
		antalT--;
		return f�rstaT;
	}

	// Metod som sparar highscore f�r en spelomg�ng
	public void saveGame() {
		String name = JOptionPane.showInputDialog(null, "Sorry, Game Over!\nWrite your name:", "Highscore", JOptionPane.PLAIN_MESSAGE);
		
		highscore[placeringar++] = new Highscore(p, name);

		Sort(highscore, placeringar);//Sorterar
	}

	// metod som �terst�ller samtliga v�rden s� att man kan starta en ny omg�ng
	public void startNewGame() {
		p = 0;
		kropp = 3;
		antalK = 0;
		antalT = 0;
		forts�tt = 0;
		v�nta = 1;
		vx = hastighet;
		vy = 0;
		east = true;
		west = false;
		north = false;
		south = false;
		gameOver = false;

		// startkoordinater
		for (int i = 0; i < kropp; i++)
		{
			coord[i][0] = 100 + 10 * i;
			coord[i][1] = 100;
		}
	}
	
	//Metod som sorterar highscore
	public static void Sort(Highscore[] data, int antal){
		for(int m=1; m<antal; m++)
		{
			int position=m;
			Highscore temp=data[m];
			
			while(position>0 && data[position-1].compare(temp)>0)
			{
				data[position]=data[position-1];
				position--;
			}
			data[position]=temp;
		}
	}
	public void keyReleased(KeyEvent ke) {}

	public void keyTyped(KeyEvent ke) {}
}