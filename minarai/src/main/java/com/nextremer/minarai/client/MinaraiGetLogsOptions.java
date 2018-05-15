package com.nextremer.minarai.client;

import android.support.annotation.NonNull;

/**
 * Get log options class.
 *
 * @author nextremer.com
 */
public class MinaraiGetLogsOptions {

    private String ltDate;  // .
    private int limit;      // Limit.

    /**
     * Constructor.
     *
     * @param ltDate .
     * @param limit  Limit.
     */
    public MinaraiGetLogsOptions(@NonNull String ltDate, int limit) {

        if(ltDate == null)
            throw new IllegalArgumentException("ltDate must not be null.");

        this.ltDate = ltDate;
        this.limit = limit;
    }

    /**
     * Gets .
     */
    public String getLtDate() {
        return ltDate;
    }

    /**
     * Gets limit.
     */
    public int getLimit() {
        return limit;
    }
}
