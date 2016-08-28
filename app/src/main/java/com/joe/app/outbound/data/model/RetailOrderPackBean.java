package com.joe.app.outbound.data.model;

import java.util.List;

/**
 * Created by Joe on 2016/8/28.
 * Email-joe_zong@163.com
 */

public class RetailOrderPackBean {
    public List<Result> result;
    public class Result{
        public String id;
        public String barcode;
        public String volume;
        public String quantity;
        public String material;
        public String color;
        public String craft;
        public String weight;
    }
}
