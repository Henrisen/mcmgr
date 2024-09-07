package cloud.thehsi.mcmgr;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Menu implements CommandExecutor, Listener {
    Plugin plugin;
    int item_select_page = 0;

    public Menu(Plugin plugin) {
        this.plugin = plugin;
    }

    private void generateMgrInventory(Inventory inv) {
        ItemStack itm = new ItemStack(Material.STONE_AXE, 1);
        ItemMeta meta = itm.getItemMeta();
        if (meta == null) return;
        meta.setItemName("World Management");
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "is_mcmgr_tab_item"), PersistentDataType.BOOLEAN, true);
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "is_mcmgr_unmovable_item"), PersistentDataType.BOOLEAN, true);
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "mcmgr_tab"), PersistentDataType.STRING, "world_management");

        itm.setItemMeta(meta);

        inv.setItem(0, itm);

        itm = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
        meta = itm.getItemMeta();
        if (meta == null) return;
        meta.setHideTooltip(true);
        meta.setItemName("");
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "is_mcmgr_unmovable_item"), PersistentDataType.BOOLEAN, true);
        itm.setItemMeta(meta);
        inv.setItem(9, itm);
        inv.setItem(10, itm);
        inv.setItem(11, itm);
        inv.setItem(12, itm);
        inv.setItem(13, itm);
        inv.setItem(14, itm);
        inv.setItem(15, itm);
        inv.setItem(16, itm);
        inv.setItem(17, itm);
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String command_str, @Nonnull String[] args) {
        Inventory inv = Bukkit.getServer().createInventory((Player) commandSender, 9 * 6, "Management Console");

        generateMgrInventory(inv);

        Player p = (Player) commandSender;

        p.openInventory(inv);

        return true;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getCursor() == null) return;

        ItemStack cursor = event.getCurrentItem();

        List<Material> blocks = new ArrayList<>();

        for (Material m : Material.values()) {
            if (m.isBlock() & !m.isAir()) blocks.add(m);
        }
        int item_select_pages = (int) Math.floor((double) blocks.size() / 52) - 1;

        if (cursor == null) return;
        if (!cursor.hasItemMeta()) return;
        if (Objects.requireNonNull(cursor.getItemMeta()).getPersistentDataContainer().isEmpty()) return;
        Player p = (Player) event.getWhoClicked();
        Location l = p.getLocation();
        Chunk c = l.getChunk();

        if (cursor.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "is_mcmgr_unmovable_item"))) event.setCancelled(true);
        if (cursor.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "mcmgr_item_select_scroll_up"))) {
            if (item_select_page > 0) item_select_page--;
            showFillCurrentChunkBlockSelectionGUI(item_select_page, blocks, p);
        }else if (cursor.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "mcmgr_item_select_scroll_down"))) {
            if (item_select_page < item_select_pages - 1) item_select_page++;
            showFillCurrentChunkBlockSelectionGUI(item_select_page, blocks, p);
        }

        if (cursor.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "mcmgr_item_select_item"))) {
            p.closeInventory();
            for (int x = 0; x < 16; x++) {
                final int gx = x;
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    for (int y = c.getWorld().getMaxHeight(); y > c.getWorld().getMinHeight() - 1; y--) {
                        for (int z = 0; z < 16; z++) {
                            Objects.requireNonNull(l.getWorld()).getBlockAt(c.getX() * 16 + gx, y, c.getZ() * 16 + z).setType(cursor.getType());
                        }
                    }
                }, x);
            }
        }


        if (cursor.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "is_mcmgr_tab_item"))) {
            if (!cursor.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "mcmgr_tab"), PersistentDataType.STRING)) return;
            switch (Objects.requireNonNull(cursor.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "mcmgr_tab"), PersistentDataType.STRING))) {
                case "world_management":
                    ItemStack itm = new ItemStack(Material.BARRIER, 1);
                    ItemMeta meta = itm.getItemMeta();
                    if (meta == null) return;
                    meta.setItemName("Remove Current Chunk");
                    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "is_mcmgr_action_item"), PersistentDataType.BOOLEAN, true);
                    meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "is_mcmgr_unmovable_item"), PersistentDataType.BOOLEAN, true);
                    meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "mcmgr_action"), PersistentDataType.STRING, "world_management_remove_current_chunk");
                    itm.setItemMeta(meta);
                    event.getInventory().setItem(18, itm);

                    itm = new ItemStack(Material.BUCKET, 1);
                    meta = itm.getItemMeta();
                    if (meta == null) return;
                    meta.setItemName("Fill Current Chunk");
                    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "is_mcmgr_action_item"), PersistentDataType.BOOLEAN, true);
                    meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "is_mcmgr_unmovable_item"), PersistentDataType.BOOLEAN, true);
                    meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "mcmgr_action"), PersistentDataType.STRING, "world_management_fill_current_chunk");
                    itm.setItemMeta(meta);
                    event.getInventory().setItem(19, itm);

                    break;
                case "":
                    throw new IllegalStateException("Empty Value");
                default:
                    throw new IllegalStateException("Unexpected value: " + Objects.requireNonNull(cursor.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "mcmgr_tab"), PersistentDataType.STRING)));
            }
        }else if (cursor.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "is_mcmgr_action_item"))) {
            if (!cursor.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "mcmgr_action"), PersistentDataType.STRING)) return;

            switch (Objects.requireNonNull(cursor.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "mcmgr_action"), PersistentDataType.STRING))) {
                case "world_management_remove_current_chunk":
                    for (int x = 0; x < 16; x++) {
                        final int gx = x;
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            for (int y = c.getWorld().getMaxHeight(); y > c.getWorld().getMinHeight()-1; y--) {
                                for (int z = 0; z < 16; z++) {
                                    Objects.requireNonNull(l.getWorld()).getBlockAt(c.getX() * 16 + gx, y, c.getZ() * 16 + z).setType(Material.AIR);
                                }
                            }
                        }, x);
                        p.closeInventory();
                    }
                    break;
                case "world_management_fill_current_chunk":
                    showFillCurrentChunkBlockSelectionGUI(item_select_page, blocks, p);
                    break;
            }
        }
    }

    @EventHandler
    public void onWoodenShovelClick(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) return;
        if (event.getItem() == null) return;
        if (event.getItem().getType() != Material.WOODEN_SHOVEL) return;

        if (!event.getPlayer().hasPermission("cloud.thehsi.admin") || !event.getPlayer().isOp()) return;

        Inventory inv = Bukkit.getServer().createInventory(event.getPlayer(), 9 * 6, "Management Console");

        generateMgrInventory(inv);

        event.getPlayer().openInventory(inv);

        event.setCancelled(true);
    }

    private void showFillCurrentChunkBlockSelectionGUI(int item_select_page, List<Material> blocks, Player p) {
        int indexMin = item_select_page * 54;

        Inventory itemSelect = Bukkit.createInventory(p, 54, "Select Block to Fill");

        for (int i = 0; i < 52; i++) {
            ItemStack stack = new ItemStack(blocks.get(i + indexMin), i+1);
            ItemMeta meta = stack.getItemMeta();
            if (meta == null) continue;
            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "is_mcmgr_unmovable_item"), PersistentDataType.BOOLEAN, true);
            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "mcmgr_item_select_item"), PersistentDataType.BOOLEAN, true);
            stack.setItemMeta(meta);
            itemSelect.setItem(i, stack);
        }
        ItemStack stack = new ItemStack(Material.LIME_WOOL, 1);
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return;
        meta.setItemName("Page UP");
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "is_mcmgr_unmovable_item"), PersistentDataType.BOOLEAN, true);
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "mcmgr_item_select_scroll_up"), PersistentDataType.BOOLEAN, true);
        stack.setItemMeta(meta);
        itemSelect.setItem(52, stack);

        stack = new ItemStack(Material.RED_WOOL, 1);
        meta = stack.getItemMeta();
        if (meta == null) return;
        meta.setItemName("Page DOWN");
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "is_mcmgr_unmovable_item"), PersistentDataType.BOOLEAN, true);
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "mcmgr_item_select_scroll_down"), PersistentDataType.BOOLEAN, true);
        stack.setItemMeta(meta);
        itemSelect.setItem(53, stack);

        p.openInventory(itemSelect);
    }
}
