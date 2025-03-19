package me.mogubea.profile;

import java.sql.Timestamp;

public class FriendRequest {

    private final int senderId; // Sender of this request
    private final int receiverId; // Receiver of this request
    private final Timestamp requestTime; // Time of request
    private boolean reviewResult; // The result of the review

    protected FriendRequest(int sender, int receiver) {
        this.senderId = sender;
        this.receiverId = receiver;
        this.requestTime = new Timestamp(System.currentTimeMillis());
    }

}
