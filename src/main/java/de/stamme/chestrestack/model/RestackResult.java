package de.stamme.chestrestack.model;

import de.stamme.chestrestack.config.MessagesConfig;
import de.stamme.chestrestack.util.StringFormatter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.List;

public record RestackResult (
        @Nullable Material onlyMaterialMoved,
        @Nullable Material onlyMaterialNotMoved,
        int numberOfItemsMoved,
        int numberOfItemsNotMoved,
        boolean sorted,
        Material destinationBlockType
) {

    public RestackResult(List<ItemStack> itemsMoved, List<ItemStack> itemsNotMoved, boolean sorted, Material destinationBlockType) {
        this(getOnlyMaterial(itemsMoved), getOnlyMaterial(itemsNotMoved), countItems(itemsMoved), countItems(itemsNotMoved), sorted, destinationBlockType);
    }

    private static int countItems(List<ItemStack> items) {
        return items.stream().map(ItemStack::getAmount).reduce(Integer::sum).orElse(0);
    }

    /**
     * Find the only material in this list of items.
     * If there is more than one material return null.
     * @param items the list of items to check
     * @return the only material or null
     */
    @Nullable
    private static Material getOnlyMaterial(List<ItemStack> items) {
        if (items.isEmpty()) return null;
        Material material = items.stream().findFirst().get().getType();
        for (ItemStack itemStack : items) {
            if (itemStack.getType() != material && itemStack.getType() != Material.AIR) return null;
        }
        return material;
    }

    public String getMessage() {
        if (numberOfItemsMoved + numberOfItemsNotMoved == 0) {
            if (sorted) {
                return MessageFormat.format(MessagesConfig.getMessage("restack.container.sorted"), StringFormatter.formatMaterialName(destinationBlockType));
            } else {
                return MessagesConfig.getMessage("restack.container.nothing-to-move");
            }
        } else {
            String message = "";
            if (numberOfItemsMoved > 0) {
                if (onlyMaterialMoved != null) {
                    message += MessageFormat.format(MessagesConfig.getMessage("restack.container.moved-single-type"), numberOfItemsMoved, StringFormatter.formatMaterialName(onlyMaterialMoved));
                } else {
                    message += MessageFormat.format(MessagesConfig.getMessage("restack.container.moved-multiple-types"), numberOfItemsMoved);
                }
            }
            if (numberOfItemsNotMoved > 0) {
                if (numberOfItemsMoved > 0) message += " - ";
                if (onlyMaterialNotMoved != null) {
                    message += MessageFormat.format(MessagesConfig.getMessage("restack.container.no-space-single-type"), numberOfItemsNotMoved, StringFormatter.formatMaterialName(onlyMaterialNotMoved));
                } else {
                    message += MessageFormat.format(MessagesConfig.getMessage("restack.container.no-space-multiple-types"), numberOfItemsNotMoved);
                }
            }
            return message;
        }
    }
}


