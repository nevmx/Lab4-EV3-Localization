import lejos.robotics.SampleProvider;

public class USLocalizer {
	public enum LocalizationType { FALLING_EDGE, RISING_EDGE };
	public static double ROTATION_SPEED = 30;
	private static int FILTER_OUT = 5;

	private Odometer odo;
	private SampleProvider usSensor;
	private float[] usData;
	private LocalizationType locType;
	private int filterControl;
	private float distance;
	
	public USLocalizer(Odometer odo,  SampleProvider usSensor, float[] usData, LocalizationType locType) {
		this.odo = odo;
		this.usSensor = usSensor;
		this.usData = usData;
		this.locType = locType;
		filterControl = 0;
		distance = 0;
	}
	
	public void doLocalization() {
		double [] pos = new double [3];
		double angleA, angleB;
		
		if (locType == LocalizationType.FALLING_EDGE) {
			// rotate the robot until it sees no wall
			
			// keep rotating until the robot sees a wall, then latch the angle
			
			// switch direction and wait until it sees no wall
			
			// keep rotating until the robot sees a wall, then latch the angle
			
			// angleA is clockwise from angleB, so assume the average of the
			// angles to the right of angleB is 45 degrees past 'north'
			
			// update the odometer position (example to follow:)
			odo.setPosition(new double [] {0.0, 0.0, 0.0}, new boolean [] {true, true, true});
		} else {
			/*
			 * The robot should turn until it sees the wall, then look for the
			 * "rising edges:" the points where it no longer sees the wall.
			 * This is very similar to the FALLING_EDGE routine, but the robot
			 * will face toward the wall for most of it.
			 */
			
			//
			// FILL THIS IN
			//
		}
	}
	
	private float getFilteredData() {
		usSensor.fetchSample(usData, 0);
		float distance = usData[0];
		
		
		// Filter code taken from Lab 1
		if (distance >= 255 && filterControl < FILTER_OUT) {
			// bad value, do not set the distance var, however do increment the
			// filter value
			filterControl++;
		} else if (distance >= 255) {
			// We have repeated large values, so there must actually be nothing
			// there: leave the distance alone
			this.distance = distance;
		} else {
			// distance went below 255: reset filter and leave
			// distance alone.
			filterControl = 0;
			this.distance = distance;
		}
				
		return this.distance;
	}

}
