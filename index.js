
import {DeviceEventEmitter, NativeAppEventEmitter, NativeModules, Platform} from 'react-native';

const { RNSevenMoor } = NativeModules;
export default class RNSevenMoorModule {
    static registerSDK(key,userName,userId,cb) {
        RNSevenMoor.registerSDK(key,userName,userId)
    }

    /***
     * 获取未读消息数量 promise 方法
     * @param key
     * @param userName
     * @param userId
     * @returns {*}
     */
    static sdkGetUnReadMessage(key,userName,userId) {
        return RNSevenMoor.sdkGetUnReadMessage(key,userName,userId)
    }
}
