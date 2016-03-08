#!/bin/python
#Frame processing and distance estimation for 
#goal

import cv2
import math
import numpy
import sys

#Color array macro
def cvClr( R, G, B ):
    return( numpy.array( [R,G,B], numpy.uint8 ) )

#Approx. the green color range
MASK_LOW = cvClr( 0,0,245 )
MASK_HIGH = cvClr( 255,70,255 )

class Point:
    x = 0
    y = 0

def find_goal( contours, debug=False ):
    """ Filter out contours based on area """
    ret = []

    for x in contours:
        area = cv2.contourArea( x )
        if debug:
            print area
        if area > 75:
            ret.append( x )
    return( ret )

def find_center( contours ):
    """ Find the center of a contour based on moments """
    ret = []

    for x in contours: 
        M = cv2.moments( x )
        pt = Point()
        pt.x = int( M['m10']/M['m00'] )
        pt.y = int( M['m01']/M['m00'] )
        
        ret.append( pt ) 

    return( ret );

def convex_hull_area( contours, debug= False ):
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

def angle_from_point( x, img_width, fov_angle=45 ):
    return( -( ( img_width / 2 ) - x ) * fov_angle )

def dist_from_goal( area ):
    lkup = 25

def proc_frame( frame, debug=False ):
    #convert to HSV so we can mask more easily
    hsv_frame = cv2.cvtColor( frame, cv2.COLOR_BGR2HSV )

    #Apply the color mask defined at the top of file
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

    #Apply our color mask 
    masked_frame = cv2.bitwise_and( hsv_frame, hsv_frame, mask = color_mask )

    #Contours stuff...
    bw_frame = cv2.cvtColor( masked_frame, cv2.COLOR_BGR2GRAY )
    im2, contours, hierarchy = cv2.findContours( bw_frame, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE )
    draw = find_goal( contours )
    hull_areas, hulls = convex_hull_area( draw )
    centers = find_center( draw )
    
    #If debug mode, show the result of the line finding in a GUI
    if( debug ):
        #for line in lines:
            #cv2.line( frame, (line[0], line[1]), (line[2],line[3]), (0,0,255), 2 )

        #contours
        cv2.drawContours( frame, draw, -1, ( 0, 255, 0 ), 3 )
        cv2.drawContours( frame, hulls, -1, ( 255, 255, 0 ), 3 )
        for i in centers:
            cv2.circle( frame, ( i.x, i.y ), 3, ( 0, 255, 255 ),  )
            #print "X = {0} Y = {1}".format( i.x, i.y )

        cv2.imshow( "Goal", frame )
        cv2.imshow( "Mask", masked_frame )

    #return dist_from_goal( hull_area ), angle_from_point( centers[0].x, len( frame[0] ) )
    return( 3, 4 )
        
