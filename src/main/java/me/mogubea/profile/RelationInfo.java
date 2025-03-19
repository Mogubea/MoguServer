package me.mogubea.profile;

import java.sql.Timestamp;

public class RelationInfo {

    private final Timestamp establishmentTime; // Time of relation

    protected RelationInfo() {
        this(new Timestamp(System.currentTimeMillis()));
    }

    public RelationInfo(Timestamp time) {
        this.establishmentTime = time;
    }

}
