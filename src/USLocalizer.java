import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.LCD;
import lejos.hardware.lcd.TextLCD;
import lejos.robotics.SampleProvider;

public class USLocalizer {
	public enum LocalizationType { FALLING_EDGE, RISING_EDGE };
	private final static int ROTATION_SPEED = 60;
	private final static int FILTER_OUT = 10;
	private final static int UPPER_NOISE_BOUND = 110;
	private final static int LOWER_NOISE_BOUND = 100;
	private final static int FILTER_VALUE = 200;
	
	private Odometer odo;
	private SampleProvider usSensor;
	private float[] usData;
	private LocalizationType locType;
	private Navigation navigation;
	private int filterControl;
	private float distance;
	
	public USLocalizer(Odometer odo,  SampleProvider usSensor, float[] usData, LocalizationType locType) {
		this.odo = odo;
		this.usSensor = usSensor;
		this.usData = usData;
		this.locType = locType;
		this.navigation = new Navigation(odo);
		filterControl = 0;
		distance = 0;
	}
	
	public void doLocalization() {
		double [] pos = new double [3];
		double angleA, angleB;
		double deltaTheta = 0.0;
		
		navigation.setSpeeds(-ROTATION_SPEED, ROTATION_SPEED);
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		boolean robotIsFacingWall = getFilteredData() < ((UPPER_NOISE_BOUND + LOWER_NOISE_BOUND)/2.0);
		
		if (locType == LocalizationType.FALLING_EDGE) {
			
			// rotate the robot until it sees no wall			
			while (robotIsFacingWall) {
				// Robot will keep rotating until it no longer faces the wall
				if (getFilteredData() > UPPER_NOISE_BOUND) {
					robotIsFacingWall = false;
				}
			}
			
			// No wall
			Sound.beep();
			
			boolean wasWithinMargin = false;
			double angleOne = 0.0;
			double angleTwo = 0.0;
			
			// keep rotating until the robot sees a wall, then latch the angle
			while (!robotIsFacingWall) {
				float distance = getFilteredData();
				if (wasWithinMargin && distance < LOWER_NOISE_BOUND) {
					angleTwo = odo.getAng();
					robotIsFacingWall = true;
				} else if (distance < UPPER_NOISE_BOUND) {
					wasWithinMargin = true;
					angleOne = odo.getAng();
				}
			}
			
			// Wall
			Sound.twoBeeps();
			
			angleA = (angleOne + angleTwo)/2.0;
			
			// switch direction and wait until it sees no wall
			navigation.setSpeeds(ROTATION_SPEED, -ROTATION_SPEED);
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			while (robotIsFacingWall) {
				// Robot will keep rotating until it no longer faces the wall
				if (getFilteredData() > UPPER_NOISE_BOUND) {
					robotIsFacingWall = false;
				}
			}
			
			Sound.beep();
			
			wasWithinMargin = false;
			
			// keep rotating until the robot sees a wall, then latch the angle
			while (!robotIsFacingWall) {
				float distance = getFilteredData();
				if (wasWithinMargin && distance < LOWER_NOISE_BOUND) {
					angleTwo = odo.getAng();
					robotIsFacingWall = true;
				} else if (distance < UPPER_NOISE_BOUND) {
					wasWithinMargin = true;
					angleOne = odo.getAng();
				}
			}
			Sound.twoBeeps();
			navigation.setSpeeds(0, 0);
			angleB = (angleOne + angleTwo)/2.0;
			
			// angleA is clockwise from angleB, so assume the average of the
			// angles to the right of angleB is 45 degrees past 'north'
			
			if (angleA < angleB) {
				deltaTheta = 45.0 - ((angleA + angleB) / 2.0);
			} else {
				deltaTheta = 225.0 - ((angleA + angleB) / 2.0);
			}
			
		} else {
			/*
			 * The robot should turn until it sees the wall, then look for the
			 * "rising edges:" the points where it no longer sees the wall.
			 * This is very similar to the FALLING_EDGE routine, but the robot
			 * will face toward the wall for most of it.
			 */
						
			while (!robotIsFacingWall) {
				if (getFilteredData() < LOWER_NOISE_BOUND) {
					robotIsFacingWall = true;
					Sound.twoBeeps();
				}
			}
			
			double angleOne = 0.0;
			double angleTwo = 0.0;
			boolean wasWithinMargin = false;
			
			while (robotIsFacingWall) {
				float distance = getFilteredData();
				if (wasWithinMargin && distance > UPPER_NOISE_BOUND) {
					angleTwo = odo.getAng();
					robotIsFacingWall = false;
					Sound.beep();
				} else if (distance > LOWER_NOISE_BOUND) {
					wasWithinMargin = true;
					angleOne = odo.getAng();
				}
			}
			
			angleA = (angleOne + angleTwo)/2.0;
			navigation.setSpeeds(ROTATION_SPEED, -ROTATION_SPEED);
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			while (!robotIsFacingWall) {
				if (getFilteredData() < LOWER_NOISE_BOUND) {
					robotIsFacingWall = true;
					Sound.twoBeeps();
				}
			}
			
			wasWithinMargin = false;
			
			while (robotIsFacingWall) {
				float distance = getFilteredData();
				if (wasWithinMargin && distance > UPPER_NOISE_BOUND) {
					angleTwo = odo.getAng();
					robotIsFacingWall = false;
					Sound.beep();
				} else if (distance > LOWER_NOISE_BOUND) {
					wasWithinMargin = true;
					angleOne = odo.getAng();
				}
			}
			
			angleB = (angleOne + angleTwo)/2.0;
			navigation.setSpeeds(0, 0);
			
			if (angleA < angleB) {
				deltaTheta = 225.0 - ((angleA + angleB) / 2.0);
			} else {
				deltaTheta = 45.0 - ((angleA + angleB) / 2.0);
			}
		}
		odo.setPosition(new double[] {0.0, 0.0, Odometer.fixDegAngle(odo.getAng() + deltaTheta)}, new boolean[] {false, false, true});
		navigation.turnTo(0.0, true);
	}
	
	private float getFilteredData() {
		usSensor.fetchSample(usData, 0);
		float distance = usData[0] * (float)100.0;
		
		
		// Filter code taken from Lab 1
		if (distance >= FILTER_VALUE && filterControl < FILTER_OUT) {
			// bad value, do not set the distance var, however do increment the
			// filter value
			filterControl++;
		} else if (distance >= FILTER_VALUE) {
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
