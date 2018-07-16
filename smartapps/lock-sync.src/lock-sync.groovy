/**
 * Synchronize Locks
 *
 * Author: Spiros Papadimitriou
 *
 * This file is released under the MIT License:
 * https://opensource.org/licenses/MIT
 *
 * This software is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied.
 */

definition(
  name: "Lock Sync",
  namespace: "spapadim",
  author: "Spiros Papadimitriou",
  description: "Whenever the state of a lock changes, lock or unlock other doors as well",
  category: "Safety & Security",
  iconUrl: "https://raw.githubusercontent.com/spapadim/smartthings/master/smartapps/lock-sync.src/lock-sync.png",
  iconX2Url: "https://raw.githubusercontent.com/spapadim/smartthings/master/smartapps/lock-sync.src/lock-sync@2x.png"
)

preferences {
  page: "preferencesPage", title: "Lock Sync"
}

def preferencesPage() {
  dynamicPage(name: "preferencesPage", install: true, uninstall: true, submitOnChange: true) {
    section("Locks to keep synchronized"){
      input "master", "capability.lock", title: "Master lock (trigger)"
      input "slaves", "capability.lock", title: "Slave locks", multiple: true
    }
    section("Actions to synchronize") {
      input "syncLock", "bool", title: "Lock slaves when master locks", defaultValue: true
      input "syncUnlock", "bool", title: "Unlock slaves when master unlocks", defaultValue: false
    }
    section("Only if one of these people is present...") {
      input "people", "capability.presenceSensor", title: "TODO", multiple: true, required: false
      if (people) {
        input "alwaysLock", "bool", title: "Always lock", defaultValue: true
        input "notifyIfNobodyHome", "bool", title: "Notify if nobody home", defaultValue: true
      }
    }
    section("Additional options", hidden: true) {
      input "notifyOnAction", "bool", title: "Notify on any triggered action", defaultValue: false
      input "notifyIfJammed", "bool", title: "Notify if master is jammed", defaultValue: true
      //input "jammedWithinMinutes", "number", title: "Only if jam occurs within", range: "1..5"
      //input "onlyManualOperation", "bool", title: "Only trigger on manual operation (EXPERIMENTAL)", defaultValue: false
    }
  }
}


def initialize() {
  if (syncLock || notifyIfJammed) {
    subscribe(master, "lock", lockChangedHandler)
  }
  if (syncUnlock) {
    subscribe(master, "unlock", lockChangedHandler)
  }
}

def installed() {
  initialize()
}

def updated() {
  unsubscribe()
  initialize()
}

def lockChangedHandler(evt) {
  log.debug "$evt.value: $evt, $settings"
  def anyoneThere = (people == null || people.any{ it.currentPresence == "present" })
  log.debug "anyoneThere: $anyoneThere"
  if (evt.value == "locked") {
    if (syncLock && (anyoneThere || alwaysLock)) {
      slaves.lock()
      if (notifyOnAction) {
        sendPushMessage("${master.displayName} initiated slave lock")
      }
    }
  } else if (evt.value == "unlocked") {
    if (syncUnlock && anyoneThere) {
      slaves.unlock()
      if (notifyOnAction) {
        sendPushMessage("${master.displayName} initiated slave unlock")
      }
    }
  } else if (evt.value == "unknown") {
    sendPush("WARNING: ${master.displayName} lock is jammed!")
  }

  if (!anyoneThere && notifyIfNobodyHome) {
    sendPush("WARNING: ${master.displayName} lock triggered while nobody home!")
  }
}
