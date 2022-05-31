目前Android有三类API
1.Camera (为了便于区分 下面简称 Camera1)
此类是用于控制设备相机的旧版API，在Android5.0以下使用，现已Deprecated

2.Camera2
Android 5.0以上升级的方案，控制设备相机的API，并且开放出硬件支持级别的厂商定制
(谷歌开放出官方库CameraView 帮助解决相机兼容性问题，也有其他一些三方库)

3.CameraX
JetPack中引入，基与Cmaera2 API封装，简化了开发流程，并增加生命周期控制

封装Camera和Camera2的第三方库有：

CameraView
https://github.com/google/cameraview

camera-samples
https://github.com/android/camera-samples

camerakit-android
https://github.com/CameraKit/camerakit-android

CameraView
https://github.com/natario1/CameraView


作者：PandaWatcher
链接：https://juejin.cn/post/7008579597696499742
来源：稀土掘金
著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
