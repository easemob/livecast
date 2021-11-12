# 前提条件
开始前，请确保你的开发环境满足如下条件：
- Xcode 12.0 或以上版本。
- Cocoapods。你可以参考 [Getting Started with CocoaPods](https://guides.cocoapods.org/using/getting-started.html#getting-started) 安装。
- iOS 11.0 或以上版本的设备。部分模拟机可能无法支持本项目的全部功能，所以推荐使用真机。

# 操作步骤
### 获取示例项目
前往 GitHub 下载或克隆 [InteractivePodcast](https://github.com/easemob/livecast/iOS) 示例项目。

### 注册环信

前往[环信官网](https://console.easemob.com/user/register)注册项目，生成appKey
替换工程 AppDelegate 文件中的 appkey

### 注册Agora

前往 [Agora官网](https://console.agora.io/) 注册项目，生产appId，然后替换 **Config.swift** 中 **AppId**。

### Targets

- InteractivePodcast_LeanCloud, 数据源基于 Leancloud, 实现参考 [LeanCloud.swift](https://github.com/easemob/livecast/iOS/InteractivePodcast/Server/LeanCloud.swift)
- InteractivePodcast_Firebase, 数据源基于 Firebase, 实现参考 [Firebase.swift](https://github.com/easemob/livecast/iOS/InteractivePodcast/Server/Firebase.swift)

### 注册Leancloud

1. 前往 [Leancloud官网](https://www.leancloud.cn/) 注册项目，生成 appId、LeanCloudAppId、LeanCloudAppKey、LeanCloudServerUrl。
- 替换 **Config.swift** 中 **AppId**、**LeanCloudAppId**、**LeanCloudAppKey**、**LeanCloudServerUrl**。
- 替换 [LeanCloudHelp.py](./LeanCloudHelp.py) 中 **appid** 和 **appkey**。
2. 启用LiveQuery，前往 [Leancloud控制台](https://www.leancloud.cn/)，打开**数据存储**-**服务设置**-**启用 LiveQuery**。
3. 安装 [Python](https://www.python.org/)，如果已经安装请忽略。
4. Python安装之后，控制台执行以下命令。
```
pip install leancloud
或者
pip3 install leancloud
```
5. Terminal 中执行文件 [LeanCloudHelp.py](./LeanCloudHelp.py)。
```
python ./LeanCloudHelp.py
或者
python3 ./LeanCloudHelp.py
```

### 注册Firebase
前往 [Firebase官网](https://firebase.google.com/) 注册项目，生成文件 **GoogleService-Info.plist**，然后放到 InteractivePodcast 根目录下面。

### 运行示例项目
1. 在iOS项目路径下，使用 "pod install" 命令去链接所有需要依赖的库。
2. 最后使用 Xcode 打开 InteractivePodcast.xcworkspace，连接 iPhone／iPad 测试设备，设置有效的开发者签名后即可运行。

### 项目截图
![房间列表](https://img-blog.csdnimg.cn/748051bb9fcd483d87638afb4ed05b51.png#pic_center)

![房间详情](https://img-blog.csdnimg.cn/ca89686401c940418c20470f085155b5.png#pic_center)

## 联系我们

- 如果你遇到了困难，可以先参阅 [常见问题](https://docs-im.easemob.com/)
- 如果你想了解更多官方示例，可以参考 [官方SDK示例](https://www.easemob.com/download/im)
- 如果你想了解声网SDK在复杂场景下的应用，可以参考 [官方场景案例](https://www.easemob.com/download/demo)
- 如果你想了解环信的一些社区开发者维护的项目，可以查看 [社区开源项目](https://www.imgeek.org/code/)
- 完整的 API 文档见 [文档中心](https://docs-im.easemob.com/) 
- 若遇到问题需要开发者帮助，你可以到  [开发者社区](https://www.imgeek.org/) 提问 
- 如果发现了示例代码的 bug，欢迎提交 [issue](https://github.com/easemob/EasemobVoice/issues)

## 代码许可

The MIT License (MIT)
