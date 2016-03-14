//Final Code for the 2016 Stronghold Game

package org.usfirst.frc.team1984.robot;

import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.CANTalon.TalonControlMode;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
//import com.ni.vision.NIVision;
import com.ni.vision.NIVision.*;


public class Robot extends IterativeRobot {
		private int 			drive = 1,
								startBack = 0;
		private Joystick		controller;
		private Drivetrain  	base;
		private DigitalInput	switchy;
		private Spark			intake;
		private CANTalon        shooter;
		private Solenoid        hood;
		private PIDControl		PID;
		private SendableChooser autonomousChooser;
		
		//private Timer 			time;
		int session;
		Image frame;
		ADIS16448_IMU imu;
		double shooter_p = 0;
		double goal_spd = 0;
		double adjusted_spd;
		double shooter_speed;
		
		 private GoalTCP goal_sock;
		 public float vision_dst;
		 public float vision_ang;    
		
    public void robotInit() 
    {
    	 controller 	= new Joystick(0);
    	 base 			= new Drivetrain(0,1,2,3, controller);
    	 intake 		= new Spark(4);
    	 switchy 		= new DigitalInput(0);
    	 shooter		= new CANTalon(1);
    	 shooter.setFeedbackDevice( CANTalon.FeedbackDevice.CtreMagEncoder_Relative );
    	 shooter.changeControlMode(TalonControlMode.Speed);
    	 shooter.setInverted(false);
    	 hood 			= new Solenoid(0);
    	 for(int x = 0; x > 10; x++)
    	 {
    	 try{
    		 imu = new ADIS16448_IMU();
    		 //Timer.delay(1);
    		 } catch ( Exception e) {
    			 System.out.println("gyro exception");

    		 }//Connecting to the breakout board
    	 Timer.delay(.2);
    	 }
    	 PID 			= new PIDControl(imu);
    	 PID.setPID(0.01, 0.8, 0);
    	 PID.setMotorDeadZone(.5);
    	
    	 //shootingTime   = new Timer();
    	 SmartDashboard.putNumber("Motor Speed",600);
    	 SmartDashboard.putNumber("Shooter Speed",0);
    	 
    	 //Autonomous Chooser code///////////////////////////////////////////////////////////////////////
    	 autonomousChooser   = new SendableChooser();
    	 autonomousChooser.addDefault("Don't Move", Integer.valueOf(0));
     	 autonomousChooser.addObject("Move", Integer.valueOf(1));
     	 autonomousChooser.addObject("Other Defense", Integer.valueOf(2));
     	 autonomousChooser.addObject("Test", Integer.valueOf(3));
     	 SmartDashboard.putData("Autonomous Chooser",autonomousChooser);
    	 
    	 /*Smartdashboard Testing shooter PID////////////////////////////////////////////////////////////
    	 SmartDashboard.putNumber( "Shooter_Speed", 0.0 );
    	 SmartDashboard.putNumber( "shooter_p", 0.0 );
    	 SmartDashboard.putNumber( "shooter_i", 0.0 );
    	 SmartDashboard.putNumber( "shooter_d", 0.0 );
    	 SmartDashboard.putNumber( "p * shooter speed", 0.0 );
    	 */
    	 
     	 //Initialize socket thread//////////////////////////////////////////////////////////////////////
     	 try
	        {
	        goal_sock = new GoalTCP();
	        goal_sock.open_socket();
	        goal_sock.start();
	        System.out.println( "Client socket open..." );
	        }
	    catch( Exception e )
	        {
	        System.out.println( "Closing socket..." );
	        if( goal_sock != null )
	            {
	            goal_sock.close();
	            }
	        }
     	 
    	 //Camera on Smartdashboard//////////////////////////////////////////////////////////////////////
    	 //frame = NIVision.imaqCreateImage(NIVision.ImageType.IMAGE_RGB, 0);
    	 //session = NIVision.IMAQdxOpenCamera("cam0",
           //      NIVision.IMAQdxCameraControlMode.CameraControlModeController);//Cam1 = stupid round thingy
         //NIVision.IMAQdxConfigureGrab(session);
//         SmartDashboard.putNumber("width", 0);
//         SmartDashboard.putNumber("height", 0);
//         SmartDashboard.putNumber("top - x", 0);
//         SmartDashboard.putNumber("top - y", 0);
    	 
    }
	
    public void teleopPeriodic() {
    	//NIVision.IMAQdxStartAcquisition(session);
    		/*NIVision.Rect rect = new NIVision.Rect(	(int)SmartDashboard.getNumber("top - y"), 
					   								(int)SmartDashboard.getNumber("top - x"), 
					   								(int)SmartDashboard.getNumber("height"), 
					   								(int)SmartDashboard.getNumber("width"));
			*/
		while (isOperatorControl() && isEnabled()) {
    	//	NIVision.IMAQdxGrab(session, frame, 1);

    		//NIVision.imaqDrawShapeOnImage(frame, frame, rect,
    		//DrawMode.DRAW_VALUE, ShapeMode.SHAPE_RECT, 0.0f);
    	//CameraServer.getInstance().setImage(frame);
    	
    	//Code for when the robot is running//////////////////////////////////////////////////////////
    	if(imu != null)
		SmartDashboard.putNumber("Gyro", imu.getAngleZ()%360);
    	shooter_speed	= SmartDashboard.getNumber("Shooter Speed");
    	//SmartDashboard.putNumber( "Enc velocity", shooter.getEncVelocity());
    	if(controller.getRawButton(8))
    		imu.reset();
 
    	//Get camera angle and dist
    	vision_dst = goal_sock.get_dst();
    	vision_ang = goal_sock.get_ang();
    	SmartDashboard.putNumber("VisionDst", vision_dst );
    	SmartDashboard.putNumber("VisionAng", vision_ang );
    	SmartDashboard.putNumber("Enc Val", shooter.getEncVelocity());
    	//Change Driving Controls////////////////////////////////////////////////////////////////////
    	if(controller.getRawButton(3) && drive == 0)
    	{
			drive = 1;
    		Timer.delay(0.25);
    	}
    	
    	if(controller.getRawButton(3) && drive == 1)
    	{
			drive = 0;
			Timer.delay(0.25);
			startBack = 1;
    	}
    	
    	//Switch for both driving directions//////////////////////////////////////////////////////////
    	switch(drive)
    	{
    		case 1: // forward driving
    			base.forwardDrive();
    			
    			if (controller.getRawButton(6))//shoot out bottom
    				intake.set(1);
    			else
    				intake.set(0);
    			
    			if(controller.getRawAxis(3) > 0.9)//far shot
    			{
    				//Shooty Code///////////////////////////////
    				shooter.setPID( 1, 0, 0);
    		    	goal_spd = shooter_speed * 190;
    		    	adjusted_spd = ( (goal_spd - shooter.getEncVelocity())  + goal_spd ) / 190;
    		    	    
    				hood.set(true);
    				shooter.set(-700);//-adjusted_spd );
    				Timer.delay(3);
//    				SmartDashboard.putNumber("Enc Speed", shooter.getEncVelocity());
    				intake.set(-1);
    				Timer.delay(1);
    				intake.set(0);
    				Timer.delay(1);
    				shooter.set(0);
    				
    				hood.set(false);
    			}
    			
    			if(controller.getRawAxis(2) > 0.9)//close shot
    			{
    				//Shooty Code///////////////////////////////
    				shooter.setPID( 1, 0, 0);
    		    	goal_spd = shooter_speed * 190;
    		    	adjusted_spd = ( (goal_spd - shooter.getEncVelocity())  + goal_spd ) / 190;
    		    	
    				shooter.set(-700);
    				Timer.delay(3);
    				//SmartDashboard.putNumber("Enc Speed", shooter.getEncVelocity());
    				intake.set(-1);
    				Timer.delay(1);
    				intake.set(0);
    				Timer.delay(1);
    				shooter.set(0);
    			}
    			break;
    			
    		case 0: //backward driving
    			base.backwardDrive();
    			
    			if (controller.getRawButton(6))//shoot out bottom
    			{
    				intake.set(1);
    			}
    			else if(switchy.get()) //switchy is true when not pressed
    			{
    				intake.set(-.75);
    				startBack = 0;
    			}
    			else
    			{
    				intake.set(0);
    				if(startBack==0)
    					drive = 1;
    			}
    			break;
    	}
    	Timer.delay(0.001);
    }
    //NIVision.IMAQdxStopAcquisition(session);

    }
    
	public void disabledPeriodic() 
	{
/*		if(imu == null)
		{
			try
			{
				imu = new ADIS16448_IMU();
				
			}catch( Error e){}
		}
		
*/		if(imu != null)
			SmartDashboard.putNumber("Gyro", imu.getAngleZ()%360);
		if(controller.getRawButton(7))
		{
			//while(true){try{imu = new ADIS16448_IMU();break;}catch( Error e){}}
			if(imu != null)
				imu.reset();
			else
				 try{imu = new ADIS16448_IMU();}catch( Exception e){}
			DriverStation.reportError("YAY Gyro Time ", false);
			Timer.delay(.5);
		}
		if(controller.getRawButton(8))
		{
    		goal_sock.close();
    		try
    	    {
    	        goal_sock = new GoalTCP();
    	        goal_sock.open_socket();
    	        goal_sock.start();
    	        System.out.println( "Client socket open..." );
    	    }
    	    catch( Exception e )
    	    {
    	        System.out.println( "Closing socket..." );
    	        if( goal_sock != null )
    	        {
    	            goal_sock.close();
    	        }
    	    }
    		DriverStation.reportError("YAY Socket Time ", false);
    		Timer.delay(.5);
		}
	}

    public void autonomousInit() {
    	int automode = ((Integer) autonomousChooser.getSelected()).intValue();
    	double current_angle;
    	if(imu != null)
    		current_angle = (double)(imu.getAngleZ()%360);
    	else
    		current_angle = 0;
    	switch(automode)
    	{
    	case 0: //"Don't Move"
    		base.stopDrive();
    		break;
    		
    	case 1: //"Low Bar"
    		
    		AutoHandler.forward(PID,base, 2.5, .8, current_angle);//0 degrees
    		if(imu != null)
        		current_angle = (double)(imu.getAngleZ()%360);
        	else
        		current_angle = 0;
    		//AutoHandler.forward(PID,base, 1, .7,current_angle+45);//45 degrees turn
    		vision_ang = goal_sock.get_ang();
    		//AutoHandler.turn(PID,base,2,current_angle+vision_ang);
    		//hood.set(true);
    		//AutoHandler.shoot( -700, intake, shooter );
    		//hood.set(false);
    		
    		break;
    		
    	case 2://"Other Defense"
    		//AutoHandler.forward(PID, base, 2, 1, current_angle);
    		vision_ang = goal_sock.get_ang();
    		AutoHandler.turn(PID,base,2,current_angle+vision_ang);
    		hood.set(true);
    		AutoHandler.shoot( -700, intake, shooter );
    		hood.set(false);
    		break;
    		
    	case 3://Test Things
    		current_angle = (double)(imu.getAngleZ()%360);
    		vision_dst = goal_sock.get_dst();
    		while(vision_dst < 20 && vision_dst > 8.5)
    		{
    			base.forwardDrive(PID.setAngle(current_angle),.6);
    			vision_dst = goal_sock.get_dst();
    		}
    		vision_ang = goal_sock.get_ang();
    		AutoHandler.turn(PID,base,2,current_angle+vision_ang);
    		
    		break;
    	}
    }
    
    //Other stuff for later programming
    public void testPeriodic() 
    {
    	/*Testing Code////////////////////////////////////////////////////////////////////////////////
    	if(controller.getRawButton(7))
    		hood.set(true);
    	else
    		hood.set(false);
    	*/
    	
    	/*shooter.set( SmartDashboard.getNumber("Shooter_Speed"));
    	shooter.setPID( SmartDashboard.getNumber( "shooter_p"), SmartDashboard.getNumber( "shooter_i"), SmartDashboard.getNumber( "shooter_d"));
    	goal_spd = SmartDashboard.getNumber("Shooter_Speed") * 190;
    	adjusted_spd = ( (goal_spd - shooter.getEncVelocity())  + goal_spd ) / 190;
    	shooter.set( adjusted_spd );    
    	SmartDashboard.putNumber( "p * shooter speed", adjusted_spd );
    	
    	if (controller.getRawButton(6))
		{
			intake.set(-1);
		}else
			intake.set(0);
    	*/
    }
    public void autonomousPeriodic() {}
    public void teleopInit() {}
    public void disabledInit(){}
}