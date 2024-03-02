package de.stamme.chestrestack.listeners;

import de.stamme.chestrestack.ChestRestack;
import de.stamme.chestrestack.PlayerPreferences;
import de.stamme.chestrestack.config.Config;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static org.bukkit.event.block.Action.LEFT_CLICK_BLOCK;
import static org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK;

public class ClickBlockListener implements Listener {
    @EventHandler
    public void onClickBlock(@NotNull PlayerInteractEvent event) {

        Player player = event.getPlayer();
        PlayerPreferences preferences = ChestRestack.getPlugin().getPlayerPreference(player.getUniqueId());

        if (ignoreEvent(event, preferences)) return;

        // These assertions are ensured by the above ignoreEvent method
        assert event.getClickedBlock() != null;
        assert event.getClickedBlock().getState() instanceof InventoryHolder;
        Inventory inventory = ((InventoryHolder) event.getClickedBlock().getState()).getInventory();

        switch (inventory.getType()) {
            case CHEST:
            case BARREL:
            case SHULKER_BOX:
            case ENDER_CHEST:
            case HOPPER:
            case DISPENSER:
            case DROPPER:
            case CHISELED_BOOKSHELF:
                handleChestClick(event.getPlayer(), inventory, preferences);
            case SMOKER:
            case BLAST_FURNACE:
            case FURNACE:
                if (!preferences.isMoveToFurnaces()) break;
                if (!(inventory instanceof FurnaceInventory)) break;
                handleFurnaceClick(event.getPlayer(), (FurnaceInventory) inventory, preferences);
                break;
            case BREWING:
            default: break;
        }
    }

    /**
     * Used to filter events that are not supposed to trigger the restack action
     * @param event the event is question
     * @param preferences the player's preferences
     * @return whether the plugin should ignore the event
     */
    private boolean ignoreEvent(PlayerInteractEvent event, PlayerPreferences preferences) {
        if (!preferences.isEnabled()) return true;
        Block block = event.getClickedBlock();
        if (block == null) return true;
        // Clicked a block

        if (!(block.getState() instanceof InventoryHolder)) return true;
        // The clicked block has an inventory

        switch (preferences.getClickMode()) {
            case SHIFT_LEFT:
                if (!event.getPlayer().isSneaking()) return true;
                if (!event.getAction().equals(LEFT_CLICK_BLOCK)) return true;
                break;
            case SHIFT_RIGHT:
                if (!event.getPlayer().isSneaking()) return true;
                if (!event.getAction().equals(RIGHT_CLICK_BLOCK)) return true;
                break;
        }
        // Player used correct action on block

        // cancel default action
        event.setCancelled(true);

        // Only handle the main hand interaction (the event is called twice - once for each hand)
        return !Objects.equals(event.getHand(), EquipmentSlot.HAND);
    }

    private void handleChestClick(Player player, Inventory chestInventory, PlayerPreferences preferences) {
        PlayerInventory playerInventory = player.getInventory();
        boolean sortingEnabled = Config.getSortingEnabledGlobal() && preferences.isSortingEnabled();
        ItemStack[] playerInventoryContents = playerInventory.getStorageContents().clone();
        int itemsNotMoved = 0;
        boolean didInventorySort = false;

        // Sort the chest before to get space
        if (sortingEnabled) {
            didInventorySort = sortInventory(chestInventory);
        }

        // Find item stacks to move to the chest // TODO: Exclude Tools, Armor, Hotbar if preferences say so
        Set<Material> materialsToMove = Arrays.stream(playerInventory.getStorageContents()).filter(Objects::nonNull).map(ItemStack::getType).filter(chestInventory::contains).collect(Collectors.toSet());
        materialsToMove.removeAll(preferences.getMaterialsToNotMove());
        Map<Integer, ItemStack> itemsToMove = findItemsToMove(playerInventory, materialsToMove, preferences);
        int numberOfItemsToMove = itemsToMove.values().stream().map(ItemStack::getAmount).reduce(Integer::sum).orElse(0);

        // Update player inventory contents
        for (Map.Entry<Integer, ItemStack> entry : itemsToMove.entrySet()) {
            // remove from player inventory
            playerInventoryContents[entry.getKey()] = null;

            // add to container inventory
            Map<Integer, ItemStack> itemsToReturn = chestInventory.addItem(entry.getValue());

            if (itemsToReturn.values().stream().findFirst().isPresent()) {
                // return items to player that did not fit
                playerInventoryContents[entry.getKey()] = itemsToReturn.values().stream().findFirst().get();
                itemsNotMoved += itemsToReturn.values().stream().findFirst().get().getAmount();
            }
        }

        // update player inventory
        playerInventory.setStorageContents(playerInventoryContents);

        // Sort the chest after to clean up
        if (sortingEnabled) {
            didInventorySort = sortInventory(chestInventory) || didInventorySort;
        }

        // Send message
        int itemsMoved = numberOfItemsToMove - itemsNotMoved;

        if (itemsMoved + itemsNotMoved == 0) {
            if (didInventorySort) {
                ChestRestack.sendActionMessage(player, "Chest sorted");
            } else {
                ChestRestack.sendActionMessage(player, "No items to move");
            }
        } else {
            String message = "";
            if (itemsMoved > 0) {
                message += String.format("%s items moved", itemsMoved);
            }
            if (itemsNotMoved > 0) {
                if (itemsMoved > 0) message += " - ";
                message += String.format("§cNo space for %s items", itemsNotMoved);
            }
            ChestRestack.sendActionMessage(player, message);
        }
    }

    private void handleFurnaceClick(Player player, FurnaceInventory furnaceInventory, PlayerPreferences preferences) {
        PlayerInventory playerInventory = player.getInventory();
        ItemStack[] playerInventoryContents = playerInventory.getStorageContents().clone();

        // Get current fuel and smelting material
        Material fuelMaterial = null;
        Material smeltMaterial = null;

        if (furnaceInventory.getFuel() != null) {
            fuelMaterial = furnaceInventory.getFuel().getType();
        }
        if (furnaceInventory.getSmelting() != null) {
            smeltMaterial = furnaceInventory.getSmelting().getType();
        }
        if (fuelMaterial == null) {
            fuelMaterial = Material.COAL; // TODO Get preferred fuel material from config
        }


        // MOVE FUEL
        // Find fuel item stacks to move to the furnace
        Set<Material> fuelMaterialsToMove = new HashSet<>(List.of(fuelMaterial));
        fuelMaterialsToMove.removeAll(preferences.getMaterialsToNotMove());
        Map<Integer, ItemStack> fuelToMove = findItemsToMove(playerInventory, fuelMaterialsToMove, preferences);

        int numberOfFuelToMove = fuelToMove.values().stream().map(ItemStack::getAmount).reduce(Integer::sum).orElse(0);
        int fuelNotMoved = 0;

        for (Map.Entry<Integer, ItemStack> entry : fuelToMove.entrySet()) {
            // remove from player inventory
            playerInventoryContents[entry.getKey()] = null;

            // add to fuel slot
            ItemStack furnaceFuelItem = furnaceInventory.getFuel();
            if (furnaceFuelItem == null) {
                furnaceInventory.setFuel(entry.getValue());
            } else if (furnaceFuelItem.getType() == entry.getValue().getType()) {
                List<ItemStack> fuelItems = stackify(List.of(furnaceFuelItem, entry.getValue()));
                furnaceInventory.setFuel(fuelItems.get(0));
                if (fuelItems.size() > 1) {
                    playerInventoryContents[entry.getKey()] = fuelItems.get(1);
                    fuelNotMoved += fuelItems.get(1).getAmount();
                }
            }
        }

        // update player inventory
        playerInventory.setStorageContents(playerInventoryContents);


        int numberOfItemsToMove = 0;
        int itemsNotMoved = 0;
        if (smeltMaterial != null) {
            // MOVE SMELT ITEM
            // Find smelt item stacks to move to the furnace
            Set<Material> smeltMaterialsToMove = new HashSet<>(List.of(smeltMaterial));
            smeltMaterialsToMove.removeAll(preferences.getMaterialsToNotMove());
            Map<Integer, ItemStack> itemsToMove = findItemsToMove(playerInventory, smeltMaterialsToMove, preferences);

            numberOfItemsToMove = itemsToMove.values().stream().map(ItemStack::getAmount).reduce(Integer::sum).orElse(0);

            for (Map.Entry<Integer, ItemStack> entry : itemsToMove.entrySet()) {
                // remove from player inventory
                playerInventoryContents[entry.getKey()] = null;

                // add to smelt slot
                ItemStack furnaceSmeltItem = furnaceInventory.getSmelting();
                if (furnaceSmeltItem == null) {
                    furnaceInventory.setSmelting(entry.getValue());
                } else if (furnaceSmeltItem.getType() == entry.getValue().getType()) {
                    List<ItemStack> smeltItems = stackify(List.of(furnaceSmeltItem, entry.getValue()));
                    furnaceInventory.setSmelting(smeltItems.get(0));
                    if (smeltItems.size() > 1) {
                        playerInventoryContents[entry.getKey()] = smeltItems.get(1);
                        itemsNotMoved += smeltItems.get(1).getAmount();
                    }
                }
            }

            // update player inventory
            playerInventory.setStorageContents(playerInventoryContents);
        }


        // Send message
        int fuelMoved = numberOfFuelToMove - fuelNotMoved;
        int itemsMoved = numberOfItemsToMove - itemsNotMoved;

        if (fuelMoved + itemsMoved == 0) {
            ChestRestack.sendActionMessage(player, "Nothing to add");
        } else {
            if (fuelMoved > 0 && itemsMoved > 0) {
                ChestRestack.sendActionMessage(player, "Fuel and items added");
            } else if (fuelMoved > 0) {
                ChestRestack.sendActionMessage(player, "Fuel added");
            } else if (itemsMoved > 0) {
                ChestRestack.sendActionMessage(player, "Items added");
            }
        }
    }

    private Map<Integer, ItemStack> findItemsToMove(PlayerInventory inventory, Set<Material> materialsToMove, PlayerPreferences preferences) {
        Map<Integer, ItemStack> itemsToMove = new HashMap<>();
        int startInventoryIndex = 0;
        if (!preferences.isMoveFromHotbar()) startInventoryIndex = 9;
        for (int i = startInventoryIndex; i < inventory.getStorageContents().length; i++) {
            ItemStack itemStack = inventory.getStorageContents()[i];
            if (itemStack == null) continue;
            if (materialsToMove.contains(itemStack.getType())) {
                itemsToMove.put(i, itemStack);
            }
        }
        return itemsToMove;
    }

    private boolean sortInventory(Inventory inventory) {
        ItemStack[] inventoryBefore = inventory.getStorageContents().clone();

        // Stack items together
        List<ItemStack> sortedItems = stackify(Arrays.stream(inventory.getStorageContents()).toList());

        // Sort by name // TODO: Use sort-mode from player preferences
        sortedItems.sort(Comparator.comparing(i -> i.getType().name()));

        // Update inventory contents
        ItemStack[] inventoryAfter = sortedItems.toArray(new ItemStack[inventory.getStorageContents().length]);
        inventory.setStorageContents(inventoryAfter);

        return !isInventoryEqual(inventoryBefore, inventoryAfter);
    }

    private boolean isInventoryEqual(ItemStack[] inv1, ItemStack[] inv2) {
        if (inv1 == null || inv2 == null || inv1.length != inv2.length) {
            return false;
        }

        // Iterate through each ItemStack in both lists
        for (int i = 0; i < inv1.length; i++) {

            // empty slot in both inventories
            if (inv1[i] == null && inv2[i] == null)
                continue;

            // empty slot in ONE inventory
            if (inv1[i] == null || inv2[i] == null)
                return false;

            // compare item stacks
            if (!inv1[i].equals(inv2[i])) {
                return false;
            }
        }

        return true;
    }

    private List<ItemStack> stackify(List<ItemStack> items) {
        List<ItemStack> result = new ArrayList<>();
        // iterate through the list of items to 'merge'
        for (ItemStack itemStack : items) {
            if (itemStack == null) continue;
            // get the first similar non-full item stack if it exists
            int index = getNextSlotToStack(result, itemStack);
            if (index > -1) {
                // if there is a similar stack, figure out how much to add
                ItemStack itemStackToAddTo = result.get(index);
                int amount = itemStackToAddTo.getAmount();
                if (amount + itemStack.getAmount() > itemStack.getMaxStackSize()) {
                    itemStackToAddTo.setAmount(itemStack.getMaxStackSize());
                    itemStack.setAmount(itemStack.getAmount() + amount - itemStack.getMaxStackSize());
                    result.add(itemStack);
                } else {
                    itemStackToAddTo.setAmount(amount + itemStack.getAmount());
                }
            } else {
                // else just add as a new stack
                result.add(itemStack);
            }
        }
        return result;
    }

    private int getNextSlotToStack(List<ItemStack> items, ItemStack input) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).isSimilar(input) && items.get(i).getAmount() < items.get(i).getMaxStackSize()) {
                return i;
            }
        }
        return -1;
    }
}
