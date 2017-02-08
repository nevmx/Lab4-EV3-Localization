import lejos.robotics.SampleProvider;

public class LightLocalizer {
	public static int ROTATION_SPEED = 60;
	
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
		navigation.turnTo(45, true);
		navigation.goForward(10.0);
		
		double finalX = 0.0;
		double finalY = 0.0;
		double finalTheta = 0.0;
		
		// start rotating and clock all 4 gridlines
		
		
		// do trig to compute (0,0) and 0 degrees
		
		odo.setPosition(new double[] {finalX, finalY, finalTheta}, new boolean[] {true, true, true});
		// when done travel to (0,0) and turn to 0 degrees
		navigation.travelTo(0.0, 0.0);
		navigation.turnTo(0.0, true);
	}

}
