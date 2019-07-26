package com.storeyfilms.zenith.doc;

public class User {
    public String username;
    public String email;
    public String contribute;
    public String reserved1;


    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email, String contribute, String reserved1) {
        this.username = username;
        this.email = email;
        this.contribute = contribute;
        this.reserved1 = reserved1;
    }
}
