
public class Highscore {
	//Datamedlemmar
	int score;
	String name;
	
	public Highscore(int s, String n){//Konstruktor
		score=s;
		name=n;
	}
	
	public int getScore(){//Returnerar poäng
		return score;
	}
	
	public void setScore(int s){//Ändrar poäng
		score=s;
	}
	
	public String getName(){//Returnerar namn
		return name;
	}
	
	public void setName(String n){//Ändrar namn
		name=n;
	}
	
	public String toString(){//Skriver ut poäng & namn
		return score + " " + name;
	}
	public int compare(Highscore h){//Kontrollerar i vilken ordning två poängvärden kommer
		int test=0;
		if(score>h.score)
			test=-1;
		else if(score<h.score)
			test=1;
		else
			test=0;
		return test;
		
	}
}