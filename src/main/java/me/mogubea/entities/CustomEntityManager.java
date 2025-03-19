package me.mogubea.entities;

import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

/**
 * Load before anything else to ensure no issues.
 */
public class CustomEntityManager {

    protected void tryToConvert(@NotNull Entity oldEntity) {
        PersistentDataContainer container = oldEntity.getPersistentDataContainer();
        String identifier = container.get(CustomEntityType.KEY_ENTITY_TYPE, PersistentDataType.STRING);
        CustomEntityType<?> customEntity = identifier != null ? CustomEntityType.fromIdentifier(identifier) : null;

        if (customEntity != null)
            customEntity.replace(oldEntity);
    }

}
