
import {DeviceEventEmitter, NativeAppEventEmitter, NativeModules, Platform} from 'react-native';

const { RNSevenMoor } = NativeModules;
export default class RNSevenMoorModule {
    static registerSDK(key,userName,userId,cb) {
        RNSevenMoor.registerSDK(key,userName,userId)
    }
    static sdkGetUnReadMessage(key,userName,userId,cb) {
        RNSevenMoor.registerSDK(key,userName,userId)
    }
}
