package me.mogubea.portals;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

public class Portal {

    private final int id;

    private World world;
    private BoundingBox box;
    private Location targetLocation;
    private Portal targetPortal;

    private boolean dirty;

    protected Portal(int id, @NotNull Block cornerOne, @NotNull Block cornerTwo) {
        this.id = id;
        this.box = BoundingBox.of(cornerOne, cornerTwo);
        this.world = cornerOne.getWorld();
    }

}
