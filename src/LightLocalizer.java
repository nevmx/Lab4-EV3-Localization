import lejos.hardware.Sound;
import lejos.robotics.SampleProvider;

public class LightLocalizer {
	private static final double COLOR_SENSOR_RADIUS = 11.5;
	private static final int ROTATION_SPEED = 60;
	private static final double COLOR_SENSOR_BOUND = 0.40;
	
	private Odometer odo;
	private SampleProvider colorSensor;
	private float[] colorData;
	private Navigation navigation;
	
	public LightLocalizer(Odometer odo, SampleProvider colorSensor, float[] colorData) {
		this.odo = odo;
		this.colorSensor = colorSensor;
		this.colorData = colorData;
		this.navigation = new Navigation(odo);
	}
	
	public void doLocalization() {
		// drive to location listed in tutorial
		navigation.turnTo(45.0, true);
		navigation.goForward(10.0);
		
		double finalX = 0.0;
		double finalY = 0.0;
		double finalTheta = 0.0;
		
		// start rotating and clock all 4 gridlines
		navigation.setSpeeds(-ROTATION_SPEED, ROTATION_SPEED);
		int clockedLines = 0;
		
		boolean sensorAboveLine = false;
		
		double angleOne = 0.0;
		double angleTwo = 0.0;
		
		double[] angles = new double[4];
		
		while (clockedLines < 4) {
			// Get the color reading
			colorSensor.fetchSample(this.colorData, 0);

			if (this.colorData[0] < this.COLOR_SENSOR_BOUND && !sensorAboveLine) {
				sensorAboveLine = true;
				angleOne = odo.getAng();
			}
			
			else if (this.colorData[0] > this.COLOR_SENSOR_BOUND && sensorAboveLine) {
				angleTwo = odo.getAng();
				angles[clockedLines++] = (angleOne + angleTwo)/2.0;
				sensorAboveLine = false;
				Sound.beep();
			}
		}
		
		navigation.setSpeeds(0, 0);
		
		// do trig to compute (0,0) and 0 degrees
		// X
		double thetaX = Math.abs(angles[0] - angles[2]);
		
		if (thetaX > 180) {
			thetaX = 360 - thetaX;
		}
		
		finalX = -COLOR_SENSOR_RADIUS * Math.cos(Math.toRadians(thetaX / 2.0));
		
		// Y
		double thetaY = Math.abs(angles[1] - angles[3]);
		
		if (thetaY > 180) {
			thetaY = 360 - thetaY;
		}
		
		finalY = -COLOR_SENSOR_RADIUS * Math.cos(Math.toRadians(thetaY / 2.0));
		
		// Theta
		double deltaTheta = 270 + (thetaX/2.0) - angles[0];
		finalTheta = odo.getAng() + deltaTheta;
		
		if (finalTheta > 180.0) {
			finalTheta -= 180.0;
		}
		
		odo.setPosition(new double[] {finalX, finalY, finalTheta}, new boolean[] {true, true, true});
		// when done travel to (0,0) and turn to 0 degrees
		navigation.travelTo(0.0, 0.0);
		navigation.turnTo(0.0, true);
		
		odo.setPosition(new double[] {0.0,  0.0, 0.0}, new boolean[] {true, true, false});
	}

}
