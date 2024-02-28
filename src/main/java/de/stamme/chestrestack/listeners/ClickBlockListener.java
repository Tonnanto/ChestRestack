package de.stamme.chestrestack.listeners;

import de.stamme.chestrestack.ChestRestack;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK;

public class ClickBlockListener implements Listener {
    @EventHandler
    public void onClickBlock(@NotNull PlayerInteractEvent event) {
        Block block = event.getClickedBlock();

        if (block == null) return;
        // Clicked a block

        if (!event.getPlayer().isSneaking()) return;
        // Player was sneaking

        if (!event.getAction().equals(RIGHT_CLICK_BLOCK)) return;
        // Player right-clicked

        if (!(block.getState() instanceof InventoryHolder)) return;
        // The clicked block has an inventory

        event.setCancelled(true);
        handleChestClick(event.getPlayer(), ((InventoryHolder) block.getState()).getInventory(), true);
    }

    private void handleChestClick(Player player, Inventory chestInventory, boolean sortChest) {

        Inventory playerInventory = player.getInventory();

        // Key = position at inventory
        List<ItemStack> itemsToMove = new ArrayList<>();

        for (int i = 0; i < playerInventory.getStorageContents().length; i++) {
            ItemStack itemStack = playerInventory.getStorageContents()[i];
            if (itemStack == null) continue;
            if (chestInventory.contains(itemStack.getType())) {
                itemsToMove.add(itemStack);
            }
        }
//        ChestRestack.log("Items to move: " + itemsToMove.stream().map(ItemStack::toString).collect(Collectors.joining()));

        itemsToMove = stackify(itemsToMove, true);
        int numberOfItemsToMove = itemsToMove.stream().map(ItemStack::getAmount).reduce(Integer::sum).orElse(0);

//        ChestRestack.log("Items to move: " + itemsToMove.stream().map(ItemStack::toString).collect(Collectors.joining()));

        int itemsNotMoved = 0;
        for (ItemStack itemStack : itemsToMove) {
            // remove from player inventory
            playerInventory.remove(itemStack.getType());

            // add to container inventory
            Map<Integer, ItemStack> itemsToReturn = chestInventory.addItem(itemStack);
//            ChestRestack.log("Items to return: " + itemsToReturn.values().stream().map(ItemStack::toString).collect(Collectors.joining()));

            if (!itemsToReturn.isEmpty()) {
                // return items to player that did not fit
                itemsNotMoved += itemsToReturn.values().stream().map(ItemStack::getAmount).reduce(Integer::sum).orElse(0);
                playerInventory.addItem(itemsToReturn.get(0));
            }
        }


        // Sort chest inventory
        if (sortChest) {
            sortInventory(chestInventory);
        }

        // Send message
        int itemsMoved = numberOfItemsToMove - itemsNotMoved;

        if (itemsMoved + itemsNotMoved == 0) {
            ChestRestack.sendMessage(player, "No items to move");
        } else {
            String message = "";
            if (itemsMoved > 0) {
                message += String.format("%s items moved", itemsMoved);
            }
            if (itemsNotMoved > 0) {
                if (itemsMoved > 0) message += " - ";
                message += String.format("§cNo space for %s items", itemsNotMoved);
            }
            ChestRestack.sendMessage(player, message);

        }

    }

    private void sortInventory(Inventory inventory) {
        List<ItemStack> items = Arrays.stream(inventory.getStorageContents()).toList();
        items = stackify(items, false);
        items.sort(Comparator.comparing(i -> i.getType().name()));

        inventory.setStorageContents(items.toArray(new ItemStack[0]));
    }

    private List<ItemStack> stackify(List<ItemStack> items, boolean exceedMaxStackSize) {
        List<ItemStack> result = new ArrayList<>();
        for (ItemStack itemStack : items) { // iterate through the list of items to 'merge'
            if (itemStack == null) continue;
            int index = getNextSlotToStack(result, itemStack, exceedMaxStackSize); // get the first similar item, if it exists
            if (index > -1) { // if there is a similar stack, figure out how much to add
                ItemStack itemStackToAddTo = result.get(index);
                int amount = itemStackToAddTo.getAmount();
                if (!exceedMaxStackSize && amount + itemStack.getAmount() > itemStack.getMaxStackSize()) {
                    itemStackToAddTo.setAmount(itemStack.getMaxStackSize());
                    itemStack.setAmount(itemStack.getAmount() + amount - itemStack.getMaxStackSize());
                    result.add(itemStack);
                } else {
                    itemStackToAddTo.setAmount(amount + itemStack.getAmount());
                }
                result.set(index, itemStackToAddTo);
            } else { // else just add the stack
                result.add(itemStack);
            }
        }
        return result;
    }

    private int getNextSlotToStack(List<ItemStack> items, ItemStack input, boolean exceedMaxStackSize) {
        for (int i = 0; i < items.size(); i++) {
            if (exceedMaxStackSize && items.get(i).isSimilar(input)) {
                return i;
            }
            if (!exceedMaxStackSize && items.get(i).isSimilar(input) && items.get(i).getAmount() < items.get(i).getMaxStackSize()) {
                return i;
            }
        }
        return -1;
    }
}
