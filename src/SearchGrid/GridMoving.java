package SearchGrid;
import java.util.ArrayList;

import lejos.nxt.Button;
import lejos.nxt.LightSensor;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;
import lejos.nxt.TouchSensor;
import lejos.util.Delay;
import rp13.search.interfaces.Agenda;
import agendas.AgendaListA;
import search.SearchingFramework;
import Grid.Grid;
import Grid.Grid.RobotMove;
import Grid.GridSuccessorFunction;

	/** the class for part 2 grid
	 * 
	 * @author Nelson
	 * @param light front sensor
	 * @param ligh2 side sensor
	 * @param black black value
	 * @param sensor touchsensor
	 * 
	 * 
	 */
	public class GridMoving {

		private static LightSensor light,light2;
		private static MotorRun motor;
		private final static int black = 400;
		private static TouchSensor sensor;
		
		
	public static void main(String[] args) {
	
		 //initialize the grid situation

		GridSuccessorFunction function = new GridSuccessorFunction();
		Grid puzzle = new Grid(5,5);
		puzzle.setRobot(0,0);
		puzzle.setGoal(4,4);
		puzzle.setBlock(1,1,2,1);
		puzzle.setBlock(3,4 , 4,4);
		puzzle.setBlock(4,3,3,3);
		puzzle.setBlock(2, 2, 3, 2);
		puzzle.setBlock(3,0,4,0);
		Agenda<Grid> agenda = new AgendaListA<Grid>();
		SearchingFramework<RobotMove,Grid,GridSuccessorFunction > search 
		= new SearchingFramework<RobotMove,Grid,GridSuccessorFunction >
		(function, puzzle, agenda);
		search.Search();
		ArrayList<RobotMove> solution = new ArrayList<RobotMove>();
		solution.addAll(search.getResult());
		
		//to the moves
		System.out.println("Grid");
		Button.waitForAnyPress();
		int degree = 2;
		light = new LightSensor(SensorPort.S2);
		light2 = new LightSensor(SensorPort.S3);
		sensor = new TouchSensor(SensorPort.S1);
		motor = new MotorRun();
		Delay.msDelay(500);
		int index = 0;//index for the list of moves
		ArrayList<RobotMove> moved = new ArrayList<RobotMove>(); //record moves for detect online
		
		/** the algorithm is more or less the same as line following 
		 * 
		 */
		while(true){
			
				if(light.readNormalizedValue()<=black){
					motor.forward();
					Thread.yield();
					degree=2;
					//if side sensor reads black means it is cross turn when going forward
					if(light2.getNormalizedLightValue()<=black)
					{
						motor.AdjLeft(solution.get(index).r_move*90);//left(1)*90 forward(0)*90 right(-1)*90
						index++;//increase index every time robot turns (like a for loop)
						moved.add(solution.get(index));	// add move to the list to store for online detection
					}
				}
				if(light.readNormalizedValue()>black){
						motor.AdjLeft(degree);
						Thread.yield();
						degree=degree+2;	
				}
				if(light.readNormalizedValue()>black){
						motor.AdjRight(degree);
						Thread.yield();
						degree=degree+2;					
				}
				
			if(sensor.isPressed()) //if detect block online
			{
				// re initialize the new puzzle
				Grid currS = new Grid(search.getState(moved));// current position
				moved.remove(moved.size()-1);//remove last move
				Grid beforeS =new Grid(search.getState(moved));//the last position
				puzzle=new Grid(beforeS); // new puzzle to solve but different direction
				puzzle.setRobotDirection(currS.getRobotDirection());//robot facing new direction
				puzzle.setBlock(beforeS.getRobotX(),beforeS.getRobotY() , currS.getRobotX(), currS.getRobotY());
				//re initialize the searching stuff
				agenda = new AgendaListA<Grid>();
				function = new GridSuccessorFunction();
				search = new SearchingFramework<RobotMove,Grid,GridSuccessorFunction >(function, puzzle, agenda);
				search.Search();
				solution = new ArrayList<RobotMove>();
				solution.addAll(search.getResult());//solution be new path
				index=0;//reset index to 0
				moved = new ArrayList<RobotMove>();// new moved
				System.out.println(puzzle);//print the puzzle state for debug
				while(light2.getNormalizedLightValue()>black)
				{
					motor.backward(); //go backwards until reach back -> the state before bumping to wall
				}

			}
			if(index==solution.size()-1)
			{
				Sound.beep();//reaches goal
			}
			if(!search.Search()){ //if no solution then turn turn turn turn
				motor.TurnRight();
			}
		}	
	}			
}


