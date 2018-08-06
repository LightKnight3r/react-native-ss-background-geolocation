'use strict'

var { NativeModules } = require('react-native')


var BackgroundService = NativeModules.GeoLocationModule || {};
console.log('ahihi',BackgroundService);
var BackgroundServiceAndroid = {
    startService(serverAddr,memberToken,time,distance,accuracy) {
      console.log('ahihi start');
      return BackgroundService.startService(serverAddr,memberToken,time,distance,accuracy);
    },
    stopService() {
      console.log('ahihi stop service');
      return BackgroundService.stopService();
    }
};
console.log('ahihi BackgroundServiceAndroid',BackgroundServiceAndroid);
module.exports = BackgroundServiceAndroid;
