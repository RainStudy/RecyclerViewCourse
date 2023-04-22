# RecyclerView相关

如果在 Kotlin 的使用上有什么问题，这波我线下直接帮你们解决。

> RecyclerView 的源码设计中其实体现了相当多 *组合优于继承* 的思想

> 其实 RecyclerView 和 自定义View 都不属于我擅长的这一块技术。事实上在这篇课件开写之前，我对于 RecyclerView 的了解可能并没有你们多，讲的比较浅只能说请大家多多包涵了。

## 复习: 基础使用

我们先复习一下对我们来说 RecyclerView 最常用的用法。

~~~kotlin
typealias AdapterData = MainAdapter.Data

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val data = listOf(
        AdapterData("CMU 15-213", "Computer Systems: A Programmer's Perspective"),
        AdapterData("Stanford CS143", "Compilers"),
        AdapterData("Stanford CS144", "Computer Network"),
        AdapterData("CMU 15-445", "Database Systems"),
        AdapterData("MIT6.824", "Distributed System")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // 设置 LayoutManager 和 Adapter
        binding.rv.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = MainAdapter(data)
        }
    }
}
~~~

每次使用 RecylcerView 都要记得设置 LayoutManager 和 Adapter。Adapter 我觉得你们不可能忘，关键就在于设置 LayoutManager 这一步骤。一旦忘记了设置 LayoutManager，RecyclerView 会直接跳过布局这一步骤。这一步小小的疏漏很可能会让你 Debug 很久（当然这些坑踩过了以后再遇到就能很快定位到问题）。

那么关键的部分来了，Adapter 要怎么写？

```kotlin
class MainAdapter(private val data: List<Data>) : RecyclerView.Adapter<MainAdapter.Holder>() {

    data class Data(val title: String, val desc: String)

    // 那么在 ViewHolder 中要如何使用 ViewBinding 呢
    class Holder(root: View) : ViewHolder(root) {
        val tvTitle = root.findViewById<TextView>(R.id.tv_title)
        val tvDesc = root.findViewById<TextView>(R.id.tv_desc)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        // 拿到 LayoutInflater
        val inflater = LayoutInflater.from(parent.context)
        // inflate item 布局
        val view = inflater.inflate(R.layout.item_main, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val itemData = data[position]
        holder.apply {
            tvTitle.text = itemData.title
            tvDesc.text = itemData.desc
        }
    }
}
```

在 onCreateViewHolder 这一步中创建自己定义的 ViewHolder，inflate 一个 item view 并将其传入 ViewHolder。

在 onBindViewHolder 这一步中绑定视图属性 （比如设置 TextView 的 text 等）

至于 getItemCount，一般的做法是在构造 Adapter 时传入一个元素列表，返回这个列表的大小，之后就可以在 onBindViewHolder 中通过 position 直接拿到对应的元素。

最终效果如下

![img_v2_7701b077-8126-4cbe-8554-4f4f70ae872g](https://persecution-1301196908.cos.ap-chongqing.myqcloud.com/image_bed/img_v2_7701b077-8126-4cbe-8554-4f4f70ae872g.jpg?imageMogr2/thumbnail/!25p)

那么如果存在多种类型的元素，不同的元素需要生成不同的 View，我们应该怎么做？当然是重写 getItemViewType，为每种Item分配view type 以供 RecyclerView 区分。并且我们需要提供多个不同类型的 ViewHolder。

下面我提供一个利用 Kotlin **密封类/接口 (sealed class/interface)** 简单实现的多元素类型 Adapter 样例（因为写得简单，有很多不优雅的地方），如果下来有人感兴趣的话可以尝试封装一个比我更好的。封装方便优雅的工具也是编程的一大乐趣所在。

~~~kotlin
class MainAdapter(private val data: List<Data>) : RecyclerView.Adapter<MainAdapter.Holder>() {

    companion object {
        const val TYPE_LESSON = 0
        const val TYPE_COLLEGE = 1
    }

    sealed class Data(val type: Int) {
        data class Lesson(val title: String, val desc: String): Data(TYPE_LESSON)
        data class College(val name: String, val avatar: String): Data(TYPE_COLLEGE)
    }

    sealed class Holder(root: View) : ViewHolder(root) {
        class Lesson(root: View) : Holder(root) {
            val tvTitle: TextView = root.findViewById(R.id.tv_title)
            val tvDesc: TextView = root.findViewById(R.id.tv_desc)
        }
        class College(root: View) : Holder(root) {
            val tvName: TextView = root.findViewById(R.id.tv_name)
            val ivAvatar: ImageView = root.findViewById(R.id.iv_avatar)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        // 拿到 LayoutInflater
        val inflater = LayoutInflater.from(parent.context)
        // inflate item 布局
        val view = inflater.inflate(if (viewType == TYPE_LESSON) R.layout.item_lesson else R.layout.item_college, parent, false)
        return if (viewType == TYPE_LESSON) Holder.Lesson(view) else Holder.College(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.apply {
            when (val itemData = data[position]) {
                is Data.Lesson -> {
                    this as Holder.Lesson
                    tvTitle.text = itemData.title
                    tvDesc.text = itemData.desc
                }
                is Data.College -> {
                    this as Holder.College
                    tvName.text = itemData.name
                    Glide.with(ivAvatar)
                        .load(itemData.avatar)
                        .centerCrop()
                        .into(ivAvatar)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return data[position].type
    }
}
~~~

效果图

![img_v2_9ef59a55-fc78-4579-b38e-021530b05a2g](https://persecution-1301196908.cos.ap-chongqing.myqcloud.com/image_bed/img_v2_9ef59a55-fc78-4579-b38e-021530b05a2g.jpg?imageMogr2/thumbnail/!25p)

### 不推荐在onBindViewHolder中设置回调

这个属于老生常谈的话题了

~~~kotlin
override fun onBindViewHolder(holder: Holder, position: Int) {
    holder.itemView.setOnClickListener {
        "不推荐这样做！".toast()
    }
}
~~~

为什么？其实是因为这样写实际上是生成了一个匿名内部类的实例传入了 setOnClickListener 方法。而 onBind 操作实际上执行得非常频繁，堆空间中会产生大量的匿名内部类实例，对内存造成很大的负担，进而导致 gc 频繁发生，app卡顿。

那么我们应该在哪里进行设置呢？如果我们能拿到最新的 position，那其实只需要在创建 ViewHolder 的时候设置一次就可以了。没错，说的就是 onCreateViewHolder。事实上，最合适的地方是直接写在 ViewHolder 的构造方法。在 Kotlin 中我们不需要为了它专门去重写构造器，使用 init 块即可。（Java中也有普通代码块和静态代码块，不过我猜你们之前没怎么用过，在 Kotlin 中很多情况下我们都不需要重写构造器~~（重写起来也比Java麻烦）~~，所以 init 块用得就多起来了）

~~~kotlin
// 将 Holder 写成内部类，使其能访问 Adapter 的字段
inner class Holder(root: View): RecyclerView.ViewHolder(root) {
    init {
        itemView.setOnClickListener {
            "这样做只会设置一次, 在回调中拿到的 ViewHolder 的 position 永远都是最新的".toast()
            // 这样拿到点击位置对应 item 的元素
            val pos = bindingAdapterPosition
            val itemData = data[pos]
            // ...
        }
    }
}
~~~

## 进阶 (并不) 使用

### LayoutAnimation

实际上我们可以使用 LayoutAnimation 为 RecyclerView 设置淡入淡出动画，当然我们这里指的只是属性动画

新建动画xml文件 res/anim/anim_fade_in.xml

~~~xml
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android"
    <!-- 动画时长 -->
    android:duration="300"
	<!-- 选用的插值器 -->
    android:interpolator="@android:anim/overshoot_interpolator"
    android:shareInterpolator="true">
    <!-- 不透明度从0渐变到1 -->
    <alpha
        android:fromAlpha="0.0"
        android:toAlpha="1.0" />
	<!-- 大小从0.8倍渐变到1倍 -->
    <scale
        android:fromXScale="0.8"
        android:fromYScale="0.8"
        android:toXScale="1"
        android:toYScale="1"/>
	<!-- 从一开始的y轴向下偏移20%到没有偏移 -->
    <translate
        android:fromYDelta="20%"
        android:toYDelta="0%"/>
</set>
~~~

然后我们将其设置为 RecyclerView 的 LayoutAnimation

```kotlin
binding.rv.apply {
    // 设置 LayoutAnimation
    layoutAnimation = LayoutAnimationController(AnimationUtils.loadAnimation(this@MainActivity, R.anim.anim_fade_in))
    layoutManager = LinearLayoutManager(this@MainActivity)
    adapter = MainAdapter(data)
}
```

效果还不错对吧

![Peek 2023-04-18 14-39](https://persecution-1301196908.cos.ap-chongqing.myqcloud.com/image_bed/Peek%202023-04-18%2014-39.gif?imageMogr2/thumbnail/!25p)

### 各种LayoutManager

LayoutManager 其实是从 RecyclerView 的业务逻辑中抽离出来的一个帮助类，主要负责管理 RecyclerView 的布局部分。这个类接管了RecyclerView 的 measure layout draw 三大流程。

也就是说，如果我们想要拓展 RecyclerView 的一些功能，不需要像其他 view 一样进行继承，只需要自己设计一个 LayoutManager 就可以了。这个设计的优秀之处在于能够把每个部分的职能都分得很开，我们在自定义时也只需要与渲染布局的逻辑打交道。但如果按照传统的继承 view 来做很难不干扰到渲染布局以外的逻辑，所以RecyclerView 的这个设计也一定程度的体现出了 **组合优于继承** 的思想。

实际上官方为我们提供了不少种类的 LayoutManager，所以大多数情况下我们是没有自定义 LayoutManager 的需求的。

#### LinearLayoutManager

这个相信大家都不陌生了，最常用的 LayoutManager 。

~~~kotlin
// 最常用的用法，默认方向为竖直方向
binding.rv.layoutManager = LinearLayoutManager(this@MainActivity)
// 设置方向为水平方向
binding.rv.layoutManager = LinearLayoutManager(this@MainActivity, RecyclerView.HORIZONTAL, false)
// 设置翻转，这样 item 会从底部开始放置，类似 QQ 聊天
binding.rv.layoutManager = LinearLayoutManager(this@MainActivity, RecyclerView.VERTICAL, true)
~~~

#### GridLayoutManager

网格布局，也就是说可以设置有n列或n行

~~~kotlin
// 默认方向是竖直方向，也就是说这里设置得固定有两列
binding.rv.layoutManager = GridLayoutManager(this@MainActivity, 2)
// 设置为水平方向，也就是说这里是固定有2行
binding.rv.layoutManager = GridLayoutManager(this@MainActivity, 2, RecyclerView.HORIZONTAL, false)
// 当然 网格布局也是可以翻转的
binding.rv.layoutManager = GridLayoutManager(this@MainActivity, 2, RecyclerView.VERTICAL, true)
~~~

并且我们能控制每一行/列元素所占格子的多少，比如入下图，我想让第一个元素占两格，其余的只占一个（找的网图）

![image-20230418200255922](https://persecution-1301196908.cos.ap-chongqing.myqcloud.com/image_bed/image-20230418200255922.png?imageMogr2/thumbnail/!50p)

```kotlin
val manager = GridLayoutManager(this@MainActivity, 2, RecyclerView.HORIZONTAL, false)
manager.spanSizeLookup = object : SpanSizeLookup() {
    override fun getSpanSize(position: Int): Int {
        if (position == 0) return 2
        return 1
    }
}
layoutManager = manager
```

我们可以通过设置 SpanSizeLookUp 的方法巧妙的控制每个元素的大小

#### StaggeredGridLayoutManager

瀑布流布局，其实就是有一根轴不用对齐的网格布局。

~~~kotlin
binding.rv.layoutManager = StaggeredGridLayoutManager(2, RecyclerView.VERITICAL)
~~~

效果的话随便找了张网图

![img](https://upload-images.jianshu.io/upload_images/6650005-d2c82ffd298d7fd2?imageMogr2/auto-orient/strip|imageView2/2/w/668/format/webp)

#### FlexBoxLayoutManager

> 如果你写过前端，应该对这个东西不能更熟悉了。没错！我们 Android 也可以用 Flex 大法了！

flexbox 是谷歌官方推出的拓展控件，允许开发者在 android 上使用 web 前端上的 flex 布局方式进行布局。并且提供了 RecyclerView 的 LayoutManager 拓展

首先导入依赖

~~~groovy
implementation 'com.google.android.flexbox:flexbox:3.0.0'
~~~

然后我们就可以使用 FlexboxLayoutManager 了

~~~kotlin
// 默认主轴: FlexDirection.ROW (水平排列) 换行策略: FlexWarp.WRAP (换行)
val manager = FlexboxLayoutManager(this@MainActivity)
manager.apply {
    // 主轴对齐方式 我这里让他居中
    justifyContent = JustifyContent.CENTER
    // 附轴对齐方式 我这里让他沿着轴从头到尾排
    alignItems = AlignItems.FLEX_START
}
~~~

这种布局可以轻而易举的实现之前我们需要使用自定义 View 实现的流式布局。

![img_v2_7ee64ebd-dcaf-483a-8f26-0dfeedb49bbg](https://persecution-1301196908.cos.ap-chongqing.myqcloud.com/image_bed/img_v2_7ee64ebd-dcaf-483a-8f26-0dfeedb49bbg.jpg?imageMogr2/thumbnail/!50p)

发现了吗，这种布局每行的 item 个数都是不同的，填满了一行就会自动换行，这正是我们要的流式布局的效果。如果有同学有兴趣的话可以用自定义 View 手写一下这种布局，其实也很简单（我之前就不知道有这个 FlexBoxLayoutManager，于是自己自定义了一个流式布局）。

Flex布局当然不仅仅能做流式布局，它是非常自由的，并且能非常好的适配屏幕大小变化的情况（也就是响应式布局），在前端 flex 布局基本能解决一切布局问题（在不考虑缓存的情况下 FlexBoxLayout这个布局甚至可以说比 RecyclerView 还要强大，而 FlexBoxLayoutManager 让 RecyclerView 也能兼容这种布局方式，可谓无敌）。关于 Flex 布局的用法，如果你还想学习更多: https://www.runoob.com/w3cnote/flex-grammar.html

### 配合 ViewBinding 使用

其实只需要在创建视图时使用 ViewBinding 类进行 inflate，然后保留产生的 ViewBinding 实例即可。

~~~kotlin
class TagAdapter(val tags: List<String>) : RecyclerView.Adapter<TagAdapter.Holder>() {
    class Holder(val binding: ItemTagBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(ItemTagBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return tags.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.binding.tvTag.text = tags[position]
    }
}
~~~

让 ViewHolder 持有 ViewBinding 实例，然后将 ViewBinding 的根布局传入父类构造器。之后就可以直接通过 ViewHolder 拿到 ViewBinding，非常简单。

### 差分刷新

`DiffUtil` 是谷歌官方提供的用于规范使用 `notifyXXX` 的工具类，其本质上是封装了一种差分算法，目的是在 Adapter 中数据发生变动时做出最节省资源的决策（也就是如何才能调用最少次 `notifyXXX` 方法）。

打个比方，如果我们需要更新 Adapter 中的数据，而我们拿到的新数据却是一个跟之前的数据截然不同的数据。要如何 `notifyItemRemoved` `notifyItemMoved` `notifyItemInserted` 才能让旧数据对应的布局变成新数据对应的布局呢？你可能会说，我不能直接 `notifyDataSetChanged` 吗？最好不要，因为这样做其一是会造成性能的浪费，在 RecyclerView 中元素足够多的情况下可能会导致帧率降低，甚至直接卡死渲染线程 (画面卡死，ANR)。其次是如果直接 `notifyDataSetChanged` ，我们就不知道具体那些元素产生了变化（哪些元素插入进来了，哪些元素被删掉了，哪些元素移动到了别的位置），那么自然也享受不到 RecyclerView 带给我们的动画效果。

这个算法的具体实现相当复杂，我就不讲了，如果你们有兴趣了解的话可以看下这篇文章: [Myers 差分算法 (Myers Difference Algorithm) —— DiffUtils 之核心算法（一）](https://cloud.tencent.com/developer/article/1488583)

其实这种差分算法不仅仅可以用在 RecyclerView 上，事实上，当我们将一个 View 动态的添加到 View 树中，或者说我们修改了 View 的属性时（本质就是在 View 中调用 `invalidate` 方法），在进行重绘的过程中不太可能将 View 树中的所有 View 都重绘一遍，那也太耗性能了。这时候我们就需要一种差分算法计算出需要更新的 View，然后进行局部更新。前端的虚拟 DOM 也是类似的原理。

言尽于此，如何使用？

```kotlin
val oldList = data
val newList = listOf<MainAdapter.Data>(
    AdapterLessonData("Stanford CS143", "Compilers"),
    AdapterLessonData("Stanford CS144", "Computer Network"),
)
// 这里有没有觉得可以把这个 Callback 封装一下
val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return data.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    // 这里判断两个 Item 是否是同一个 Item
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        if (oldItem.type == newItem.type) {
            return when (oldItem) {
                is AdapterLessonData -> {
                    oldItem.title == (newItem as AdapterLessonData).title
                }
                is AdapterCollegeData -> {
                    oldItem.name == (newItem as AdapterCollegeData).name
                }
            }
        }
        return false
    }

    // 这里判断两个 Item 的内容是否相同
    // 这里是因为 我给数据类写成了 data class，默认重写了 equals 方法，对比的是各个类的各个属性是否相等
    // 如果没有重写的话就不能直接 ==
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        return oldItem == newItem
    }
})
// 这一步之前不要忘了把 adapter 中的 list 给改为 newList
diff.dispatchUpdatesTo(binding.rv.adapter!!)
```

用法看起来还算简单易懂，不过存在一个问题。其实在 List 元素个数较多的情况下，计算差分会非常耗时。所以我们不能把它放到渲染进程计算（可能会让帧率变低，甚至卡死app）。那我们应该怎么办呢？

当然是把计算的操作放进其他线程，计算完成后再切换回主线程咯。

```kotlin
val handler = Handler(Looper.getMainLooper())
thread {
    // 这里有没有觉得可以把这个 Callback 封装一下
    val diff = DiffUtil.calculateDiff(/* 略 */)
    handler.post {
        diff.dispatchUpdatesTo(binding.rv.adapter!!) 
    }
}
```

当然，我们自己不用这样做，因为谷歌已经帮我们封装好了切换线程计算差分的工具，它就是 `AsyncListDiffer`

```kotlin
class TagAdapter : RecyclerView.Adapter<TagAdapter.Holder>() {

    private val differ = AsyncListDiffer(this, object : ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    })

    class Holder(val binding: ItemTagBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(ItemTagBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.binding.tvTag.text = differ.currentList[position]
    }

    fun submitList(newList: List<String>) {
        differ.submitList(newList)
    }
}
```

AsyncListDiffer 在构造时需要传入对应的 adapter 和 ItemCallback，内部维护了一个对应泛型的列表，调用 submitList 方法后会在后台线程计算差分，然后切换回主线程对 adapter 进行 notify。

### ListAdapter

其实就是一个组合了 `AsyncListDiffer` 的 RecyclerView.Adapter 封装，总之官方提供了我们就不需要自己封装了。我们平时基本上都是直接用 `ListAdapter`，其实真需要我们手动用 `DiffUtil` 计算差分的情况是几乎没有的（不过还是要知道有这个东西）。

~~~kotlin
class TagAdapter : ListAdapter<String, TagAdapter.Holder>(
    object : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
) {

    class Holder(val binding: ItemTagBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(ItemTagBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.binding.tvTag.text = getItem(position)
    }
}
~~~

在构造器中传入 ItemCallback 即可，剩下都帮你封装好了，拿元素只需要使用 `getItem` 方法。

### ConcatAdapter

故名思义就是可以把多个 adapter 连接在一起。其实前面你们应该见识过了写一个支持多类型 ViewHolder 的 Adapter 有多麻烦，如果事后还有增加或删除 ViewHolder 类型的需求时会牵扯到大量的业务代码。可谓是牵一发而动全身，换句话说，耦合度太高了。于是出现了无数对 Adapter 进行解耦封装的仁人志士，直到官方提出了另外一种思路——不是对 Adapter 的多类型 ViewHolder 进行封装，而是简单的将多个 Adapter 组合到一起，这样单个 Adapter 需要考虑的 ViewHolder 种类就变少了。

```kotlin
binding.rv.apply {
            layoutAnimation = LayoutAnimationController(AnimationUtils.loadAnimation(this@MainActivity, R.anim.anim_fade_in))
            val manager = FlexboxLayoutManager(this@MainActivity)
            manager.apply {
                justifyContent = JustifyContent.FLEX_START
                alignItems = AlignItems.FLEX_START
            }
            layoutManager = manager
            adapter = ConcatAdapter(TagAdapter().apply { submitList(tags) }, MainAdapter(data))
        }
```

原本需要一个考虑两种 ViewHolder 的 Adapter 才能实现的需求现在只需要两个相对简单的 Adapter 就可以实现，非常的棒。

![img_v2_d72bc6c9-d225-41c6-9a55-8313ec3ccfeg](https://persecution-1301196908.cos.ap-chongqing.myqcloud.com/image_bed/img_v2_d72bc6c9-d225-41c6-9a55-8313ec3ccfeg.jpg?imageMogr2/thumbnail/!35p)

### ItemDecoration

顾名思义就是装饰，我们可以使用它来为 RecyclerView 的 item 加上一些装饰，这个“装饰”的自由度其实很高，可以把 item 给移开一块区域进行绘制，甚至还可以绘制到 item 的上层，并不是说只能画个边框做点简单的装饰，并且很重要的一点在于 **ItemDecoration是可以组合的**，这个设计进一步解耦了 RecyclerView 的功能，按照传统思路继承 RecyclerView 来干预其绘制的做法已经没有其必要性了。

官方只为我们提供了屈指可数的几个简单实现

![image-20230420225829490](https://persecution-1301196908.cos.ap-chongqing.myqcloud.com/image_bed/image-20230420225829490.png)

> 1. DividerItemDecoration，MaterialDividerItemDecoration 分割线，是我们最常用的 ItemDecoration。这两个其实区别不大，就是后面那个适配了 Material 主题
> 2. FastScroller 就是在侧边供快速滑动的一个条
> 3. ItemTouchHelper 虽然 ItemTouchHelper 并不是纯粹由 ItemDecoration 实现，但它通过继承 ItemDecoration 来控制 Item 的绘制

果然还是得讲讲自定义，不过在那之前姑且讲讲如何把一个 ItemDecoration 添加到 RecyclerView 上。

```kotlin
binding.rv.addItemDecoration(DividerItemDecoration(this@MainActivity, RecyclerView.VERTICAL))
```

这样就把 DividerItemDecoration 添加了上去，这样每个 Item 都会被一根分隔线隔开。

#### 自定义 ItemDecoration

```kotlin
class CustomItemDecoration : RecyclerView.ItemDecoration() {
    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        // 在 Item 的下层绘制
        // 也就是说你得利用 getItemOffsets 给 Decoration 腾出一块空间，再在这块区域进行绘制
        // 或者在 Item 的空白区域作画也行
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        // 在 Item 的上层绘制
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        // 对 Item 进行偏移
    }
}
```

自定义一个 ItemDecoration 只需要重写这三个方法。利用 onDraw 和 getItemOffsets，我们可以简单的实现一下 DividerItemDecoration。

```kotlin
class CustomItemDecoration(private val context: Context) : RecyclerView.ItemDecoration() {

    private val mPaint by lazy {
        Paint().apply {
            color = context.getColor(R.color.black)
        }
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        // 在 Item 的下层绘制
        // 也就是说你得利用 getItemOffsets 给 Decoration 腾出一块空间，再在这块区域进行绘制
        // 或者在 Item 的空白区域作画也行
        val childCount = parent.childCount
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight

        for (i in 0..childCount) {
            // 拿到 Item View
            val view = parent.getChildAt(i) ?: continue
            val top = view.bottom
            val bottom = view.bottom + context.resources.displayMetrics.density.toInt()
            c.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), mPaint)
        }
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        // 在 Item 的上层绘制
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        // 给底部腾出 1dp 位置
        outRect.bottom += context.resources.displayMetrics.density.toInt()
    }
}
```

思路就是在 getItemOffsets 给每个 Item 的底部腾出 1dp 的位置，在 onDraw 根据 RecyclerView 中的子 View 的位置计算出这腾出来的1dp 的位置，绘制。

效果还不错吧

![img_v2_7aac666a-a23d-4629-a925-155dacccbfeg](https://persecution-1301196908.cos.ap-chongqing.myqcloud.com/image_bed/img_v2_7aac666a-a23d-4629-a925-155dacccbfeg.jpg?imageMogr2/thumbnail/!35p)

### ItemTouchHelper

ItemTouchHelper 是 RecyclerView 的一个帮助类，它通过继承 ItemDecoration 来一定程度的干涉 Item 的偏移，内部持有了一个OnItemTouchListener 用于处理 Item 的 TouchEvent，实现 OnChildAttachStateChangeListener 来在 Item Detach 时注销 onTouchListener 来防止内存泄漏。

![image-20230421203858043](https://persecution-1301196908.cos.ap-chongqing.myqcloud.com/image_bed/image-20230421203858043.png)

![image-20230421225923940](https://persecution-1301196908.cos.ap-chongqing.myqcloud.com/image_bed/image-20230421225923940.png)

ItemTouchHelper 是一个封装得相当彻底的一个帮助类，只需要简单的配置我们就可以实现 上下拖拽Item，左滑右滑删除 这样高级的功能。

```kotlin
val helper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        // 允许往上往下拖拽移动
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        // 允许左滑右滑删除
        val swipeFlags = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        // 开启长按拖拽，并且拖拽改变顺序
        val from = viewHolder.bindingAdapterPosition
        val to = target.bindingAdapterPosition
        // 交换下位置
        Collections.swap(data, from, to)
        // notify 下
        adapter!!.notifyItemMoved(from, to)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // 被滑走时 这里把 item 删掉
        val pos = viewHolder.bindingAdapterPosition
        data.removeAt(pos)
        binding.rv.adapter!!.notifyItemRemoved(pos)
    }

})
helper.attachToRecyclerView(binding.rv)
```

效果是非常不错的

![Peek 2023-04-22 13-12](https://persecution-1301196908.cos.ap-chongqing.myqcloud.com/image_bed/Peek%202023-04-22%2013-12.gif)

### notifyItemChanged 的 payload 参数的作用

实际上是一种从外部向 ViewHolder 传递数据的手段。比如我在外面设置了一个按钮，我希望点击这个按钮能改变 RecyclerView 中指定 ViewHolder 的内容，于是我们就需要使用 notifyItemChanged 通知 Adapter 来修改这个 ViewHolder，并且具体我们希望怎么改是可以通过传参 (payload) 来指定的。

```kotlin
// 在 ViewHolder 中先重写这个方法适配部分更新的逻辑
override fun onBindViewHolder(holder: Holder, position: Int, payloads: MutableList<Any>) {
    val payload = payloads.getOrNull(0) as? String
    holder.apply {
        when (val itemData = data[position]) {
            is Data.Lesson -> {
                this as Holder.Lesson
                tvTitle.text = payload ?: itemData.title
                tvDesc.text = itemData.desc
            }
            is Data.College -> {
                this as Holder.College
                tvName.text = payload ?: itemData.name
                Glide.with(ivAvatar)
                    .load(itemData.avatar)
                    .centerCrop()
                    .into(ivAvatar)
            }
        }
    }
}

// 在外部 notify 时传入 payload
binding.rv.adapter?.notifyItemChanged(0, "卡耐基梅隆大学")
```

## ViewHolder的缓存复用机制

### 引

不知道大家有没有产生一个疑惑，为什么 Adapter 要将创建视图(onCreateViewHolder)与绑定视图(onBindViewHolder)的操作分开？在Activity中，我们对视图进行操作是直接在创建视图(onCreate)之后的，为什么 RecyclerView 不直接像这样设计？

实际上这样设计是为了配合 RecyclerView 对 ViewHolder 的缓存复用。实际上 ViewHolder 在创建之后就会被放入一个缓存池中，需要用的时候拿出来，绑定一下显示出来，不需要的时候又放回缓存池。也就是说一个 ViewHolder 被创建之后可能会被绑定很多次，那么将创建视图和绑定视图的操作分离开就有其必要性了。得益于这个缓存复用机制，即便 RecyclerView 存在大量的元素也非常的流畅，相比之下 ListView 应对同样的状况可能就会非常卡了。

RecyclerView 跟 ListView 之间最大的一点不同就是 ViewHolder 的缓存复用机制，相比于 ListView，RecyclerView 对其包含元素的缓存更加彻底，因此性能也更好。事实上，RecyclerView 的 ViewHolder 一共有三级缓存。

### 三级缓存

> RecyclerView 将自身功能解耦为了多个帮助类
>
> LayoutManager：接管RecyclerView的Measure，Layout，Draw的过程
>
> Recycler：缓存池
>
> Adapter：ViewHolder的生成器和内容绑定器。

ViewHolder 的缓存主要是交给了 Recycler 这个帮助类。

~~~java
public final class Recycler {
        final ArrayList<ViewHolder> mAttachedScrap = new ArrayList<>();
        ArrayList<ViewHolder> mChangedScrap = null;

        final ArrayList<ViewHolder> mCachedViews = new ArrayList<ViewHolder>();

        private final List<ViewHolder>
                mUnmodifiableAttachedScrap = Collections.unmodifiableList(mAttachedScrap);

        private int mRequestedCacheMax = DEFAULT_CACHE_SIZE;
        int mViewCacheMax = DEFAULT_CACHE_SIZE;

        RecycledViewPool mRecyclerPool;

        private ViewCacheExtension mViewCacheExtension;

        static final int DEFAULT_CACHE_SIZE = 2;
    // ...
}
~~~

> ArrayList mAttachedScrap：未与RecyclerView分离的ViewHolder列表,如果仍依赖于 RecyclerView （比如已经滑动出可视范围，但还没有被移除掉），但已经被标记移除的 ItemView 集合会被添加到 mAttachedScrap 中
>
> - 按照id和position来查找ViewHolder
>
> ArrayList mChangedScrap：表示数据已经改变的viewHolder列表,存储 notifXXX 方法时需要改变的 ViewHolder,匹配机制按照position和id进行匹配
>
> ArrayList mCachedViews：缓存ViewHolder，主要用于解决RecyclerView滑动抖动时的情况，还有用于保存Prefetch的ViewHoder
>
> - 最大的数量为：mViewCacheMax = mRequestedCacheMax + extraCache（extraCache是由prefetch的时候计算出来的）
>
> ViewCacheExtension mViewCacheExtension：开发者可自定义的一层缓存，是虚拟类ViewCacheExtension的一个实例，开发者可实现方法getViewForPositionAndType(Recycler recycler, int position, int type)来实现自己的缓存。
>
> - 适用场景：[android.jlelse.eu/anatomy-of-…](https://medium.com/android-news/anatomy-of-recyclerview-part-1-a-search-for-a-viewholder-continued-d81c631a2b91)
> - 位置固定
> - 内容不变
> - 数量有限
>
> mRecyclerPool ViewHolder缓存池，在有限的mCachedViews中如果存不下ViewHolder时，就会把ViewHolder存入RecyclerViewPool中。
>
> - 按照Type来查找ViewHolder
> - 每个Type默认最多缓存5个

RecyclerView在设计的时候讲上述5个缓存对象分为了3级。每次创建ViewHolder的时候，会按照优先级依次查询缓存创建ViewHolder。每次将ViewHolder缓存到Recycler缓存的时候，也会按照优先级依次缓存进去。三级缓存分别是：

- 一级缓存：返回布局和内容都都有效的ViewHolder
  - 按照position或者id进行匹配
  - **命中一级缓存无需onCreateViewHolder和onBindViewHolder**
  - mAttachScrap中存放的是当前RecyclerView中能看见的ViewHolder
  - mChangedScarp中存放的是受到 adapter.notifyXXX 影响需要变化的 ViewHolder 。当调用了 adapter.notifyXXX 时，首先计算出需要变化的 ViewHolder 放入 mChangedScarp，然后进行一次重绘。重绘时会根据 mChangedScarp 中记录的 ViewHolder 进行局部更新（onBindViewHolder）。
  - mCachedViews：默认大小为2, 缓存已经划出 RecyclerView 的 ViewHolder，此时这个 ViewHolder 上绑定的视图是仍然有效的。
- 二级缓存：返回View (自定义，不常用)
  - 按照position和type进行匹配
  - 直接返回View
  - 需要自己继承ViewCacheExtension实现
  - 位置固定，内容不发生改变的情况，比如说Header如果内容固定，就可以使用
- 三级缓存：返回布局有效，内容无效的ViewHolder
  - 按照type进行匹配，每个type缓存值默认=5
  - layout是有效的，但是内容是无效的
  - 多个RecyclerView可共享,可用于多个RecyclerView的优化

![image-20230412125549198](https://persecution-1301196908.cos.ap-chongqing.myqcloud.com/image_bed/image-20230412125549198.png)

### 实例分析

#### 出屏幕时候的情况-mCacheViews未满

![image-20230412125639414](https://persecution-1301196908.cos.ap-chongqing.myqcloud.com/image_bed/image-20230412125639414.png)

1. 当ViewHolder（position=0，type=1）出屏幕的时候，由于mCacheViews是空的，那么就直接放在mCacheViews里面（从0-N是由老到新）。此时ViewHolder在mCacheViews里面布局和内容都是有效的，因此可以直接复用。
2. ViewHolder（position=1，type=2）同步骤1

#### 出屏幕时候的情况-mCacheViews已经满

![image-20230412125719437](https://persecution-1301196908.cos.ap-chongqing.myqcloud.com/image_bed/image-20230412125719437.png)

1. 当ViewHolder（position=2，type=1）出屏幕的时候由于一级缓存mCacheViews已经满了，因此然后移除mCacheViews里面最老的ViewHolder(position=0,type=1)到RecyclePool中，然后将ViewHolder（position=2，type=1）存入mCacheViews。此时被移除到RecyclePool的ViewHolder的内容会被标记为无效，当其复用的时候需要再次通过Adapter.bindViewHolder来绑定内容。
2. ViewHolder（position=3，type=3）同步骤1

#### 进屏幕时候的情况

![image-20230412125749119](https://persecution-1301196908.cos.ap-chongqing.myqcloud.com/image_bed/image-20230412125749119.png)

1. 当ViewHolder（position=3，type=3）进入屏幕绘制的时候，由于Recycler的mCacheViews里面找不到position匹配的View，同时RecyclerPool里面找不到type匹配的View，因此，其只能通过adapter.createViewHolder来创建ViewHolder，然后通过adapter.bindViewHolder来绑定内容。
2. 当ViewHolder（position=11，type=1）进入屏幕的时候，发现ReccylerPool里面能找到type=1的缓存，因此直接从ReccylerPool里面取来使用。由于内容是无效的，因此还需要调用bindViewHolder来绑定布局。同时ViewHolder（position=4，type=3）需要出屏幕，会经历步骤3回收的过程
3. ViewHolder（position=12，type=3）同步骤2

#### 屏幕往下拉ViewHoder（position=1）进入屏幕的情况

![image-20230412125821972](https://persecution-1301196908.cos.ap-chongqing.myqcloud.com/image_bed/image-20230412125821972.png)

1. 由于mCacheView里面的有position=1的ViewHolder与之匹配，直接返回。由于内容是有效的，因此无需再次绑定内容
2. ViewHolder（position=0）同步骤1

### 绘制流程

> - RecyclerView.requestLayout开始发生绘制，忽略Measure的过程 (onMeasure 交给了 LayoutManager 处理)
>
> - 在Layout的过程会通过LayoutManager.fill去将RecyclerView填满 （如果没有设置layout manager的话会在log里发个警告，跳过layout流程)
>
> - LayoutManager.fill会调用LayoutManager.layoutChunk去生成一个具体的ViewHolder
>
> - 然后LayoutManager就会调用Recycler.getViewForPosition向Recycler去要ViewHolder
>
> - Recycler首先去一级缓存里面查找是否命中，如果命中直接返回。如果一级缓存没有找到，则去三级缓存查找，如果三级缓存找到了则调用Adapter.bindViewHolder来绑定内容，然后返回。如果三级缓存没有找到，那么就通过Adapter.createViewHolder创建一个ViewHolder，然后调用Adapter.bindViewHolder绑定其内容，放入Recycler。
>
> 一直重复步骤3-5，直到创建的ViewHolder填满了整个RecyclerView为止。

![img](https://p1-jj.byteimg.com/tos-cn-i-t2oaga2asx/gold-user-assets/2018/8/20/1655326cbddd7d9e~tplv-t2oaga2asx-zoom-in-crop-mark:4536:0:0:0.image)

我不打算讲源码，但如果有对源码感兴趣的同学可以根据上面的思路阅读源码。
