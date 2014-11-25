global String Context = ""
global String State = ""
global Epoch activityTimer = null
global list[String,Epoch] bufferedActivities = new List[string,Epoch]()


def isAtHome(): Boolean
def isSleeping(): Boolean
def isTransitionAllowed(String From, String To, Epoch detectionTime ): Boolean
def getCurrentActivity(): String
def areWeAtHome(): Unit
def recordFingerprint(): Unit

def newActivityDetected(): Boolean {
	
	Epoch detectionTime = getCurrentDate.toEpoch()
	if (Context.isEmpty()) {
		areWeAtHome()
	}
	if (State.isEmpty()) {
		State = AndroidSystem.AndroidSystem.getCurrentActivity()
		activityTimer = detectionTime
		recordActivityInDatabase(detectedActivity, detectionTime)
	}	
	detectedActivity = AndroidSystem.getCurrentActivity()
		
	if (detectedActivity == State ) {
	       	//If we are here probably the threshold is passed and we can empty the buffer
		bufferedActivities.removeAll()
		recordActivityInDatabase(detectedActivity, detectionTime)
		return True
	} else {
       		if(isThresholdPassed(State, detectedActivity, detectionTime) {
			State = detectedActivity
			activityTimer = detectionTime
			for each item in bufferedActivities
				recordActivityInDatabase(item(0),item(1))
			recordActivityInDatabase(detectedActivity, detectionTime)
			bufferedActivities.removeAll()
		}
	}
	
	if ( isTransitionAllowed(State, detectedActivity, detectionTime)) {
		State = detectedActivity
		activityTimer = getCurrentDate.toEpoch()
		bufferedActivities.removeAll()
		recordActivityInDatabase(detectedActivity, detectionTime)
	}
	else {
		bufferedActivities.put(detectedActivity, detectionTime)
	}

}

