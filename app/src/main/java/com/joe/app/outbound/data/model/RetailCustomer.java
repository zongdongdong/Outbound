package com.joe.app.outbound.data.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Joe on 2016/8/27.
 * Email-joe_zong@163.com
 * 实体类---零售单客户
 */

public class RetailCustomer implements Serializable {
    public List<Result> result;
    public class Result{
        public String id;
        public String name;
    }
}
