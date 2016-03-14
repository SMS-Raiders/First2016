package org.usfirst.frc.team1984.robot;

import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.RobotDrive.MotorType;

public class Drivetrain 
{
	private Talon		left1,
						left2,
						right1,
						right2;
	private RobotDrive	base;
	private Joystick	stick;
	
	public Drivetrain(int l1, int l2, int r1, int r2, Joystick stick)
	{
		left1	= new Talon(l1);
		left2	= new Talon(l2);
		right1	= new Talon(r1);
		right2	= new Talon(r2);
		base = new RobotDrive(left1, left2, right1, right2);
		base.setInvertedMotor(MotorType.kFrontLeft, true);
		base.setInvertedMotor(MotorType.kRearLeft, true);
		this.stick = stick;
	}
	
	public void forwardDrive()
	{
		base.arcadeDrive(-stick.getY(), -stick.getX(), true);
	}
	
	public void backwardDrive()
	{
		base.arcadeDrive(stick.getY(), -stick.getX(), true);
	}
	
	//Autonomous Drive Code
	public void forwardDrive(double angle, double speed)
	{
		base.arcadeDrive(speed,-angle,true);
	}
	
	public void turnDrive(double angle)
	{
		base.arcadeDrive(0,-angle);
	}
	
	public void stopDrive()
	{
		base.arcadeDrive(0,0);
	}
}
