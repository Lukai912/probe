package com.csmijo.probbugtags.bean;

/**
 * Created by chengqianqian-xy on 2016/7/4.
 */
public class MyMessage {
    private int flag;
    private String msg;

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String toString() {
        return "message [flag = " + flag + ",msg=" + msg + "]";
    }

}
