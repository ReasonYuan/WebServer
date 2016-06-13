package com.yiyihealth.ds.date.timeline;

import java.util.Date;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	
    	Date d = new Date();
    	System.out.println("1个月后  "+TimeLine.convertDate(d, "1个月后"));
    	System.out.println("1个月后  "+TimeLine.convertDate(d, "1日后"));
    	System.out.println("1个月后  "+TimeLine.convertDate(d, "1天后"));
    	System.out.println("一个月前  "+TimeLine.convertDate(d, "一个月前"));
        System.out.println("下个月  "+TimeLine.convertDate(d, "下个月"));
        System.out.println("上个月  "+TimeLine.convertDate(d, "上个月"));
        System.out.println("下个周  "+ TimeLine.convertDate(d, "下个周"));
        System.out.println( "上个周  "+TimeLine.convertDate(d, "上个周"));
        System.out.println("二十个周后  "+TimeLine.convertDate(d, "二十个月后"));
        System.out.println("一天前  "+TimeLine.convertDate(d, "一天前"));
        System.out.println("5个月后  "+TimeLine.convertDate(d, "5个月后"));
        System.out.println( "3个月前  "+TimeLine.convertDate(d, "3个月前"));
        System.out.println("两个月后  "+TimeLine.convertDate(d, "两个月后"));
        System.out.println("两个月前  "+TimeLine.convertDate(d, "两个月前"));
        
        
    }
}
