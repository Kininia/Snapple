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

	private int p = 0, hastighet = 10;// poang far spelare && antal pixlar ormen flyttar sig/uppdatering
	private int[][] coord = new int[250][2], intKo = new int[250][2];//intfalt far koordinater far kroppen && infalt som sparar koordinaterna
	private int[] tangentKo = new int[15];//intfalt som haller reda pa intryckta tangenter
	private int a = 0, b = 0;// omodifierade koordinater far ett "startapple"
	int vx = hastighet, vy = 0;// hastigheter far ormen
	int kropp = 3/*antal kroppsdelar*/ , antalK = 0/*antal sparade kroppsdelar*/, antalT = 0/*antal sparade tangenter*/;
	int fortsatt = 0/*se langre ner*/, vanta = 1/*se langre ner*/, placeringar = 0/*antal spelade omgangar*/;

	private Highscore[] highscore = new Highscore[100];//Highscore-falt innehallande en int och en string/position
	private Font storre = new Font("Arial", Font.BOLD, 15); // typsnitt far gameover text
	private boolean north = false, south = false, west = false, east = true;// riktningar
	boolean gameOver = false/*game over?*/, taken = true/*apple taget?*/, newGame = false/*nytt spel?*/, pause = false/*paus?*/;
	Image bufferImage;
	Graphics bufferGraphics;

	public void init() {//Initialiseringsmetod, anropas automatiskt i barjan
		setSize(500, 400);//Appletens storlek
		setBackground(Color.black);// Bakgrundsfarg

		// Startkoordinater
		for (int i = 0; i < kropp; i++) {
			coord[i][0] = 100 + 10 * i;
			coord[i][1] = 100;
		}
		addKeyListener(this);// Appleten lyssnar pa tangenttryck

		Thread th = new Thread(this);// en egen aktivitet till programmet
		th.start();

		setVisible(true); // istallet far i main

		bufferImage = createImage(500, 400);// Skapar en buffert till grafiken i appleten
		bufferGraphics = bufferImage.getGraphics();
		bufferGraphics.setColor(Color.black);//Fargrundsfarg
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
			bufferGraphics.setFont(storre);
			bufferGraphics.drawString(" " + p, 250, 20);//Skriver ut spelarens poang
			
			if (pause)//Om det ar paus skrivs "Pause"
				bufferGraphics.drawString("Pause", 240, 100);

			for (int i = 0; i < kropp; i++) {//Ritar ut ormen
				bufferGraphics.setColor(Color.white);
				bufferGraphics.fillOval(coord[i][0], coord[i][1], 10, 10);// Kroppsdel
			}

			if (coord[0][0] == a && coord[0][1] == b)// Om ormens huvud och applet hamnar pa samma koordinater
			{
				taken = true;
				p++;//Poangen akar med 1
				kropp++;//Langden pa kroppen akar med 1
			}

			if (taken)// Om applet ar taget skapas ett nytt slumpvis
			{
				int x = (int) (Math.random() * 37);
				int y = (int) (Math.random() * 27);
				a = (10 * x) + 40;
				b = (10 * y) + 40;
				taken = false;
			}

			bufferGraphics.setColor(Color.green);
			bufferGraphics.fillOval(a, b, 10, 10);//ritar ett grant apple
			g.drawImage(bufferImage, 0, 0, this);
		}

		else//Om det blir game over
		{
			saveGame();//se langre ner
			
			bufferGraphics.setColor(Color.black);
			bufferGraphics.fillRect(0, 0, 500, 400);//Ritar aver ormen och applena
			bufferGraphics.setColor(Color.red);
			bufferGraphics.setFont(storre);
			bufferGraphics.drawString("Highscore:", 190, 110);//Skriver rubriken "Highscore"
			
			//Skriver ut highscorelistan (om det ar mindre an 5 spelomgangar)
			if(placeringar<5)
			{
			for (int i=0; i<placeringar; i++)
				bufferGraphics.drawString(""+highscore[i], 190, 135+(i*25));//Skriver ut poang och namn
			}
			//Skriver ut highscorelistan, top 5 (om det ar mer an 5 spelomgangar)
			else
			{
				for (int i=0; i<5; i++)
					bufferGraphics.drawString(""+highscore[i], 190, 135+(i*25));//Skriver ut poang och namn
			}
			
			for(int x=3; x>0; x--){//Starta om spelet "timer"
				bufferGraphics.setColor(Color.black);
				bufferGraphics.fillRect(145, 260, 250, 30);
				bufferGraphics.setColor(Color.red);
				bufferGraphics.drawString("The game will restart in " + x + " second(s)", 150, 280);
				g.drawImage(bufferImage, 0, 0, this);
				try {Thread.sleep(1000);} catch (InterruptedException ie) {}
			}
			startNewGame();//se langre ner
		}
	}

	public void run(){

		while(true)
		{
			if (!gameOver)//Sa lange det inte ar game over...
			{
				
				if (!pause && vanta == 1)//..och spelet inte ar pausat...
				{
					for (int i = 0; i < kropp - 1; i++)
					{
						pushCoord(coord[i][0], coord[i][1]);//...laggs alla kroppsdelar utom sista i ett falt
					}
					
					if(antalT==1)//Svang sker bara om en enda tangent ar intryckt
					{
						switch(popTangent())//Tar emot farsta tangentvardet
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
						case 3://vanster
							west=true;
							north=false;
							south=false;
							break;
						case 4://hager
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
					
					//...och resterande kroppsdelar far koordinaterna som kroppsdelen framfar hade
					for (int i = 1; i < kropp; i++)
					{
						coord[i][0] = (popX());
						coord[i][1] = (popY());
						
						//Om huvudet och en kroppsdel ar pa samma koordinater blir det game over - man har krockat med sig sjalv
						if (coord[0][0] == coord[i][0] && fortsatt == 2)
							if (coord[0][1] == coord[i][1] && !pause)
								gameOver = true;
					}
				}
				
				if(antalT>1)//Om fler an en tangent trycktes in nollstalls tangenterna
					antalT=0;
				
				//"Tammer" farlyttningskan efter att man pausat spelet
				if (vanta < 1)
					vanta++;
				
				//Fortsatt ser till att det inte blir game over direkt i barjan av spelet
				if (fortsatt < 2)
					fortsatt++;

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

				if (west)//vanster
				{
					vx = -hastighet;
					vy = 0;
				}

				if (east)//hager
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
		  
		if(ke.getKeyCode()==KeyEvent.VK_A && !east && !pause)//vanster
			pushTangent(3);
		
		if(ke.getKeyCode()==KeyEvent.VK_D && !west && !pause)//hager
			pushTangent(4);
		
		if(ke.getKeyCode()==KeyEvent.VK_SPACE && !pause)//paus
		{
			pause=true;
			fortsatt=0;
			vanta=0;
		}
		
		if(ke.getKeyCode()==KeyEvent.VK_ENTER && pause)//Spelet fortsatter efter paus
			pause=false;
	}

	public void pushCoord(int i, int x){//Kametod som tar emot koordinater far samtliga kroppsdelar
		intKo[antalK][0] = i;
		intKo[antalK][1] = x;
		antalK++;
	}
	
	public int popX(){//Kametod som returnerar X-koordinaten far samtliga kroppsdelar
		int farstaX = intKo[0][0];
		for (int i = 0; i < antalK - 1; i++)
			intKo[i][0] = intKo[i + 1][0];
		antalK--;
		return farstaX;
	}

	public int popY(){//Kametod som returnerar Y-koordinaten far samtliga kroppsdelar
		int farstaY = intKo[0][1];
		for (int i = 0; i < antalK; i++)
			intKo[i][1] = intKo[i + 1][1];
		return farstaY;
	}
	
	public void pushTangent(int t){//Kametod som tar emot tangenttryckningar
		tangentKo[antalT] = t;
		antalT++;
	}
	
	public int popTangent(){//Kametod som returnerar farsta tangenten i kan
		int farstaT = tangentKo[0];
		for (int i = 0; i < antalT - 1; i++)
			tangentKo[i] = tangentKo[i+1];
		antalT--;
		return farstaT;
	}

	// Metod som sparar highscore far en spelomgang
	public void saveGame() {
		String name = JOptionPane.showInputDialog(null, "Sorry, Game Over!\nWrite your name:", "Highscore", JOptionPane.PLAIN_MESSAGE);
		
		highscore[placeringar++] = new Highscore(p, name);

		Sort(highscore, placeringar);//Sorterar
	}

	// metod som aterstaller samtliga varden sa att man kan starta en ny omgang
	public void startNewGame() {
		p = 0;
		kropp = 3;
		antalK = 0;
		antalT = 0;
		fortsatt = 0;
		vanta = 1;
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