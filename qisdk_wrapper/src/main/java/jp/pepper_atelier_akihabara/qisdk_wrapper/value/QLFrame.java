package jp.pepper_atelier_akihabara.qisdk_wrapper.value;

public class QLFrame {
    public static final int FRAME_TYPE_ROBOT = 1;
    public static final int FRAME_TYPE_GAZE = 2;
    // public static final int FRAME_TYPE_HUMAN = 3;
    public static final int FRAME_TYPE_MAP = 4;

    private int type;
    private long timestamp = 0L;

    public QLFrame(int type){
        this.type = type;
    }

    public QLFrame(int type, long timestamp){
        this.type =type;
        this.timestamp = timestamp;
    }

    public int getType(){
        return type;
    }

    public long getTimestamp(){
        return timestamp;
    }

}
