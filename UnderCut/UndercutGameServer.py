# -*- coding:utf-8 -*-
"""
Created on Jan 21, 2011

@author: Aman
"""

import pyGameServer
import thread
import time, sys, traceback

def main():
    #Login and Authenticate with server
    GS = pyGameServer.new('cw_undercut_pytest', '31415')
       
    #thread.start_new_thread(ping, (bot, 15))


if __name__ == "__main__":
    try:
        while 1:
            print "Connecting..."

            main()
    except IOError as (errno, strerror):
        print '\n----------------'
        if errno == 9:
            print "Quitting:", errno 
        else:
            traceback.print_exc(file=sys.stdout)
        print '----------------\nExited.'
    except KeyboardInterrupt:
        print '----------------\nExited by User.'   
    except:
        traceback.print_exc(file=sys.stdout)
        raise
