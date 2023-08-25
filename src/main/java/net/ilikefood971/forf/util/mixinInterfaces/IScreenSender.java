package net.ilikefood971.forf.util.mixinInterfaces;

import net.minecraft.inventory.Inventory;
import net.minecraft.text.Text;

import java.util.OptionalInt;

public interface IScreenSender {
    
    OptionalInt sendScreenHandler(Inventory inventory, Text name);
    
}
