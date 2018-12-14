from sdk import OpenMoticsApi, OpenMoticsCloudApi, traceback

import logging
import threading
import time
import sys
import pprint 

USERNAME = "user"
PASSWORD = "pass"

#logger configuration
logging.basicConfig(level=logging.INFO,
					format='[%(asctime)-15s %(levelname)s] (%(threadName)-10s) %(message)s',
					)

#for pretty print the gw responses
pp = pprint.PrettyPrinter(indent=4)

#helper function to check if threads are still executing
def has_live_threads(threads):
	return True in [t.isAlive() for t in threads]

#create a listener for incoming messages
def msg_processor(api):
	try:
		api.msg_loop([ OpenMoticsCloudApi.MSG_OUTPUT_CHANGE ], callback)
	except Exception as e:
		traceback.print_exc()
		#print "%s" % e.getMessage()

def callback(msg):
    logging.info(msg)

def playground(api):
	logging.info(pp.pformat(api.get_status()))
	logging.info(pp.pformat(api.get_installations()))
	logging.info(pp.pformat(api.get_modules()))
	logging.info(pp.pformat(api.get_output_status()))
	logging.info(pp.pformat(api.flash_leds(0, 5)))
	logging.info(pp.pformat(api.set_output(31, True)))

def main():
	try:
		# Test code for the cloud api.
		api = OpenMoticsCloudApi(USERNAME, PASSWORD, True)
		threads=[]
		m = threading.Thread(name="msg_processor", target=msg_processor, args=(api,))
		m.setDaemon(True)
		threads.append(m)
		m.start()

		p = threading.Thread(name="playground", target=playground, args=(api,))
		p.setDaemon(True)
		threads.append(p)
		p.start()
	except Exception as e:
		print "Error starting app", e
		sys.exit(2)

	while has_live_threads(threads):
		try:
			# synchronization timeout of threads kill
			[t.join(1) for t in threads if t is not None and t.isAlive()]
		except KeyboardInterrupt:
			# Ctrl-C handling and send kill to threads
			print "Ctrl-c pressed ..."
			sys.exit(1)

if __name__ == '__main__':
	main()

