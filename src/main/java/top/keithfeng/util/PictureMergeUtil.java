package top.keithfeng.util;

import top.keithfeng.bean.MergeBean;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PictureMergeUtil {

    /*
    该类借用了 https://blog.csdn.net/Mael_Xu/article/details/122360208 提供的拼接工具类
     */

    /*
     * 描述 多张图片拼接
     *
     * @param fileUrls 本地地址路径
     * @param savePath 拼接图片存放路径
     * @param isHorizontal 是否是水平拼接
     * true:是(水平拼接)  flase:否(竖直拼接)
     * @return void
     * @author gx
     * @date 2022/1/6 14:39:22
     * @version 1.0
     */
    public static void merge(List<String> fileUrls, String savePath, Boolean isHorizontal) {
        try {
            List<Integer> widthList = new ArrayList<>();//存放所有图片宽度
            List<Integer> heightList = new ArrayList<>();//存放所有图片高度
            List<MergeBean> imgInfoList = new ArrayList<>();//存放所有图片信息
            //获取图片信息并存放上面定义的变量中
            getImageInfoList(fileUrls, widthList, heightList, imgInfoList);
            BufferedImage imageResult = null;
            if (isHorizontal) {//水平方向
                imageResult = buildHorizontalImage(widthList, heightList, imgInfoList);
            } else {//垂直方向
                imageResult = buildVerticalImage(widthList, heightList, imgInfoList);
            }
            File outFile = new File(savePath);
            if (!outFile.exists()) {
                outFile.createNewFile();
            }
            int temp = savePath.lastIndexOf(".") + 1;
            ImageIO.write(imageResult, savePath.substring(temp), outFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /*
     * 描述
     *
     * @param fileUrls 图片本地地址
     * @param widthList 图片宽度
     * @param heightList 图片高度
     * @param imgInfoList 图片信息
     * @return
     * @author gx
     * @date 2022/1/6 15:54:11
     * @version 1.0
     */
    private static void getImageInfoList(List<String> fileUrls, List<Integer> widthList, List<Integer> heightList, List<MergeBean> imgInfoList) throws IOException {
        for (String url : fileUrls) {
            MergeBean mergeBean = new MergeBean();
            /* 1 读取第一张图片 */
            File file = new File(url);
            BufferedImage imageFirst = ImageIO.read(file);
            int width = imageFirst.getWidth();// 图片宽度
            int height = imageFirst.getHeight();// 图片高度
            int[] imageArray = new int[width * height];// 从图片中读取RGB
            imageArray = imageFirst.getRGB(0, 0, width, height, imageArray, 0, width);

            widthList.add(width);
            heightList.add(height);
            mergeBean.setWidth(width);
            mergeBean.setHeight(height);
            mergeBean.setImageArray(imageArray);
            imgInfoList.add(mergeBean);
        }
    }
    /*
     * 描述
     *
     * @param widthList 图片宽度
     * @param heightList 图片高度
     * @param imgInfoList 图片信息
     * @return
     * @author gx
     * @date 2022/1/6 15:55:39
     * @version 1.0
     */
    private static BufferedImage buildVerticalImage(List<Integer> widthList, List<Integer> heightList, List<MergeBean> imgInfoList) {
        //查找最大的宽度
        Integer maxWidth = Collections.max(widthList);
        //计算所有图片高度之和
        Integer sumHeight = heightList.stream().reduce(Integer::sum).orElse(0);
        // 生成新图片  (新图片的宽度之和，新图片的高度,BufferedImage.TYPE_INT_RGB)
        BufferedImage imageResult = new BufferedImage(maxWidth, sumHeight, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < imgInfoList.size(); i++) {
            if (i == 0) {//第一张图片
                //创建第一张图片
                createOneVerticalImg(imageResult, maxWidth, imgInfoList.get(i));
            } else {//大于一张图片
                int nowHeightNum = getNowHeightNum(imgInfoList, i);//计算上一张图片结束的宽度，当前拼接的图片的开始位置
                //创建图片
                createOtherVerticalImg(imageResult, maxWidth, imgInfoList.get(i), nowHeightNum);
            }
        }
        return imageResult;
    }

    /*
     * 描述 第二张图片开始创建
     *
     * @param imageResult 新图的对象
     * @param maxWidth 最大宽度
     * @param mergeBean 图片信息
     * @param nowHeightNum 上一张图片结束时的高度，该图片开始创建的位置
     * @return void
     * @author gx
     * @date 2022/1/6 14:27:20
     * @version 1.0
     */
    private static void createOtherVerticalImg(BufferedImage imageResult, Integer maxWidth, MergeBean mergeBean, int nowHeightNum) {
        int k1 = 0;
        for (int i1 = 0; i1 < mergeBean.getHeight(); i1++) {
            for (int j1 = 0; j1 < maxWidth; j1++) {
                if (mergeBean.getWidth() > j1) {
                    imageResult.setRGB(j1, i1 + nowHeightNum, mergeBean.getImageArray()[k1]);
                    k1++;
                } else {
                    imageResult.setRGB(j1, i1 + nowHeightNum, -328966);
                }
            }
        }
    }

    /*
     * 描述 创建第一张图片
     *
     * @param imageResult 新建图片对象
     * @param maxWidth 最大宽度
     * @param mergeBean 图片信息
     * @return void
     * @author gx
     * @date 2022/1/6 14:35:14
     * @version 1.0
     */
    private static void createOneVerticalImg(BufferedImage imageResult, Integer maxWidth, MergeBean mergeBean) {
        int k = 0;
        for (int i = 0; i < mergeBean.getHeight(); i++) {
            for (int j = 0; j < maxWidth; j++) {
                if (mergeBean.getWidth() > j) {
                    imageResult.setRGB(j, i, mergeBean.getImageArray()[k]);
                    k++;
                } else {
                    imageResult.setRGB(j, i, -328966);
                }
            }
        }
    }

    /*
     * 描述
     *
     * @param widthList 存放所有图片宽度
     * @param heightList存放所有图片高度
     * @param imgInfoList 存放所有图片信息
     * @return java.awt.image.BufferedImage  定义的新图片
     * @author gx
     * @date 2022/1/6 14:22:30
     * @version 1.0
     */
    private static BufferedImage buildHorizontalImage(List<Integer> widthList, List<Integer> heightList, List<MergeBean> imgInfoList) {
        //查找最大的高度
        Integer maxHeight = Collections.max(heightList);
        //计算所有图片宽度之和
        Integer sumWidth = widthList.stream().reduce(Integer::sum).orElse(0);
        // 生成新图片  (新图片的宽度之和，新图片的高度,BufferedImage.TYPE_INT_RGB)
        BufferedImage imageResult = new BufferedImage(sumWidth, maxHeight, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < imgInfoList.size(); i++) {
            if (i == 0) { //第一张图片
                //创建第一张图片
                createOneHorizontalImg(imageResult, maxHeight, imgInfoList.get(i));
            } else {//大于一张图片
                int nowWidthNum = getNowWidthNum(imgInfoList, i);//计算上一张图片结束的宽度，当前拼接的图片的其实位置
                //创建图片
                createOtherHorizontalImg(imageResult, maxHeight, imgInfoList.get(i), nowWidthNum);
            }
        }
        return imageResult;
    }

    /*
     * 描述  计算上一张图片结束的高度，当前拼接的图片的开始位置
     *
     * @param imgInfoList  存放所有图片信息
     * @param i 遍历到图片的位置
     * @return int 返回上一张照片结束的位置，当前图片的起始位置
     * @author gx
     * @date 2022/1/6 14:25:13
     * @version 1.0
     */
    private static int getNowHeightNum(List<MergeBean> imgInfoList, int i) {
        int nowHeightNum = 0;
        for (int k = 0; k < i; k++) {
            nowHeightNum += imgInfoList.get(k).getHeight();
        }
        return nowHeightNum;
    }

    /*
     * 描述  计算上一张图片结束的宽度，当前拼接的图片的开始位置
     *
     * @param imgInfoList  存放所有图片信息
     * @param i 遍历到图片的位置
     * @return int 返回上一张照片结束的位置，当前图片的起始位置
     * @author gx
     * @date 2022/1/6 14:25:13
     * @version 1.0
     */
    private static int getNowWidthNum(List<MergeBean> imgInfoList, int i) {
        int nowWidthNum = 0;
        for (int k = 0; k < i; k++) {
            nowWidthNum += imgInfoList.get(k).getWidth();
        }
        return nowWidthNum;
    }

    /*
     * 描述 第二张图片开始创建
     *
     * @param imageResult 新图的对象
     * @param maxHeight 最大高度
     * @param mergeBean 图片信息
     * @param nowWidthNum 上一张图片结束时的宽度，该图片开始创建的位置
     * @return void
     * @author gx
     * @date 2022/1/6 14:27:20
     * @version 1.0
     */
    private static void createOtherHorizontalImg(BufferedImage imageResult, Integer maxHeight, MergeBean mergeBean, int nowWidthNum) {
        int k1 = 0;
        for (int j1 = 0; j1 < maxHeight; j1++) {
            for (int i1 = 0; i1 < mergeBean.getWidth(); i1++) {
                if (mergeBean.getHeight() > j1) {
                    imageResult.setRGB(i1 + nowWidthNum, j1, mergeBean.getImageArray()[k1]);
                    k1++;
                } else {
                    imageResult.setRGB(i1 + nowWidthNum, j1, -328966);
                }
            }
        }
    }

    /*
     * 描述 创建第一张图片
     *
     * @param imageResult 新图的对象
     * @param maxHeight 最大高度
     * @param mergeBean 图片信息
     * @return void
     * @author gx
     * @date 2022/1/6 14:27:20
     * @version 1.0
     */
    private static void createOneHorizontalImg(BufferedImage imageResult, Integer maxHeight, MergeBean mergeBean) {
        int k = 0;
        for (int j = 0; j < maxHeight; j++) {
            for (int i = 0; i < mergeBean.getWidth(); i++) {
                if (mergeBean.getHeight() > j) {
                    imageResult.setRGB(i, j, mergeBean.getImageArray()[k]);
                    k++;
                } else {
                    imageResult.setRGB(i, j, -328966);
                }
            }
        }
    }


    /**
     * 删除指定文件夹下所有文件
     * @param path 文件夹完整绝对路径 ,"Z:/xuyun/save"
     */
    public static boolean delAllFile(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            return flag;
        }
        if (!file.isDirectory()) {
            return flag;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + tempList[i]);//先删除文件夹里面的文件
                delFolder(path + "/" + tempList[i]);//再删除空文件夹
                flag = true;
            }
        }
        return flag;
    }
    /*************************删除文件夹delFolder / 删除文件夹中的所有文件delAllFile *start***********/

    /**
     * 删除文件夹
     * @param folderPath 文件夹完整绝对路径 ,"Z:/xuyun/save"
     */
    public static void delFolder(String folderPath) {
        try {
            delAllFile(folderPath); //删除完里面所有内容
            String filePath = folderPath;
            filePath = filePath.toString();
            File myFilePath = new File(filePath);
            myFilePath.delete(); //删除空文件夹
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * 描述 新建文件夹
     *
     * @param folderPath  新建文件夹路径
     * @return void
     * @author gx
     * @date 2022/1/6 16:00:10
     * @version 1.0
     */
    public static void newFolder(String folderPath) {
        String filePath = folderPath;
        File myFilePath = new File(filePath);

        try {
            if (myFilePath.isDirectory()) {
//                System.out.println("文件夹已存在");
            } else {
                myFilePath.mkdir();
//                System.out.println("新建目录成功");
            }
        } catch (Exception var4) {
            System.out.println("新建目录操作出错");
            var4.printStackTrace();
        }

    }
}
