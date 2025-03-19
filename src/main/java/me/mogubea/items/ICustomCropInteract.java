package me.mogubea.items;

import me.mogubea.events.MoguCropInteractEvent;
import org.jetbrains.annotations.NotNull;

public interface ICustomCropInteract {

    void onCustomCropInteract(@NotNull MoguCropInteractEvent event);

}
