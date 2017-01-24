package com.rutgerssustainability.android.rutgerssustainability.pojos;

/**
 * Created by shreyashirday on 1/23/17.
 */
public class TrashWrapper {
    private String status;
    private String message;
    private Trash[] trash;

    public String getStatus() {
        return this.status;
    }

    public String getMessage() {
        return this.message;
    }

    public Trash[] getTrash() {
        return this.trash;
    }

}
