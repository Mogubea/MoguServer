package me.mogubea.profile;

import java.util.*;

public class PlayerRelations {

    private final PlayerProfile profile;
    private final Map<Integer, RelationInfo> friends;
    private final Map<Integer, RelationInfo> blocks; // These players messages cannot be seen, and they cannot send dms
    private final Set<Integer> tpAllow; // These players can bypass /tpa and use /tp
    private final Set<Integer> tpDeny; // These players cannot send /tpa requests

    public PlayerRelations(PlayerProfile profile) {
        this(profile, new HashMap<>(), new HashMap<>(), new HashSet<>(), new HashSet<>());
    }

    public PlayerRelations(PlayerProfile profile, Map<Integer, RelationInfo> friends, Map<Integer, RelationInfo> blocks, Set<Integer> tpAllow, Set<Integer> tpDeny) {
        this.profile = profile;
        this.friends = friends;
        this.blocks = blocks;
        this.tpAllow = tpAllow;
        this.tpDeny = tpDeny;
    }

    /**
     * Check if this player is blocking the specified player
     * @param id The player's database ID
     * @return If blocked
     */
    public boolean isBlocking(int id) {
        return this.blocks.containsKey(id);
    }

    /**
     * Check if this player is friends with the specified player
     * @param id The player's database ID
     * @return If friends
     */
    public boolean isFriends(int id) {
        return this.friends.containsKey(id);
    }

    /**
     * Friend a player
     * @param id The player's database ID
     */
    public void addFriend(int id) {
        if (!isFriends(id) && profile.getManager().getDatasource().editPlayerRelation(profile, id, false, true)) {
            this.blocks.remove(id);
            this.friends.put(id, new RelationInfo());
            if (profile.getManager().isProfileLoaded(id)) // If the opposing party is online, directly add to their friends list too.
                Objects.requireNonNull(profile.getManager().getPlayerProfile(id)).getRelationships().friends.put(profile.getId(), new RelationInfo());
        }
    }

    /**
     * Block a player
     * @param id The player's database ID
     */
    public void blockPlayer(int id) {
        if (!isBlocking(id) && profile.getManager().getDatasource().editPlayerRelation(profile, id, false, false)) {
            this.friends.remove(id);
            this.blocks.put(id, new RelationInfo());
            if (profile.getManager().isProfileLoaded(id)) // If the opposing party is online, directly remove from their friends list too.
                Objects.requireNonNull(profile.getManager().getPlayerProfile(id)).getRelationships().friends.remove(profile.getId());
        }
    }

    /**
     * Unfriend a player
     * @param id The player's database ID
     */
    public void removeFriend(int id) {
        if (isFriends(id) && profile.getManager().getDatasource().editPlayerRelation(profile, id, true, true)) {
            this.friends.remove(id);
            if (profile.getManager().isProfileLoaded(id)) // If the opposing party is online, directly remove from their friends list too.
                Objects.requireNonNull(profile.getManager().getPlayerProfile(id)).getRelationships().friends.remove(profile.getId());
        }
    }

    /**
     * Unblock a player
     * @param id The player's database ID
     */
    public void unblockPlayer(int id) {
        if (isBlocking(id) && profile.getManager().getDatasource().editPlayerRelation(profile, id, true, true))
            this.blocks.remove(id);
    }

    public int getFriendCount() {
        return this.friends.size();
    }

    public int getBlockedCount() {
        return this.blocks.size();
    }

    /**
     * Allow the specified player to teleport to this player without using /tpa
     * @param id The player's database ID
     */
    public void allowTeleports(int id) {
        this.tpDeny.remove(id);
        this.tpAllow.add(id);
    }

    /**
     * Prevent the specified player from sending /tpa requests to this player
     * @param id The player's database ID
     */
    public void blockTeleports(int id) {
        this.tpAllow.remove(id);
        this.tpDeny.add(id);
    }

}
