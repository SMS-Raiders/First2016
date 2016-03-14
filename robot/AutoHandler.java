package org.usfirst.frc.team1984.robot;
import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class AutoHandler {
	public static void forward(PIDControl PID, Drivetrain base, double time, double speed, double angle)
	{
		double begin = Timer.getFPGATimestamp();
		while (Timer.getFPGATimestamp()-begin < time){
			base.forwardDrive(PID.setAngle(angle),speed);
		}
		base.stopDrive();
	}
	
	public static void turn(PIDControl PID, Drivetrain base, double time, double angle)
	{
		double begin = Timer.getFPGATimestamp();
		while (Timer.getFPGATimestamp()-begin < time){
			base.turnDrive(PID.setAngle(angle));
		}
		base.stopDrive();
	}
	public static void shoot( double speed, Spark intake, CANTalon shooter )
	{
		shooter.set(speed);
		Timer.delay(3);
		intake.set(-1);
		Timer.delay(1);
		intake.set(0);
		Timer.delay(1);
		shooter.set(0);
	}
}
