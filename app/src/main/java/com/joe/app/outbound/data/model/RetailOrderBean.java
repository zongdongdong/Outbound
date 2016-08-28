package com.joe.app.outbound.data.model;

import java.io.Serializable;
import java.util.List;

import static android.R.attr.id;

/**
 * Created by Joe on 2016/8/28.
 * Email-joe_zong@163.com
 */

public class RetailOrderBean implements Serializable{
    public List<Data> result;
    public class Data implements Serializable{
        public String id;
        public String code;
        public String billdate;
        public String volume;
        public String quantity;
        public String customer_name;
        public String quantity_string;
    }
}
