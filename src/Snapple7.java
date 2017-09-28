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

	private int p = 0, hastighet = 10;// poäng för spelare && antal pixlar ormen flyttar sig/uppdatering
	private int[][] coord = new int[250][2], intKo = new int[250][2];//intfält för koordinater för kroppen && infält som sparar koordinaterna
	private int[] tangentKo = new int[15];//intfält som håller reda på intryckta tangenter
	private int a = 0, b = 0;// omodifierade koordinater för ett "startäpple"
	int vx = hastighet, vy = 0;// hastigheter för ormen
	int kropp = 3/*antal kroppsdelar*/ , antalK = 0/*antal sparade kroppsdelar*/, antalT = 0/*antal sparade tangenter*/;
	int fortsätt = 0/*se längre ner*/, vänta = 1/*se längre ner*/, placeringar = 0/*antal spelade omgångar*/;

	private Highscore[] highscore = new Highscore[100];//Highscore-fält innehållande en int och en string/position
	private Font större = new Font("Arial", Font.BOLD, 15); // typsnitt för gameover text
	private boolean north = false, south = false, west = false, east = true;// riktningar
	boolean gameOver = false/*game over?*/, taken = true/*äpple taget?*/, newGame = false/*nytt spel?*/, pause = false/*paus?*/;
	Image bufferImage;
	Graphics bufferGraphics;

	public void init() {//Initialiseringsmetod, anropas automatiskt i början
		setSize(500, 400);//Appletens storlek
		setBackground(Color.black);// Bakgrundsfärg

		// Startkoordinater
		for (int i = 0; i < kropp; i++) {
			coord[i][0] = 100 + 10 * i;
			coord[i][1] = 100;
		}
		addKeyListener(this);// Appleten lyssnar på tangenttryck

		Thread th = new Thread(this);// en egen aktivitet till programmet
		th.start();

		setVisible(true); // istället för i main

		bufferImage = createImage(500, 400);// Skapar en buffert till grafiken i appleten
		bufferGraphics = bufferImage.getGraphics();
		bufferGraphics.setColor(Color.black);//Förgrundsfärg
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
			bufferGraphics.setFont(större);
			bufferGraphics.drawString(" " + p, 250, 20);//Skriver ut spelarens poäng
			
			if (pause)//Om det är paus skrivs "Pause"
				bufferGraphics.drawString("Pause", 240, 100);

			for (int i = 0; i < kropp; i++) {//Ritar ut ormen
				bufferGraphics.setColor(Color.white);
				bufferGraphics.fillOval(coord[i][0], coord[i][1], 10, 10);// Kroppsdel
			}

			if (coord[0][0] == a && coord[0][1] == b)// Om ormens huvud och äpplet hamnar på samma koordinater
			{
				taken = true;
				p++;//Poängen ökar med 1
				kropp++;//Längden på kroppen ökar med 1
			}

			if (taken)// Om äpplet är taget skapas ett nytt slumpvis
			{
				int x = (int) (Math.random() * 37);
				int y = (int) (Math.random() * 27);
				a = (10 * x) + 40;
				b = (10 * y) + 40;
				taken = false;
			}

			bufferGraphics.setColor(Color.green);
			bufferGraphics.fillOval(a, b, 10, 10);//ritar ett grönt äpple
			g.drawImage(bufferImage, 0, 0, this);
		}

		else//Om det blir game over
		{
			saveGame();//se längre ner
			
			bufferGraphics.setColor(Color.black);
			bufferGraphics.fillRect(0, 0, 500, 400);//Ritar över ormen och äpplena
			bufferGraphics.setColor(Color.red);
			bufferGraphics.setFont(större);
			bufferGraphics.drawString("Highscore:", 190, 110);//Skriver rubriken "Highscore"
			
			//Skriver ut highscorelistan (om det är mindre än 5 spelomgångar)
			if(placeringar<5)
			{
			for (int i=0; i<placeringar; i++)
				bufferGraphics.drawString(""+highscore[i], 190, 135+(i*25));//Skriver ut poäng och namn
			}
			//Skriver ut highscorelistan, top 5 (om det är mer än 5 spelomgångar)
			else
			{
				for (int i=0; i<5; i++)
					bufferGraphics.drawString(""+highscore[i], 190, 135+(i*25));//Skriver ut poäng och namn
			}
			
			for(int x=3; x>0; x--){//Starta om spelet "timer"
				bufferGraphics.setColor(Color.black);
				bufferGraphics.fillRect(145, 260, 250, 30);
				bufferGraphics.setColor(Color.red);
				bufferGraphics.drawString("The game will restart in " + x + " second(s)", 150, 280);
				g.drawImage(bufferImage, 0, 0, this);
				try {Thread.sleep(1000);} catch (InterruptedException ie) {}
			}
			startNewGame();//se längre ner
		}
	}

	public void run(){

		while(true)
		{
			if (!gameOver)//Så länge det inte är game over...
			{
				
				if (!pause && vänta == 1)//..och spelet inte är pausat...
				{
					for (int i = 0; i < kropp - 1; i++)
					{
						pushCoord(coord[i][0], coord[i][1]);//...läggs alla kroppsdelar utom sista i ett fält
					}
					
					if(antalT==1)//Sväng sker bara om en enda tangent är intryckt
					{
						switch(popTangent())//Tar emot första tangentvärdet
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
						case 3://vänster
							west=true;
							north=false;
							south=false;
							break;
						case 4://höger
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
					
					//...och resterande kroppsdelar får koordinaterna som kroppsdelen framför hade
					for (int i = 1; i < kropp; i++)
					{
						coord[i][0] = (popX());
						coord[i][1] = (popY());
						
						//Om huvudet och en kroppsdel är på samma koordinater blir det game over - man har krockat med sig själv
						if (coord[0][0] == coord[i][0] && fortsätt == 2)
							if (coord[0][1] == coord[i][1] && !pause)
								gameOver = true;
					}
				}
				
				if(antalT>1)//Om fler än en tangent trycktes in nollställs tangenterna
					antalT=0;
				
				//"Tömmer" förlyttningskön efter att man pausat spelet
				if (vänta < 1)
					vänta++;
				
				//Fortsätt ser till att det inte blir game over direkt i början av spelet
				if (fortsätt < 2)
					fortsätt++;

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

				if (west)//vänster
				{
					vx = -hastighet;
					vy = 0;
				}

				if (east)//höger
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
		  
		if(ke.getKeyCode()==KeyEvent.VK_A && !east && !pause)//vänster
			pushTangent(3);
		
		if(ke.getKeyCode()==KeyEvent.VK_D && !west && !pause)//höger
			pushTangent(4);
		
		if(ke.getKeyCode()==KeyEvent.VK_SPACE && !pause)//paus
		{
			pause=true;
			fortsätt=0;
			vänta=0;
		}
		
		if(ke.getKeyCode()==KeyEvent.VK_ENTER && pause)//Spelet fortsätter efter paus
			pause=false;
	}

	public void pushCoord(int i, int x){//Kömetod som tar emot koordinater för samtliga kroppsdelar
		intKo[antalK][0] = i;
		intKo[antalK][1] = x;
		antalK++;
	}
	
	public int popX(){//Kömetod som returnerar X-koordinaten för samtliga kroppsdelar
		int förstaX = intKo[0][0];
		for (int i = 0; i < antalK - 1; i++)
			intKo[i][0] = intKo[i + 1][0];
		antalK--;
		return förstaX;
	}

	public int popY(){//Kömetod som returnerar Y-koordinaten för samtliga kroppsdelar
		int förstaY = intKo[0][1];
		for (int i = 0; i < antalK; i++)
			intKo[i][1] = intKo[i + 1][1];
		return förstaY;
	}
	
	public void pushTangent(int t){//Kömetod som tar emot tangenttryckningar
		tangentKo[antalT] = t;
		antalT++;
	}
	
	public int popTangent(){//Kömetod som returnerar första tangenten i kön
		int förstaT = tangentKo[0];
		for (int i = 0; i < antalT - 1; i++)
			tangentKo[i] = tangentKo[i+1];
		antalT--;
		return förstaT;
	}

	// Metod som sparar highscore för en spelomgång
	public void saveGame() {
		String name = JOptionPane.showInputDialog(null, "Sorry, Game Over!\nWrite your name:", "Highscore", JOptionPane.PLAIN_MESSAGE);
		
		highscore[placeringar++] = new Highscore(p, name);

		Sort(highscore, placeringar);//Sorterar
	}

	// metod som återställer samtliga värden så att man kan starta en ny omgång
	public void startNewGame() {
		p = 0;
		kropp = 3;
		antalK = 0;
		antalT = 0;
		fortsätt = 0;
		vänta = 1;
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