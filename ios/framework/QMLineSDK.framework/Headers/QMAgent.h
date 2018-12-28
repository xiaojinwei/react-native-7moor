//
//  QMAgent.h
//  QMLineSDK
//
//  Created by haochongfeng on 2018/10/23.
//  Copyright © 2018年 haochongfeng. All rights reserved.
//

#import <Foundation/Foundation.h>

/**
 当前客服状态
 */
typedef enum: NSInteger {
    QMKStatusRobot = 0,
    
    QMKStatusOnline = 1,
    
    QMKStatusOffline = 2,
    
    QMKStatusClaim = 3,
    
    QMKStatusFinish = 4,
    
    QMKStatusVip = 5,
}QMKStatus;

#pragma mark -- 坐席信息 --
@interface QMAgent : NSObject

/**
 坐席(客服)工号
 */
@property (nonatomic, copy) NSString * exten;

/**
 坐席(客服)名称
 */
@property (nonatomic, copy) NSString * name;

/**
 坐席(客服)头像
 */
@property (nonatomic, copy) NSString * icon_url;

/**
 坐席(客服)类型
 */
@property (nonatomic, copy) NSString * type;

@end
