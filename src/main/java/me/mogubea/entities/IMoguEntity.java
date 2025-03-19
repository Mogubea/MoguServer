package me.mogubea.entities;

import org.bukkit.entity.Entity;

public interface IMoguEntity {

    void postCreation();

    /**
     * Called when replacing a pre-existing vanilla entity with a custom entity. This method transfers valid {@link org.bukkit.persistence.PersistentDataContainer} entries
     * over to the new custom entity.
     */
    void transferData(Entity oldEntity);

}
