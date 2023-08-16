package top.keithfeng.bean;

import lombok.Data;

@Data
public class MergeBean {

    //图片宽
    private int width;
    //图片高
    private int height;
    //图片的像素集合
    private int[] imageArray;
}
