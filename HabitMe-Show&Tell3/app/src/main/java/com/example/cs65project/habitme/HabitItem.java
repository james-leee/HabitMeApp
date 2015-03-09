package com.example.cs65project.habitme;

/**
 * Created by Mubing on 2/27/15.
 * This is the definition of habit object
 */
public class HabitItem {

    private Long id;
    private String habitTitle;
    private String checkTimeList;
    private int frequency;
    private int createType;
    private int choosePosition;
    private int useNum;
    private String timeLength;
    private String location;

    public HabitItem(){
        checkTimeList = "none";
        frequency = 0;
        timeLength = "0";
    }

    public HabitItem(String title, String time, int f){
        checkTimeList = "none";
        habitTitle = title;
        checkTimeList += "," + String.valueOf(time);
        frequency = f;
        timeLength = "0";

    }

    public void setTimeLength(String timeLength) {
        this.timeLength = timeLength;
    }

    public String getTimeLength() {
        return timeLength;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setChoosePosition(int choosePosition) {
        this.choosePosition = choosePosition;
    }

    public int getChoosePosition() {
        return choosePosition;
    }

    public void setUseNum(int useNum) {
        this.useNum = useNum;
    }

    public int getUseNum() {
        return useNum;
    }

    public void setCreateType(int createType) {
        this.createType = createType;
    }

    public int getCreateType() {
        return createType;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public void setHabitTitle(String habitTitle) {
        this.habitTitle = habitTitle;
    }
    public void addCheckTimeList(long time){
        this.checkTimeList += "," + String.valueOf(time);
    }

    public void setCheckTimeList(String checkTimeList) {
        this.checkTimeList = checkTimeList;
    }

    public int getFrequency() {
        return frequency;
    }

    public String getHabitTitle() {
        return habitTitle;
    }
    public String getCheckTimeList(){
        return this.checkTimeList;
    }
}
