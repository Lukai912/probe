package com.csmijo.probbugtags.bean;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by chengqianqian-xy on 2016/6/14.
 */
public class BugTag implements Parcelable {
    private int type;   //{1:bug; 2:建议 3:崩溃 4:反馈}
    private int priority;       //3=紧急,2=高,1=普通,0=低
    private String assignee;
    private String description;
    private int direction;  //{0:left;1:right}
    private int postion_x;
    private int postion_y;
    private String attachPicName;


    public BugTag() {
        this.type = 0;
        this.priority = 1;
        this.assignee = "";
        this.description = "";
        this.direction = 0;
        this.postion_x = 0;
        this.postion_y = 0;
        this.attachPicName = "";
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public int getPostion_x() {
        return postion_x;
    }

    public void setPostion_x(int postion_x) {
        this.postion_x = postion_x;
    }

    public int getPostion_y() {
        return postion_y;
    }

    public void setPostion_y(int postion_y) {
        this.postion_y = postion_y;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public String getAttachPicName() {
        return attachPicName;
    }

    public void setAttachPicName(String attachPicName) {
        this.attachPicName = attachPicName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<BugTag> CREATOR = new Creator<BugTag>() {
        @Override
        public BugTag createFromParcel(Parcel source) {
            BugTag bugTag = new BugTag();
            bugTag.type = source.readInt();
            bugTag.priority = source.readInt();
            bugTag.assignee = source.readString();
            bugTag.description = source.readString();
            bugTag.direction = source.readInt();
            bugTag.postion_x = source.readInt();
            bugTag.postion_y = source.readInt();
            bugTag.attachPicName = source.readString();
            return bugTag;
        }

        @Override
        public BugTag[] newArray(int size) {
            return new BugTag[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeInt(priority);
        dest.writeString(assignee);
        dest.writeString(description);
        dest.writeInt(direction);
        dest.writeInt(postion_x);
        dest.writeInt(postion_y);
        dest.writeString(attachPicName);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o instanceof BugTag) {
            BugTag bugTag = (BugTag) o;
            return bugTag.getPostion_x() == this.postion_x && bugTag.getPostion_y() == this.postion_y;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + postion_x;
        result = 37 * result + postion_y;
        return result;
    }

    @Override
    public String toString() {
        return "type=" + this.type + ",priority=" + this.priority + ",assignee=" + this.assignee + "," +
                "direction=" + this.direction + ",(x=" + postion_x + ",y=" + postion_y + ")," +
                "attachPicName=" + attachPicName +
                "description=" + description;
    }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject object = new JSONObject();

        object.put("type", this.getType());
        object.put("priority", this.getPriority());
        object.put("assignee", this.getAssignee());
        object.put("description", this.getDescription());
        object.put("direction", this.getDirection());
        object.put("position_x", this.getPostion_x());
        object.put("position_y", this.getPostion_y());
        object.put("attachPicName", this.getAttachPicName());
        return object;
    }
}
