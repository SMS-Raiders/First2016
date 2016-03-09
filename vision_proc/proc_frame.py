#!/bin/python
#Frame processing and distance estimation for
#goal

#-------------------------------------------------------------------------------
#                                   IMPORTS
#-------------------------------------------------------------------------------
import cv2
import math
import numpy
import sys

#-------------------------------------------------------------------------------
#                                  VARIABLES
#-------------------------------------------------------------------------------

#=====================================================================
# Approx. The green color range
#=====================================================================
MASK_LOW  = cvClr( 0,   0,  245 )
MASK_HIGH = cvClr( 255, 70, 255 )

#=====================================================================
# Approximate Areas for the goal (Pixels)
#=====================================================================
#MIN_AREA = 250
MIN_AREA = 1600
#MAX_AREA = 4000
MAX_AREA = 5000

#=================================================================
# Numbers Determined from experiment apart from 0 and 20
# Straight on to Goal
# width and height and area are in pixel area
#=================================================================
goal_lkup = [
            { 'dist ft' :  0, 'width' : 200, 'height' : 90, 'area' : 9000, 'ratio h_w' : 0.58 },  #0ft not tested needs to be large
            { 'dist ft' :  7, 'width' : 151, 'height' : 88, 'area' : 4828, 'ratio h_w' : 0.58 },
            { 'dist ft' :  8, 'width' : 141, 'height' : 85, 'area' : 4700, 'ratio h_w' : 0.60 },
            { 'dist ft' :  9, 'width' : 132, 'height' : 81, 'area' : 4300, 'ratio h_w' : 0.61 },
            { 'dist ft' : 10, 'width' : 123, 'height' : 78, 'area' : 3860, 'ratio h_w' : 0.63 },
            { 'dist ft' : 11, 'width' : 114, 'height' : 75, 'area' : 3420, 'ratio h_w' : 0.65 },
            { 'dist ft' : 12, 'width' : 108, 'height' : 73, 'area' : 3120, 'ratio h_w' : 0.67 },
            { 'dist ft' : 13, 'width' : 102, 'height' : 70, 'area' : 2770, 'ratio h_w' : 0.68 },
            { 'dist ft' : 14, 'width' : 96 , 'height' : 68, 'area' : 2357, 'ratio h_w' : 0.71 },
            { 'dist ft' : 20, 'width' : 60 , 'height' : 35, 'area' : 1000, 'ratio h_w' : 0.9  }  ] #20 ft not tested needs to be small

#-------------------------------------------------------------------------------
#                                   CLASSES
#-------------------------------------------------------------------------------
class Point:
    """Simple Class for XY point"""
    x = 0
    y = 0

#-------------------------------------------------------------------------------
#                                  PROCEDURES
#-------------------------------------------------------------------------------

def cvClr( R, G, B ):
    """
    Color array macro
    """
    return( numpy.array( [R,G,B], numpy.uint8 ) )

def find_squares( contours, debug=False ):
    """
    Find square shaped objects
    """
    ret = []

    for shape in contours:
        x, y, w, h = cv2.boundingRect( shape )
        if debug:
            print "Area", (w * h)
            print "Width ", w
            print "Height", h
        x_y_ratio= x / y
        ret.append( shape )

    return( ret )

def filter_area( contours, debug=False ):
    """
    Filter out contours based on area
    """
    ret = []

    for x in contours:
        area = cv2.contourArea( x )
        if area > MIN_AREA and area < MAX_AREA:
            if debug:
                print "Area", area
            ret.append( x )
    return( ret )

def find_center( contours ):
    """
    Find the center of a contour based on moments
    """
    ret = []

    for x in contours:
        M = cv2.moments( x )
        pt = Point()
        pt.x = int( M['m10']/M['m00'] )
        pt.y = int( M['m01']/M['m00'] )

        ret.append( pt )

    return( ret );

def convex_hull_area( contours, debug= False ):
    """
    Find the Area of convex Hulls
    """
    ret_areas = []
    ret_hulls = []
    for c in contours:
        hull = cv2.convexHull( c )
        area = cv2.contourArea( hull )
        ret_areas.append( area )
        ret_hulls.append( hull )
        if( debug ):
            print( "Hull area: {0}".format( area ) )

    return ( ret_areas, ret_hulls )

def angle_from_point( x, img_width=640, fov_angle=44 ):
    """
    Calculate the angle from a point
    """
    return( -( ( img_width / 2 ) - x ) * fov_angle )

def lin_scale( val, x1, y1, x2, y2 ):
    """
    Linearly scale Val to y1 and y2 from x1 and x2 range
    x1 and y1 are low values
    """
    x_range = (x2 - x1)
    new_val = 0
    if x_range is 0:
        new_val = y1
    else:
        y_range = ( y2 - y1 )
        new_val = ( ( ( val - x1 ) * y_range ) / x_range ) + y1

    return new_val

def dist_from_goal( area ):
    """
    Calculates the distance to the Goal based on area, x, y
    Args:
        area: the area in pixels of the target
    Returns:
        Feet from goal
    """
    dist = 99
    prev = goal_lkup[ 0 ]

    for cur in goal_lkup:
        #=============================================================
        # If the area is less than the currently selected area, but
        # greater then the previous area, then the distance is some
        # where in between.  Then do linear interpolation
        #=============================================================
        if area < cur[ 'area' ] and area > prev[ 'area' ]:
            dist = lin_scale( area, prev[ 'area' ], prev[ 'dist ft' ], cur[ 'area' ], cur[ 'dist ft' ] )
            return dist
    return dist


def proc_frame( frame, debug=False ):
    """
    Process a frame
    """

    #=================================================================
    # Convert to HSV so we can mask more easily
    #=================================================================
    hsv_frame = cv2.cvtColor( frame, cv2.COLOR_BGR2HSV )

    #=================================================================
    # Apply the color mask defined at the top of file
    #=================================================================
    if( debug ):
        hlo = cv2.getTrackbarPos( "H low", "Mask" )
        hhi = cv2.getTrackbarPos( "H hi", "Mask" )
        slo = cv2.getTrackbarPos( "S low", "Mask" )
        shi = cv2.getTrackbarPos( "S hi", "Mask" )
        vlo = cv2.getTrackbarPos( "V low", "Mask" )
        vhi = cv2.getTrackbarPos( "V hi", "Mask" )
        lo = numpy.array( [ hlo, slo, vlo ], numpy.uint8 )
        hi = numpy.array( [ hhi, shi, vhi ], numpy.uint8 )
        color_mask = cv2.inRange( hsv_frame, lo, hi )
    else:
        color_mask = cv2.inRange( hsv_frame, MASK_LOW, MASK_HIGH )

    #=================================================================
    # Apply our color mask
    #=================================================================
    masked_frame = cv2.bitwise_and( hsv_frame, hsv_frame, mask = color_mask )

    #=================================================================
    # Contours stuff
    # First convert to Gray and find the contours
    #=================================================================
    bw_frame = cv2.cvtColor( masked_frame, cv2.COLOR_BGR2GRAY )
    contours, hierarchy = cv2.findContours( bw_frame, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE )

    #=================================================================
    # Filter the contours based on area, convex hull area etc...
    #=================================================================
    draw = filter_area( contours )
    hull_areas, hulls = convex_hull_area( draw )
    squares = find_squares( hulls )
    centers = find_center( squares )

    #=================================================================
    # If debug mode, show the result of the line finding in a GUI
    #=================================================================
    if( debug ):

        #contours
        cv2.drawContours( frame, draw, -1, ( 0, 255, 0 ), 3 )
        cv2.drawContours( frame, squares, -1, ( 255, 255, 0 ), 3 )
        for i in centers:
            cv2.circle( frame, ( i.x, i.y ), 3, ( 0, 255, 255 ),  )
            #print "X = {0} Y = {1}".format( i.x, i.y )

        cv2.imshow( "Goal", frame )
        #cv2.imshow( "Mask", masked_frame )

    #return dist_from_goal( hull_area ), angle_from_point( centers[0].x, len( frame[0] ) )
    return( 3, 4 )
