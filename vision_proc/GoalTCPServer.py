#Socket server handler class

import Queue
import SocketServer
import threading


data_queue = None

class GoalTCPHandler(SocketServer.BaseRequestHandler):
    #The client will only send a connect request and then after that
    #just listens for us to output

    def handle( self ):
        global data_queue
        socket = self.request
        cur_thread = threading.current_thread()
        # try:
        #If there is new data in the queue, send it, otherwise just close
        #the socket
        if( not data_queue.empty() ):
            #If there is older data, discard it and use the newest
            while( not data_queue.empty() ):
                next_data = data_queue.get()
            socket.sendto( next_data, self.client_address )
        # except:
            # #Handle socket errors gracefully
            # print( "Socket error!." )

#Don't make part of class def!!
def GoalTCPStartHandler( que ):
    global data_queue
    data_queue = que
    #HOST, PORT = "192.168.1.250", 4545
    HOST, PORT = "localhost", 4545
    server = SocketServer.TCPServer((HOST, PORT), GoalTCPHandler )

    print "Create Socket Thread"
    server_thread = threading.Thread( target=server.serve_forever )
    server_thread.daemon = True
    print "Start Socket Thread"
    server_thread.start()
    print "Socket Thread Started"        
        