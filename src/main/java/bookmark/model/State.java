package bookmark.model;

public class State {
    public final static Integer notReady = -2;
    public final static Integer maintenance = -1;
    public final static Integer unlocked = 0;
    public final static Integer locked = 1;
    public final static Integer error = 2;
    public final static Integer reset = 3;

    public State(Integer state) {
        this.state = state;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    Integer state;
}
