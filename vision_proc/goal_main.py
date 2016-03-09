#!/bin/python
#Goal finding and position estimation.
#This file handles the i/o operations and hands
#off image processing to proc_frame.py

#-------------------------------------------------------------------------------
#                                   IMPORTS
#-------------------------------------------------------------------------------
import cv2
import sys
import proc_frame
from Queue import Queue as Q
from GoalTCPServer import GoalTCPHandler, GoalTCPStartHandler

#-------------------------------------------------------------------------------
#                                  VARIABLES
#-------------------------------------------------------------------------------
debug = True
use_still = False
shared_q = Q()

#-------------------------------------------------------------------------------
#                                   CLASSES
#-------------------------------------------------------------------------------

#-------------------------------------------------------------------------------
#                                  PROCEDURES
#-------------------------------------------------------------------------------


def fake_callback( arg ):
    """ Fake Callback for slider bars"""
    pass

def main( dbg, use_picture ):
    """Main Loop, called from below"""

    #=====================================================================
    # Open the webcam and get an image if we aren't using a picture
    #=====================================================================
    if( not use_picture ):

        print( "Opening Video Capture on Device 0" )
        vidcap = cv2.VideoCapture( 0 )

        #=================================================================
        # Get the first frame, or loop until the feed opens
        #=================================================================
        if vidcap.isOpened():
            ret, frame = vidcap.read()
        else:
            ret = False
        while not ret:
            if vidcap.isOpened():
                ret, frame = vidcap.read()
            else:
                ret = False
    else:
        ret = True

    #=====================================================================
    # If we are debugging create 2 windows for the Goal and for the filter
    #=====================================================================
    if( dbg ):
        cv2.namedWindow( "Goal" )
        cv2.namedWindow( "Mask" )
        cv2.createTrackbar( "H low", "Mask", 0,   255, fake_callback )
        cv2.createTrackbar( "H hi",  "Mask", 255, 255, fake_callback )
        cv2.createTrackbar( "S low", "Mask", 0,   255, fake_callback )
        cv2.createTrackbar( "S hi",  "Mask", 70,  255, fake_callback )
        cv2.createTrackbar( "V low", "Mask", 245, 255, fake_callback )
        cv2.createTrackbar( "V hi",  "Mask", 255, 255, fake_callback )


    #=================================================================
    # Start the TCP server with dummy params
    #=================================================================
    GoalTCPStartHandler( shared_q )

    #=================================================================
    # Loop until we decide to quit
    #=================================================================
    while ret:
        if( use_picture ):
            frame = cv2.imread( "goal_frame3.jpg", cv2.IMREAD_COLOR )
        else:
            ret, frame = vidcap.read()

        dst, ang = proc_frame.proc_frame( frame, debug )
        shared_q.put( "{0},{1}\n".format( dst,ang ) )

        #=================================================================
        # Quit on esc press
        #=================================================================
        key = cv2.waitKey(20)
        if( key == 27 ):
            cv2.imwrite( "goal_frame.jpg", frame )
            break

    if( dbg ):
        cv2.destroyWindow( "Goal" )
        cv2.destroyWindow( "Mask" )
    sys.exit()

if __name__ == '__main__':
    main( debug, use_still )
