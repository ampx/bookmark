package bookmark.model.meta;

public class BookmarkState {

    public BookmarkState(Integer state) {
        this.state = state;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Boolean isReady() {
        if (state > -1) {
            return true;
        }
        return false;
    }

    public static BookmarkState notReadyState() {
        return new BookmarkState(-2);
    }

    public static BookmarkState maintenanceState() {
        return new BookmarkState(-1);
    }

    public static BookmarkState unlockedState() {
        return new BookmarkState(0);
    }

    public static BookmarkState lockedState() {
        return new BookmarkState(1);
    }

    public static BookmarkState errorState() {
        return new BookmarkState(2);
    }

    public static BookmarkState resetState() {
        return new BookmarkState(3);
    }



    Integer state;

    public String toString() {
        return state.toString();
    }
}
