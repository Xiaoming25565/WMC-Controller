# WMC-Controller

一款发送**微软 Windows Media Center 遥控器红外信号**的安卓应用：
（**RC-6 MCE 协议 · 36 kHz 载波 · 厂商码 0x800F**）从手机红外发射头发送出去，
用于控制带 MCE 红外接收器的电脑（Windows Media Center / Kodi / MythTV 等）或机顶盒。

> 需要手机**带红外发射头**（如小米/红米、华为/荣耀部分机型、vivo/OPPO 部分机型等）。

## AI生成提示

本软件由豆包AI生成，本介绍部分由豆包AI生成

你可以修改里面的UI内容

## 一、信号规格

| 项目 | 值 |
| --- | --- |
| 协议 | RC-6 模式 6a（`rc6_mce`） |
| 载波频率 | **36 000 Hz** |
| 基准单元 T | 444 µs |
| 引导码 | 6T 亮（2664 µs）+ 2T 灭（888 µs） |
| 头部 4 位 | 起始位(1) + 模式位(110)，即 `0b1110`，MSB 先发 |
| 头部切换位 | 1 位 × 2T，固定发送 0 |
| 数据 32 位 | 高 16 位厂商码 `0x800F` + 低 16 位按键码（bit15 为 MCE 切换位），MSB 先发 |
| 曼彻斯特编码 | 位 `1` = 前半周期亮（444+444）；位 `0` = 前半周期灭（444+444） |
| 帧尾 | 6T 空闲（2664 µs） |
| 单帧时长 | ≈ 39.96 ms |

按键码表取自 Linux 内核官方按键映射 `drivers/media/rc/keymaps/rc-rc6-mce.c`。
编码器已通过独立往返校验（`tools/selfcheck/RC6SelfCheck.java`，66 项检查全部通过）。

## 二、安装

 [下载](https://github.com/Xiaoming25565/WMC-Controller/releases/tag/Debug) 

 支持安卓5以上

## 三、使用

1. 安装 APK 后打开"WMC 遥控器"，顶部状态栏显示红外检测结果。
2. **手机顶部（红外发射窗）对准 MCE 红外接收器**，按对应按键。
3. 长按 音量± / 频道± / 方向键 / 快退快进 / 上一曲下一曲 可连续发射。
4. 每次按键，状态栏显示刚发送的按键名与 scancode（如 `0x0416` = 播放）。

### 接收端要求

必须是能解 RC-6 MCE 的红外接收器，例如：Windows Media Center 时代的 **eHome 红外收发器**(如下图)
<img width="485" height="518" alt="image" src="https://github.com/user-attachments/assets/77e367df-0582-4a26-9141-048f02faedb2" />

你可以在一个二手交易平台购买到配套的接收器
<img width="1920" height="898" alt="image" src="https://github.com/user-attachments/assets/e556f277-2063-45c5-9bfa-673fa48571c2" />



## 四、工程结构

```
WMCRemoteIR/
├── app/src/main/
│   ├── AndroidManifest.xml
│   ├── java/com/wmc/remote/   # MainActivity / IREmitter / RC6Encoder / MceKey
│   └── res/                   # 布局 / 按钮皮肤 / 图标
├── tools/selfcheck/RC6SelfCheck.java   # 编码器往返校验
├── .github/workflows/build-apk.yml     # GitHub 云端编译（无作用）
└── gradlew / gradle/wrapper/           # Gradle 包装器
```

## 五、常见问题

- **状态栏显示"未检测到红外发射器"**：手机没有红外头，换带红外的手机。
- **按了没反应**：手机顶部红外窗是否正对接收器（2~5 米内）；接收器是否支持 RC-6 MCE；
  手机壳是否遮挡。
- **音量/频道一次只动一格**：长按即可连续调节。
- **与某些"MCE 接收器"不兼容**：山寨接收器只认 NEC 等协议，需确认支持 `rc6_mce` / Media Center。
- **开机按钮无法使用**：部分电脑是有两套红外系统必须只能搭配原装遥控器

## 六、协议数据来源

- Linux 内核按键映射：`drivers/media/rc/keymaps/rc-rc6-mce.c`
- Linux 内核 RC-6 编解码器：`drivers/media/rc/ir-rc6-decoder.c`、`drivers/media/rc/rc-ir-raw.c`
- Linux 内核文档：`Documentation/media/uapi/rc/rc-protos.rst`
