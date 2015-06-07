package dos.common.util;

public class HealthFactor implements Comparable<HealthFactor> {
	int healthfactor=1;
	public int compareTo(HealthFactor another){
		return this.healthfactor-another.healthfactor;
	}
}
