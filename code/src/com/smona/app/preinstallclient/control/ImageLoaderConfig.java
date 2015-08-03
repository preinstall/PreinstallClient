package com.smona.app.preinstallclient.control;

import android.content.Context;
import android.graphics.Bitmap;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.smona.app.preinstallclient.R;

public class ImageLoaderConfig {

    public static DisplayImageOptions getDefaultOptions() {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                // 设置图片在下载期间显示的图片
                .showImageOnLoading(R.drawable.default_icon)
                // 设置图片Uri为空或是错误的时候显示的图片
                .showImageForEmptyUri(R.drawable.default_icon)
                // 设置图片加载/解码过程中错误时候显示的图片
                .showImageOnFail(R.drawable.default_icon)
                .cacheInMemory(true)
                // 设置下载的图片是否缓存在内存中
                .cacheOnDisc(true)
                // 设置下载的图片是否缓存在SD卡中
                .considerExifParams(true)
                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
                .bitmapConfig(Bitmap.Config.RGB_565).considerExifParams(true)
                .displayer(new RoundedBitmapDisplayer(20))
                .build();
        return options;
    }

    /**
     * 异步图片加载ImageLoader的初始化操作，在Application中调用此方法
     * 
     * @param context
     *            上下文对象
     * @param cacheDisc
     *            图片缓存到SDCard的目录，只需要传入SDCard根目录下的子目录即可，默认会建立在SDcard的根目录下
     */
    public static void initImageLoader(Context context, String cacheDisc) {
        // 实例化Builder
        ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(
                context);
        // 设置线程数量
        builder.threadPoolSize(3);
        // 设定线程等级比普通低一点
        builder.threadPriority(Thread.NORM_PRIORITY);
        // 设定内存缓存为弱缓存
        builder.memoryCache(new WeakMemoryCache());
        builder.memoryCacheSizePercentage(60);
        // 设定内存图片缓存大小限制，不设置默认为屏幕的宽高
        // builder.memoryCacheExtraOptions(480, 800);
        // 设定只保存同一尺寸的图片在内存
        builder.denyCacheImageMultipleSizesInMemory();
        // 设定缓存的SDcard目录，UnlimitDiscCache速度最快
        if (null != cacheDisc) {
            // 获取本地缓存的目录，该目录在SDCard的根目录下
            builder.discCache(new UnlimitedDiscCache(StorageUtils
                    .getOwnCacheDirectory(context, cacheDisc)));
        }
        // 设定缓存到SDCard目录的文件命名
        builder.discCacheFileNameGenerator(new HashCodeFileNameGenerator());
        // 设置ImageLoader的配置参数
        builder.defaultDisplayImageOptions(getDefaultOptions());

        // 初始化ImageLoader
        ImageLoader.getInstance().init(builder.build());
        ImageLoader.getInstance().handleSlowNetwork(true);
    }
}
