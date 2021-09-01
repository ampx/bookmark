package bookmark.model.meta;

public class BookmarkConfig {
    public Integer getTxnRetentionPolicy() {
        return txnRetentionPolicy;
    }

    public void setTxnRetentionPolicy(Integer txnRetentionPolicy) {
        this.txnRetentionPolicy = txnRetentionPolicy;
    }

    Integer txnRetentionPolicy;
}
