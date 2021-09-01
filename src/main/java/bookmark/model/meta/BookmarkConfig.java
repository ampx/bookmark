package bookmark.model.meta;

public class BookmarkConfig {

    public boolean defaultRetentionPolicy() {
        if (txnRetentionPolicy == 0) {
            return true;
        }
        return false;
    }

    public boolean isTtlDisabled() {
        if (txnRetentionPolicy < 0) {
            return true;
        }
        return false;
    }

    public void useDefaultTtl() {
        txnRetentionPolicy = 0;
    }

    public void disableTtl() {
        txnRetentionPolicy = -1;
    }

    public Integer getTxnRetentionPolicy() {
        return txnRetentionPolicy;
    }

    public void setTxnRetentionPolicy(Integer txnRetentionPolicy) {
        this.txnRetentionPolicy = txnRetentionPolicy;
    }

    Integer txnRetentionPolicy = 0;
}
