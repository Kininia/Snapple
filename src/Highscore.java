
public class Highscore {
	//Datamedlemmar
	int score;
	String name;
	
	public Highscore(int s, String n){//Konstruktor
		score=s;
		name=n;
	}
	
	public int getScore(){//Returnerar po�ng
		return score;
	}
	
	public void setScore(int s){//�ndrar po�ng
		score=s;
	}
	
	public String getName(){//Returnerar namn
		return name;
	}
	
	public void setName(String n){//�ndrar namn
		name=n;
	}
	
	public String toString(){//Skriver ut po�ng & namn
		return score + " " + name;
	}
	public int compare(Highscore h){//Kontrollerar i vilken ordning tv� po�ngv�rden kommer
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