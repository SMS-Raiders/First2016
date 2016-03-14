package org.usfirst.frc.team1984.robot;


public class PIDControl 
{
	private ADIS16448_IMU imu;
	private float p;
	private float i;
	private float d;
	private float motorDeadZone;
	private float gyroDeadZone;
	private float topSpeed;
	
	/**
	 * Sets up the gyro to be used in PID
	 *  
	 * @param imu ADIS16448 IMU
	 * @param motorDeadZone Auto set to: 0.6
	 * @param gyroDeadZone Auto set to: 0.01
	 * @param topSpeed Auto set to: 0.9
	 */
	public PIDControl(ADIS16448_IMU imu)
	{
		this.imu = imu;
		motorDeadZone = (float)0.6;
		gyroDeadZone = (float)0.01;
		topSpeed = (float)0.9;
	}
	
	/**
	 * Sets the values of the PID controls
	 * @param p
	 * @param i
	 * @param d
	 */
	public void setPID(double p, double i, double d)
	{
		this.p = (float)p;
		this.i = (float)i;
		this.d = (float)d;
	}
	/**
	 * Sets the dead zone that the motors won't move the robot.
	 * @param d value from 0 to 1
	 */
	public void setMotorDeadZone(double d)
	{
		motorDeadZone = (float)d;
	}
	
	/**
	 * Sets the value the gyro that will no longer make the robot move if 
	 * it is close to the target.
	 * @param g Have fun guessing; maybe 0.01
	 */
	public void setGyroDeadZone(double g)
	{
		gyroDeadZone = (float)g;
	}
	
	/**
	 * Sets the top speed that you will allow the robot to move if it 
	 * is far from the desired angle.
	 * @param s value 0 to 1
	 */
	public void setTopSpeed(double s)
	{
		topSpeed = (float) s;
	}
	
	/**
	 * @param angle -360 to 360 degrees
	 * @return value -1 to 1 to move the robot
	 */
	public double setAngle(double angleIn)
	{
		float m_angle = 0;
		float goal_angle;
		float angle;
		float i_angle = 0;
		float set_angle;
		float last_angle = 0;
		float d_angle = 0;
		float p_angle;
		float mod_angle;
		
		goal_angle = (float) angleIn;
		if(imu != null)
    	angle = ( float)( imu.getAngleZ() ) % 360;
		else
		angle = goal_angle;
		set_angle = ( goal_angle - angle ); 
		
		if( set_angle < -180 )
		{
			set_angle += 360;
		}
		if( set_angle > 180 )
		{
			set_angle -= 360;
		}
		d_angle = last_angle - set_angle;
		d_angle *= d;
		i_angle = i_angle + set_angle;
		p_angle = set_angle;
		p_angle *= p;
		
		last_angle = set_angle;
		mod_angle = ( p_angle + d_angle + ( i * i_angle ) );
		
		float scale_angle;
		scale_angle = ( mod_angle ) / 180;
		scale_angle /= 2;
		m_angle = scale_angle;
		if( scale_angle > gyroDeadZone )//.1
		{
			m_angle += motorDeadZone;//0.6
		}else if( scale_angle < -gyroDeadZone)//.1
		{
			m_angle -= motorDeadZone;//0.6
		}
		if( m_angle > topSpeed)//1
		{
			m_angle = (float)topSpeed;//1
		}
		if( m_angle < -topSpeed)//1
		{
			m_angle = (float)-topSpeed;//1
		}
		return m_angle;
	}
}
