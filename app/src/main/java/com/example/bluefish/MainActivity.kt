package com.example.bluefish

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.graphics.PointF
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.text.InputType
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

// ================= 1000+ 语料库 =================
object FishDialogue {
    private fun pick(vararg arr: String) = arr[Random.nextInt(arr.size)]

    // 前缀词（增加随机组合度）
    private val prefixes = arrayOf("咕噜～", "嘿嘿，", "哎呀，", "那个……", "告诉你哦，", "本鱼宣布：", "其实吧，", "你听我说，", "")
    private val suffixes = arrayOf("～", "！", "哦。", "呢。", "呀！", "咕噜咕噜……", "")

    private fun mix(vararg arr: String): String {
        val base = arr[Random.nextInt(arr.size)]
        val p = prefixes[Random.nextInt(prefixes.size)]
        val s = suffixes[Random.nextInt(suffixes.size)]
        return "$p$base$s"
    }

    // 1. 身体点击 (60条 + 组合)
    private val bodyLines = arrayOf(
        "别戳啦，再戳就变成鱼丸了！", "今天也要摸鱼吗？", "我是一条努力不摸鱼的蓝色大肥鱼。", "戳我一下，鱼生快乐一秒。",
        "哎哟，痒痒的！", "你是不是很闲？巧了，我也是。", "我在思考一个哲学问题：先有鱼还是先有水？", "报告！大肥鱼随时待命！",
        "今天的海水有点甜。", "再戳我就吐泡泡给你看。", "你的手指好暖，鱼鳞都舒服了。", "我刚刚是不是睡着了？",
        "检测到一只人类正在戳我。", "我游啊游，游到你的屏幕上来了。", "别看我只是条鱼，我可有梦想。",
        "摸鱼使我快乐，被摸使我更快乐。", "喂，戳归戳，别把我戳漏气。", "我是一条有原则的鱼：只在饿的时候吃，困的时候睡。",
        "今天的 KPI 是：被你戳三次。", "我正在练习吐泡泡，请看——噗。", "你的屏幕好干净，是不是为了迎接我？",
        "大肥鱼的日常：吃饭、睡觉、被你戳。", "警告：本鱼含糖量超标。", "我胖吗？我这是圆润。",
        "再戳，再戳我就……就再被你戳一下。", "你说，海的那边会有小鱼干吗？", "我有个小秘密：其实我会飞，只是不想证明。",
        "戳我一下，我就多喜欢你一点。", "我是一只有脾气的鱼，脾气就是没有脾气。", "别看我圆，我圆得很有道理。",
        "我在数你的指纹，数到一半忘了。", "这条鱼正在上班摸鱼，请勿打扰。", "你好，人类。我是来监督你休息的。",
        "今天的我，比昨天更可爱了吗？", "如果我是真的鱼，你会养我吗？", "我有七秒记忆，但每一秒都是你。",
        "肚子好饿，有没有小鱼干？", "你按到我的笑穴了。", "作为一条胖鱼，我压力很大。", "游啊游，游到外婆桥。",
        "快看，我吐了一个心形的泡泡！", "水好凉，你的屏幕好暖。", "我是不是该减肥了？算了明天再说。",
        "本鱼不轻易出水，除非你请我吃大餐。", "你戳我，我就翻肚皮给你看。", "今天的你看起来很开心呢。",
        "我是一条来自深海的鱼，现在搁浅在你的屏幕上了。", "再戳我就叫保安了！", "保安呢？哦，我就是保安。",
        "看什么看，没见过这么圆润的鱼吗？", "我在练习仰泳，别打扰我。", "你手机发烫了，是不是我太可爱了？",
        "滴滴，大肥鱼卡已打卡。", "你盯着我看也没用，我也不会掉金币。", "如果你不开心，就多戳我几下。",
        "我是一个没有感情的吐泡机器。", "脑容量不大，但装满了你。", "想带你去海底看星空。", "海浪的声音，你听到了吗？",
        "咕噜咕噜……我好像冒了个大泡泡。", "别捏我的脸，会变形的！", "偷偷告诉你，我是海里的老大。"
    )

    // 2. 摸头 (50条)
    private val headLines = arrayOf(
        "咕噜咕噜～好舒服", "再摸一下也可以哦～", "摸头可以，别摸鱼鳞！", "舒服到尾巴都在摇～",
        "这就是传说中的摸鱼吗？", "我的头不是按钮啦……不过可以多按几下。", "感受到你的温柔了，鱼心暖暖的。",
        "摸头 +1，好感度 +100。", "啊，这是传说中的「电子抚摸」吗？", "头顶的鳍都翘起来了～",
        "再摸我要睡着了哦。", "这条鱼已被摸到满足。", "你的手掌有海的味道。", "好，接下来换我摸你——算了，我没有手。",
        "唔……被摸头的感觉，真好。", "再摸摸头顶那块，最舒服了。", "我的鱼鳞都亮起来了。", "你是我见过最会摸头的人类。",
        "摸一次头，可以许一个愿哦。", "好想一直这样被摸下去。", "这条鱼已经被你收服了。", "摸头杀，我真的扛不住。",
        "好啦好啦，再摸就停不下来了。", "轻点轻点，头发乱了。", "本鱼的头只能你摸。", "在摸了在摸了，舒服得想翻白眼。",
        "你的手法很专业嘛。", "感觉打通了任督二脉。", "头顶传来一股暖流。", "好想给你个五星好评。",
        "再摸一下，就一下。", "我已经是一条废鱼了，被摸废的。", "咕噜噜，开心得冒泡。", "别停下，继续，对，就是那里。",
        "你是不是偷偷练过？", "摸头使我快乐，我是一只快乐的胖鱼。", "再来一次，这次我要许愿暴富。", "摸头神功，天下无敌。",
        "啊～仿佛回到了大海的怀抱。", "你看我乖不乖？", "我的鳍都软了。", "这个世界上怎么会有这么舒服的事。",
        "你就是我的饲养员吗？", "被摸头的感觉，就像吃了一大口棉花糖。", "今天也是被摸头的一天呢。", "如果每天都能被摸头就好了。",
        "我决定了，以后就赖着你了。", "别走，再摸五分钟。", "摸摸摸，就知道摸。", "好吧，其实我很喜欢。"
    )

    // 3. 挠尾巴 (40条)
    private val tailLines = arrayOf(
        "呀！别挠尾巴，最怕痒了！", "尾巴是我用来保持平衡的，不是玩具！", "哈哈哈哈，停停停～", "尾巴摇摇，心情好好。",
        "你再挠，我就用尾巴拍你。", "这可是我最漂亮的地方～", "尾巴被摸，鱼脸通红。", "咕噜！尾巴都竖起来了。",
        "痒痒的……你别跑。", "我的尾巴会说话：啪嗒啪嗒。", "挠尾巴是犯规动作！", "尾巴一抖，水花四溅。",
        "好舒服，但也好痒。", "别挠了，我要笑到冒泡泡。", "这条尾巴已经记下你了。", "别碰！那是我的方向盘！",
        "哎哎哎，痒死了。", "尾巴打结了，快帮我解开。", "你再挠我要咬人了哦！", "看我的神龙摆尾！",
        "我的尾巴可是能扇起海啸的。", "尾巴一甩，谁也不爱。", "轻轻拍打水面，啪嗒啪嗒。", "求放过，最怕别人碰我尾巴了。",
        "你再这样我可不理你了。", "尾巴都被你摸秃了。", "本来就很痒了，你还挠。", "哎呀，我的老腰。",
        "别挠，再挠我就要变身了。", "我可是有尾气（尾巴的脾气）的。", "尾巴说它想休息了。", "你这手法，是跟我有仇吗？",
        "哈哈哈哈哈哈哈，救命啊。", "笑得我鱼鳔都疼了。", "我就知道你没安好心。", "尾巴连心，你不知道吗？",
        "好了好了，我认输。", "别再挠啦，再挠我就变成泥鳅了。", "摇摇尾巴，我是你忠实的小跟班。", "这条尾巴今天归你管了。"
    )

    // 4. 摸肚子 (40条)
    private val bellyLines = arrayOf(
        "肚子圆滚滚的，好摸吧？", "别按，会按出小鱼干来的。", "我的肚子是我的隐私……好吧，可以摸。", "唔……那是我的软肋。",
        "吃饱了，肚子鼓鼓的。", "摸摸肚子，暖洋洋。", "喂，别用戳的，用摸的。", "我的肚子，装满了海和梦。",
        "咕噜咕噜……肚子在唱歌。", "你摸得我好想睡觉。", "肚子是鱼的第二张脸。", "再摸就变成汤圆了。",
        "这条鱼已经被摸到圆滚滚。", "别看我肚子大，游得可快了。", "揉肚子 +1，幸福感 +50。",
