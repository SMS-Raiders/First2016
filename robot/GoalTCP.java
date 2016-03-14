/*
Object class for opening and listening on a TCP socket
to get the positioning data for the goal
*/
package org.usfirst.frc.team1984.robot;

import java.lang.Thread;
import java.net.Socket;
import java.io.*;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

class GoalTCP extends Thread
{

private static final String HOST_NAME = "10.19.84.10";
//private static final String HOST_NAME = "localhost";
private static final int    HOST_PORT = 4545;

public float dst;
public float ang;

private Socket goalSocket;
private BufferedReader socket_in;
private boolean loop;
private int num_pkts;

//Create the socket and start listening
public void open_socket()
    {
	num_pkts = 0;
    try
        {
        this.goalSocket = new Socket( HOST_NAME, HOST_PORT );
        this.socket_in = new BufferedReader( new InputStreamReader( goalSocket.getInputStream() ) );
        this.dst = 0.0f;
        this.ang = 0.0f;
        this.loop = true;
        System.out.println( "Socket created..." );
        }
    catch( Exception e )
        {
        System.out.println( "Error creating socket... " );
        this.dst = 0.0f;
        this.ang = 0.0f;
        this.loop = false;            
        }
    }

//Thread main loop, exits when loop is FALSE (i.e. when the object is closed)
public void run()
    {
    String next_data;
    String[] data_items;
    System.out.println( "Start of thread..." );
    SmartDashboard.putNumber("NumPackets",num_pkts++);
    while( this.loop )
        {
        //Just run at 10Hz, otherwise the console fills too quickly
        try
            {
            Thread.sleep( 100 );
            next_data = this.socket_in.readLine();
            if( next_data != null )
                {
                data_items = next_data.split( "," );
                this.set_dst( Float.parseFloat( data_items[0] ) );
                this.set_ang( Float.parseFloat( data_items[1] ) );
                SmartDashboard.putString("SocketData",next_data);
                SmartDashboard.putNumber("NumPackets",num_pkts++);
                }
            
            }
        catch( Exception e )
            {
            //Do nothing for socket errors...
            System.out.println( "Thread error..." + e.getMessage() );
            }
        }
    }

public synchronized float get_ang()
    {
    return( this.ang );
    }
public synchronized float get_dst()
    {
    return( this.dst );
    }
public synchronized void set_ang( float a )
    { 
    this.ang = a;
    }
public synchronized void set_dst( float d )
    {
    this.dst = d;
    }

//Close the socket and stop the thread
public void close()
    {
    if( this.loop )
        {
        this.loop = false;
        try
            {
            this.socket_in.close();
            this.goalSocket.close();
            }
        catch( Exception e )
            {
            System.out.println( "Socket close error..."  + e.getMessage() );
            }
        }
    }

public static void main( String args[] )
    {
    GoalTCP goal_sock = null;
    float last_dst = -1;
    float last_ang = -1;

    //Create the thread and wait until we ctrl-c to end the listening
    try
        {
        goal_sock = new GoalTCP();
        goal_sock.open_socket();
        goal_sock.start();
        System.out.println( "Client socket open..." );
        while( true )
            {
            if( last_dst != goal_sock.get_dst() || last_ang != goal_sock.get_ang() )
                {
                System.out.println( "Current dst = " + goal_sock.get_dst() + " Current ang = " + goal_sock.get_ang() );
                last_dst = goal_sock.get_dst();
                last_ang = goal_sock.get_ang();
                }
            }
        }
    catch( Exception e )
        {
        System.out.println( "Closing socket..." );
        if( goal_sock != null )
            {
            goal_sock.close();
            }
        }

    }

}